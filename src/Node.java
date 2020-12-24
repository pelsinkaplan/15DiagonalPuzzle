/* Class describing a complete node/state in the game tree */
class Node {
    private int[][] puzzle;
    private int g_n;  // cost
    private int h_n;  // heuristic distance

    private Node(int[][] puzzle, int g_n, int h_n) {
        this.puzzle = puzzle;
        this.g_n = g_n;
        this.h_n = h_n;
    }

    private int[][] getpuzzle() {
        return puzzle;
    }

    private int getG_n() {
        return g_n;
    }

    private int getH_n() {
        return h_n;
    }

    private int getF_n() {
        return (g_n + h_n);
    }
}

