package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

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

    public String getSHA1(String fileName) {
        if (files.containsKey(fileName)) {
            return files.get(fileName);
        }
        return null;
    }

    public Map<String, String> getFiles() {
        return files;
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
}
