import java.util.*;

import static java.lang.System.exit;

public class FifteenPuzzle {

    private static final int SIZE = 4;
    private static final int ERROR = -1;
    private static final int FINISHED = 0;
    private static final int UCS = 1;
    private static final int ILS = 2;
    private static final int A_STAR_H1 = 3;
    private static final int A_STAR_H2 = 4;
    private static final int A_STAR_H3 = 5;

    private static PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparing(Node::getF_n));
    private static ArrayList<Node> explored = new ArrayList<>();

    private static int[][] puzzle1 = {{0, 1, 3, 4},
            {12, 13, 2, 5},
            {11, 14, 15, 6},
            {10, 9, 8, 7}};

    private static int[][] puzzle2 = {{1, 3, 5, 4},
            {2, 13, 14, 15},
            {11, 12, 9, 6},
            {0, 10, 8, 7}};

    private static int[][] puzzle3 = {{1, 13, 3, 4},
            {12, 11, 2, 5},
            {9, 8, 15, 7},
            {10, 6, 14, 0}};

    private static int[][] goalState = {{1, 2, 3, 4},
            {12, 13, 14, 5},
            {11, 0, 15, 6},
            {10, 9, 8, 7}};

    private static int expandedNodes = 0;
    private static int maxFrontierSize = 1;
    private static int zeroLocX = -1;
    private static int zeroLocY = -1;
    private static int costLimit = 0;
    private static Node solution;

    public static void main(String[] args) {
        int isFinished = -1;
        int algorithm, depth, choice;
        int[][] initialState = null;
        Scanner scanner = new Scanner(System.in);
        System.out.println("For custom puzzles enter 1 else enter 0.");
        choice = Integer.parseInt(scanner.nextLine());
        if (choice == 0) {
            System.out.println("Please enter a depth: ");
            depth = Integer.parseInt(scanner.nextLine());
            initialState = createPuzzle(depth);
        } else if (choice == 1) {
            System.out.println("Select a puzzle (1, 2, 3)");
            choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    initialState = puzzle1;
                    break;
                case 2:
                    initialState = puzzle2;
                    break;
                case 3:
                    initialState = puzzle3;
                    break;
                default:
                    System.out.println("Please enter a value between 1 and 3!");
                    System.exit(1);
            }
        }

        System.out.println("Please select a search algorithm: \n1) Uniform Cost Search \n2) Iterative Lengthening Search \n3) A* Misplaced Tile \n4) A* Manhattan \n5) A* New Heurisctic ");
        algorithm = Integer.parseInt(scanner.nextLine());
        if (algorithm < 1 || algorithm > 6) {
            System.err.println("Please enter a correct algorithm: 1-5!");
            exit(1);
        } else {
            isFinished = graphSearch(initialState, algorithm);
        }
        if (isFinished == FINISHED) {
            System.out.println("Number of expanded nodes : " + expandedNodes);
            System.out.println("Maximum number of nodes stored in memory: " + maxFrontierSize);
        } else if (isFinished == ERROR) {
            System.out.println("Error: Given input has no solution!");
        }
    }

    private static int graphSearch(int[][] puzzle, int algorithm) {
        frontier.add(new Node(puzzle, 0, 0, null));
        explored.add(new Node(puzzle, 0, 0, null));
        while (true) {
            if (algorithm == ILS) {
                Node temp = frontier.peek();
                int[][] topNode = temp.getPuzzle().clone();
                Node top = new Node(topNode, temp.getG(), temp.getH(), temp.parent);
                System.out.println("g(n)=" + top.getG() + " , h(n)=" + top.getH() + " , f(n)=" + top.getF_n() + "\n");
                printNode(top.getPuzzle());
                frontier.remove();
                if (arePuzzlesEqual(top.getPuzzle(), goalState)) { // successs
                    System.out.println("Solved!");
                    top.setH(0);
                    solution = top;
                    printPath();
                    System.out.println("\nCost of the Solution : " + (top.getF_n()));
                    return FINISHED;
                } else {
                    if (getChildren(top, algorithm).size() == 0 && frontier.size() != 0) {
                        System.out.println("There is no child, continue.\n");
                        continue;
                    } else {
                        System.out.println("There is  " + getChildren(top, algorithm).size() + " children.\n");
                    }
                    expandedNodes++;
                    for (Node child : getChildren(top, algorithm)) {
                        if (!containsChild(child)) {  // if unique state
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > maxFrontierSize)
                                maxFrontierSize = frontier.size();
                            printNode(child.getPuzzle());
                        }
                    }
                    if (frontier.isEmpty()) {
                        costLimit++;
                        explored.clear();
                        frontier.add(new Node(puzzle, 0, 0, null));
                        explored.add(new Node(puzzle, 0, 0, null));
                        maxFrontierSize = 0;
                        continue;
                    }
                }
            } else {
                Node temp = frontier.peek();
                int[][] topNode = temp.getPuzzle().clone();
                Node top = new Node(topNode, temp.getG(), temp.getH(), temp.parent);
                System.out.println("g(n)=" + top.getG() + " , h(n)=" + top.getH() + " , f(n)=" + top.getF_n() + "\n");
                printNode(top.getPuzzle());
                frontier.remove();
                if (arePuzzlesEqual(top.getPuzzle(), goalState)) {
                    System.out.println("Solved!");
                    top.setH(0);
                    solution = top;
                    printPath();
                    System.out.println("\nCost of the Solution : " + (top.getF_n()));

                    return FINISHED;
                } else {
                    System.out.println("There is  " + getChildren(top, algorithm).size() + " children.\n");
                    if (getChildren(top, algorithm).size() == 0) {
                        continue;
                    }
                    expandedNodes++;
                    for (Node child : getChildren(top, algorithm)) {
                        if (!containsChild(child)) {  // if unique state
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > maxFrontierSize)
                                maxFrontierSize = frontier.size();
                            printNode(child.getPuzzle());
                        }
                    }
                }
            }
        }
    }

    private static ArrayList<Node> getChildren(Node node, int algorithm) {
        ArrayList<Node> children = new ArrayList<>();
        int[][] puzzle = node.getPuzzle();
        int g = node.getG();
        findZeroLoc(puzzle);

        int h = 0;
        switch (algorithm) {
            case UCS:
            case ILS:
                break;
            case A_STAR_H1:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        if (puzzle[i][j] != goalState[i][j])
                            h++;
                    }
                break;
            case A_STAR_H2:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        int target = puzzle[i][j];
                        for (int k = 0; k < SIZE; k++)
                            for (int l = 0; l < SIZE; l++) { // Note: Deeply-nested for loop is not a bottle neck here due to PUZZLE_SIZE limit
                                if (goalState[k][l] == target) {
                                    h += Math.abs(k - i) + Math.abs(l - j);
                                }
                            }
                    }
                break;
            case A_STAR_H3:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        for (int k = 0; k < SIZE; k++) {
                            if (puzzle[i][j] != goalState[i][k])
                                h++;
                            if (puzzle[i][j] != goalState[k][j])
                                h++;
                        }
                        if (i == j || i == 3 - j || j == 3 - i) {
                            if (puzzle[i][j] != goalState[i][j])
                                h++;
                        }

                    }
                break;
            default:
                System.err.println("ERROR!");
                System.exit(1);
                break;
        }
        if (algorithm == ILS) {
            if (g + 1 <= costLimit) {
                int[][] updatedPuzzle = moveUp(puzzle).clone();
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 1, h, node));
                }
                updatedPuzzle = moveDown(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 1, h, node));
                }
                updatedPuzzle = moveLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 1, h, node));
                }
                updatedPuzzle = moveRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 1, h, node));
                }
            }
            if (g + 3 < costLimit) {
                int[][] updatedPuzzle = moveUpLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 3, h, node));
                }
                updatedPuzzle = moveDownLeft(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 3, h, node));
                }
                updatedPuzzle = moveUpRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 3, h, node));
                }
                updatedPuzzle = moveDownRight(puzzle);
                if (!Arrays.equals(puzzle, updatedPuzzle)) {
                    children.add(new Node(updatedPuzzle, g + 3, h, node));
                }
            }
        } else {
            int[][] updatedPuzzle = moveUp(puzzle).clone();
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 1, h, node));
            }
            updatedPuzzle = moveDown(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 1, h, node));
            }
            updatedPuzzle = moveLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 1, h, node));
            }
            updatedPuzzle = moveRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 1, h, node));
            }
            updatedPuzzle = moveUpLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 3, h, node));
            }
            updatedPuzzle = moveDownLeft(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 3, h, node));
            }
            updatedPuzzle = moveUpRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 3, h, node));
            }
            updatedPuzzle = moveDownRight(puzzle);
            if (!Arrays.equals(puzzle, updatedPuzzle)) {
                children.add(new Node(updatedPuzzle, g + 3, h, node));
            }
        }
        return children;
    }


    private static int[][] moveUp(int[][] puzzle) {
        if (zeroLocX >= 1) {
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

    private static int[][] moveDown(int[][] puzzle) {
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

    private static int[][] moveLeft(int[][] puzzle) {
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

    private static int[][] moveRight(int[][] puzzle) {
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

    private static int[][] moveUpRight(int[][] puzzle) {
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

    private static int[][] moveDownRight(int[][] puzzle) {
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

    private static int[][] moveUpLeft(int[][] puzzle) {
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

    private static int[][] moveDownLeft(int[][] puzzle) {
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

    private static void findZeroLoc(int[][] puzzle) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] == 0) {
                    zeroLocX = i;
                    zeroLocY = j;
                }
            }
    }

    private static void printNode(int[][] puzzle) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] < 10)
                    System.out.print(puzzle[i][j] + "  ");
                else
                    System.out.print(puzzle[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static int[][] createPuzzle(int depth) {
        int a;
        int[][] goal = goalState.clone();
        ArrayList<int[][]> exploredStates = new ArrayList<int[][]>();
        int last = 0;
        for (int i = 0; i < depth; i++) {
            a = (int) (Math.random() * 8);
            findZeroLoc(goal);
            switch (a) {
                case 0:
                    if (!Arrays.equals(goalState, moveUp(goal)) && !Arrays.equals(goal, moveUp(goal)) && last != 1 && !exploredStates.contains(moveUp(goal))) {
                        goal = moveUp(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("up");
                        last = 0;
                    } else i--;
                    break;
                case 1:
                    if (!Arrays.equals(goalState, moveDown(goal)) && !Arrays.equals(goal, moveDown(goal)) && last != 0 && !exploredStates.contains(moveDown(goal))) {
                        goal = moveDown(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("down");
                        last = 1;
                    } else i--;
                    break;
                case 2:
                    if (!Arrays.equals(goalState, moveLeft(goal)) && !Arrays.equals(goal, moveLeft(goal)) && last != 3 && !exploredStates.contains(moveLeft(goal))) {
                        goal = moveLeft(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("left");
                        last = 2;
                    } else i--;
                    break;
                case 3:
                    if (!Arrays.equals(goalState, moveRight(goal)) && !Arrays.equals(goal, moveRight(goal)) && last != 2 && !exploredStates.contains(moveRight(goal))) {
                        goal = moveRight(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("right");
                        last = 3;
                    } else i--;
                    break;
                case 4:
                    if (!Arrays.equals(goalState, moveUpLeft(goal)) && !Arrays.equals(goal, moveUpLeft(goal)) && last != 7 && !exploredStates.contains(moveUpLeft(goal))) {
                        goal = moveUpLeft(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("upleft");
                        last = 4;
                    } else i--;
                    break;
                case 5:
                    if (!Arrays.equals(goalState, moveDownLeft(goal)) && !Arrays.equals(goal, moveDownLeft(goal)) && last != 6 && !exploredStates.contains(moveDownLeft(goal))) {
                        goal = moveDownLeft(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("downleft");
                        last = 5;
                    } else i--;
                    break;
                case 6:
                    if (!Arrays.equals(goalState, moveUpRight(goal)) && !Arrays.equals(goal, moveUpRight(goal)) && last != 5 && !exploredStates.contains(moveUpRight(goal))) {
                        goal = moveUpRight(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("upright");
                        last = 6;
                    } else i--;
                    break;
                case 7:
                    if (!Arrays.equals(goalState, moveDownRight(goal)) && !Arrays.equals(goal, moveDownRight(goal)) && last != 4 && !exploredStates.contains(moveDownRight(goal))) {
                        goal = moveDownRight(goal).clone();
                        exploredStates.add(goal);
                        System.out.println("downright");
                        last = 7;
                    } else i--;
                    break;
                default:
                    System.err.println("random number is not between 0-8");
                    break;

            }
        }
        return goal;
    }

    private static boolean arePuzzlesEqual(int[][] puzzle1, int[][] puzzle2) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle1[i][j] != puzzle2[i][j])
                    return false;
        return true;
    }

    private static void printPath() {
        System.out.println("\nSolution Path : \n");
        ArrayList<Node> pathOfSolution = new ArrayList<>();
        Node parent = solution;
        pathOfSolution.add(new Node(goalState, 0, 0, null));
        while (parent.getParent() != null) {
            pathOfSolution.add(parent.getParent());
            parent = parent.getParent();
        }
        int a = 0;
        for (int k = pathOfSolution.size() - 1; k >= 0; k--) {
            int[][] array = pathOfSolution.get(k).getPuzzle();
            a++;
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
        System.out.println("Path Length : " + (a - 1));

    }

    private static boolean containsChild(Node node) {
        int[][] puzzle = node.getPuzzle().clone();
        for (Node tempNode : explored) {
            int[][] temp = tempNode.getPuzzle().clone();
            boolean statement = true;
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    if (temp[i][j] != puzzle[i][j])
                        statement = false;
            if (statement)
                return true;
        }
        return false;
    }

    private static class Node {
        private int[][] puzzle;
        private int g;
        private int h;
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

        private int getF_n() {
            return (g + h);
        }

        private Node getParent() {
            return parent;
        }

        private void setH(int h) {
            this.h = h;
        }

    }
}