package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    private static final long serialVersionUID = 2229685098267757691L;

    private String commitID;
    private String name;

    public Branch(String name, String pointTo) {
        this.name = name;
        this.commitID = pointTo;
    }

    public void changeTo(String dest) {
        this.commitID = dest;
    }

    public String getName() {
        return this.name;
    }

    public String getCommitID() {
        return this.commitID;
    }


}
