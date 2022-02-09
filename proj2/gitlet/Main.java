package gitlet;


/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1, "Incorrect operands.");
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2, "Incorrect operands.");
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2, "Please enter a commit message.");
                Repository.commit(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1, "Incorrect operands.");
                Repository.log();
                break;
            case "checkout":
                validateNumArgs(args, 2, 4, "Incorrect operands.");
                Repository.checkout(args);
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int n, String msg) {
        if (args.length != n) {
            Utils.message(msg);
            System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int low, int high, String msg) {
        if (args.length < low || args.length > high) {
            Utils.message(msg);
            System.exit(0);
        }
    }
}
