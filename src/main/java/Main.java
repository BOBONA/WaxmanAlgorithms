public class Main {

    public static void main(String[] args) {
        System.out.println(timeMilli(() -> DynamicProgramming.binomialCoefficient(50, 8)));
        System.out.println(timeMilli(() -> DynamicProgramming.binomialCoefficientInefficient(50, 8)));
    }

    public static double timeMilli(Runnable function) {
        long start = System.nanoTime();
        function.run();
        long end = System.nanoTime();
        return (end - start) * Math.pow(10, 6);
    }
}
