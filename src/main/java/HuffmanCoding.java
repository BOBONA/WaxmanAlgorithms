import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Uses Huffman coding to compress files.
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

    /** A simple class to store node info */
    public static class HuffmanTreeNode {

        int frequency;
        boolean isInternal;
        int character;
        HuffmanTreeNode left;
        HuffmanTreeNode right;

        /**
         * @param frequency the number of times the code occurs
         * @param isInternal whether the node is an internal node or a leaf
         * @param character the byte character that the node represents, not important if the node is internal
         */
        public HuffmanTreeNode(int frequency, boolean isInternal, int character) {
            this.frequency = frequency;
            this.isInternal = isInternal;
            this.character = character;
        }
    }

    /** A simple class that allows writing to a file bit by bit.
     * It uses a buffer to hold 8 bits at a time. This class is intended to fully wrap an OutputStream. */
    public static class BitWriter {

        OutputStream output;
        int next;
        int filledBits;

        /**
         * Constructor
         * @param output an output stream for the BitWriter to write to
         */
        public BitWriter(OutputStream output) {
            this.output = output;
        }

        /**
         * Writes an entire byte to the stream
         * @param b byte to be written
         * @throws IOException if an error occurs during io operations
         */
        public void writeByte(int b) throws IOException {
            // writes bit by bit
            for (int i = 7; i >= 0; i--) {
                writeBit((b >> i) & 1);
            }
        }

        /**
         * Writes a bit to the stream
         * @param b a bit, 0 or 1
         * @throws IOException if an error occurs during io operations
         */
        public void writeBit(int b) throws IOException {
            // if the byte has been filled then write it to the output stream
            if (filledBits == 8) {
                output.write(next);
                filledBits = 0;
            }
            // shift the byte and set the lowest bit
            next = next * 2 + b;
            filledBits++;
        }

        /**
         * Writes the remaining bits and closes the output stream
         * @return the number of bits that were remaining
         * @throws IOException if an error occurs during io operations
         */
        public int close() throws IOException {
            // write the remaining bits
            next <<= 8 - filledBits;
            output.write(next);
            output.close();
            return filledBits;
        }
    }

    /** A simple class that allows reading from a file bit by bit.
     * It uses a buffer to read a byte from a file and then extract the bits individually.
     * Because of the way InputStreams handle end of files, this class also stores the next
     * following byte in order to know ahead of time if the end of the stream has been reached.
     * This class is intended to fully wrap an InputStream. */
    public static class BitReader {

        InputStream input;
        int next;
        int readBits;
        int following; // to keep track of whether the end of the input is reached

        /**
         * Constructor
         * @param input an input stream for the BitReader to read from
         * @throws IOException if an error occurs during io operations
         */
        public BitReader(InputStream input) throws IOException {
            this.input = input;
            following = input.read();
            readBits = 8;
        }

        /**
         * Reads an entire byte from the stream
         * @return read byte as an int
         * @throws IOException if an error occurs during io operations
         */
        public int nextByte() throws IOException {
            // reads bit by bit
            int b = 0;
            for (int i = 0; i < 8; i++) {
                b = b * 2 + nextBit();
            }
            return b;
        }

        /**
         * Reads a single bit from the stream
         * @return a bit, 0 or 1
         * @throws IOException if an error occurs during io operations
         */
        public int nextBit() throws IOException {
            // if all the bits of the buffer byte have been read then read a new byte
            if (readBits == 8) {
                next = following;
                following = input.read();
                readBits = 0;
            }
            // read a bit and shift the byte
            int bit = (next >> 7) & 1;
            next <<= 1;
            readBits++;
            return bit;
        }

        /**
         * @return whether the BitReader is reading from the last byte of the stream.
         */
        public boolean isLastByte() {
            return following == -1;
        }

        /**
         * closes the BitReader
         * @throws IOException if an error occurs while closing the InputStream
         */
        public void close() throws IOException {
            input.close();
        }
    }

    /**
     * The main method encodes/decodes files according to arguments
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Expected 3 arguments: mode <e or d>, " +
                    "input <filepath>, output <filepath>");
        } else {
            try {
                // encode or decode depending on program arguments
                File input = new File(args[1]);
                File output = new File(args[2]);
                if (args[0].toUpperCase().startsWith("E")) {
                    encode(input, output);
                    System.out.printf("Reduced file size by %f%%\n",
                            100 * (1 - (double) output.length() / input.length()));
                } else {
                    decode(input, output);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Compresses a file using Huffman coding
     * @param inputFile handle to the input file
     * @param outputFile handle to the output file
     * @throws IOException if an error occurs during io operations
     */
    public static void encode(File inputFile, File outputFile) throws IOException {
        // read file
        InputStream input = new FileInputStream(inputFile);
        // find frequencies
        HuffmanTreeNode[] frequencies = new HuffmanTreeNode[256];
        int next;
        while ((next = input.read()) != -1) {
            if (frequencies[next] == null) {
                // create a new frequency node
                HuffmanTreeNode node = new HuffmanTreeNode(0, false, next);
                frequencies[next] = node;
            }
            frequencies[next].frequency++;
        }
        // create tree
        HuffmanTreeNode tree = createHuffmanTreeFromFrequencies(frequencies);
        // calculate huffman codes
        String[] codes = new String[256];
        findCodes(tree, codes, "");
        // wrap output stream in a BitWriter
        OutputStream output = new FileOutputStream(outputFile);
        BitWriter bits = new BitWriter(output);
        // beginning of file can't be calculated until later
        bits.writeByte(0);
        bits.writeByte(0);
        // write each character and its corresponding Huffman code to the file,
        // see the top of this class for details of file structure
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
        // write the encoded data
        input.close();
        input = new FileInputStream(inputFile);
        while ((next = input.read()) != -1) {
            for (char character : codes[next].toCharArray()) {
                bits.writeBit(character - '0');
            }
        }
        int usedBits = bits.close();
        // set the first two bytes
        RandomAccessFile file = new RandomAccessFile(outputFile, "rw");
        file.seek(0); // first byte
        file.writeByte(usedBits);
        file.seek(1); // second byte
        file.writeByte(codeCount - 1);
        file.close();
    }

    /**
     * Decompresses a file that had been compressed with the encode method
     * @param inputFile handle to the compressed input file
     * @param outputFile handle to the output file
     * @throws IOException if an error occurs during io operations
     */
    public static void decode(File inputFile, File outputFile) throws IOException {
        // wrap an input stream in a BitReader
        InputStream input = new FileInputStream(inputFile);
        BitReader bits = new BitReader(input);
        // fetch first two bytes
        int usedBits = bits.nextByte();
        int codeCount = bits.nextByte() + 1;
        // recreate tree
        HuffmanTreeNode tree = createHuffmanTreeFromFile(bits, codeCount);
        // decode file
        OutputStream output = new FileOutputStream(outputFile);
        int lastBits = 0;
        HuffmanTreeNode cursor;
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

    /**
     * creates a huffman tree using a priority queue and a list of frequencies
     * @param frequencies list of frequencies
     * @return root of huffman tree
     */
    public static HuffmanTreeNode createHuffmanTreeFromFrequencies(HuffmanTreeNode[] frequencies) {
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
            // join smallest two nodes into one internal node
            HuffmanTreeNode min = queue.remove();
            HuffmanTreeNode min2 = queue.remove();
            HuffmanTreeNode internal = new HuffmanTreeNode(
                    min.frequency + min2.frequency, true, -1
            );
            internal.left = min;
            internal.right = min2;
            queue.add(internal);
        }
        return queue.remove();
    }

    /**
     * creates a Huffman tree from a file which is already reading at the proper location
     * see top of this class for file structure details
     * @param bits a BitReader to read from
     * @param codeCount the number of Huffman codes stored in the file
     * @return a Huffman tree
     * @throws IOException if something goes wrong while reading
     */
    public static HuffmanTreeNode createHuffmanTreeFromFile(BitReader bits, int codeCount) throws IOException {
        HuffmanTreeNode tree = new HuffmanTreeNode(0, true, -1);
        HuffmanTreeNode cursor;
        for (int i = 0; i < codeCount; i++) {
            int character = bits.nextByte();
            int codeSize = bits.nextByte();
            cursor = tree;
            // follow each code, creating internal nodes as needed
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
        return tree;
    }

    /**
     * Recursive function that finds each code in a tree
     * @param node the current node
     * @param codes an array of Huffman codes for each possible byte
     * @param path the Huffman code that would arrive at the current node
     */
    public static void findCodes(HuffmanTreeNode node, String[] codes, String path) {
        if (node == null) return;
        if (!node.isInternal) {
            codes[node.character] = path;
        } else {
            findCodes(node.left, codes, path + "0");
            findCodes(node.right, codes, path + "1");
        }
    }
}
