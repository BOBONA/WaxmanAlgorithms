public class DynamicProgramming {

    public static int binomialCoefficient(int j, int k) {
        int[][] lookup = new int[j + 1][k + 1];
        binomialCoefficient(j, k, lookup);
        return lookup[j][k];
    }

    public static void binomialCoefficient(int j, int k, int[][] lookup) {
        if (lookup[j][k] != 0) {
            return;
        }
        if (k == 0 || j == k) {
            lookup[j][k] = 1;
        } else {
            binomialCoefficient(j - 1, k - 1, lookup);
            binomialCoefficient(j - 1, k, lookup);
            lookup[j][k] = lookup[j - 1][k - 1] + lookup[j - 1][k];
        }
    }

    // binomial coefficient without dynamic programming
    public static int binomialCoefficientInefficient(int j, int k) {
        if (k == 0 || j == k) {
            return 1;
        } else {
            return binomialCoefficientInefficient(j - 1, k - 1)
                    + binomialCoefficientInefficient(j - 1, k);
        }
    }
}
