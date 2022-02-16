import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Encoded file description
 * 1 byte: the number of bits used in the last byte
 * 1 byte: the number of codes (minus 1 since the range is [1, 256])
 * for each code
 *      1 byte: the character
 *      1 byte: the size of the code
 *      x bytes: the code itself
 * the encoded data
 */
public class HuffmanCoding {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Expected 3 arguments: mode <e or d>, input <filepath>, output <filepath>");
        } else {
            try {
                File input = new File(args[1]);
                File output = new File(args[2]);
                if (args[0].toUpperCase().startsWith("E")) {
                    encode(input, output);
                    System.out.printf("Reduced file size by %d%%\n", 1 - output.length() / input.length());
                } else {
                    decode(input, output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void encode(File inputFile, File outputFile) throws IOException {
        // read file
        InputStream input = new FileInputStream(inputFile);
        // find frequencies
        HuffmanTreeNode[] frequencies = new HuffmanTreeNode[256];
        int next;
        while ((next = input.read()) != -1) {
            if (frequencies[next] == null) {
                HuffmanTreeNode node = new HuffmanTreeNode(0, false, next);
                frequencies[next] = node;
            }
            frequencies[next].frequency++;
        }
        // create tree
        PriorityQueue<HuffmanTreeNode> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.frequency));
        for (HuffmanTreeNode node : frequencies) {
            if (node != null) {
                queue.add(node);
            }
        }
        if (queue.size() == 1) { // edge case
            queue.add(new HuffmanTreeNode(0, true, -1));
        }
        while (queue.size() > 1) {
            HuffmanTreeNode min = queue.remove();
            HuffmanTreeNode min2 = queue.remove();
            HuffmanTreeNode internal = new HuffmanTreeNode(
                    min.frequency + min2.frequency, true, -1
            );
            internal.left = min;
            internal.right = min2;
            queue.add(internal);
        }
        HuffmanTreeNode tree = queue.remove();
        String[] codes = new String[256];
        findCodes(tree, codes, "");
        // encode data and write file
        OutputStream output = new FileOutputStream(outputFile);
        BitWriter bits = new BitWriter(output);
        // file info
        bits.writeByte(0);
        bits.writeByte(0);
        // codes
        int codeCount = 0;
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i];
            if (code == null) continue;
            bits.writeByte(i);
            bits.writeByte(code.length());
            for (char character : code.toCharArray()) {
                bits.writeBit(character - '0');
            }
            codeCount++;
        }
        // data
        input.close();
        input = new FileInputStream(inputFile);
        while ((next = input.read()) != -1) {
            for (char character : codes[next].toCharArray()) {
                bits.writeBit(character - '0');
            }
        }
        int usedBits = bits.close();
        // set the first two info bytes
        RandomAccessFile file = new RandomAccessFile(outputFile, "rw");
        file.seek(0);
        file.writeByte(usedBits);
        file.seek(1);
        file.writeByte(codeCount - 1);
        file.close();
    }

    public static void decode(File inputFile, File outputFile) throws IOException {
        // read data
        InputStream input = new FileInputStream(inputFile);
        BitReader bits = new BitReader(input);
        int usedBits = bits.nextByte();
        int codeCount = bits.nextByte() + 1;
        // recreate tree
        HuffmanTreeNode tree = new HuffmanTreeNode(0, true, -1);
        HuffmanTreeNode cursor;
        for (int i = 0; i < codeCount; i++) {
            int character = bits.nextByte();
            int codeSize = bits.nextByte();
            cursor = tree;
            for (int j = 0; j < codeSize; j++) {
                int bit = bits.nextBit();
                if (bit == 0) {
                    if (cursor.left == null) {
                        cursor.left = new HuffmanTreeNode(0, true, -1);
                    }
                    cursor = cursor.left;
                } else {
                    if (cursor.right == null) {
                        cursor.right = new HuffmanTreeNode(0, true, -1);
                    }
                    cursor = cursor.right;
                }
            }
            cursor.isInternal = false;
            cursor.character = character;
        }
        // decode file
        OutputStream output = new FileOutputStream(outputFile);
        int lastBits = 0;
        while (!(bits.isLastByte() && lastBits == usedBits)) {
            cursor = tree;
            while (cursor.isInternal) {
                if (bits.nextBit() == 0) {
                    cursor = cursor.left;
                } else {
                    cursor = cursor.right;
                }
                if (bits.isLastByte()) {
                    lastBits++;
                }
            }
            output.write(cursor.character);
        }
        bits.close();
        output.close();
    }

    public static void findCodes(HuffmanTreeNode node, String[] codes, String path) {
        if (node == null) return;
        if (!node.isInternal) {
            codes[node.character] = path;
        } else {
            findCodes(node.left, codes, path + "0");
            findCodes(node.right, codes, path + "1");
        }
    }

    public static class HuffmanTreeNode {

        int frequency;
        boolean isInternal;
        int character;
        HuffmanTreeNode left;
        HuffmanTreeNode right;

        public HuffmanTreeNode(int frequency, boolean isInternal, int character) {
            this.frequency = frequency;
            this.isInternal = isInternal;
            this.character = character;
        }
    }

    public static class BitWriter {

        OutputStream output;
        int next;
        int filledBits;

        public BitWriter(OutputStream output) {
            this.output = output;
        }

        public void writeByte(int b) throws IOException {
            for (int i = 7; i >= 0; i--) {
                writeBit((b >> i) & 1);
            }
        }

        public void writeBit(int b) throws IOException {
            if (filledBits == 8) {
                output.write(next);
                filledBits = 0;
            }
            next <<= 1;
            if (b == 1) {
                next |= 1;
            } else {
                next &= ~1;
            }
            filledBits++;
        }

        public int close() throws IOException {
            next <<= 8 - filledBits;
            output.write(next);
            output.close();
            return filledBits;
        }
    }

    public static class BitReader {

        InputStream input;
        int next;
        int readBits;
        int following; // to keep track of whether the end of the input is reached

        public BitReader(InputStream input) throws IOException {
            this.input = input;
            following = input.read();
            readBits = 8;
        }

        public int nextByte() throws IOException {
            int b = 0;
            for (int i = 0; i < 8; i++) {
                b <<= 1;
                if (nextBit() == 1) {
                    b |= 1;
                } else {
                    b &= ~1;
                }
            }
            return b;
        }

        public int nextBit() throws IOException {
            if (readBits == 8) {
                next = following;
                following = input.read();
                readBits = 0;
            }
            int bit = (next >> 7) & 1;
            next <<= 1;
            readBits++;
            return bit;
        }

        public boolean isLastByte() {
            return following == -1;
        }

        public void close() throws IOException {
            input.close();
        }
    }
}
