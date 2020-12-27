import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;

import static java.lang.System.exit;

public class PuzzleSolver {
    public static void main(String[] args) {
        new PuzzleSolver();
    }

    private static final int PUZZLE_SIZE = 4; // 15 puzzle has side length of 4. Can be changed for larger puzzles.
    private static final int ERROR = -1;  // impossible layout
    private static final int FINISHED = 0;  // found goal state
    private static final int UNIFORM_COST_SEARCH = 1;
    private static final int A_STAR_MISPLACED_TILE = 2;
    private static final int A_STAR_MANHATTAN = 3;

    private static PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparing(Node::getF));  // Sort queue by f value
    private static ArrayList<Node> explored = new ArrayList<>();  // previously explored states

    private static int[][] goalState = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 0, 15, 6},
            {10, 9, 8, 7}};

    private static int[][] depth2 = {{1, 2, 3, 4},
            {12, 13, 0, 5},
            {11, 14, 15, 6},
            {10, 9, 8, 7}};

    private static int[][] impossible = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 0, 6, 15},
            {10, 9, 8, 7}};

    private static int nodesExpanded = 0;
    private static int maxQueueSize = 1;

    PuzzleSolver() {
        System.out.println("+=================================+");
        System.out.println("|         15-Puzzle Solver         |");
        System.out.println("|      PK ve İMÇ Gururla Sunar     |");
        System.out.println("+=================================+\n");
        int returnVal = -1;
        int algorithm;
        long startTime = -1;
        long endTime = -1;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please select a search algorithm: \n1) Uniform Cost Search \n2) A* Misplaced Tile \n3) A* Manhattan");
        algorithm = Integer.parseInt(scanner.nextLine());
        if (algorithm <= 0 || algorithm > 3) {
            System.err.println("Please enter a correct algorithm: 1-3!");
            exit(1);
        } else {
            startTime = System.currentTimeMillis();
            System.out.println("Input state:");
            printState(depth2);
            returnVal = generalSearch(depth2, algorithm);
        }
        endTime = System.currentTimeMillis();

        if (returnVal == FINISHED) {
            System.out.println("Solved!\n");
            System.out.println("Number of nodes expanded: " + nodesExpanded);
            System.out.println("Max queue size: " + maxQueueSize);
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
        } else if (returnVal == ERROR) {
            System.out.println("Error: Given input has no solution!");
        }
    }

    private int generalSearch(int[][] currPuzzle, int searchFunction) {
        queue.add(new Node(currPuzzle, 0, 0));
        explored.add(new Node(currPuzzle, 0, 0));
        while (true) {
            if (queue.isEmpty()) return ERROR;
            ArrayList<Node> children;
            Node tempState = queue.peek();
            int[][] tempNode = tempState.getPuzzle();
            int[][] topNode = new int[currPuzzle.length][];  // clone top of stack
            for (int i = 0; i < currPuzzle.length; i++)
                topNode[i] = tempNode[i].clone();
            Node topState = new Node(topNode, tempState.getG(), tempState.getH());
            System.out.println("Parent Node : g(n)=" + topState.getG() + "  h(n)=" + topState.getH() + "  f(n)=" + topState.getF() + "\n");
            printState(topState.getPuzzle());
            queue.remove();
            if (stateEqual(topState.getPuzzle(), goalState)) { // successs
                return FINISHED;
            } else {  // keep expanding
                children = getChildren(topState, searchFunction);
                System.out.println("There is " + children.size() + " children.\n");
                if (children.size() == 0) {
                    System.out.println("There is no child, continue.\n");
                    continue;  // go to next node in queue if topNode has no descendants
                }
                nodesExpanded++;
//                System.out.println("Expanding top node in queue...");
                for (Node child : children) {
                    if (!containsChild(child)) {  // if unique state
                        queue.add(child);
                        explored.add(child);
                        if (queue.size() > maxQueueSize) maxQueueSize = queue.size();
//                        System.out.println("Adding following child to queue:");
                        printState(child.getPuzzle());
                    }
                }
            }
        }

    }

    /* Gets child nodes, does not handle parent state exception.
     * returns children in move 0 lt, move 0 rt, move 0 up and mov 0 dn order */
    private ArrayList<Node> getChildren(Node currState, int searchFunction) {
        ArrayList<Node> retArray = new ArrayList<>();
        int[][] currPuzzle = currState.getPuzzle();
        int g = currState.getG();
        int zero_loc_x = -1;
        int zero_loc_y = -1;
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            for (int j = 0; j < PUZZLE_SIZE; j++) {
                if (currPuzzle[i][j] == 0) {
                    zero_loc_x = i;
                    zero_loc_y = j;
                }
            }
        }

        int h = 0;
        switch (searchFunction) {
            case UNIFORM_COST_SEARCH:
                h = 0;
                break;
            case A_STAR_MISPLACED_TILE:
                for (int i = 0; i < PUZZLE_SIZE; i++)
                    for (int j = 0; j < PUZZLE_SIZE; j++) {
                        if (currPuzzle[i][j] != goalState[i][j])
                            h++;
                    }
                break;
            case A_STAR_MANHATTAN:
                for (int i = 0; i < PUZZLE_SIZE; i++)
                    for (int j = 0; j < PUZZLE_SIZE; j++) {
                        int target = currPuzzle[i][j];
                        for (int k = 0; k < PUZZLE_SIZE; k++)
                            for (int l = 0; l < PUZZLE_SIZE; l++) { // Note: Deeply-nested for loop is not a bottle neck here due to PUZZLE_SIZE limit
                                if (goalState[k][l] == target) {
                                    h += Math.abs(k - i) + Math.abs(l - j);
                                }
                            }
                    }
                break;
            default:
                System.err.println("Error: Entered wrong algorithm!");
                break;
        }
        if (zero_loc_x >= 1) {  // Can move up
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();
            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x - 1][zero_loc_y];
            workingPuzzle[zero_loc_x - 1][zero_loc_y] = temp;
            retArray.add(new Node(workingPuzzle, g + 1, h));
        }
        if (zero_loc_x < PUZZLE_SIZE - 1) {  // Can move down
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x + 1][zero_loc_y];
            workingPuzzle[zero_loc_x + 1][zero_loc_y] = temp;
            retArray.add(new Node(workingPuzzle, g + 1, h));
        }
        if (zero_loc_y >= 1) {  // Can move left
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x][zero_loc_y - 1];
            workingPuzzle[zero_loc_x][zero_loc_y - 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 1, h));
        }
        if (zero_loc_y < PUZZLE_SIZE - 1) {  // Can move right
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x][zero_loc_y + 1];
            workingPuzzle[zero_loc_x][zero_loc_y + 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 1, h));
        }
        if (zero_loc_y < PUZZLE_SIZE - 1 && zero_loc_x >= 1) {  // Can move right and up
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x - 1][zero_loc_y + 1];
            workingPuzzle[zero_loc_x - 1][zero_loc_y + 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 3, h));
        }

        if (zero_loc_y < PUZZLE_SIZE - 1 && zero_loc_x < PUZZLE_SIZE - 1) {  // Can move right and down
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x + 1][zero_loc_y + 1];
            workingPuzzle[zero_loc_x + 1][zero_loc_y + 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 3, h));
        }
        if (zero_loc_y >= 1 && zero_loc_x >= 1) {  // Can move left and up
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x - 1][zero_loc_y - 1];
            workingPuzzle[zero_loc_x - 1][zero_loc_y - 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 3, h));
        }
        if (zero_loc_y >= 1 && zero_loc_x < PUZZLE_SIZE - 1) {  // Can move left and down
            // Clone currPuzzle to workingPuzzle
            int[][] workingPuzzle = new int[currPuzzle.length][];
            for (int i = 0; i < currPuzzle.length; i++)
                workingPuzzle[i] = currPuzzle[i].clone();

            int temp = workingPuzzle[zero_loc_x][zero_loc_y];
            workingPuzzle[zero_loc_x][zero_loc_y] = workingPuzzle[zero_loc_x + 1][zero_loc_y - 1];
            workingPuzzle[zero_loc_x + 1][zero_loc_y - 1] = temp;
            retArray.add(new Node(workingPuzzle, g + 3, h));
        }
        return retArray;
    }

    /* Determines whether the child passed in has previously been explored */
    private boolean containsChild(Node state) {
        int[][] child = state.getPuzzle();
        for (Node state2 : explored) {
            int[][] temp = state2.getPuzzle();
            boolean identical = true;
            for (int i = 0; i < PUZZLE_SIZE; i++)
                for (int j = 0; j < PUZZLE_SIZE; j++)
                    if (temp[i][j] != child[i][j])
                        identical = false;
            if (identical)
                return true;
        }
        return false;
    }

    /* Determines whether two board layouts are identical */
    private boolean stateEqual(int[][] puzzle1, int[][] puzzle2) {
        for (int i = 0; i < PUZZLE_SIZE; i++)
            for (int j = 0; j < PUZZLE_SIZE; j++)
                if (puzzle1[i][j] != puzzle2[i][j])
                    return false;
        return true;
    }

    /* Prints out current board state */
    private void printState(int[][] currPuzzle) {
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            for (int j = 0; j < PUZZLE_SIZE; j++) {
                System.out.print(currPuzzle[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }


    private static class Node {
        private int[][] puzzle;
        private int g;  // cost
        private int h;  // heuristic distance

        private Node(int[][] puzzle, int g, int h) {
            this.puzzle = puzzle;
            this.g = g;
            this.h = h;
        }

        private int[][] getPuzzle() {
            return puzzle;
        }

        private int getG() {
            return g;
        }

        private int getH() {
            return h;
        }

        private int getF() {
            return (g + h);
        }
    }

}
