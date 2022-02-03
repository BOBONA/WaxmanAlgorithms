import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

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

    // problem with stack overflows
    public static int ackermann(int m, int n) {
        Map<Pair<Integer, Integer>, Integer> lookup = new HashMap<>();
        ackermann(m, n, lookup);
        return lookup.get(p(m, n));
    }

    public static Pair<Integer, Integer> p(int m, int n) {
        return new Pair<>(m, n);
    }

    public static void ackermann(int m, int n, Map<Pair<Integer, Integer>, Integer> lookup) {
        if (lookup.get(p(m, n)) != null) {
            return;
        }
        if (m == 0) {
            lookup.put(p(m, n), n + 1);
        } else if (n == 0) {
            ackermann(m - 1, 1, lookup);
            lookup.put(p(m, n), lookup.get(p(m - 1, 1)));
        } else {
            ackermann(m, n - 1, lookup);
            ackermann(m - 1, lookup.get(p(m, n - 1)), lookup);
            lookup.put(p(m, n), lookup.get(p(m - 1, lookup.get(p(m, n - 1)))));
        }
    }
}
