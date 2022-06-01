public class Main {

    public static void main(String[] args) {
        int number = 20;
        double totalRatio = 0;
        for (int i = 0; i < number; i++) {
            double[][] graph = TravelingSalesman.generateCompleteGraph(10, 5d, 5d);
            int[] path = TravelingSalesman.approximate(graph);
            int[] optimal = TravelingSalesman.solve(graph);
            totalRatio += TravelingSalesman.length(graph, path) / TravelingSalesman.length(graph, optimal);
        }
        System.out.println("Average ratio approximation/optimal: " + totalRatio / number);
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
