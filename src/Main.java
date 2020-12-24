import java.util.*;

public class Main {
    private int[][] goalState = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 0, 15, 6},
            {10, 9, 8, 7}};

    private int[][] depth2_1 = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 15, 6, 0},
            {10, 9, 8, 7}};

    private int[][] depth2_2 = {{1, 2, 3, 4},
            {0, 12, 14, 5},
            {11, 13, 15, 6},
            {10, 9, 8, 7}};

    private static List<Node> explored = new ArrayList<>();
    private static List<Node> frontier = new ArrayList<>();

    public static void main(String[] args) {


    }

    /********************** Movement Methods *********************/

    public void moveEmptyTileUp(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i > 0) {
                    int temp = puzzle[i - 1][j];
                    puzzle[i - 1][j] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }

    public void moveEmptyTileDown(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i < 3) {
                    int temp = puzzle[i + 1][j];
                    puzzle[i + 1][j] = puzzle[i][j];
                    puzzle[i][j] = temp;
                    return;
                }
    }

    public void moveEmptyTileRight(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && j < 3) {
                    int temp = puzzle[i][j + 1];
                    puzzle[i][j + 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                    return;
                }
    }

    public void moveEmptyTileLeft(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && j > 0) {
                    int temp = puzzle[i][j - 1];
                    puzzle[i][j - 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }

    public void moveEmptyTileUpLeft(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i > 0 && j > 0) {
                    int temp = puzzle[i][j - 1];
                    puzzle[i][j - 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }

    public void moveEmptyTileUpRight(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i > 0 && j < 3) {
                    int temp = puzzle[i][j - 1];
                    puzzle[i][j - 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }

    public void moveEmptyTileDownLeft(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i < 3 && j > 0) {
                    int temp = puzzle[i][j - 1];
                    puzzle[i][j - 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }

    public void moveEmptyTileDownRight(int[][] puzzle) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (puzzle[i][j] == 0 && i < 3 && j < 3) {
                    int temp = puzzle[i][j - 1];
                    puzzle[i][j - 1] = puzzle[i][j];
                    puzzle[i][j] = temp;
                }
    }


    private static int graphSearch(int[][] puzzle) {
        frontier.add(new Node(puzzle, 0, 0));
        explored.add(new Node(puzzle, 0, 0));

//        while (true) {
//            if (frontier.isEmpty()) return ERROR;
        ArrayList<Node> children;
        Node temp = null;
        if (!frontier.isEmpty()) {
            temp = frontier.get(0);
        }
        int[][] tempNode = temp.getGrid();
        int[][] topNode = new int[puzzle.length][];  // clone top of stack
        for (int i = 0; i < puzzle.length; i++)
            topNode[i] = tempNode[i].clone();
        Node top = new Node(topNode, temp.getG_n(), temp.getH_n());
        System.out.println("Fetching top node in frontier with g(n)=" + top.getG_n() + " and h(n)=" + top.getH_n() + " and f(n)=" + top.getF_n() + "...");

        printState(top.getGrid());
        if (!frontier.isEmpty()) {
            frontier.remove(0);
        }

//            if (stateEqual(topState.getGrid(), goalState)) { // successs
//                return FINISHED;
//            } else {  // keep expanding
//                children = getChildren(topState, searchFunction);
//                System.out.println("children size -> " + children.size() + "\n");
//                if (children.size() == 0) {
//                    System.out.println("Top node in frontier has no descendants, going to next node.\n");
//                    continue;  // go to next node in frontier if topNode has no descendants
//                }
//                nodesExpanded++;
//                System.out.println("Expanding top node in frontier...");
//                for (State child : children) {
//                    if (!containsChild(child)) {  // if unique state
//                        frontier.add(child);
//                        explored.add(child);
//                        if (frontier.size() > maxfrontierSize) maxfrontierSize = frontier.size();
//                        System.out.println("Adding following child to frontier:");
//                        printState(child.getGrid());
//                    } else {
////                        System.out.println("Already explored following child:");  // DEBUG
////                        printState(child.getGrid());  // DEBUG
//                    }
//                }
//            }
//        }
        return 1; //öylesine yazdım
    }

    /* Prints out current board state */
    private static void printState(int[][] currGrid) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(currGrid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void createPuzzle() {
        int puzzle[][] = new int[4][4];
        Integer[] intArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        List<Integer> intList = Arrays.asList(intArray);
        Collections.shuffle(intList);
        intList.toArray(intArray);

        int k = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j == 3)
                    break;

                puzzle[i][j] = intArray[k];
                k++;
            }
        }
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 4; j++) {
//                if (puzzle[i][j] == 0)
//                    System.out.print("    ");
//                else {
//                    if (puzzle[i][j] < 10)
//                        System.out.print("   " + puzzle[i][j] + "   ");
//                    else
//                        System.out.print("   " + puzzle[i][j] + "  ");
//                }
//            }
//            System.out.println();
//        }
    }

    public boolean isSolved(int[][] puzzle) {
        if (puzzle[0][0] == 1 && puzzle[0][1] == 2 && puzzle[0][2] == 3 && puzzle[0][3] == 4 &&
                puzzle[1][0] == 12 && puzzle[1][1] == 13 && puzzle[1][2] == 14 && puzzle[1][3] == 5 &&
                puzzle[2][0] == 11 && puzzle[2][2] == 16 && puzzle[2][3] == 15 && puzzle[3][0] == 10 &&
                puzzle[3][1] == 9 && puzzle[3][2] == 8 && puzzle[3][3] == 7)
            return true;
        return false;
    }

    public int values(int x, int y, int[][] puzzle) {
        int value = 0;
        if (puzzle[x][y] == puzzle[0][0]) value = 1;
        else if (puzzle[x][y] == puzzle[0][1]) value = 2;
        else if (puzzle[x][y] == puzzle[0][2]) value = 3;
        else if (puzzle[x][y] == puzzle[0][3]) value = 4;
        else if (puzzle[x][y] == puzzle[1][0]) value = 12;
        else if (puzzle[x][y] == puzzle[1][1]) value = 13;
        else if (puzzle[x][y] == puzzle[1][2]) value = 14;
        else if (puzzle[x][y] == puzzle[1][3]) value = 5;
        else if (puzzle[x][y] == puzzle[2][0]) value = 11;
        else if (puzzle[x][y] == puzzle[2][2]) value = 16;
        else if (puzzle[x][y] == puzzle[2][3]) value = 15;
        else if (puzzle[x][y] == puzzle[3][0]) value = 10;
        else if (puzzle[x][y] == puzzle[3][1]) value = 9;
        else if (puzzle[x][y] == puzzle[3][2]) value = 8;
        else if (puzzle[x][y] == puzzle[3][3]) value = 7;
        return value;
    }

    /* Class describing a complete node/state in the game tree */
    private static class Node {
        private int[][] grid;
        private int g_n;  // cost
        private int h_n;  // heuristic distance

        private Node(int[][] grid, int g_n, int h_n) {
            this.grid = grid;
            this.g_n = g_n;
            this.h_n = h_n;
        }

        private int[][] getGrid() {
            return grid;
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

}

