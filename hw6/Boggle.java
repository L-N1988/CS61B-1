import java.util.*;

public class Boggle {

    // File path of dictionary file
    static String dictPath = "words.txt";
    static Trie trie = new Trie();

    private static class StringComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            int cmp = s1.length() - s2.length();
            if (cmp != 0) {
                return cmp;
            } else {
                return s2.compareTo(s1);
            }
        }
    }

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
        StringComparator sc = new StringComparator();
        PriorityQueue<String> priorityQueue = new PriorityQueue<>(sc);
        int width = board[0].length;
        int height = board.length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TreeSet<Integer> visited = new TreeSet<>();
                explore(i, j, priorityQueue, board, k, "", visited);
            }
        }
        List<String> list = new LinkedList<>();
        int size = Math.min(k, priorityQueue.size());
        for (int i = 0; i < size; i++) {
            list.add(0, priorityQueue.remove());
        }
        return list;
    }

    private static boolean validCoordinate(int i, int j, int width, int height) {
        if (i < 0 || i > width - 1 || j < 0 || j > height - 1) {
            return false;
        }
        return true;
    }

    private static void explore(int i, int j, PriorityQueue<String> pq,
                                char[][] board, int maxLength, String curr, Set<Integer> visited) {
        if (!visited.contains(i * board[0].length + j) && trie.hasPrefix(curr)) {
            curr += board[i][j];
            visited.add(i * board[0].length + j);

            if (trie.contain(curr) && curr.length() >= 3) {
                if (!pq.contains(curr)) {
                    if (pq.size() < maxLength) {
                        pq.add(curr);
                    } else {
                        pq.add(curr);
                        pq.remove();
                    }
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    if (validCoordinate(i + dx, j + dy, board[0].length, board.length)) {
                        Set<Integer> copy = new TreeSet<>(visited);
                        explore(i + dx, j + dy, pq, board, maxLength, curr, copy);
                    }
                }
            }
        } else {
            return;
        }
    }

    private static void createTrie() {
        In in = new In(dictPath);
        while (!in.isEmpty()) {
            String word = in.readString();
            trie.addWord(word);
        }
    }

    private static char[][] createBoard(String boardFilePath) {
        In in = new In(boardFilePath);
        String[] strings = in.readAllStrings();
        int height = strings.length;
        int width = strings[0].length();
        char[][] board = new char[height][width];
        for (int i = 0; i < height; i++) {
            if (strings[i].length() != width) {
                throw new IllegalArgumentException();
            }
            for (int j = 0; j < width; j++) {
                board[i][j] = strings[i].charAt(j);
            }
        }
        return board;
    }

//    public static void main(String[] args) {
//        List<String> list = solve(7, "exampleBoard.txt");
//        for (String s : list) {
//            System.out.println(s);
//        }
//    }
}
