package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.join;

/** Class that runs commands as methods and store the entire data.
 *  @author Jenna Jang
 */
public class Methods implements Serializable {

    /** Working Directory. */
    private File _CWD = new File(System.getProperty("user.dir"));
    /** New gitlet directory. */
    private File newDir = new File(_CWD, ".gitlet");
    /** Repo stage. */
    private Stage _Stage;
    /** Current working branch. */
    private Branch _Branchpoint;
    /** The program is fetched or not. */
    private boolean _Fetch = false;
    /** Commits made so far. */
    private ArrayList<Commit> _Commits;
    /** Working branched. */
    private ArrayList<Branch> _Branches;
    /** Untracked files. */
    private ArrayList<String> _untracking;
    /** Modified files not committed. */
    private ArrayList<String> _modifying;
    /** All remotes system. */
    private HashMap<String, String> _Remotes;

    /** Command constructor used to load and save data. */
    public Methods() throws IOException {
        newDir.mkdir();
        File reading = join(newDir, "data");
        reading.createNewFile();
        _Stage = new Stage();
        _Commits = new ArrayList<Commit>();
        _Branches = new ArrayList<Branch>();
        _Remotes = new HashMap<String, String>();
        Commit init = new Commit("initial commit", null, null);
        _Commits.add(init);
        Branch master = new Branch("master", init);
        _Branchpoint = master;
        _Branches.add(master);
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     * For this reason, adding a file is also called
     * staging the file for addition.
     * Staging an already-staged file overwrites the previous entry
     * in the staging area with the new contents.
     * The staging area should be somewhere in .gitlet.
     * If the current working version of the file is identical to the version
     * in the current commit, do not stage it to be added, and remove it
     * from the staging area if it is already there
     * (as can happen when a file is changed, added, and then changed back).
     * The file will no longer be staged for removal (see gitlet rm),
     * if it was at the time of the command.
     * @param fileName
     */
    void add(String fileName) {
        File add = new File(_CWD, fileName);
        if (add.exists()) {
            Blob changing = new Blob(add);
            Commit crHead = _Branchpoint.head();
            if (_Stage.rtAddition().containsKey(fileName)) {
                _Stage.replace(fileName, changing);
            }
            if (crHead.getParent() != null) {
                Blob commCont = crHead.whereBlob(fileName);
                if (commCont != null) {
                    String nHash = changing.hash();
                    String currHash = commCont.hash();
                    HashMap<String, Blob> removed = _Branchpoint.removed();
                    if (removed.containsKey(fileName)
                            && nHash.equals(currHash)) {
                        _Branchpoint.rmFrrm(fileName);
                        _Stage.rmFromrm(fileName);
                    }
                    if (!nHash.equals(currHash)) {
                        _Stage.add(fileName, changing);
                    } else if (_Stage.rtAddition().containsKey(fileName)) {
                        remove(fileName);
                    }
                } else {
                    _Stage.add(fileName, changing);
                }
            } else {
                _Stage.add(fileName, changing);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    /**  Saves a snapshot of tracked files in the current commit
     * and staging area so they can be restored at a later time,
     * creating a new commit. The commit is said to be tracking the saved files.
     * By default, each commit's snapshot of files will be exactly the same
     * as its parent commit's snapshot of files;
     * it will keep versions of files exactly as they are, and not update them.
     * @param msg
     * */
    void commit(String msg) {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        if (msg.length() > 4) {
            int i = msg.indexOf(' ');
            String substring = msg.substring(0, i);
            if (!substring.equals("Merged")
                    && !substring.equals("Encountered")) {
                if (_Stage.rtAddition().isEmpty()
                        && _Stage.rtRemoval().isEmpty()) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                }
            }
        }
        Commit crHead = _Branchpoint.head();
        Commit commit = new Commit(crHead);
        commit.setMessage(msg);
        commit.setTimestamp();
        commit.setBlobs(_Stage.rtAddition());
        commit.setParent(crHead);
        commit.setHash(commit.getMessage() + commit.getTimestamp());
        _Branchpoint.gvCommtoHead(commit);
        _Commits.add(commit);
        _Stage = new Stage();

    }

    /**  Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file
     * from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * @param fileName
     * */
    void remove(String fileName) {
        Commit crHead = _Branchpoint.head();
        if (crHead.getBlobs() == null
                && !_Stage.rtAddition().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (_Stage.rtAddition().containsKey(fileName)) {
            _Stage.minusFromAdd(fileName);
        } else if (crHead.getBlobs().containsKey(fileName)
                || crHead.getParent().getBlobs().containsKey(fileName)) {
            Blob crBlob = crHead.whereBlob(fileName);
            _Stage.remove(fileName, crBlob);
            _Branchpoint.plusRemoved(fileName, crBlob);
            Utils.restrictedDelete(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** Starting at the current head commit,
     * display information about each commit
     * backwards along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents found in merge commits. */
    void log() {
        Commit arrow = _Branchpoint.head();
        while (arrow != null) {
            printLog(arrow);
            arrow = arrow.getParent();
        }
    }

    /** Helper function of the log.
     * @param commit
     */
    void printLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getHash());
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    /** Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     * Hint: there is a useful method in gitlet.
     * Utils that will help you iterate over files within a directory. */
    void globalLog() {
        for (Commit i : _Commits) {
            printLog(i);
        }
    }

    /** Helper function of the find.
     * Return boolean value whether the given message does exist.
     * @param msgtoCommit
     * */
    boolean findMsg(String msgtoCommit) {
        for (Commit i : _Commits) {
            if (i.getMessage().equals(msgtoCommit)) {
                return true;
            }
        }
        return false;
    }

    /** Prints out the ids of all commits that have
     * the given commit message, one per line.
     * If there are multiple such commits,
     * it prints the ids out on separate lines.
     * The commit message is a single operand; to indicate a multiword message,
     * put the operand in quotation marks, as for the commit command above.
     * @param msgtoCommit
     * */
    void find(String msgtoCommit) {
        if (findMsg(msgtoCommit)) {
            for (Commit i : _Commits) {
                if (i.getMessage().equals(msgtoCommit)) {
                    System.out.println(i.getHash());
                }
            }
        } else {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Displays what branches currently exist,
     * and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * An example of the exact format it should follow is as follows. */
    void status() {
        System.out.println("=== Branches ===");
        for (Branch i : _Branches) {
            if (i.name().equals(_Branchpoint.name())) {
                System.out.println("*" + _Branchpoint.name());
            } else {
                System.out.println(i.name());
            }
        }
        System.out.println("\n=== Staged Files ===");
        if (_Stage.rtAddition() != null) {
            for (String fileName : _Stage.rtAddition().keySet()) {
                System.out.println(fileName);
            }
        }
        System.out.println("\n=== Removed Files ===");
        if (_Stage.rtRemoval() != null) {
            for (String fileName : _Stage.rtRemoval().keySet()) {
                System.out.println(fileName);
            }
        }
        _modifying = new ArrayList<>();
        _untracking = new ArrayList<>();
        statushelper1();
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String file : _modifying) {
            System.out.println(file);
        }

        System.out.println("\n=== Untracked Files ===");
        for (String file : _untracking) {
            System.out.println(file);
        }
        System.out.println();
    }

    /** Helper function for status. */
    void statushelper1() {
        Commit crHead = _Branchpoint.head();
        HashMap<String, Blob> traFiles = crHead.getBlobs();
        Commit prvHead = crHead.getParent();
        List<String> evFiles = Utils.plainFilenamesIn(_CWD);
        if (evFiles != null) {
            for (String s : evFiles) {
                ststushelpr2(s, traFiles, crHead, prvHead);
            }
        }
        deleted(traFiles);
    }

    /** Helper function for setup using
     * name, trackingfiles, current head, previous Head.
     * @param name
     * @param trFiles
     * @param crHead
     * @param prHead
     * */
    void ststushelpr2(String name, HashMap<String, Blob> trFiles,
                     Commit crHead, Commit prHead) {
        File file = new File(_CWD, name);
        boolean tracking = false, addStaging = false;
        boolean del = !file.exists();
        boolean removeStaging = false;
        Blob contStage = null;

        if (trFiles != null) {
            tracking = trFiles.containsKey(name);
        }
        if (!_Stage.rtAddition().isEmpty()) {
            addStaging = _Stage.rtAddition().containsKey(name);
            contStage = _Stage.rtAddition().get(name);
        }
        if (!_Stage.rtRemoval().isEmpty()) {
            removeStaging = _Stage.rtRemoval().containsKey(name);
        }
        boolean sameORdiff = matchFlBl(file, contStage);
        boolean situat1 = addStaging && sameORdiff;
        boolean situat2 = addStaging && del;
        boolean situat3 = !removeStaging && tracking && del;
        if (prHead == null) {
            if (situat1) {
                _modifying.add(name + " (modified)");
            }
            if (!tracking && !addStaging) {
                _untracking.add(name);
            }
        } else if (prHead.getParent() == null) {
            boolean situa4 = tracking && sameORdiff && !addStaging;
            boolean situa5 = tracking
                    && matchFlBl(file, crHead.whereBlob(name));
            if (situa4 || situat1 || situa5) {
                _modifying.add(name + " (modified)");
            }
            if (!tracking && !addStaging) {
                _untracking.add(name);
            }
        } else {
            Blob prvBlob = prHead.whereBlob(name);
            boolean prChg = matchFlBl(file, prvBlob);
            boolean situa6 = tracking && prChg && !addStaging;
            if (situa6 || situat1) {
                _modifying.add(name + " (modified)");
            }
            boolean merging = Utils.readContentsAsString
                    (file).contains("<<<<<<< HEAD");
            if (!tracking && prChg && !merging) {
                _untracking.add(name);
            }
        }
        if (situat2 || situat3) {
            _modifying.add(name + " (deleted)");
        }
    }

    /** Helper function for the status.
     * deleting file to the tracking files.
     * @param trfiles
     */
    void deleted(HashMap<String, Blob> trfiles) {
        if (trfiles != null) {
            for (String s : trfiles.keySet()) {
                File file = new File(_CWD, s);
                if (!file.exists() && !_Stage.rtRemoval().containsKey(s)) {
                    _modifying.add(s + " (deleted)");
                }
            }
        }
    }

    /** Helper function for the status.
     * Whether file and blob matches or not.
     * @param file
     * @param blob
     * @return !orgnHash.equals(hash)
     */
    boolean matchFlBl(File file, Blob blob) {
        Blob orgn = new Blob(file);

        if (blob == null) {
            return false;
        }
        String orgnHash = orgn.hash();
        String hash = blob.hash();
        return !orgnHash.equals(hash);
    }

    /** Checkouts based on ARGS
     * Usages:
     * java gitlet.Main checkout -- [file name]
     * java gitlet.Main checkout [commit id] -- [file name]
     * java gitlet.Main checkout [branch name]. */
    void checkout(String... args) throws IOException {
        if (args.length == 3) {
            if (args[1].equals("--")) {
                coFile(args[2]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                coID(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 2) {
            coBranch(args[1]);
        } else {
            System.out.println("Invalid input format");
            System.exit(0);
        }
    }

    /** Checkouts the FILENAME.
     * java gitlet.Main checkout -- [file name]
     * @param fileName
     * */
    void coFile(String fileName) {
        File file = new File(_CWD, fileName);
        Commit crrCommit = _Branchpoint.head();
        HashMap<String, Blob> tracking = crrCommit.getBlobs();
        if (tracking.containsKey(fileName)) {
            Blob blob = crrCommit.whereBlob(fileName);
            if (blob != null) {
                Utils.writeContents(file, blob.content());
            }
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** Checkout the commit ID and file name.
     * Java gitlet.Main checkout [commit id] -- [file name]
     * @param hash
     * @param fName
     * */
    void coID(String hash, String fName) throws IOException {
        Commit commit = whereHash(hash);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        HashMap<String, Blob> tracking = commit.getBlobs();
        if (!tracking.containsKey(fName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File file = new File(_CWD, fName);
        Blob blob = commit.whereBlob(fName);
        if (blob != null) {
            if (!file.exists()) {
                file.createNewFile();
            }
            Utils.writeContents(file, blob.content());
        }
    }

    /** Checkout the Branch.
     * Java gitlet.Main checkout [branch name]
     * @param brName
     * */
    void coBranch(String brName) throws IOException {
        Branch branch = whereBranch(brName);
        if (branch == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (brName.equals(_Branchpoint.name())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit coCommit = branch.head();
        HashMap<String, Blob> coTracking = coCommit.getBlobs();
        Commit crrCommit = _Branchpoint.head();
        HashMap<String, Blob> crrTracking = crrCommit.getBlobs();
        if (coTracking != null) {
            for (Map.Entry<String, Blob> i : coTracking.entrySet()) {
                String fileName = i.getKey();
                File file = new File(_CWD, fileName);
                Blob blob = i.getValue();
                boolean rm = _Branchpoint.removed().containsKey(fileName);
                if (!rm || coCommit.getMessage().equals("msg1")) {
                    if (file.exists()) {
                        noTracwriteReplacing(crrTracking, fileName, blob);
                    } else {
                        file.createNewFile();
                    }
                    Utils.writeContents(file, blob.content());
                }
            }
        }
        if (crrTracking != null) {
            for (Map.Entry<String, Blob> i : crrTracking.entrySet()) {
                String fileName = i.getKey();
                File file = new File(_CWD, fileName);
                boolean noTracking = true;
                if (coTracking != null) {
                    noTracking = !coTracking.containsKey(fileName);
                }
                if (noTracking) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        if (!_Branchpoint.removed().isEmpty()) {
            for (String i : _Branchpoint.removed().keySet()) {
                HashMap<String, Blob> eve = branch.head().
                        getParent().getBlobs();
                if (eve != null) {
                    if (eve.containsKey(i)) {
                        File file = new File(_CWD, i);
                        file.createNewFile();
                        Blob blob = branch.head().getParent().whereBlob(i);
                        Utils.writeContents(file, blob.content());
                    }
                }
            }
        }
        _Branchpoint = branch;
        _Stage = new Stage();
    }

    /**
     *
     * @param crTracking
     * @param fileName
     * @param blob
     */
    void noTracwriteReplacing(HashMap<String, Blob> crTracking,
                              String fileName, Blob blob) {
        File file = new File(_CWD, fileName);
        boolean noTracking = false;
        if (!_Branchpoint.head().getMessage().equals("msg3")) {
            if (crTracking != null) {
                noTracking = !crTracking.containsKey(fileName);
            } else {
                noTracking = Utils.plainFilenamesIn(_CWD) != null;
            }
            boolean overwrite = matchFlBl(file, blob);
            if (noTracking && overwrite) {
                System.out.println("There is an untracked file"
                        + " in the way delete it,"
                        + " or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**
     * @param hash
     * @return
     */
    Commit whereHash(String hash) {
        for (Commit i : _Commits) {
            if (hash.substring(0, 6).equals(i.getHash().substring(0, 6))) {
                return i;
            }
        }
        return null;
    }

    /**
     * @param branchName
     * @return
     */

    Branch whereBranch(String branchName) {
        for (Branch i : _Branches) {
            if (i.name().equals(branchName)) {
                return i;
            }
        }
        return null;
    }

    /**
     * @param brName
     */
    void branch(String brName) {
        if (whereBranch(brName) == null) {
            Branch nBranch = new Branch(brName, _Branchpoint.head());
            _Branches.add(nBranch);
        } else {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    /**
     * @param branch
     */
    void removeBranch(String branch) {
        Branch rm = whereBranch(branch);
        if (rm == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (_Branchpoint.name().equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        _Branches.remove(rm);
    }

    /**
     * @param hash
     * @throws IOException
     */
    void reset(String hash) throws IOException {
        Commit commit = whereHash(hash);
        if (_Fetch) {
            System.exit(0);
        }
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        boolean wrReplacing = false;
        boolean noTracking = false;
        List<String> evFiles = Utils.plainFilenamesIn(_CWD);
        HashMap<String, Blob> tracking = commit.getBlobs();
        if (evFiles != null) {
            for (String s : evFiles) {
                File file = new File(_CWD, s);
                if (tracking != null) {
                    noTracking = tracking.containsKey(s);
                }
                if (file.exists()) {
                    wrReplacing = matchFlBl(file, commit.whereBlob(s));
                }
            }
        }
        if (wrReplacing && noTracking) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            System.exit(0);
        }
        for (String name : tracking.keySet()) {
            coID(hash, name);
        }
        HashMap<String, Blob> hdTracking = _Branchpoint.head().getBlobs();
        for (String s : hdTracking.keySet()) {
            if (!tracking.containsKey(s)) {
                Utils.restrictedDelete(s);
            }
        }
        _Branchpoint.gvCommtoHead(commit);
        _Stage = new Stage();
    }


    /**
     * @param brName
     * @throws IOException
     */
    void merge(String brName) throws IOException {
        Branch branch = whereBranch(brName);
        errorMerge(branch);
        Commit spPoint = splitWhere(_Branchpoint, branch);
        Commit crHead = _Branchpoint.head();
        Commit gvnHead = branch.head();
        boolean conflict = false;
        HashMap<String, Blob> crrTra = crHead.getBlobs();
        HashMap<String, Blob> gvnTrac = gvnHead.getBlobs();
        if (gvnTrac != null) {
            for (String s : gvnTrac.keySet()) {
                File file = new File(_CWD, s);
                Blob crBlob = crHead.whereBlob(s);
                Blob gvnBlob = gvnHead.whereBlob(s);
                if (file.exists()) {
                    noTracwriteReplacing(crrTra, s, gvnBlob);
                }
                boolean modifiGivn = modiFilename(s, spPoint, gvnHead);
                boolean modifiCurr = modiFilename(s, spPoint, crHead);
                boolean splitWhere = spPoint.whereBlob(s) != null;
                boolean aCurr = crBlob != null;
                boolean aGvn = gvnBlob != null;
                if (!aCurr) {
                    if (!splitWhere && aGvn) {
                        file.createNewFile();
                        Utils.writeContents(file, gvnBlob.content());
                        add(s);
                    }
                    if (crHead.getMessage().equals("msg3")) {
                        Utils.writeContents(file, gvnBlob.content());
                        add(s);
                    }
                } else if (!aGvn) {
                    if (splitWhere && !modifiCurr) {
                        remove(s);
                    }
                } else {
                    boolean contentSD = crBlob.contentSameDif(gvnBlob);
                    boolean removGiv = branch.removed().containsKey(s);
                    boolean removCurr = _Branchpoint.removed().
                            containsKey(s);
                    boolean conflict1 = modifiGivn && !aCurr;
                    boolean conflict2 = modifiCurr && modifiGivn && !contentSD;
                    boolean conflict3 = !splitWhere && !contentSD;
                    conflict = conflict1 || conflict2 || conflict3;
                    if (conflict) {
                        crBlobGvnBlob(crBlob, gvnBlob, file);
                    } else if (modifiGivn && !modifiCurr) {
                        Utils.writeContents(file, gvnBlob);
                        add(file.getName());
                    }
                }
            }
        }
        checkRemoveGiven(branch);
        boolean edges = cases(crrTra, branch);
        boolean lsConf = lastConflict(branch, spPoint);
        conflict = conflict || lsConf || edges;
        whattoPrint(conflict, brName);
    }

    /**
     * @param branch
     * @throws IOException
     */
    void errorMerge(Branch branch) throws IOException {
        if (branch == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit crHead = _Branchpoint.head(), givenHead = branch.head();
        boolean addStage = !_Stage.rtAddition().isEmpty();
        boolean rmStage = !_Stage.rtRemoval().isEmpty();
        if (addStage || rmStage) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (_Branchpoint.name().equals(branch.name())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String spPtHash = splitWhere(_Branchpoint, branch).getHash();
        boolean smGvnCom = spPtHash.equals(givenHead.getHash());
        boolean smCrrCom = spPtHash.equals(crHead.getHash());
        if (smGvnCom) {
            System.out.println("Given branch is an ancestor"
                    + " of the current branch.");
            System.exit(0);
        }
        if (smCrrCom) {
            coBranch(branch.name());
            for (String s : branch.removed().keySet()) {
                Utils.restrictedDelete(s);
            }
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /**
     *
     * @param crrTracking
     * @param gvn
     * @return
     */
    boolean cases(HashMap<String, Blob> crrTracking, Branch gvn) {
        Commit gvnHead = gvn.head();
        for (String s : crrTracking.keySet()) {
            if (gvnHead.getParent().getBlobs() != null) {

                boolean case1 = gvn.removed().isEmpty();
                boolean case2 = _Branchpoint.removed().isEmpty();
                boolean case3 = gvnHead.getParent().
                        getBlobs().containsKey(s);
                boolean case4 = gvnHead.whereBlob(s) == null;
                if (case1 && case2 && case3 && case4) {
                    Blob blob = gvnHead.getParent().whereBlob(s);
                    if (!crrTracking.get(s).contentSameDif(blob)) {
                        crBlobGvnBlob(crrTracking.get(s),
                                blob, new File(_CWD, s));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param given
     */
    void checkRemoveGiven(Branch given) {
        if (!given.removed().isEmpty()) {
            for (String fileName : given.removed().keySet()) {
                File file = new File(_CWD, fileName);
                if (file.exists()) {
                    Utils.restrictedDelete(fileName);
                }
            }
        }
    }

    /**
     *
     * @param gvnBranch
     * @param spPoint
     * @return
     */
    boolean lastConflict(Branch gvnBranch, Commit spPoint) {
        Commit crrHead = _Branchpoint.head();
        Commit gvnHead = gvnBranch.head();
        if (crrHead.getMessage().length() > 4) {
            int i = crrHead.getMessage().indexOf(' ');
            String word = crrHead.getMessage().substring(0, i);
            if (word.equals("Merged")) {
                return false;
            }
            HashMap<String, Blob> crrTracking = crrHead.getBlobs();
            if (crrTracking != null) {
                for (String s : gvnBranch.removed().keySet()) {
                    boolean modifyingCrr = modiFilename(s, spPoint, crrHead);
                    if (crrTracking.containsKey(s) && modifyingCrr) {
                        Blob gvnBlob = gvnHead.whereBlob(s);
                        Blob crBlob = crrHead.whereBlob(s);
                        crBlobGvnBlob(crBlob, gvnBlob,
                                new File(_CWD, s));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param cfl
     * @param brNAme
     */
    void whattoPrint(boolean cfl, String brNAme) {
        String crName = _Branchpoint.name();
        if (!cfl) {
            commit("Merged " + brNAme + " into " + crName + ".");
        } else {
            System.out.println("Encountered a merge conflict.");
            commit("Encountered a merge conflict.");
        }
    }

    /**
     *
     * @param crrBlob
     * @param gvnBlob
     * @param file
     */
    void crBlobGvnBlob(Blob crrBlob, Blob gvnBlob, File file) {
        String crContent = "";
        String gvnContent = "";
        if (crrBlob != null) {
            crContent = crrBlob.content();
        }
        if (gvnBlob != null) {
            gvnContent = gvnBlob.content();
        }
        String msg = "<<<<<<< HEAD\n"
                + crContent
                + "=======\n"
                + gvnContent
                + ">>>>>>>\n";
        Utils.writeContents(file, msg);
    }

    /**
     *
     * @param fileName
     * @param spPoint
     * @param head
     * @return
     */
    boolean modiFilename(String fileName,
                         Commit spPoint, Commit head) {
        boolean modifying = false;


        if (spPoint.getBlobs() == null) {
            return true;
        }
        Blob dividingHash = spPoint.whereBlob(fileName);
        Blob hashBr = head.whereBlob(fileName);

        if (dividingHash != null && hashBr != null) {
            modifying = !dividingHash.hash().equals(hashBr.hash());
        }
        return modifying;
    }

    /**
     *
     * @param curr
     * @param merge
     * @return
     */
    Commit splitWhere(Branch curr, Branch merge) {

        ArrayList<String> everHahsNow = curr.rtAllHash();
        Commit arrow = merge.head();

        while (arrow != null) {
            String hashArrow = arrow.getHash();
            if (everHahsNow.contains(hashArrow)) {
                return arrow;
            }
            arrow = arrow.getParent();
        }
        return null;
    }

    /** Saves the given login information under the given remote name.
     * Attempts to push or pull from the given remote name
     * will then attempt to use this .gitlet directory.
     * @param args */
    void addRemote(String... args) {
        String rmName = args[1];
        String login = args[2];
        if (_Remotes.containsKey(rmName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        _Remotes.put(rmName, login);
    }

    /** Remove information associated with the given remote name.
     * The idea here is that if you ever wanted to change a remote
     * that you added, you would have to first remove it
     * and then re-add it.
     * @param remoteName
     * */
    void rmRemote(String remoteName) {
        if (!_Remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        _Remotes.remove(remoteName);
    }

    /**This command only works if the remote branch's head
     * is in the history of the current local head,
     * which means that the local branch contains
     * some commits in the future of the remote branch.
     * In this case, append the future commits to the remote branch.
     * Then, the remote should reset to the front of
     * the appended commits (so its head will be the
     * same as the local head). This is called fast-forwarding.
     * @param rmName
     * @param brNAme */
    void push(String rmName, String brNAme) {
        String login = _Remotes.get(rmName);
        Methods command = null;
        File rmFile = new File(_CWD, login);
        File rmRepo = new File(rmFile, "data");
        if (rmFile.exists()) {
            command = Utils.readObject(rmRepo, Methods.class);
        } else {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Commit crrHead = _Branchpoint.head();
        Commit copy = new Commit(crrHead);
        Branch rmBranch = command.whereBranch(brNAme);
        if (rmBranch == null) {
            Branch branch = new Branch(brNAme, copy);
            command.addBranch(branch);
            System.exit(0);
        }
        Commit rmHead = rmBranch.head();
        if (!crrHead.historyOrNot(rmHead)) {
            System.out.println("Please pull down"
                    + " remote changes before pushing.");
            System.exit(0);
        }
        rmBranch.gvCommtoHead(copy);
        File newSave = new File(rmFile, "data");
        Utils.writeObject(newSave, command);
    }

    /** Helper function of the push.
     * @param branch */
    public void addBranch(Branch branch) {
        _Branches.add(branch);
    }


    /** Brings down commits from the remote Gitlet repository
     * into the local Gitlet repository.
     * Basically, this copies all commits and blobs
     * from the given branch in the remote repository
     * (that are not already in the current repository)
     * into a branch named [remote name]/[remote branch name]
     * in the local .gitlet (just as in real Git),
     * changing [remote name]/[remote branch name] to point to the head commit
     * (thus copying the contents of the branch from the remote
     * repository to the current one).
     * This branch is created in the local repository
     * if it did not previously exist.
     * @param rmName
     * @param brName
     */
    void fetch(String rmName, String brName) {
        String loginInfo = _Remotes.get(rmName);
        File rmCWD = new File(_CWD, loginInfo);
        File data = new File(rmCWD, "data");
        Methods command = null;
        _Fetch = true;

        if (rmCWD.exists()) {
            command = Utils.readObject(data, Methods.class);
        } else {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        if (command.whereBranch(brName) == null) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        Commit crHead = _Branchpoint.head();
        Commit rmHead = command.whereBranch(brName).head();
        Commit evCopy = new Commit(rmHead);
        Commit copy = new Commit(rmHead);
        Commit arrow = copy;
        if (!arrow.getHash().equals(crHead.getHash())) {
            while (arrow.getParent() != null) {
                if (arrow.getParent().
                        getHash().equals(crHead.getHash())) {
                    break;
                }
                arrow = arrow.getParent();
            }
        }
        arrow.setParent(_Branchpoint.head());
        String nbrName = brName + "@" + rmName;
        Branch newBranch = whereBranch((nbrName));
        if (newBranch != null) {
            newBranch.gvCommtoHead(copy);
        } else {
            Branch branch = new Branch(nbrName, evCopy);
            _Branches.add(branch);
        }
        File saveNe = new File(rmCWD, "data");
        Utils.writeObject(saveNe, command);
    }

    /** Fetches branch [remote name]/[remote branch name]
     * as for the fetch command,
     * and then merges that fetch into the current branch.
     * @param remoteName
     * @param branchName */
    void pull(String remoteName, String branchName) throws IOException {
        commit("Fetch and merge together.");
    }


}
