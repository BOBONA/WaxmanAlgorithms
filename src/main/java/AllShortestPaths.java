public class AllShortestPaths {

    // uses Integer.MAX_VALUE to mean infinity
    public static int[][] ExtendShortestPaths(int[][] L, int[][] W) {
        int n = L.length;
        int[][] LL = new int[n][n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                LL[i][j] = Integer.MAX_VALUE;
                for (int k = 0; k < n - 1; k++) {
                    int sum;
                    if (L[i][k] == Integer.MAX_VALUE || W[k][j] == Integer.MAX_VALUE) {
                        sum = Integer.MAX_VALUE;
                    } else {
                        sum = L[i][k] + W[k][j];
                    }
                    LL[i][j] = Math.min(LL[i][j], sum);
                }
            }
        }
        return LL;
    }

    public static int[][][] FloydWarshall(int[][] W) {
        int n = W.length;
        int[][][] D = new int[n + 1][n][n];
        D[0] = W;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int sum;
                    if (D[k][i][k] == Integer.MAX_VALUE || D[k][k][j] == Integer.MAX_VALUE) {
                        sum = Integer.MAX_VALUE;
                    } else {
                        sum = D[k][i][k] + D[k][k][j];
                    }
                    D[k + 1][i][j] = Math.min(D[k][i][j], sum);
                }
            }
        }
        return D;
    }
}
