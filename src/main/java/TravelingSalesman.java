import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

public class TravelingSalesman {

    /**
     * Approximates the traveling salesman problem, never more than twice the cost of the optimal solution
     * @param graph an adjacency matrix of a complete graph where values correspond to the distance between two vertices
     * @return a list of the indexes of the vertices of an optimal hamiltonian cycle
     */
    public static int[] approximate(double[][] graph) {
        int root = ThreadLocalRandom.current().nextInt(graph.length);
        int[][] minTree = minimumSpanningTree(graph, root);
        int[] walk = new int[graph.length];
        preorderTreeWalk(minTree, root, walk, 0);
        return walk;
    }

    /**
     * Uses Prim's algorithm to compute the MST of a graph
     * @param graph an adjacency matrix of a graph which includes the edge weights
     * @param root the index of the vertex to start the algorithm with
     * @return the adjacency matrix of a minimum spanning tree, starting at the root
     */
    public static int[][] minimumSpanningTree(double[][] graph, int root) {
        // initialize variables
        int[] predecessors = new int[graph.length];
        double[] key = new double[graph.length];
        for (int i = 0; i < graph.length; i++) {
            predecessors[i] = -1;
            key[i] = Integer.MAX_VALUE;
        }
        key[root] = 0;
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingDouble(a -> key[a]));
        for (int i = 0; i < graph.length; i++) {
            queue.add(i);
        }
        // add a vertex at a time, adjusting the neighboring key values as needed
        while (!queue.isEmpty()) {
            int next = queue.remove();
            for (int i = 0; i < graph.length; i++) {
                if (graph[next][i] > 0 && queue.contains(i) && graph[next][i] < key[i]) {
                    predecessors[i] = next;
                    key[i] = graph[next][i];
                }
            }
        }
        // construct a matrix from the predecessor array
        int[][] minTree = new int[graph.length][graph.length];
        for (int i = 0; i < predecessors.length; i++) {
            if (predecessors[i] != -1) {
                minTree[predecessors[i]][i] = 1;
            }
        }
        return minTree;
    }

    /**
     * Set the walk array to a preorder listing of the tree's nodes
     * @param tree an adjacency matrix of a tree
     * @param root the root of the tree
     * @param walk an array to store the walk
     * @param first the first index of the array that the method should use
     * @return the number of indexes in the array that the method used
     */
    public static int preorderTreeWalk(int[][] tree, int root, int[] walk, int first) {
        walk[first] = root;
        int index = first;
        index += 1;
        for (int i = 0; i < tree.length; i++) {
            if (tree[root][i] > 0) {
                int spaceUsed = preorderTreeWalk(tree, i, walk, index);
                index += spaceUsed;
            }
        }
        return index - first;
    }

    /**
     * Generates a graph corresponding to vertices on a 2d space
     * @param size the number of vertices
     * @param width the width of the space
     * @param height the height of the space
     * @return a random adjacency matrix of a complete graph that satisfies the triangle inequality
     */
    public static double[][] generateCompleteGraph(int size, double width, double height) {
        // generate the coordinates of the points
        double[] x = new double[size];
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = ThreadLocalRandom.current().nextDouble(width);
            y[i] = ThreadLocalRandom.current().nextDouble(height);
        }
        // create an adjacency matrix
        double[][] graph = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                graph[i][j] = Math.hypot(x[i] - x[j], y[i] - y[j]);
            }
        }
        return graph;
    }
}
