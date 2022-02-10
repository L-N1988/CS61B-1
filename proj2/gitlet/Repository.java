package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *
 * @author Christina0031
 */
public class Repository {

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

//    private static final String slash = System.getProperty("file.separator");

    private static final String slash = "/";

    private static void makeDir() {
        String[] directory = {"blobs", "commits", "branches", "info"};
        File dirs;
        for (String s : directory) {
            dirs = new File(GITLET_DIR + slash + s);
            dirs.mkdirs();
        }
    }


    public static String initCommit() {
        Commit init = new Commit("initial commit", new Date(0), new TreeMap<>(), null);
        byte[] serialized = Utils.serialize(init);
        String sha1 = sha1(serialized);
        File file = new File(GITLET_DIR + slash + "commits" + slash + sha1);
        Utils.writeContents(file, serialized);
        return sha1;
    }

    private static void initStagingArea() {
        StagingArea stagingArea = new StagingArea();
        cleanStagingArea(stagingArea);
    }

    private static void cleanStagingArea(StagingArea stagingArea) {
        stagingArea.clean();
        saveStagingArea(stagingArea);
    }

    private static void saveStagingArea(StagingArea stagingArea) {
        File f = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        Utils.writeObject(f, stagingArea);
    }

    private static Branch createBranch(String name, String pointTo) {
        Branch branch = new Branch(name, pointTo);
        File f = new File(GITLET_DIR + slash + "branches" + slash + name);
        Utils.writeObject(f, branch);
        return branch;
    }

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

    private static StagingArea getStagingArea() {
        File file = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        StagingArea stagingArea = Utils.readObject(file, StagingArea.class);
        return stagingArea;
    }

    private static Branch getHeadBranch() {
        File f = new File(GITLET_DIR + slash + "branches" + slash + "HEAD");
        return Utils.readObject(f, Branch.class);
    }

    private static Commit getLastCommit() {
        String commitID = getHeadBranch().getCurrCommit();
        return getCommitFromID(commitID);
    }

