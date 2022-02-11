package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.checkout;
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
        Commit commit = new Commit(msg, allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        String commitID = sha1(serialized);
        File f = new File(GITLET_DIR + SLASH + "commits" + SLASH + commitID);
        Utils.writeContents(f, serialized);
        return commitID;
    }

    static String makeMergeCommit(
            String msg, Map<String, String> allFiles, String parent, String secondParent) {
        Commit commit = new Commit(msg, allFiles, parent, secondParent);
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

    static void writeBlob(String contents, String fileID) {
        File file = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
        Utils.writeContents(file, contents);
    }

    static void readBlob(String fileName, String fileID) {
        File blob = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
        File file = new File(fileName);
        Utils.writeContents(file, Utils.readContentsAsString(blob));
    }

    static String printCommit(String commitID) {
        Commit commit = getCommitFromID(commitID);
        Utils.message("===");
        Utils.message("commit " + commitID);
        if (commit.hasSecondParent()) {
            Utils.message("Merge: " + commit.getParent().substring(0, 7)
                    + " " + commit.getSecondParent().substring(0, 7));
        }
        Date date = commit.getDate();
        SimpleDateFormat f = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
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

    static Map<String, String> getCommitFileSet(StagingArea stagingArea, Commit commit) {
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
                String contents = Utils.readContentsAsString(file);
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

    static String latestCommit(String c1, String c2) {
        if (c1 == null) {
            return c2;
        } else if (c2 == null) {
            return c1;
        }
        Commit commit1 = getCommitFromID(c1);
        Commit commit2 = getCommitFromID(c2);
        int cmp = commit1.getDate().compareTo(commit2.getDate());
        if (cmp > 0) {
            return c1;
        } else {
            return c2;
        }
    }

    static String getSplitPoint(String commitID1, String commitID2) {
        if (commitID1.equals(commitID2)) {
            return commitID2;
        }
        Commit c1 = getCommitFromID(commitID1);
        Commit c2 = getCommitFromID(commitID2);

        if (c1.hasSecondParent()) {
            String res1 = getSplitPoint(c1.getParent(), commitID2);
            String res2 = getSplitPoint(c1.getSecondParent(), commitID2);
            return latestCommit(res1, res2);
        } else if (c2.hasSecondParent()) {
            String res1 = getSplitPoint(commitID1, c2.getParent());
            String res2 = getSplitPoint(commitID1, c2.getSecondParent());
            return latestCommit(res1, res2);
        } else {
            String latest = latestCommit(commitID1, commitID2);
            if (latest.equals(commitID1)) {
                if (c1.hasParent()) {
                    return getSplitPoint(c1.getParent(), commitID2);
                } else {
                    return null;
                }
            } else {
                if (c2.hasParent()) {
                    return getSplitPoint(commitID1, c2.getParent());
                } else {
                    return null;
                }
            }
        }
    }

    static String splitPoint(Branch curr, Branch given) {
        String originCurr = curr.getCommitID();
        String originGiven = given.getCommitID();
        String splitPoint = getSplitPoint(originCurr, originGiven);
        if (splitPoint.equals(originCurr)) {
            String[] args = {"checkout", given.getName()};
            checkout(args);
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (splitPoint.equals(originGiven)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        return splitPoint;
    }

    static boolean equal(Map<String, String> map1, Map<String, String> map2,
                         String name) {
        return map1.get(name).equals(map2.get(name));
    }

    static void handleConflict(Map<String, String> currFiles, Map<String, String> givenFiles,
                               String conflictedFileName) {
        File file = new File(conflictedFileName);
        String fileHead = "<<<<<<< HEAD\n";
        String separator = "=======\n";
        String fileFoot = ">>>>>>>\n";
        String givenContent = null;
        String currContent = null;
        if (currFiles.containsKey(conflictedFileName)) {
            String fileID = currFiles.get(conflictedFileName);
            File blob = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
            currContent = Utils.readContentsAsString(blob);
        }
        if (givenFiles.containsKey(conflictedFileName)) {
            String fileID = givenFiles.get(conflictedFileName);
            File blob = new File(GITLET_DIR + SLASH + "blobs" + SLASH + fileID);
            givenContent = Utils.readContentsAsString(blob);

        }
        if (givenContent == null) {
            Utils.writeContents(file, fileHead, currContent, separator, "", fileFoot);
        } else if (currContent == null) {
            Utils.writeContents(file, fileHead, "", separator, givenContent, fileFoot);
        } else {
            Utils.writeContents(file,
                    fileHead, currContent, separator, givenContent, fileFoot);
        }
    }

    static boolean processFiles(
            String splitPointID, Commit lastCommit, Branch given, StagingArea stagingArea) {
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
                } else if (split && !equal(givenFiles, currFiles, s)
                        && !equal(givenFiles, splitFiles, s)
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
        return conflict;
    }
}
