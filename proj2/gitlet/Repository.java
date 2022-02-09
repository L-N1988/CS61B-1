package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Christina0031
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    private static final String slash = System.getProperty("file.separator");

    private static void makeDir() {
        String[] directory = {"blobs", "commits", "branches", "info"};
        File f;
        for (String s : directory) {
            f = new File(GITLET_DIR + slash + s);
            f.mkdirs();
        }
    }

    private static Branch createBranch(String name, Commit pointTo) {
        Branch branch = new Branch(name, pointTo);
        File f = new File(GITLET_DIR + slash + "branches" + slash + name);
        Utils.writeObject(f, branch);
        return branch;
    }

    public static Commit initCommit() {
        Commit init = new Commit("initial commit", new Date(0), new TreeMap<String, String>(), null);
        byte[] serialized = Utils.serialize(init);
        File f = new File(GITLET_DIR + slash + "commits" + slash + sha1(serialized));
        Utils.writeContents(f, serialized);
        return init;
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

    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else {
            makeDir();
            initStagingArea();
            Commit initCommit = initCommit();
            createBranch("HEAD", initCommit);
            createBranch("master", initCommit);
        }
    }

    private static StagingArea getStagingArea() {
        File f = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        StagingArea stagingArea = Utils.readObject(f, StagingArea.class);
        return stagingArea;
    }

    private static Branch getHeadBranch() {
        File f = new File(GITLET_DIR + slash + "branches" + slash + "HEAD");
        return Utils.readObject(f, Branch.class);
    }

    public static void add(String fileName) {
        File file = new File(CWD, fileName);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        } else {
            StagingArea stagingArea = getStagingArea();
            Commit lastCommit = getHeadBranch().getPosition();
            addToStagingArea(fileName, stagingArea, lastCommit);
        }
    }

    public static void addToStagingArea(String fileName, StagingArea stagingArea, Commit lastCommit) {
        File file = new File(CWD, fileName);
        byte[] serialized = Utils.serialize(file);
        Map<String, String> map = stagingArea.getFiles();

        String sha1 = sha1(serialized);
        if (sha1 == lastCommit.getSHA1(fileName)) {
            if (map.containsKey(fileName)) {
                map.remove(fileName);
                deleteFile(sha1);
            }
            return;
        }
        map.put(fileName, sha1);
        writeFile(serialized, sha1);
        saveStagingArea(stagingArea);
    }

    private static void deleteFile(String name) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + name);
        restrictedDelete(file);
    }

    private static void writeFile(byte[] serialized, String sha1) {
        File file = new File(GITLET_DIR + slash + "blobs" + slash + sha1);
        Utils.writeContents(file, serialized);
    }


    public static void commit(String message) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Branch head = getHeadBranch();
        Commit lastCommit = head.getPosition();

        Map<String, String> files = stagingArea.getFiles();
        Map<String, String> originFiles = lastCommit.getFiles();
        Set<String> stagedFiles = files.keySet();
        for (String name : stagedFiles) {
            originFiles.put(name, files.get(name));
        }

        Commit newCommit = makeCommit(message, originFiles, lastCommit);
        cleanStagingArea(stagingArea);
        changeBranch(head, newCommit);
    }

    private static Branch changeBranch(Branch branch, Commit commit) {
        branch.changeTo(commit);
        File f = new File(GITLET_DIR + slash + "branches" + slash + branch.getName());
        Utils.writeObject(f, branch);
        return branch;
    }

    public static Commit makeCommit(String msg, Map<String, String> allFiles, Commit parent) {
        Commit commit = new Commit(msg, new Date(), allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        File f = new File(GITLET_DIR + slash + "commits" + slash + sha1(serialized));
        Utils.writeContents(f, serialized);
        return commit;
    }

    public static void log() {
        Commit curr = getHeadBranch().getPosition();
        while (curr != null) {
            printCommit(curr);
            curr = curr.getParent();
        }
    }

    private static void printCommit(Commit commit) {
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
    }

    public static void checkout(String... args) {
        if (args[1] == "--") {

        } else if (args[2] == "--") {

        } else {

        }
    }
}
