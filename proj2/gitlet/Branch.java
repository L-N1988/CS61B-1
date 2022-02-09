package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    private static final long serialVersionUID = 2229685098267757691L;
    private Commit pointTo;
    private String name;

    public Branch(String name, Commit pointTo) {
        this.name = name;
        this.pointTo = pointTo;
    }

    public void changeTo(Commit dest) {
        this.pointTo = dest;
    }

    public String getName() {
        return this.name;
    }

    public Commit getPosition() {
        return this.pointTo;
    }
}
