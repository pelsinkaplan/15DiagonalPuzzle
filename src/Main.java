import java.util.*;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    private static final int SIZE = 4; // 15 puzzle has side length of 4. Can be changed for larger puzzles.
    private static final int UNIFORM_COST_SEARCH = 1;
    private static final int ITERATIVE_LENGTHENING_SEARCH = 2;
    private static final int A_STAR_MISPLACED_TILE = 3;
    private static final int A_STAR_MANHATTAN = 4;
    private static final int A_STAR_NEW_HEURISTIC = 5;

    private static int costLimit;

    private static PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparing(Node::getF));
    private static ArrayList<Node> explored = new ArrayList<>();
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
            {11, 12, 9, 6},
            {0, 10, 8, 7}};
    private static int[][] puzzle3 = {{1, 13, 3, 4},
            {12, 11, 2, 5},
            {9, 8, 15, 7},
            {10, 6, 14, 0}};

    private static int nodesExpanded = 0;
    private static int frontierSize = 1;
    private int zeroLocX = -1;
    private int zeroLocY = -1;
    private Node solution;
    private int cost;


    Main() {
        int returnVal = -1;
        int algorithm;
        int depth;
        long startTime = -1;
        long endTime = -1;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a depth: ");
        depth = Integer.parseInt(scanner.nextLine());
        System.out.println("Please select a search algorithm: \n1) Uniform Cost Search \n2) Iterative Lengthening Search \n3) A* Misplaced Tile \n4) A* Manhattan \n5) A* New Heurisctic ");
        algorithm = Integer.parseInt(scanner.nextLine());
//        initalPuzzle = createPuzzle(depth).clone();
//            initalPuzzle = puzzle1.clone();
            initalPuzzle = puzzle2.clone();
//            initalPuzzle = puzzle3.clone();
        startTime = System.currentTimeMillis();
        System.out.println("Input puzzle:");
        printPuzzle(initalPuzzle);
        returnVal = graphSearch(initalPuzzle, algorithm);

        endTime = System.currentTimeMillis();

        if (returnVal == 0) {
            System.out.println("Solved!\n");
            System.out.println("The number of expanded nodes : " + nodesExpanded);
            System.out.println("Max frontier size : " + frontierSize);
            System.out.println("Time taken: " + (endTime - startTime) + "ms");
            System.out.println("The cost of the solution : " + cost);
            printPath();
        }
    }

    private int graphSearch(int[][] puzzle, int searchFunction) {
        frontier.add(new Node(puzzle, 0, 0, null));
        explored.add(new Node(puzzle, 0, 0, null));
        while (true) {
            if (searchFunction == ITERATIVE_LENGTHENING_SEARCH) {
                ArrayList<Node> children = new ArrayList<>();
                Node temp = frontier.peek();
                int[][] tempNode = temp.getPuzzle();
                int[][] topNode = new int[puzzle.length][];  // clone top of stack
                for (int i = 0; i < puzzle.length; i++)
                    topNode[i] = tempNode[i].clone();
                Node topState = new Node(topNode, temp.getG(), temp.getH(), temp.parent);
                System.out.println("\nParent Node : g(n)=" + topState.getG() + "  h(n)=" + topState.getH() + "  f(n)=" + topState.getF() + "\n");
                printPuzzle(topState.getPuzzle());
                frontier.remove();
                if (nodesEqual(topState.getPuzzle(), goalState)) {
                    cost = topState.getF();
                    solution = topState;
                    return 0;
                } else {
                    children = getChildren(topState, searchFunction);
                    System.out.println("There is " + children.size() + " children." + "\n");
                    if (children.size() == 0 && frontier.size() != 0) {
                        System.out.println("There is no child, continue.\n");
                        continue;
                    }

                    nodesExpanded++;
                    for (Node child : children) {
                        if (!containsChild(child)) {
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > frontierSize) frontierSize = frontier.size();
                            printPuzzle(child.getPuzzle());
                        }
                    }
                    if (frontier.isEmpty()) {
                        costLimit++;
                        explored.clear();
                        frontier.add(new Node(puzzle, 0, 0, null));
                        explored.add(new Node(puzzle, 0, 0, null));
                        frontierSize = 0;
                        continue;
                    }
                }
            } else {
                ArrayList<Node> children;
                Node temp = frontier.peek();
                int[][] tempNode = temp.getPuzzle();
                int[][] topNode = new int[puzzle.length][];
                for (int i = 0; i < puzzle.length; i++)
                    topNode[i] = tempNode[i].clone();
                Node topState = new Node(topNode, temp.getG(), temp.getH(), temp.getParent());
                System.out.println("Parent Node : g(n)=" + topState.getG() + "  h(n)=" + topState.getH() + "  f(n)=" + topState.getF() + "\n");
                printPuzzle(topState.getPuzzle());
                frontier.remove();
                if (nodesEqual(topState.getPuzzle(), goalState)) {
                    cost = topState.getF();
                    solution = topState;
                    return 0;
                } else {
                    children = getChildren(topState, searchFunction);
                    System.out.println("There is " + children.size() + " children.\n");
                    if (children.size() == 0) {
                        System.out.println("There is no child, continue.\n");
                        continue;
                    }
                    nodesExpanded++;
                    for (Node child : children) {
                        if (!containsChild(child)) {
                            frontier.add(child);
                            explored.add(child);
                            if (frontier.size() > frontierSize) frontierSize = frontier.size();
                            printPuzzle(child.getPuzzle());
                        }
                    }
                }
            }
        }
    }

    private ArrayList<Node> getChildren(Node currNode, int searchFunction) {
        ArrayList<Node> retArray = new ArrayList<>();
        int[][] puzzle = currNode.getPuzzle();
        int g = currNode.getG();


        int h = 0;
        switch (searchFunction) {
            case UNIFORM_COST_SEARCH:
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
                            for (int l = 0; l < SIZE; l++) {
                                if (goalState[k][l] == target) {
                                    h += Math.abs(k - i) + Math.abs(l - j);
                                }
                            }
                    }
                break;
            case A_STAR_NEW_HEURISTIC:
                for (int i = 0; i < SIZE; i++)
                    for (int j = 0; j < SIZE; j++) {
                        for (int k = 0; k < SIZE; k++) {
                            if (puzzle[i][j] != goalState[i][k])
                                h++;
                            if (puzzle[i][j] != goalState[k][j])
                                h++;
                            if (puzzle[i][j] != goalState[i][j])
                                h++;
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

    private boolean containsChild(Node node) {
        int[][] child = node.getPuzzle();
        for (Node node2 : explored) {
            int[][] temp = node2.getPuzzle();
            boolean statement = true;
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++)
                    if (temp[i][j] != child[i][j])
                        statement = false;
            if (statement)
                return true;
        }
        return false;
    }

    private boolean nodesEqual(int[][] puzzle1, int[][] puzzle2) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle1[i][j] != puzzle2[i][j])
                    return false;
        return true;
    }

    private void printPuzzle(int[][] puzzle) {
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

    private void printPath() {
        System.out.println("\nSolution Path : \n");
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
        System.out.println("Path Length : " + a);

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