package gitlet;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.sha1;

public class StagingArea implements Serializable {

    private static final long serialVersionUID = 4449685098267757691L;
    private Map<String, String> map = new TreeMap<>();
    private Set<String> removal = new TreeSet<>();

    public Map<String, String> getFiles() {
        return map;
    }

    public void clean() {
        map = new TreeMap<>();
        removal = new TreeSet<>();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> getRemovalFiles() {
        return removal;
    }

}
