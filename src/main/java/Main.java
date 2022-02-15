import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            HuffmanCoding.encode("test.pdf", "encoded.txt");
            HuffmanCoding.decode("encoded.txt", "decoded_test.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double timeMilli(Runnable function) {
        long start = System.nanoTime();
        function.run();
        long end = System.nanoTime();
        return (end - start) * Math.pow(10, 6);
    }
}
