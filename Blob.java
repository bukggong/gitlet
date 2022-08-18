package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class that stores the hash id and the content of the file.
 *  @author Jenna Jang
 */

public class Blob implements Serializable {

    /** Hash of the blob. */
    private String _Hash;
    /** Content of the blob. */
    private String _Content;

    /** Returns hash( in blob). */
    public String hash() {
        return _Hash;
    }

    /** Returns content( in blob) . */
    public String content() {
        return _Content;
    }

    /** takes in file and stores hash id
     * and content of the FILE.
     */
    public Blob(File file) {
        String content = Utils.readContentsAsString(file);
        _Hash = Utils.sha1(file.getName() + content);
        _Content = content;
    }

    /** Returns true blob content is same or not.
     * @param other
     * */
    public boolean contentSameDif(Blob other) {
        return this.hash().equals(other.hash());
    }



}


