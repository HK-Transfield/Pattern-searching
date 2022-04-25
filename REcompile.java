import java.util.ArrayList;

public class REcompile {

    private static ArrayList<String> ch = new ArrayList<String>(); // expected char (NULL if branch state only)
    private static ArrayList<Integer> next1 = new ArrayList<Integer>(); // possible next state numbers
    private static ArrayList<Integer> next2 = new ArrayList<Integer>(); // possible next state numbers

    private static String p; // the regular expression
    private static int j = 0; // the pointer or index for char being examined
    private static int state = 0; // tracks current state being built

    public static void main(String[] args) {
        if (args.length != 1)
            error();

        p = args[0].replaceAll("\"", "");

        System.out.println(p);

        // check that the regex has both brackets
        long leftBrackets = p.chars().filter(x -> x == '(').count();
        long rightBrackets = p.chars().filter(x -> x == ')').count();

        if (leftBrackets != rightBrackets)
            error();

        parse();
        // setState(state, " ", -1, -1);
        printStates();
    }

    /**
     * Writes all states to standard output
     */
    private static void printStates() {
        for (int i = 0; i < ch.size(); i++)
            System.out.println(i + ", " + ch.get(i) + ", " + next1.get(i) + ", " + next2.get(i));
    }

    public static boolean isVocab(char c) {
        char[] regexSpecs = { '.', '*', '+', '?', '|', '(', ')', '\\', '[', ']' };

        for (char symbol : regexSpecs)
            if (symbol == c)
                return false;

        return true;
    }

    /**
     * 
     * @param s  State number
     * @param c  Character or state description
     * @param n1
     * @param n2
     */
    public static void setState(int s, String c, int n1, int n2) {

        if (s >= ch.size()) {
            ch.add(s, c);
            next1.add(s, n1);
            next2.add(s, n2);
        } else {
            next1.set(s, n1);
            next2.set(s, n2);
        }
    }

    /**
     * Writes an appropriate error message whenever the user
     * inputs an invalid or no regular expression into the
     * terminal.
     */
    public static void error() {
        System.err.println("Error: Please enter a valid regular expression!\nExpected: java <regex>");
        System.exit(0);
    }

    /**
     * Determines whether a regexp is wellformed by
     * deriving it by the Context Free Grammar that
     * defines the language of regexps.
     */
    public static void parse() {
        int initial;
        initial = expression();

        if (j != p.length()) {
            error();
        }

        setState(state, " ", 0, 0);
    }

    /**
     * Phrase Structure:
     * E -> T
     * E -> TE
     * 
     * @return
     */
    public static int expression() {
        int r = term(); // E -> T

        if (isVocab(p.charAt(j)) || p.charAt(j) == '[') { // E -> TE
            expression();
        }

        return r;
    }

    /**
     * Phrase Structure:
     * T -> F
     * T -> F*
     * T -> F+T
     * 
     * @return
     */
    public static int term() {
        int r, t1, t2, f;

        f = state - 1;
        r = t1 = factor(); // T -> F

        if (p.charAt(j) == '*') { // T -> F*
            setState(state, " ", state + 1, t1);
            j++;
            r = state;
            state++;
        }

        if (p.charAt(j) == '+') { // T -> F+T
            if (next1.get(f) == next2.get(f)) {
                next2.set(f, state);
            }

            next1.set(f, state);

            f = state - 1;
            j++;
            r = state;
            state++;
            t2 = term();

            setState(r, " ", t1, t2);

            if (next1.get(f) == next2.get(f)) {
                next2.set(f, state);
            }

            next1.set(f, state);
        }

        return r;
    }

    /**
     * Phrase Structure
     * F -> \v
     * F -> v
     * F -> .
     * F -> [E]
     * v -> any literal
     * 
     * @return
     */
    public static int factor() {
        int r = 0;

        if (p.charAt(j) == '\\') { // F -> \v
            j++;

            String v = Character.toString(p.charAt(j));

            setState(state, v, state + 1, state + 1);

            j++;
            r = state;
            state++;
        }

        if (isVocab(p.charAt(j))) { // F -> v

            String v = Character.toString(p.charAt(j));

            setState(state, v, state + 1, state + 1);

            j++;
            r = state;
            state++;
        } else if (p.charAt(j) == '.') {

            setState(state, "..", state + 1, state + 1);

            j++;
            r = state;
            state++;
        } else {
            if (p.charAt(j) == '[') { // F-> [E]
                j++;
                r = expression();

                if (p.charAt(j) == ']') {
                    j++;
                } else {
                    error();
                }
            } else {
                error();
            }
        }
        return r;
    }
}