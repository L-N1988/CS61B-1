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
            createBranch("HEAD", initCommit);
            createBranch("master", initCommit);
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
        Branch HEAD = getHeadBranch();
        String commitID = HEAD.getCommitID();
        Commit lastCommit = getCommitFromID(commitID);
        String newCommit = makeCommit(message, getFileSet(stagingArea, lastCommit), commitID);

        cleanStagingArea(stagingArea);
        changeBranch(HEAD, newCommit);
    }

    public static void rm(String fileName) {
        StagingArea stagingArea = getStagingArea();
        Commit lastCommit = getLastCommit();
        if (stagingArea.contain(fileName)) {
            stagingArea.delete(fileName);
        } else if (lastCommit.contain(fileName)) {
            stagingArea.addRemovedFiles(fileName);
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
        String commitID = getHeadBranch().getCommitID();
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
            if (commit.getMessage().equals(message)) {       // TODO: Contain or equal?
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

        // TODO: modified and untracked
        Utils.message("=== Modifications Not Staged For Commit ===");
        Utils.message("");
        Utils.message("=== Untracked Files ===");
        Utils.message("");
    }


    public static void checkout(String... args) {
        StagingArea stagingArea = getStagingArea();
        if (args.length == 2) {
            Branch branch = getBranchFromName(args[1]);
            Branch HEAD = getHeadBranch();

            if (branch.getCommitID().equals(HEAD.getCommitID())) {
                Utils.message("No need to checkout the current branch.");
                System.exit(0);
            }

            if (!untrackedFile().isEmpty()) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            deleteAllFilesInCWD();
            Commit commit = getCommitFromID(branch.getCommitID());
            Set<String> newFiles = commit.getFilesSet();
            for (String file : newFiles) {
                commit.restoreFile(file);
            }

            cleanStagingArea(stagingArea);
            changeBranch(HEAD, branch.getCommitID());
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
}

