package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.sha1;

public class StagingArea implements Serializable {

    private static final long serialVersionUID = 4449685098267757691L;
    private Map<String, String> map = new TreeMap<>();

    public void add(String filename, Commit lastCommit) {
        File f = new File(filename);
        if (sha1(f) == lastCommit.getSHA1(filename)) {
            if (map.containsKey(filename)) {
                map.remove(filename);
            }
            return;
        }
        map.put(filename, sha1(Utils.serialize(f)));
    }

    public Map<String, String> files() {
        return map;
    }

    public void clean() {
        map = new TreeMap<>();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

}
