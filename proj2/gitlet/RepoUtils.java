package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

public class RepoUtils {
    /**
     * The current working directory.
     */
    static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    static final File GITLET_DIR = join(CWD, ".gitlet");

    //static final String slash = System.getProperty("file.separator");
    static final String slash = "/";

    static void makeDir() {
        String[] directory = {"blobs", "commits", "branches", "info"};
        File dirs;
        for (String s : directory) {
            dirs = new File(GITLET_DIR + slash + s);
            dirs.mkdirs();
        }
    }

    static void initStagingArea() {
        StagingArea stagingArea = new StagingArea();
        cleanStagingArea(stagingArea);
    }

    static void cleanStagingArea(StagingArea stagingArea) {
        stagingArea.clean();
        saveStagingArea(stagingArea);
    }

    static void saveStagingArea(StagingArea stagingArea) {
        File f = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        Utils.writeObject(f, stagingArea);
    }

    static StagingArea getStagingArea() {
        File file = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        StagingArea stagingArea = Utils.readObject(file, StagingArea.class);
        return stagingArea;
    }

    static Branch createBranch(String name, String pointTo) {
        Branch branch = new Branch(name, pointTo);
        File f = new File(GITLET_DIR + slash + "branches" + slash + name);
        Utils.writeObject(f, branch);
        return branch;
    }

    static Branch createHEAD(String pointTo) {
        Branch HEAD = new Branch(pointTo);
        File f = new File(GITLET_DIR + slash + "branches" + slash + "HEAD");
        Utils.writeObject(f, HEAD);
        return HEAD;
    }

    static void changeHEAD(String dest) {
        Branch HEAD = getBranchFromName("HEAD");
        HEAD.changePointTo(dest);
        File f = new File(GITLET_DIR + slash + "branches" + slash + "HEAD");
        Utils.writeObject(f, HEAD);
    }

    static Branch getCurrBranch() {
        String headBranch = getBranchFromName("HEAD").pointTo();
        return getBranchFromName(headBranch);
    }

    static Branch changeBranch(Branch branch, String commit) {
        branch.changeTo(commit);
        File f = new File(GITLET_DIR + slash + "branches" + slash + branch.getName());
        Utils.writeObject(f, branch);
        return branch;
    }

    static Commit getLastCommit() {
        String commitID = getCurrBranch().getCommitID();
        return getCommitFromID(commitID);
    }

    static String initCommit() {
        Commit init = new Commit("initial commit", new Date(0), new TreeMap<>(), null);
        byte[] serialized = Utils.serialize(init);
        String commitID = sha1(serialized);
        File file = new File(GITLET_DIR + slash + "commits" + slash + commitID);
        Utils.writeContents(file, serialized);
        return commitID;
    }

    static String makeCommit(String msg, Map<String, String> allFiles, String parent) {
        Commit commit = new Commit(msg, new Date(), allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        String commitID = sha1(serialized);
        File f = new File(GITLET_DIR + slash + "commits" + slash + commitID);
        Utils.writeContents(f, serialized);
        return commitID;
    }

    static void deleteBlob(String fileID) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + fileID);
        file.delete();
    }

    static void writeBlob(byte[] contents, String fileID) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + fileID);
        Utils.writeContents(file, contents);
    }

    static void readBlob(String fileName, String fileID) {
        File blob = new File(GITLET_DIR + slash + "blobs" + slash + fileID);
        File file = new File(fileName);
        Utils.writeContents(file, Utils.readContents(blob));
    }

    static String printCommit(String commitID) {
        Commit commit = getCommitFromID(commitID);
        Utils.message("===");
        Utils.message("commit " + sha1(Utils.serialize(commit)));
        if (commit.getSecondParent() != null) {
            Utils.message("Merge: " + sha1(Utils.serialize(commit.getParent())).substring(0, 8) + " " + sha1(Utils.serialize(commit.getSecondParent())).substring(0, 8));
        }
        Date date = commit.getDate();
        SimpleDateFormat f = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        Utils.message("Date: " + f.format(date));
        Utils.message(commit.getMessage());
        Utils.message("");

        return commit.getParent();
    }

    static Commit getCommitFromID(String id) {
        File commitID = new File(GITLET_DIR + slash + "commits" + slash + id);
        if (!commitID.exists()) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return Utils.readObject(commitID, Commit.class);
    }

    static Branch getBranchFromName(String name) {
        File branchName = new File(GITLET_DIR + slash + "branches" + slash + name);
        if (!branchName.exists()) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        return Utils.readObject(branchName, Branch.class);
    }

    static Map<String, String> getFileSet(StagingArea stagingArea, Commit commit) {
        Map<String, String> stagedFiles = stagingArea.getFiles();
        Map<String, String> originCommitFiles = commit.getFiles();
        for (String name : stagingArea.getRemovalFiles()) {
            originCommitFiles.remove(name);
        }
        for (String name : stagedFiles.keySet()) {
            originCommitFiles.put(name, stagedFiles.get(name));
        }
        return originCommitFiles;
    }

    static void printBranches() {
        Branch HEAD = getCurrBranch();
        List<String> branches = Utils.plainFilenamesIn(GITLET_DIR + slash + "branches");
        for (String name : branches) {
            Branch branch = getBranchFromName(name);
            if (name.equals("HEAD")) {
                continue;
            }
            if (branch.getCommitID().equals(HEAD.getCommitID())) {
                Utils.message("*" + name);
            } else {
                Utils.message(name);
            }
        }
        Utils.message("");
    }

    static Set<String> untrackedFile() {
        Set<String> untracked = new TreeSet<>();
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        for (String file : allFiles) {
            if (!getLastCommit().contain(file) && !getStagingArea().track(file)) {
                untracked.add(file);
            }
        }
        return untracked;
    }

    static void deleteAllFilesInCWD() {
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        for (String file : allFiles) {
            File f = new File(file);
            f.delete();
        }
    }
}
