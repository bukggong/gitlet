package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


/** Class for Branches.
 *  @author Jenna Jang
 */


public class Branch implements Serializable {


    /** branch. */
    private String _Name;

    /** Head commit (branch). */
    private Commit _Head;

    /** All removed files (branch). */
    private HashMap<String, Blob> _Removed;

    /** Returns all the removed files (branch). */
    public HashMap<String, Blob> removed() {
        return _Removed;
    }

    /** Add the filename and blob to the removed list.
     * @param fileName
     * @param blob
     * */
    public void plusRemoved(String fileName, Blob blob) {
        _Removed.put(fileName, blob);
    }

    /** Remove the filenmae from the removed list.
     * @param fileName
     * */
    public void rmFrrm(String fileName) {
        _Removed.remove(fileName);
    }

    /** The name of the branch.
     * @return name
     * */
    public String name() {
        return _Name;
    }

    /** The head commit of the branch.
     * @return head
     * */
    public Commit head() {
        return _Head;
    }

    /** Stores the name and head commit.
     * @param name
     * @param head*/
    public Branch(String name, Commit head) {
        _Name = name;
        _Head = head;
        _Removed = new HashMap<String, Blob>();
    }

    /** Set the head commit of the branch with that input (commit).
     * @param commit
     * */
    public void gvCommtoHead(Commit commit) {
        _Head = commit;
    }

    /** Returns all the hashs the branch has. */
    public ArrayList<String> rtAllHash() {
        Commit arrow = _Head;
        ArrayList<String> evHash = new ArrayList<String>();
        while (arrow != null) {
            String hash = arrow.getHash();
            evHash.add(hash);
            arrow = arrow.getParent();
        }
        return evHash;
    }
}


