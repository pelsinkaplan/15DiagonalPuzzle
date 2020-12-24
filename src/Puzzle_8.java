//"""
//8-Puzzle-Solver
//David Yu, Feb 2018
//"""

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;
import static java.lang.System.exit;

public class Puzzle_8 {


    private static final int PUZZLE_SIZE = 3; // 8 puzzle has side length of 3. Can be changed for larger puzzles.
    private static final int ERROR = -1;  // impossible layout
    private static final int FINISHED = 0;  // found goal state
    private static final int DEFAULT_PUZZLE = 1;
    private static final int CUSTOM_PUZZLE = 2;
    private static final int TRIVIAL = 1;
    private static final int VERY_EASY = 2;
    private static final int EASY = 3;
    private static final int DOABLE = 4;
    private static final int OH_BOY = 5;
    private static final int IMPOSSIBLE = 6;
    private static final int UNIFORM_COST_SEARCH = 1;
    private static final int A_STAR_MISPLACED_TILE = 2;
    private static final int A_STAR_MANHATTAN = 3;

    private static PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparing(State::getF_n));  // Sort queue by f_n value
    private static ArrayList<State> explored = new ArrayList<>();  // previously explored states

    private static int[][] trivial = {{1, 2, 3},
            {4, 5, 6},
            {7, 8, 0}};

    private static int[][] veryEasy = {{1, 2, 3},
            {4, 5, 6},
            {7, 0, 8}};

    private static int[][] easy = {{1, 2, 0},
            {4, 5, 3},
            {7, 8, 6}};

    private static int[][] doable = {{0, 1, 2},
            {4, 5, 3},
            {7, 8, 6}};

    private static int[][] ohBoy = {{8, 7, 1},
            {6, 0, 2},
            {5, 4, 3}};

    private static int[][] impossible = {{1, 2, 3},
            {4, 5, 6},
            {8, 7, 0}};

    private static int[][] goalState = {{1, 2, 3},
            {4, 5, 6},
            {7, 8, 0}};

    private static int nodesExpanded = 0;
    private static int maxQueueSize = 1;

    public static void main(String[] args) {
        System.out.println("+=================================+");
        System.out.println("|         8-Puzzel Solver         |");
        System.out.println("|            David Yu             |");
        System.out.println("+=================================+\n");
        System.out.println("Type '1' to use a default puzzle, or '2' to create your own.");
        int returnVal = -1;
        int choice;
        int difficulty;
        int algorithm;
        long startTime = -1;
        long endTime = -1;
        Scanner scanner = new Scanner(System.in);
        choice = Integer.parseInt(scanner.nextLine());
        if (choice == DEFAULT_PUZZLE) {
            System.out.println("Please enter difficulty:\n1) Trivial\n2) Very Easy\n3) Easy\n4) Doable\n5) Oh Boy\n6) Impossible");
            difficulty = Integer.parseInt(scanner.nextLine());
            if (difficulty <= 0 || difficulty > 6) {
                System.err.println("Please enter a correct difficulty: 1-6!");
                exit(1);
            } else {
                System.out.println("Please select a search algorithm:\n1) Uniform Cost Search\n2) A* Misplaced Tile\n3) A* Manhattan");
                algorithm = Integer.parseInt(scanner.nextLine());
                if (algorithm <= 0 || algorithm > 3) {
                    System.err.println("Please enter a correct algorithm: 1-3!");
                    exit(1);
                } else {
                    startTime = System.currentTimeMillis();
                    switch (difficulty) {
                        case TRIVIAL:
                            System.out.println("Input state:");
                            printState(trivial);
                            returnVal = generalSearch(trivial, algorithm);
                            break;
                        case VERY_EASY:
                            System.out.println("Input state:");
                            printState(veryEasy);
                            returnVal = generalSearch(veryEasy, algorithm);
                            break;
                        case EASY:
                            System.out.println("Input state:");
                            printState(easy);
                            returnVal = generalSearch(easy, algorithm);
                            break;
                        case DOABLE:
                            System.out.println("Input state:");
                            printState(doable);
                            returnVal = generalSearch(doable, algorithm);
                            break;
                        case OH_BOY:
                            System.out.println("Input state:");
                            printState(ohBoy);
                            returnVal = generalSearch(ohBoy, algorithm);
                            break;
                        case IMPOSSIBLE:
                            System.out.println("Input state:");
                            printState(impossible);
                            returnVal = generalSearch(impossible, algorithm);
                            break;
                        default:
                            System.out.println("Please enter a valid difficulty: 1-6!");
                            break;
                    }
                    endTime = System.currentTimeMillis();
                }
            }
        } else if (choice == CUSTOM_PUZZLE) {
            System.out.println("Please enter your custom puzzle with white spaces separating each number \n" +
                    "and hit enter to go to the next row. Use 0 to represent the blank slot.");
            int[][] custom = new int[PUZZLE_SIZE][PUZZLE_SIZE];
            for (int i = 0; i < PUZZLE_SIZE; i++) {
                String[] row;
                String line = scanner.nextLine();
                if (line == null) {
                    System.err.println("Please enter a valid layout!");
                    exit(1);
                }
                row = line.split(" ");
                for (int j = 0; j < PUZZLE_SIZE; j++)
                    custom[i][j] = Integer.parseInt(row[j]);
            }
            System.out.println("Please select a search algorithm:\n1) Uniform Cost Search\n2) A* Misplaced Tile\n3) A* Manhattan");
            algorithm = Integer.parseInt(scanner.nextLine());
            if (algorithm <= 0 || algorithm > 3) {
                System.err.println("Please enter a correct algorithm: 1-3!");
                exit(1);
            } else {
                System.out.println("Input state:");
                printState(custom);
                startTime = System.currentTimeMillis();
                returnVal = generalSearch(custom, algorithm);
                endTime = System.currentTimeMillis();
            }
        } else {
            System.err.println("Please enter a valid choice: '1' or '2'");
            exit(1);
        }
        if (returnVal == FINISHED) {
            System.out.println("Solved!\n");
            System.out.println("Number of nodes expanded: " + nodesExpanded);
            System.out.println("Max queue size: " + maxQueueSize);
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
        } else if (returnVal == ERROR) {
            System.out.println("Error: Given input has no solution!");
        }
    }

    private static int generalSearch(int[][] currGrid, int searchFunction) {
        queue.add(new State(currGrid, 0, 0));
        explored.add(new State(currGrid, 0, 0));
        while (true) {
            if (queue.isEmpty()) return ERROR;
            ArrayList<State> children;
            State tempState = queue.peek();
            int[][] tempNode = tempState.getGrid();
            int[][] topNode = new int[currGrid.length][];  // clone top of stack
            for (int i = 0; i < currGrid.length; i++)
                topNode[i] = tempNode[i].clone();
            State topState = new State(topNode, tempState.getG_n(), tempState.getH_n());
            System.out.println("Fetching top node in queue with g(n)=" + topState.getG_n() + " and h(n)=" + topState.getH_n() + " and f(n)=" + topState.getF_n() + "...");
            printState(topState.getGrid());
            queue.remove();
            if (stateEqual(topState.getGrid(), goalState)) { // successs
                return FINISHED;
            } else {  // keep expanding
                children = getChildren(topState, searchFunction);
                System.out.println("children size -> " + children.size() + "\n");
                if (children.size() == 0) {
                    System.out.println("Top node in queue has no descendants, going to next node.\n");
                    continue;  // go to next node in queue if topNode has no descendants
                }
                nodesExpanded++;
                System.out.println("Expanding top node in queue...");
                for (State child : children) {
                    if (!containsChild(child)) {  // if unique state
                        queue.add(child);
                        explored.add(child);
                        if (queue.size() > maxQueueSize) maxQueueSize = queue.size();
                        System.out.println("Adding following child to queue:");
                        printState(child.getGrid());
                    } else {
//                        System.out.println("Already explored following child:");  // DEBUG
//                        printState(child.getGrid());  // DEBUG
                    }
                }
            }
        }

    }

    /* Gets child nodes, does not handle parent state exception.
     * returns children in move 0 lt, move 0 rt, move 0 up and mov 0 dn order */
    private static ArrayList<State> getChildren(State currState, int searchFunction) {
        ArrayList<State> retArray = new ArrayList<>();
        int[][] currGrid = currState.getGrid();
        int g_n = currState.getG_n();
        int zero_loc_x = -1;
        int zero_loc_y = -1;
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            for (int j = 0; j < PUZZLE_SIZE; j++) {
                if (currGrid[i][j] == 0) {
                    zero_loc_x = i;
                    zero_loc_y = j;
                }
            }
        }

        int h_n = 0;
        switch (searchFunction) {
            case UNIFORM_COST_SEARCH:
                h_n = 0;  // Uniform cost search has no heuristic value
                break;
            case A_STAR_MISPLACED_TILE:
                for (int i = 0; i < PUZZLE_SIZE; i++)
                    for (int j = 0; j < PUZZLE_SIZE; j++) {
                        if (currGrid[i][j] != goalState[i][j])
                            h_n++;
                    }
                break;
            case A_STAR_MANHATTAN:
                for (int i = 0; i < PUZZLE_SIZE; i++)
                    for (int j = 0; j < PUZZLE_SIZE; j++) {
                        int target = currGrid[i][j];
                        for (int k = 0; k < PUZZLE_SIZE; k++)
                            for (int l = 0; l < PUZZLE_SIZE; l++) { // Note: Deeply-nested for loop is not a bottle neck here due to PUZZLE_SIZE limit
                                if (goalState[k][l] == target) {
                                    h_n += Math.abs(k - i) + Math.abs(l - j);
                                }
                            }
                    }
                break;
            default:
                System.err.println("Error: Entered wrong algorithm!");
                break;
        }

        if (zero_loc_x - 1 >= 0) {  // Can move up
//            System.out.println("can move up");
            // Clone currGrid to workingGrid
            int[][] workingGrid = new int[currGrid.length][];
            for (int i = 0; i < currGrid.length; i++)
                workingGrid[i] = currGrid[i].clone();
            int temp = workingGrid[zero_loc_x][zero_loc_y];
            workingGrid[zero_loc_x][zero_loc_y] = workingGrid[zero_loc_x - 1][zero_loc_y];
            workingGrid[zero_loc_x - 1][zero_loc_y] = temp;
            retArray.add(new State(workingGrid, g_n + 1, h_n));
        }
        if (zero_loc_x + 1 < PUZZLE_SIZE) {  // Can move down
//            System.out.println("can move down");
            // Clone currGrid to workingGrid
            int[][] workingGrid = new int[currGrid.length][];
            for (int i = 0; i < currGrid.length; i++)
                workingGrid[i] = currGrid[i].clone();

            int temp = workingGrid[zero_loc_x][zero_loc_y];
            workingGrid[zero_loc_x][zero_loc_y] = workingGrid[zero_loc_x + 1][zero_loc_y];
            workingGrid[zero_loc_x + 1][zero_loc_y] = temp;
            retArray.add(new State(workingGrid, g_n + 1, h_n));
        }
        if (zero_loc_y - 1 >= 0) {  // Can move left
//            System.out.println("can move left");
            // Clone currGrid to workingGrid
            int[][] workingGrid = new int[currGrid.length][];
            for (int i = 0; i < currGrid.length; i++)
                workingGrid[i] = currGrid[i].clone();

            int temp = workingGrid[zero_loc_x][zero_loc_y];
            workingGrid[zero_loc_x][zero_loc_y] = workingGrid[zero_loc_x][zero_loc_y - 1];
            workingGrid[zero_loc_x][zero_loc_y - 1] = temp;
            retArray.add(new State(workingGrid, g_n + 1, h_n));
        }
        if (zero_loc_y + 1 < PUZZLE_SIZE) {  // Can move right
//            System.out.println("can move right");
            // Clone currGrid to workingGrid
            int[][] workingGrid = new int[currGrid.length][];
            for (int i = 0; i < currGrid.length; i++)
                workingGrid[i] = currGrid[i].clone();

            int temp = workingGrid[zero_loc_x][zero_loc_y];
            workingGrid[zero_loc_x][zero_loc_y] = workingGrid[zero_loc_x][zero_loc_y + 1];
            workingGrid[zero_loc_x][zero_loc_y + 1] = temp;
            retArray.add(new State(workingGrid, g_n + 1, h_n));
        }
//        System.out.println("DEBUG, getChild() retArray");
//        for(State ele : retArray) {
//            System.out.println(ele.getF_n());
//            printState(ele.getGrid());
//        }
        return retArray;
    }

    /* Prints out current board state */
    private static void printState(int[][] currGrid) {
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            for (int j = 0; j < PUZZLE_SIZE; j++) {
                System.out.print(currGrid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /* Determines whether two board layouts are identical */
    private static boolean stateEqual(int[][] grid1, int[][] grid2) {
        for (int i = 0; i < PUZZLE_SIZE; i++)
            for (int j = 0; j < PUZZLE_SIZE; j++)
                if (grid1[i][j] != grid2[i][j])
                    return false;
        return true;
    }

    /* Determines whether the child passed in has previously been explored */
    private static boolean containsChild(State state) {
        int[][] child = state.getGrid();
        for (State state2 : explored) {
            int[][] temp = state2.getGrid();
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

    /* Class describing a complete node/state in the game tree */
    private static class State {
        private int[][] grid;
        private int g_n;  // cost
        private int h_n;  // heuristic distance

        private State(int[][] grid, int g_n, int h_n) {
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