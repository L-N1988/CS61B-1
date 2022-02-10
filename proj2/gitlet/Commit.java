package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static gitlet.RepoUtils.readBlob;

/** Represents a gitlet commit object.
 *  @author Christina0031
 */
public class Commit implements Serializable {

    private static final long serialVersionUID = 3339685098267757691L;
    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private Map<String, String> files;
    private String parent;
    private String secondParent = null;

    public Commit(String msg, Date timestamp, Map<String, String> files, String parent) {
        this.files = files;
        this.timestamp = timestamp;
        this.message = msg;
        this.parent = parent;
    }

    public Commit(String msg, Map<String, String> files, String parent) {
        this.files = files;
        this.timestamp = new Date();
        this.message = msg;
        this.parent = parent;
    }

    public String getFileID(String fileName) {
        if (files.containsKey(fileName)) {
            return files.get(fileName);
        }
        return null;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public Set<String> getFilesSet() {
        return files.keySet();
    }

    public boolean contain(String name) {
        return this.files.containsKey(name);
    }

    public String getParent() {
        return this.parent;
    }

    public Date getDate() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSecondParent() {
        return this.secondParent;
    }

    public void restoreFile(String fileName) {
        if (!contain(fileName)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }
        String fileID = getFileID(fileName);
        readBlob(fileName, fileID);
    }
}
