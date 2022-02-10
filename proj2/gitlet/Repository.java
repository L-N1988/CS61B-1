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
            Utils.message("A Gitlet version-control system already exists in the current directory.");
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
        StagingArea stagingArea = getStagingArea();
        Commit lastCommit = getLastCommit();
        stagingArea.add(fileName, lastCommit);
        saveStagingArea(stagingArea);
    }

    public static void commit(String message) {
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
        String newCommit = makeCommit(message, getFileSet(stagingArea, lastCommit), commitID);

        cleanStagingArea(stagingArea);
        changeBranch(curr, newCommit);
    }

    public static void rm(String fileName) {
        StagingArea stagingArea = getStagingArea();
        Commit lastCommit = getLastCommit();
        if (stagingArea.contain(fileName)) {
            stagingArea.delete(fileName);
            saveStagingArea(stagingArea);
        } else if (lastCommit.contain(fileName)) {
            stagingArea.addRemovedFiles(fileName);
            saveStagingArea(stagingArea);
            File file = new File(CWD, fileName);
            if (file.exists()) {
                file.delete();
            }
        } else {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void log() {
        String commitID = getCurrBranch().getCommitID();
        while (commitID != null) {
            commitID = printCommit(commitID);
        }
    }

    public static void globalLog() {
        List<String> commits = Utils.plainFilenamesIn(GITLET_DIR + slash + "commits");
        for (String commitID : commits) {
            printCommit(commitID);
        }
    }

    public static void find(String message) {
        List<String> commits = Utils.plainFilenamesIn(GITLET_DIR + slash + "commits");
        for (String commitID : commits) {
            Commit commit = getCommitFromID(commitID);
            if (commit.getMessage().equals(message)) {
                Utils.message(commitID);
            }
        }
    }

    public static void status() {
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
        StagingArea stagingArea = getStagingArea();
        if (args.length == 2) {
            Branch dest = getBranchFromName(args[1]);
            Branch curr = getCurrBranch();

            if (dest.getCommitID().equals(curr.getCommitID())) {
                Utils.message("No need to checkout the current branch.");
                System.exit(0);
            }

            if (!untrackedFile().isEmpty()) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            deleteAllFilesInCWD();
            Commit commit = getCommitFromID(dest.getCommitID());
            Set<String> newFiles = commit.getFilesSet();
            for (String file : newFiles) {
                commit.restoreFile(file);
            }

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
        File branchName = new File(GITLET_DIR + slash + "branches" + slash + name);
        if (branchName.exists()) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        Branch curr = getCurrBranch();
        createBranch(name, curr.getCommitID());
    }

    public static void rmBranch(String name) {
        File branchName = new File(GITLET_DIR + slash + "branches" + slash + name);
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
        Branch curr = getCurrBranch();
        Commit commit = getCommitFromID(commitID);
        if (!untrackedFile().isEmpty()) {
            Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        deleteAllFilesInCWD();
        Set<String> newFiles = commit.getFilesSet();
        for (String file : newFiles) {
            commit.restoreFile(file);
        }
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
}

