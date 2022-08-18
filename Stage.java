package gitlet;

import java.io.Serializable;
import java.util.HashMap;


/** Class that stores the added and removed files before commit.
 *  @author Jenna Jang
 */
public class Stage implements Serializable {

    /** Addition List. */
    private HashMap<String, Blob> _Addition;

    /** Removal List. */
    private HashMap<String, Blob> _Removal;

    /** Returns the Addition List (addition is hashmap). */
    public HashMap<String, Blob> rtAddition() {
        return _Addition;
    }

    /** Returns the Removal List (removal is hashmap). */
    public HashMap<String, Blob> rtRemoval() {
        return _Removal;
    }

    /** It is used to commit the changes. */
    public Stage() {
        _Addition = new HashMap<String, Blob>();
        _Removal = new HashMap<String, Blob>();
    }

    public void add(String fileName, Blob change) {
        _Addition.put(fileName, change);
    }


    public void minusFromAdd(String fileName) {
        _Addition.remove(fileName);
    }


    public void remove(String fileName, Blob change) {
        _Removal.put(fileName, change);

    }

    public void rmFromrm(String fileName) {
        _Removal.remove(fileName);
    }


    public void replace(String fileName, Blob change) {
        if (_Addition.containsKey(fileName)) {
            _Addition.replace(fileName, change);
        }
    }
}
