import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Boggle {

    // File path of dictionary file
    static String dictPath = "words.txt";
    static Trie trie = new Trie();

    /**
     * Solves a Boggle puzzle.
     *
     * @param k             The maximum number of words to return.
     * @param boardFilePath The file path to Boggle board file.
     * @return a list of words found in given Boggle board.
     * The Strings are sorted in descending order of length.
     * If multiple words have the same length,
     * have them in ascending alphabetical order.
     */
    public static List<String> solve(int k, String boardFilePath) {
        if (k < 0) {
            throw new IllegalArgumentException();
        }
        createTrie();
        char[][] board = createBoard(boardFilePath);
        List<String> list = new ArrayList<>();


        return null;
    }

    public static void createTrie() {
        In in = new In(dictPath);
        while (!in.isEmpty()) {
            String word = in.readString();
            trie.addWord(word);
        }
    }

    public static char[][] createBoard(String boardFilePath) {
        In in = new In(boardFilePath);
        String[] strings = in.readAllStrings();
        int heigth = strings.length;
        int width = strings[0].length();
        char[][] board = new char[heigth][width];
        for (int i = 0; i < heigth; i++) {
            if (strings[i].length() != width) {
                throw new IllegalArgumentException();
            }
            for (int j = 0; j < width; j++) {
                board[i][j] = strings[i].charAt(j);
            }
        }
        return board;
    }

    public static void main(String[] args) {
        int i = 0;
        solve(3, "exampleBoard.txt");
        int j = 0;
    }
}
