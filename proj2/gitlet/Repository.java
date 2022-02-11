package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.RepoUtils.*;

/**
 * Represents a gitlet repository.
 *
 * @author Christina0031
 */
public class Repository {

    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.message(
                    "A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            makeDir();
            initStagingArea();
            String initCommit = initCommit();
            Branch master = createBranch("master", initCommit);
            createHEAD(master.getName());
        }
    }

    public static void add(String fileName) {
        validDirectory();
        StagingArea stagingArea = getStagingArea();
        Commit lastCommit = getLastCommit();
        stagingArea.add(fileName, lastCommit);
        saveStagingArea(stagingArea);
    }

    public static void commit(String message) {
        validDirectory();
        if (message.equals("")) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Branch curr = getCurrBranch();
        String commitID = curr.getCommitID();
        Commit lastCommit = getCommitFromID(commitID);
        String newCommit = makeCommit(message, getCommitFileSet(stagingArea, lastCommit), commitID);

        cleanStagingArea(stagingArea);
        changeBranch(curr, newCommit);
    }

    public static void rm(String fileName) {
        validDirectory();
        StagingArea stagingArea = getStagingArea();
        Commit lastCommit = getLastCommit();
        if (stagingArea.contain(fileName)) {
            stagingArea.delete(fileName);
            saveStagingArea(stagingArea);
        } else if (lastCommit.contain(fileName)) {
            stagingArea.addRemovedFiles(fileName);
            saveStagingArea(stagingArea);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        } else {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void log() {
        validDirectory();
        String commitID = getCurrBranch().getCommitID();
        while (commitID != null) {
            commitID = printCommit(commitID);
        }
    }

    public static void globalLog() {
        validDirectory();
        List<String> commits = Utils.plainFilenamesIn(GITLET_DIR + SLASH + "commits");
        for (String commitID : commits) {
            printCommit(commitID);
        }
    }

    public static void find(String message) {
        validDirectory();
        boolean found = false;
        List<String> commits = Utils.plainFilenamesIn(GITLET_DIR + SLASH + "commits");
        for (String commitID : commits) {
            Commit commit = getCommitFromID(commitID);
            if (commit.getMessage().equals(message)) {
                Utils.message(commitID);
                found = true;
            }
        }
        if (!found) {
            Utils.message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        validDirectory();
        StagingArea stagingArea = getStagingArea();

        Utils.message("=== Branches ===");
        printBranches();

        Utils.message("=== Staged Files ===");
        stagingArea.printStagedFiles();

        Utils.message("=== Removed Files ===");
        stagingArea.printRemovedFiles();

        Utils.message("=== Modifications Not Staged For Commit ===");
        Set<String> modifiedFile = modifiedFile();
        for (String fileName : modifiedFile) {
            Utils.message(fileName);
        }
        Utils.message("");

        Utils.message("=== Untracked Files ===");
        Set<String> untrackedFile = untrackedFile();
        for (String fileName : untrackedFile) {
            Utils.message(fileName);
        }
        Utils.message("");
    }


    public static void checkout(String... args) {
        validDirectory();
        StagingArea stagingArea = getStagingArea();
        if (args.length == 2) {
            Branch dest = getBranchFromName(args[1]);
            Branch curr = getCurrBranch();
            if (dest.getName().equals(curr.getName())) {
                Utils.message("No need to checkout the current branch.");
                System.exit(0);
            }
            Commit commit = getCommitFromID(dest.getCommitID());
            setCommit(commit);
            cleanStagingArea(stagingArea);
            changeHEAD(dest.getName());
            return;
        }
        String fileName = "";
        Commit commit = null;
        if (args.length == 3 && args[1].equals("--")) {
            fileName = args[2];
            commit = getLastCommit();
        } else if (args.length == 4 && args[2].equals("--")) {
            fileName = args[3];
            commit = getCommitFromID(args[1]);
        } else {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
        commit.restoreFile(fileName);
        stagingArea.delete(fileName);
        saveStagingArea(stagingArea);
    }

    public static void branch(String name) {
        validDirectory();
        File branchName = new File(GITLET_DIR + SLASH + "branches" + SLASH + name);
        if (branchName.exists()) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        Branch curr = getCurrBranch();
        createBranch(name, curr.getCommitID());
    }

    public static void rmBranch(String name) {
        validDirectory();
        File branchName = new File(GITLET_DIR + SLASH + "branches" + SLASH + name);
        if (!branchName.exists()) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch curr = getCurrBranch();
        if (curr.getName().equals(name)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        removeBranch(name);
    }

    public static void reset(String commitID) {
        validDirectory();
        Branch curr = getCurrBranch();
        Commit commit = getCommitFromID(commitID);
        setCommit(commit);
        StagingArea stagingArea = getStagingArea();
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        Set<String> set = stagingArea.getKeys();
        for (String s : set) {
            if (!allFiles.contains(s)) {
                stagingArea.delete(s);
            }
        }
        set = stagingArea.getRemovalFiles();
        for (String s : set) {
            if (!allFiles.contains(s)) {
                stagingArea.deleteRemovedFiles(s);
            }
        }
        saveStagingArea(stagingArea);
        changeBranch(curr, commitID);
    }


    public static void merge(String branchName) {
        validDirectory();
        StagingArea stagingArea = getStagingArea();
        if (!stagingArea.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        File file = new File(GITLET_DIR + SLASH + "branches" + SLASH + branchName);
        if (!file.exists()) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch given = Utils.readObject(file, Branch.class);
        Branch curr = getCurrBranch();
        if (curr.getName().equals(given.getName())) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (!untrackedFile().isEmpty()) {
            String msg0 = "There is an untracked file in the way; ";
            String msg1 = "delete it, or add and commit it first.";
            Utils.message(msg0 + msg1);
            System.exit(0);
        }

        String splitPointID = splitPoint(curr, given);
        Commit lastCommit = getCommitFromID(curr.getCommitID());

        Map<String, String> splitFiles = getCommitFromID(splitPointID).getFiles();
        Map<String, String> currFiles = lastCommit.getFiles();
        Map<String, String> givenFiles = getCommitFromID(given.getCommitID()).getFiles();

        boolean conflict = false;
        for (String s : givenFiles.keySet()) {
            boolean split = splitFiles.containsKey(s);
            boolean cur = currFiles.containsKey(s);
            if (cur) {
                if (split && equal(splitFiles, currFiles, s) && !equal(splitFiles, givenFiles, s)) {
                    String[] args = {"checkout", given.getCommitID(), "--", s};
                    checkout(args);
                    stagingArea.add(s, lastCommit);
                } else if (split && !equal(givenFiles, currFiles, s) && !equal(givenFiles, splitFiles, s)
                        && !equal(currFiles, splitFiles, s)) {
                    handleConflict(currFiles, givenFiles, s);
                    conflict = true;
                    stagingArea.add(s, lastCommit);
                } else if (!split && !equal(givenFiles, currFiles, s)) {
                    handleConflict(currFiles, givenFiles, s);
                    conflict = true;
                    stagingArea.add(s, lastCommit);
                }
            } else {
                if (!split) {
                    String[] args = {"checkout", given.getCommitID(), "--", s};
                    checkout(args);
                    stagingArea.add(s, lastCommit);
                } else if (split && !equal(givenFiles, splitFiles, s)) {
                    handleConflict(currFiles, givenFiles, s);
                    conflict = true;
                    stagingArea.add(s, lastCommit);
                } else if (split && equal(splitFiles, givenFiles, s)) {
                    File f = new File(s);
                    if (f.exists()) {
                        f.delete();
                    }
                    stagingArea.delete(s);
                }
            }
        }
        for (String s : currFiles.keySet()) {
            boolean split = splitFiles.containsKey(s);
            boolean give = givenFiles.containsKey(s);
            if (!give) {
                if (split && equal(splitFiles, currFiles, s)) {
                    File f = new File(s);
                    if (f.exists()) {
                        f.delete();
                    }
                    stagingArea.delete(s);
                    stagingArea.addRemovedFiles(s);
                } else if (split && !equal(currFiles, splitFiles, s)) {
                    handleConflict(currFiles, givenFiles, s);
                    conflict = true;
                    stagingArea.add(s, lastCommit);
                }
            }
        }

        String message = "Merged " + given.getName() + " into " + curr.getName() + ".";
        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        if (conflict) {
            Utils.message("Encountered a merge conflict.");
        }

        String newCommit = makeMergeCommit(message,
                getCommitFileSet(stagingArea, lastCommit), curr.getCommitID(), given.getCommitID());

        cleanStagingArea(stagingArea);
        changeBranch(curr, newCommit);
    }

    public static void addRemote(String remoteName, String directory) {

    }
}

