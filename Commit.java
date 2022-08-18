package gitlet;




import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 *  @author Jenna J
 */
public class Commit implements Serializable {


    /** Commit message. */
    private String _Message;
    /** Commit timestamp. */
    private String _Timestamp;
    /** Commit parent. */
    private Commit _Parent;
    /** Commit blobs. */
    private HashMap<String, Blob> _Blobs;
    /** Commit hash. */
    private String _Hash;


    /** Takes in commit message, parent blobs to get timestamp.
     * @param message
     * @param parent
     * @param blobs
     */
    public Commit(String message, Commit parent, HashMap<String, Blob> blobs) {
        _Message = message;
        _Blobs = blobs;
        _Parent = parent;
        _Timestamp = "Thu Jan 01 00:00:00 1970 -0800";
        setHash(_Message + _Timestamp);
    }

    /** A commit whose initial contents and state are copied from
     *  the PARENT commit. */
    Commit(Commit parent) {
        this(null, null, null);
        commithelper(parent);
    }

    /** Set my state to a copy of the PARENT commit.
     * Referenced LOA's copyFrom method. */
    void commithelper(Commit parent) {
        _Message = parent.getMessage();
        _Parent = parent.getParent();
        _Blobs = parent.getBlobs();
        SimpleDateFormat time =
                new SimpleDateFormat("EEE MMM d hh:mm:ss yyyy Z");
        _Timestamp = time.format(new Date());
        _Hash = parent.getHash();
    }

    /** Returns the timestamp (commit). */
    public String getTimestamp() {
        return _Timestamp;
    }

    /** Returns the message (commit). */
    public String getMessage() {
        return _Message;
    }

    /** Returns the parents (commit). */
    public Commit getParent() {
        return _Parent;
    }

    /** Returns the blob (commit). */
    public HashMap<String, Blob> getBlobs() {
        return _Blobs;
    }

    /** Returns the hash (commit). */
    public String getHash() {
        return _Hash;
    }

    /** Commit message to the given message.
     * @param message  */
    public void setMessage(String message) {
        this._Message = message;
    }

    /** Commit timestamp to the given timestamp. */
    public void setTimestamp() {
        _Timestamp = getTimestamp();
    }

    /** Commit blob to the given blobs.
     * @param blobs */
    public void setBlobs(HashMap<String, Blob> blobs) {
        this._Blobs = blobs;
    }


    /** Commit parent to the given parent.
     * @param parent */
    public void setParent(Commit parent) {
        this._Parent = parent;
    }

    /** Commit hash to the given hash.
     * @param input */
    public void setHash(String input) {
        _Hash = Utils.sha1(input);
    }

    /** Return the Blob of the filename in the commit.
     * @param fileName */
    public Blob whereBlob(String fileName) {
        if (_Blobs == null) {
            return null;
        }
        for (Map.Entry<String, Blob> i : _Blobs.entrySet()) {
            String file = i.getKey();
            Blob blob = i.getValue();
            if (file.equals(fileName)) {
                return blob;
            }
        }
        return null;
    }

    /** Return true if remoteHead has history.
     * @param rmHead
     * */
    public boolean historyOrNot(Commit rmHead) {
        Commit arrow = this;
        while (arrow != null) {
            if (arrow.getHash().equals(rmHead.getHash())) {
                return true;
            }
            arrow = arrow.getParent();
        }
        return false;
    }

}
