import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        double[][] graph = TravelingSalesman.generateCompleteGraph(30, 5d, 5d);
        int[] path = TravelingSalesman.approximate(graph);
        System.out.println(Arrays.toString(path));
    }

    public static void printMatrix(int[][] m) {
        System.out.println('[');
        for (int[] ints : m) {
            for (int i : ints) {
                System.out.print((i == Integer.MAX_VALUE ? "inf" : Integer.toString(i)) + '\t');
            }
            System.out.println();
        }
        System.out.println("]\n");
    }

    public static double timeMilli(Runnable function) {
        long start = System.nanoTime();
        function.run();
        long end = System.nanoTime();
        return (end - start) * Math.pow(10, 6);
    }
}
