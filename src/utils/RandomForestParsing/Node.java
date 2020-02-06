package utils.RandomForestParsing;

/**
 * Based on EurekaTrees for python
 */
public class Node {
    private Node parent;
    private Node right;
    private Node left;
    private Object data;

    public Node(Object data) {
        this.data = data;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
