package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    private static final long serialVersionUID = 2229685098267757691L;

    private String pointToCommit;
    private String name;

    public Branch(String name, String pointTo) {
        this.name = name;
        this.pointToCommit = pointTo;
    }

    public void changeTo(String dest) {
        this.pointToCommit = dest;
    }

    public String getName() {
        return this.name;
    }

    public String getPosition() {
        return this.pointToCommit;
    }
}
