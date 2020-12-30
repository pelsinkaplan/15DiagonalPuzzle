import java.util.*;

import static java.lang.System.exit;

public class PuzzleSolver {
    public static void main(String[] args) {
        new PuzzleSolver();
    }

    private static final int SIZE = 4; // 15 puzzle has side length of 4. Can be changed for larger puzzles.
    private static final int UNIFORM_COST_SEARCH = 1;
    private static final int ITERATIVE_LENGTHENING_SEARCH = 2;
    private static final int A_STAR_MISPLACED_TILE = 3;
    private static final int A_STAR_MANHATTAN = 4;

    private static int costLimit;

    private static PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparing(Node::getF));  // Sort queue by f value
    private static ArrayList<Node> explored = new ArrayList<>();  // previously explored states
    private static ArrayList<Node> pathOfSolution = new ArrayList<>();
    private int[][] initalPuzzle;

    private static int[][] goalState = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 0, 15, 6},
            {10, 9, 8, 7}};
    private static int[][] puzzle1 = {{0, 1, 3, 4},
            {12, 13, 2, 5},
            {11, 14, 15, 6},
            {10, 9, 8, 7}};
    private static int[][] puzzle2 = {{1, 3, 5, 4},
            {2, 13, 14, 15},
            {11,12,9, 6},
            {0, 10, 8, 7}};
    private static int[][] puzzle3 = {{ 1,13, 3, 4},
            {12, 11, 2, 5},
            {9, 8, 15, 7},
            {10, 6, 14, 0}};

    private static int nodesExpanded = 0;
    private static int maxQueueSize = 1;
    private int zeroLocX = -1;
    private int zeroLocY = -1;
    private Node solution;


    PuzzleSolver() {
        System.out.println("+=================================+");
        System.out.println("|         15-Puzzle Solver         |");
        System.out.println("|      PK ve İMÇ Gururla Sunar     |");
        System.out.println("+=================================+\n");
        int returnVal = -1;
        int algorithm;
        int depth;
        long startTime = -1;
        long endTime = -1;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please select a depth: ");
        depth = Integer.parseInt(scanner.nextLine());
        System.out.println("Please select a search algorithm: \n1) Uniform Cost Search \n2) Iterative Lengthening Search \n3) A* Misplaced Tile \n4) A* Manhattan  ");
        algorithm = Integer.parseInt(scanner.nextLine());
        if (algorithm <= 0 || algorithm > 4) {
            System.err.println("Please enter a correct algorithm: 1-4!");
            exit(1);
        } else {
            //initalPuzzle = createPuzzle(depth).clone();
            initalPuzzle = puzzle1.clone();
//            initalPuzzle = puzzle2.clone();
//            initalPuzzle = puzzle3.clone();
            startTime = System.currentTimeMillis();
            System.out.println("Input state:");
            printPuzzle(initalPuzzle);
            returnVal = generalSearch(initalPuzzle, algorithm);
        }
        endTime = System.currentTimeMillis();

        if (returnVal == 0) {
            System.out.println("Solved!\n");
            System.out.println("Number of nodes expanded: " + nodesExpanded);
            System.out.println("Max queue size: " + maxQueueSize);
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
            printPath();
        } else if (returnVal == -1) {
            System.out.println("Error: Given input has no solution!");
        }
    }

    private int generalSearch(int[][] puzzle, int searchFunction) {
        frontier.add(new Node(puzzle, 0, 0, null));
        explored.add(new Node(puzzle, 0, 0, null));
        while (true) {
            if (searchFunction == ITERATIVE_LENGTHENING_SEARCH) {
                ArrayList<Node> children;
                Node tempState = frontier.peek();
                int[][] tempNode = tempState.getPuzzle();
                int[][] topNode = new int[puzzle.length][];  // clone top of stack
                for (int i = 0; i < puzzle.length; i++)
                    topNode[i] = tempNode[i].clone();
                Node topState = new Node(topNode, tempState.getG(), tempState.getH(), tempState.parent);
                System.out.println("\nParent Node : g(n)=" + topState.getG() + "  h(n)=" + topState.getH() + "  f(n)=" + topState.getF() + "\n");
                printPuzzle(topState.getPuzzle());
                frontier.remove();
                if (nodesEqual(topState.getPuzzle(), goalState)) {
                    solution = topState;
                    return 0;
                } else {  // keep expanding
                    children = getChildren(topState, searchFunction);
                    System.out.println("There is " + children.size() + " children." + "\n");
                    if (children.size() == 0 && frontier.size() != 0) {
                        System.out.println("There is no child, continue.\n");
                        continue;  // go to next node in queue if topNode has no descendants
                    }

                    nodesExpanded++;
//                System.out.println("Expanding top node in queue...");
                    for (Node child : children) {
                        if (!containsChild(child)) {  // if unique state
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > maxQueueSize) maxQueueSize = frontier.size();
//                        System.out.println("Adding following child to queue:");
                            printPuzzle(child.getPuzzle());
                        }
                    }
                    if (frontier.isEmpty()) {
                        costLimit++;
                        explored.clear();
                        frontier.add(new Node(puzzle, 0, 0, null));
                        explored.add(new Node(puzzle, 0, 0, null));
                        nodesExpanded = 0;
                        maxQueueSize = 0;
                        continue;
                    }
                }
            } else {
                if (frontier.isEmpty()) return -1;
                ArrayList<Node> children;
                Node tempState = frontier.peek();
                int[][] tempNode = tempState.getPuzzle();
                int[][] topNode = new int[puzzle.length][];  // clone top of stack
                for (int i = 0; i < puzzle.length; i++)
                    topNode[i] = tempNode[i].clone();
                Node topState = new Node(topNode, tempState.getG(), tempState.getH(), tempState.getParent());
                System.out.println("Parent Node : g(n)=" + topState.getG() + "  h(n)=" + topState.getH() + "  f(n)=" + topState.getF() + "\n");
                printPuzzle(topState.getPuzzle());
                frontier.remove();
                if (nodesEqual(topState.getPuzzle(), goalState)) { // successs
                    solution = topState;
                    return 0;
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
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > maxQueueSize) maxQueueSize = frontier.size();
//                        System.out.println("Adding following child to queue:");
                            printPuzzle(child.getPuzzle());
                        }
                    }
                }
            }
        }
    }

    /* Gets child nodes, does not handle parent state exception.
     * returns children in move 0 lt, move 0 rt, move 0 up and mov 0 dn order */
    private ArrayList<Node> getChildren(Node currNode, int searchFunction) {
        ArrayList<Node> retArray = new ArrayList<>();
        int[][] puzzle = currNode.getPuzzle();
        int g = currNode.getG();


        int h = 0;
        switch (searchFunction) {
            case UNIFORM_COST_SEARCH:
                h = 0;
                break;
            case ITERATIVE_LENGTHENING_SEARCH:
                h = 0;
                break;
            case A_STAR_MISPLACED_TILE:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        if (puzzle[i][j] != goalState[i][j])
                            h++;
                    }
                break;
            case A_STAR_MANHATTAN:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        int target = puzzle[i][j];
                        for (int k = 0; k < SIZE; k++)
                            for (int l = 0; l < SIZE; l++) { // Note: Deeply-nested for loop is not a bottle neck here due to SIZE limit
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

        findZeroLoc(puzzle);

        if (searchFunction == ITERATIVE_LENGTHENING_SEARCH) {
            if (g + 1 <= costLimit) {
                int[][] updatedPuzzle = moveUp(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
                }
                updatedPuzzle = moveDown(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
                }
                updatedPuzzle = moveLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
                }
                updatedPuzzle = moveRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
                }
            }
            if (g + 3 < costLimit) {
                int[][] updatedPuzzle = moveUpLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
                }
                updatedPuzzle = moveDownLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
                }
                updatedPuzzle = moveUpRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
                }
                updatedPuzzle = moveDownRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
                }
            }
        } else {
            int[][] updatedPuzzle = moveUp(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
            }
            updatedPuzzle = moveDown(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
            }
            updatedPuzzle = moveLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
            }
            updatedPuzzle = moveRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 1, h, currNode));
            }
            updatedPuzzle = moveUpLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
            }
            updatedPuzzle = moveDownLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
            }
            updatedPuzzle = moveUpRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
            }
            updatedPuzzle = moveDownRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                retArray.add(new Node(updatedPuzzle, g + 3, h, currNode));
            }
        }
        return retArray;
    }

    private int[][] createPuzzle(int depth) {
        int a;
        ArrayList<int[][]> exploredPuzzle = new ArrayList<>();
        int[][] goal = goalState.clone();
        for (int i = 0; i < depth; i++) {
            a = (int) (Math.random() * 8);
            findZeroLoc(goal);
            switch (a) {
                case 0:
                    if (!Arrays.equals(goal, moveUp(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveUp(goal))) {
                        exploredPuzzle.add(moveUp(goal));
                        goal = moveUp(goal).clone();
                    } else i--;
                    break;
                case 1:
                    if (!Arrays.equals(goal, moveDown(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveDown(goal))) {
                        exploredPuzzle.add(moveDown(goal));
                        goal = moveDown(goal).clone();
                    } else i--;
                    break;
                case 2:
                    if (!Arrays.equals(goal, moveLeft(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveLeft(goal))) {
                        exploredPuzzle.add(moveLeft(goal));
                        goal = moveLeft(goal).clone();
                    } else i--;
                    break;
                case 3:
                    if (!Arrays.equals(goal, moveRight(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveRight(goal))) {
                        exploredPuzzle.add(moveRight(goal));
                        goal = moveRight(goal).clone();
                    } else i--;
                    break;
                case 4:
                    if (!Arrays.equals(goal, moveUpLeft(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveUpLeft(goal))) {
                        exploredPuzzle.add(moveUpLeft(goal));
                        goal = moveUpLeft(goal).clone();
                    } else i--;
                    break;
                case 5:
                    if (!Arrays.equals(goal, moveDownLeft(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveDownLeft(goal))) {
                        exploredPuzzle.add(moveDownLeft(goal));
                        goal = moveDownLeft(goal).clone();
                    } else i--;
                    break;
                case 6:
                    if (!Arrays.equals(goal, moveUpRight(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveUpRight(goal))) {
                        exploredPuzzle.add(moveUpRight(goal));
                        goal = moveUpRight(goal).clone();
                    } else i--;
                    break;
                case 7:
                    if (!Arrays.equals(goal, moveDownRight(goal)) && !isExploredPuzzleContains(exploredPuzzle, moveDownRight(goal))) {
                        exploredPuzzle.add(moveDownRight(goal));
                        goal = moveDownRight(goal).clone();
                    } else i--;
                    break;
                default:
                    System.err.println("random number is not between 0-8");
                    break;

            }
        }
        return goal;
    }

    private boolean isExploredPuzzleContains(ArrayList<int[][]> explore, int[][] array) {
        int[][] arrayInList;
        for (int i = 0; i < explore.size(); i++) {
            arrayInList = explore.get(i).clone();
            if (Arrays.equals(array, arrayInList))
                return true;
        }
        return false;
    }

    private void findZeroLoc(int[][] puzzle) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] == 0) {
                    zeroLocX = i;
                    zeroLocY = j;
                }
            }
        }
    }

    private int[][] moveUp(int[][] puzzle) {
        if (zeroLocX >= 1) {  // Can move up
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();
            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX - 1][zeroLocY];
            updatedPuzzle[zeroLocX - 1][zeroLocY] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveDown(int[][] puzzle) {
        if (zeroLocX < SIZE - 1) {  // Can move down
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX + 1][zeroLocY];
            updatedPuzzle[zeroLocX + 1][zeroLocY] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveLeft(int[][] puzzle) {
        if (zeroLocY >= 1) {  // Can move left
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX][zeroLocY - 1];
            updatedPuzzle[zeroLocX][zeroLocY - 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveRight(int[][] puzzle) {
        if (zeroLocY < SIZE - 1) {  // Can move right
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX][zeroLocY + 1];
            updatedPuzzle[zeroLocX][zeroLocY + 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveUpRight(int[][] puzzle) {
        if (zeroLocY < SIZE - 1 && zeroLocX >= 1) {  // Can move right and up
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX - 1][zeroLocY + 1];
            updatedPuzzle[zeroLocX - 1][zeroLocY + 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveDownRight(int[][] puzzle) {
        if (zeroLocY < SIZE - 1 && zeroLocX < SIZE - 1) {  // Can move right and down
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX + 1][zeroLocY + 1];
            updatedPuzzle[zeroLocX + 1][zeroLocY + 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveUpLeft(int[][] puzzle) {
        if (zeroLocY >= 1 && zeroLocX >= 1) {  // Can move left and up
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX - 1][zeroLocY - 1];
            updatedPuzzle[zeroLocX - 1][zeroLocY - 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    private int[][] moveDownLeft(int[][] puzzle) {
        if (zeroLocY >= 1 && zeroLocX < SIZE - 1) {  // Can move left and down
            // Clone puzzle to updatedPuzzle
            int[][] updatedPuzzle = new int[puzzle.length][];
            for (int i = 0; i < puzzle.length; i++)
                updatedPuzzle[i] = puzzle[i].clone();

            int temp = updatedPuzzle[zeroLocX][zeroLocY];
            updatedPuzzle[zeroLocX][zeroLocY] = updatedPuzzle[zeroLocX + 1][zeroLocY - 1];
            updatedPuzzle[zeroLocX + 1][zeroLocY - 1] = temp;
            return updatedPuzzle;
        }
        return puzzle;
    }

    /* Determines whether the child passed in has previously been explored */
    private boolean containsChild(Node node) {
        int[][] child = node.getPuzzle();
        for (Node state2 : explored) {
            int[][] temp = state2.getPuzzle();
            boolean identical = true;
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    if (temp[i][j] != child[i][j])
                        identical = false;
            if (identical)
                return true;
        }
        return false;
    }

    /* Determines whether two board layouts are identical */
    private boolean nodesEqual(int[][] puzzle1, int[][] puzzle2) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle1[i][j] != puzzle2[i][j])
                    return false;
        return true;
    }

    /* Prints out current board state */
    private void printPuzzle(int[][] currPuzzle) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (currPuzzle[i][j] < 10)
                    System.out.print(currPuzzle[i][j] + "  ");
                else
                    System.out.print(currPuzzle[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printPath() {
        System.out.println("Solution Path : \n\n");
        Node parent = solution;
        pathOfSolution.add(new Node(goalState, 0, 0, null));
        while (parent.getParent() != null) {
            pathOfSolution.add(parent.getParent());
            parent = parent.getParent();
        }
        for (int k = pathOfSolution.size() - 1; k >= 0; k--) {
            int[][] array = pathOfSolution.get(k).getPuzzle();
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (array[i][j] < 10)
                        System.out.print(array[i][j] + "  ");
                    else
                        System.out.print(array[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }


    private static class Node {
        private int[][] puzzle;
        private int g;  // cost
        private int h;  // heuristic distance
        private Node parent;

        private Node(int[][] puzzle, int g, int h, Node parent) {
            this.puzzle = puzzle;
            this.g = g;
            this.h = h;
            this.parent = parent;
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

        private Node getParent() {
            return parent;
        }
    }
}