    public static void add(String fileName) {
        File file = new File(CWD, fileName);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        } else {
            StagingArea stagingArea = getStagingArea();
            Commit lastCommit = getLastCommit();
            addToStagingArea(fileName, stagingArea, lastCommit);
        }
    }

    public static void addToStagingArea(String fileName, StagingArea stagingArea, Commit lastCommit) {
        File file = new File(CWD, fileName);
        byte[] contents = Utils.readContents(file);
        Map<String, String> map = stagingArea.getFiles();

        if (stagingArea.getRemovalFiles().contains(fileName)) {
            stagingArea.getRemovalFiles().remove(fileName);
        }

        String sha1 = sha1(contents);
        if (sha1 == lastCommit.getSHA1(fileName)) {
            if (map.containsKey(fileName)) {
                map.remove(fileName);
                deleteFile(sha1);
            }
            return;
        }
        map.put(fileName, sha1);
        writeFile(contents, sha1);
        saveStagingArea(stagingArea);
    }

    private static void deleteFromStagingArea(String fileName) {
        StagingArea stagingArea = getStagingArea();
        Map<String, String> map = stagingArea.getFiles();
        if (map.containsKey(fileName)) {
            String sha1 = map.get(fileName);
            deleteFile(sha1);
            map.remove(fileName);
        }
        saveStagingArea(stagingArea);
    }

    private static void deleteFile(String name) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + name);
        restrictedDelete(file);
    }

    private static void writeFile(byte[] contents, String sha1) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + sha1);
        Utils.writeContents(file, contents);
    }


    public static void commit(String message) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Branch HEAD = getHeadBranch();
        String lastCommitSHA1 = HEAD.getCurrCommit();
        Commit lastCommit = getCommitFromID(HEAD.getCurrCommit());

        Map<String, String> files = stagingArea.getFiles();
        Map<String, String> originFiles = lastCommit.getFiles();
        for (String name : stagingArea.getRemovalFiles()) {
            originFiles.remove(name);
        }

        Set<String> stagedFiles = files.keySet();
        for (String name : stagedFiles) {
            originFiles.put(name, files.get(name));
        }

        String newCommit = makeCommit(message, originFiles, lastCommitSHA1);
        cleanStagingArea(stagingArea);
        changeBranch(HEAD, newCommit);
    }


    public static void rm(String fileName) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.contain(fileName)) {
            deleteFromStagingArea(fileName);
            return;
        }
        Commit lastCommit = getLastCommit();
        if (lastCommit.contain(fileName)) {
            stagingArea.getRemovalFiles().add(fileName);
            File file = new File(CWD, fileName);
            if (file.exists()) {
                restrictedDelete(file);
            }
            return;
        }
        Utils.message("No reason to remove the file.");
        System.exit(0);
    }

    private static Branch changeBranch(Branch branch, String commit) {
        branch.changeTo(commit);
        File f = new File(GITLET_DIR + slash + "branches" + slash + branch.getName());
        Utils.writeObject(f, branch);
        return branch;
    }

    public static String makeCommit(String msg, Map<String, String> allFiles, String parent) {
        Commit commit = new Commit(msg, new Date(), allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        String sha1 = sha1(serialized);
        File f = new File(GITLET_DIR + slash + "commits" + slash + sha1);
        Utils.writeContents(f, serialized);
        return sha1;
    }

    public static void log() {
        String currCommit = getHeadBranch().getCurrCommit();
        while (currCommit != null) {
            currCommit = printCommit(currCommit);
        }
    }

    private static String printCommit(String sha1) {
        Commit commit = getCommitFromID(sha1);
        Utils.message("===");
        Utils.message("commit " + sha1(Utils.serialize(commit)));
        if (commit.getSecondParent() != null) {
            Utils.message("Merge: " + sha1(Utils.serialize(commit.getParent())).substring(0, 8)
                    + " " + sha1(Utils.serialize(commit.getSecondParent())).substring(0, 8));
        }
        Date date = commit.getDate();
        SimpleDateFormat f = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        Utils.message("Date: " + f.format(date));
        Utils.message(commit.getMessage());
        Utils.message("");

        return commit.getParent();
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

    private static void readFile(String name, Commit commit) {
        String sha1 = commit.getSHA1(name);
        File blob = new File(GITLET_DIR + slash + "blobs" + slash + sha1);
        File file = new File(name);
        Utils.writeContents(file, Utils.readContents(blob));
    }

    private static void getFileFromCommit(Commit commit, String fileName) {
        if (!commit.contain(fileName)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        } else {
            readFile(fileName, commit);
            deleteFromStagingArea(fileName);
        }
    }

    private static Commit getCommitFromID(String id) {
        File commitID = new File(GITLET_DIR + slash + "commits" + slash + id);
        if (!commitID.exists()) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return Utils.readObject(commitID, Commit.class);
    }

    private static Branch getBranchFromName(String name) {
        File branchName = new File(GITLET_DIR + slash + "branches" + slash + name);
        if (!branchName.exists()) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        return Utils.readObject(branchName, Branch.class);
    }

    public static void status() {
        Branch HEAD = getHeadBranch();
        List<String> branches = Utils.plainFilenamesIn(GITLET_DIR + slash + "branches");
        Utils.message("=== Branches ===");
        for (String b : branches) {
            Branch branch = getBranchFromName(b);
            if (b.equals(HEAD.getName())) {
                continue;
            }
            if (branch.getCurrCommit().equals(HEAD.getCurrCommit())) {
                Utils.message("*" + branch.getName());
            } else {
                Utils.message(b);
            }
        }
        Utils.message("");

        Utils.message("=== Staged Files ===");
        StagingArea stagingArea = getStagingArea();
        Map<String, String> map = stagingArea.getFiles();
        Set<String> set = map.keySet();
        for (String name : set) {
            Utils.message(name);
        }
        Utils.message("");

        Utils.message("=== Removed Files ===");
        Set<String> removalFiles = stagingArea.getRemovalFiles();
        for (String name : removalFiles) {
            Utils.message(name);
        }
        Utils.message("");

        // TODO: modified and untracked
        Utils.message("=== Modifications Not Staged For Commit ===");
        Utils.message("");
        Utils.message("=== Untracked Files ===");
        Utils.message("");
    }


    public static void checkout(String... args) {
        if (args[1].equals("--")) {
            Commit commit = getLastCommit();
            getFileFromCommit(commit, args[2]);
        } else if (args[2].equals("--")) {
            Commit commit = getCommitFromID(args[1]);
            getFileFromCommit(commit, args[3]);
        } else {
            Branch branch = getBranchFromName(args[1]);
            Branch HEAD = getHeadBranch();
            if (branch.getCurrCommit().equals(HEAD.getCurrCommit())) {
                Utils.message("No need to checkout the current branch.");
                System.exit(0);
            }
            List<String> allFiles = Utils.plainFilenamesIn(CWD);
            for (String file : allFiles) {
                if (!getLastCommit().contain(file)
                        && !getStagingArea().contain(file)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            for (String file : allFiles) {
                Utils.restrictedDelete(file);
            }
            Map<String, String> newFiles = getCommitFromID(branch.getCurrCommit()).getFiles();
            for (String file : newFiles.keySet()) {
                readFile(file, getCommitFromID(branch.getCurrCommit()));
            }

            cleanStagingArea(getStagingArea());
            changeBranch(HEAD, branch.getCurrCommit());
        }
    }
}

