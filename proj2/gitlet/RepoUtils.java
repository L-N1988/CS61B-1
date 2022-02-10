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
    static final String SLASH = "/";

    static void makeDir() {
        String[] directory = {"blobs", "commits", "branches", "info"};
        File dirs;
        for (String s : directory) {
            dirs = new File(GITLET_DIR + SLASH + s);
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
        File f = new File(GITLET_DIR + SLASH + "info" + SLASH + "stagingArea");
        Utils.writeObject(f, stagingArea);
    }

    static StagingArea getStagingArea() {
        File file = new File(GITLET_DIR + SLASH + "info" + SLASH + "stagingArea");
        StagingArea stagingArea = Utils.readObject(file, StagingArea.class);
        return stagingArea;
    }

    static Branch createBranch(String name, String pointTo) {
        Branch branch = new Branch(name, pointTo);
        File f = new File(GITLET_DIR + SLASH + "branches" + SLASH + name);
        Utils.writeObject(f, branch);
        return branch;
    }

    static Branch createHEAD(String pointTo) {
        Branch head = new Branch(pointTo);
        File f = new File(GITLET_DIR + SLASH + "branches" + SLASH + "HEAD");
        Utils.writeObject(f, head);
        return head;
    }

    static void changeHEAD(String dest) {
        Branch head = getBranchFromName("HEAD");
        head.changePointTo(dest);
        File f = new File(GITLET_DIR + SLASH + "branches" + SLASH + "HEAD");
        Utils.writeObject(f, head);
    }

    static Branch getCurrBranch() {
        String headBranch = getBranchFromName("HEAD").pointTo();
        return getBranchFromName(headBranch);
    }

    static Branch changeBranch(Branch branch, String commit) {
        branch.changeTo(commit);
        File f = new File(GITLET_DIR + SLASH + "branches" + SLASH + branch.getName());
        Utils.writeObject(f, branch);
        return branch;
    }

    static void removeBranch(String name) {
        File f = new File(GITLET_DIR + SLASH + "branches" + SLASH + name);
        f.delete();
    }

    static Commit getLastCommit() {
        String commitID = getCurrBranch().getCommitID();
        return getCommitFromID(commitID);
    }

    static String initCommit() {
        Commit init = new Commit("initial commit", new Date(0), new TreeMap<>(), null);
        byte[] serialized = Utils.serialize(init);
        String commitID = sha1(serialized);
        File file = new File(GITLET_DIR + SLASH + "commits" + SLASH + commitID);
        Utils.writeContents(file, serialized);
        return commitID;
    }

    static String makeCommit(String msg, Map<String, String> allFiles, String parent) {
        Commit commit = new Commit(msg, new Date(), allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        String commitID = sha1(serialized);
        File f = new File(GITLET_DIR + SLASH + "commits" + SLASH + commitID);
        Utils.writeContents(f, serialized);
        return commitID;
    }

    static void deleteBlob(String fileID) {
        File file = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
        file.delete();
    }

    static void writeBlob(byte[] contents, String fileID) {
        File file = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
        Utils.writeContents(file, contents);
    }

    static void readBlob(String fileName, String fileID) {
        File blob = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
        File file = new File(fileName);
        Utils.writeContents(file, Utils.readContents(blob));
    }

    static String printCommit(String commitID) {
        Commit commit = getCommitFromID(commitID);
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

    static Commit getCommitFromID(String id) {
        if (id.length() < 40) {
            List<String> commits = Utils.plainFilenamesIn(GITLET_DIR + SLASH + "commits");
            for (String s : commits) {
                if (s.startsWith(id)) { // ignore duplicate
                    id = s;
                    break;
                }
            }
        }
        File commitID = new File(GITLET_DIR + SLASH + "commits" + SLASH + id);
        if (!commitID.exists()) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return Utils.readObject(commitID, Commit.class);
    }

    static Branch getBranchFromName(String name) {
        File branchName = new File(GITLET_DIR + SLASH + "branches" + SLASH + name);
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
        Branch curr = getCurrBranch();
        List<String> branches = Utils.plainFilenamesIn(GITLET_DIR + SLASH + "branches");
        for (String name : branches) {
            if (name.equals("HEAD")) {
                continue;
            }
            if (name.equals(curr.getName())) {
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
        Commit lastCommit = getLastCommit();
        StagingArea stagingArea = getStagingArea();
        for (String file : allFiles) {
            if (!lastCommit.contain(file) && !stagingArea.track(file)) {
                untracked.add(file);
            }
        }
        return untracked;
    }

    static Set<String> modifiedFile() {
        Set<String> modified = new TreeSet<>();
        Commit lastCommit = getLastCommit();
        StagingArea stagingArea = getStagingArea();
        Set<String> set = lastCommit.getFilesSet();
        for (String fileName : set) {
            if (stagingArea.track(fileName)) {
                continue;
            }
            File file = new File(fileName);
            if (file.exists()) {
                byte[] contents = Utils.readContents(file);
                String fileID = sha1(contents);
                if (lastCommit.contain(fileName)
                        && !lastCommit.getFileID(fileName).equals(fileID)) {
                    modified.add(fileName + " (modified)");
                }
            } else {
                modified.add(fileName + " (deleted)");
            }
        }
        return modified;
    }

    static void deleteAllFilesInCWD() {
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        for (String file : allFiles) {
            File f = new File(file);
            f.delete();
        }
    }

    static void validDirectory() {
        if (!GITLET_DIR.exists()) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    static void setCommit(Commit commit) {
        if (!untrackedFile().isEmpty()) {
            String msg0 = "There is an untracked file in the way; ";
            String msg1 = "delete it, or add and commit it first.";
            Utils.message(msg0 + msg1);
            System.exit(0);
        }
        deleteAllFilesInCWD();
        Set<String> newFiles = commit.getFilesSet();
        for (String file : newFiles) {
            commit.restoreFile(file);
        }
    }

    static String splitPoint(Branch curr, Branch given) {
        String cid = curr.getCommitID();
        String gid = given.getCommitID();
        String originCurr = cid;
        String originGiven = gid;
        Commit c = getCommitFromID(cid);
        Commit g = getCommitFromID(gid);
        while (!cid.equals(gid)) {
            int cmp = c.getDate().compareTo(g.getDate());
            if (cmp > 0) {
                cid = c.getParent();
                c = getCommitFromID(cid);
            } else if (cmp <= 0 && !cid.equals(gid)) {
                gid = g.getParent();
                g = getCommitFromID(gid);
            }
        }
        if (cid.equals(originCurr)) {
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (cid.equals(originGiven)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        return cid;
    }
}
