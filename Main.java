package gitlet;



import java.io.File;
import java.io.IOException;


import static gitlet.Utils.join;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jenna Jang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt >> (add) (hello.txt) == args
     *  */

    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Methods methods = read();
        if (methods == null && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            if (methods == null) {
                methods = new Methods();
            } else {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                System.exit(0);
            }
        } else if (args[0].equals("add")) {
            String filename = args[1];
            methods.add(filename);
        } else if (args[0].equals("commit")) {
            String message = args[1];
            methods.commit(message);
        } else if (args[0].equals("rm")) {
            methods.remove(args[1]);
        } else if (args[0].equals("log")) {
            methods.log();
        } else if (args[0].equals("global-log")) {
            methods.globalLog();
        } else if (args[0].equals("find")) {
            methods.find(args[1]);
        } else if (args[0].equals("status")) {
            methods.status();
        } else if (args[0].equals("checkout")) {
            methods.checkout(args);
        } else if (args[0].equals("branch")) {
            methods.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            methods.removeBranch(args[1]);
        } else if (args[0].equals("reset")) {
            methods.reset(args[1]);
        } else if (args[0].equals("merge")) {
            methods.merge(args[1]);
        } else if (args[0].equals("add-remote")) {
            methods.addRemote(args);
        } else if (args[0].equals("rm-remote")) {
            methods.rmRemote(args[1]);
        } else if (args[0].equals("push")) {
            methods.push(args[1], args[2]);
        } else if (args[0].equals("fetch")) {
            methods.fetch(args[1], args[2]);
        } else if (args[0].equals("pull")) {
            methods.fetch(args[1], args[2]);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        save(methods);
    }

    private static Methods read() {
        Methods methods = null;
        File reading = join(".gitlet", "data");

        if (reading.exists()) {
            methods = Utils.readObject(reading, Methods.class);
        }
        return methods;
    }
    private static void save(Methods methods) throws IOException {
        File saving = join(".gitlet", "data");

        if (!saving.exists()) {
            saving.createNewFile();
        }
        Utils.writeObject(saving, methods);
    }
}





