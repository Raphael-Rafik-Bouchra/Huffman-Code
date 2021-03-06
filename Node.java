public class Node {

    private Node left, right;
    private String c;
    private Integer freq;
    private boolean isLeaf;

    public Node(String c, Integer freq) {
        this.c = c;
        this.freq = freq;
        isLeaf = true;
    }

    public void insertLeft(Node left) {
        this.left = left;
        isLeaf = false;
    }

    public void insertRight(Node right) {
        this.right = right;
        isLeaf = false;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public String getC() {
        return c;
    }

    public int getFreq() {
        return freq;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public String toString() {
        return c + " " + freq + " " + isLeaf;
    }
}