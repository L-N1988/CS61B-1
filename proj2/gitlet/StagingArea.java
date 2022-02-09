package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.sha1;

public class StagingArea implements Serializable {

    private static final long serialVersionUID = 4449685098267757691L;
    private Map<String, String> map = new TreeMap<>();

    public Map<String, String> getFiles() {
        return map;
    }

    public void clean() {
        map = new TreeMap<>();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

}
