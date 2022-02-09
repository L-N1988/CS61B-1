package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
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

    private static StagingArea initStagingArea() {
        StagingArea stagingArea = new StagingArea();
        return cleanStagingArea(stagingArea);
    }

    private static StagingArea cleanStagingArea(StagingArea stagingArea) {
        stagingArea.clean();
        File f = new File(GITLET_DIR + slash + "info" + slash + "stagingArea");
        Utils.writeObject(f, stagingArea);
        return stagingArea;
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

    private static Commit getLastCommit() {
        File f = new File(GITLET_DIR + slash + "branches" + slash + "HEAD");
        Commit commit = Utils.readObject(f, Branch.class).getPosition();
        return commit;
    }

    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        } else {
            StagingArea stagingArea = getStagingArea();
            Commit lastCommit = getLastCommit();
            stagingArea.add(fileName, lastCommit);
        }
    }

    public static void commit(String message) {
        StagingArea stagingArea = getStagingArea();
        if (stagingArea.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Commit lastCommit = getLastCommit();

        Map<String, String> files = stagingArea.files(); // TODO: write into blobs
        Map<String, String> origin = stagingArea.files(); // change plus files to added in new commit

        makeCommit(message, files, lastCommit);
    }

    public static void makeCommit(String msg, Map<String, String> allFiles, Commit parent) {
        Commit commit = new Commit(msg, new Date(), allFiles, parent);
        byte[] serialized = Utils.serialize(commit);
        File f = new File(GITLET_DIR + slash + "commits" + slash + sha1(serialized));
        Utils.writeContents(f, serialized);
    }


}
