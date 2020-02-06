package utils.RandomForestParsing;

import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Based on EurekaTrees for python
 */
public class Tree {
    private Node root;
    private Integer maxDepth;
    private Integer maxBreadth;

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Integer getMaxBreadth() {
        return maxBreadth;
    }

    public void setMaxBreadth(Integer maxBreadth) {
        this.maxBreadth = maxBreadth;
    }

    public Tree(Pair<String, List<String>> tree, Map<Integer, String> columnNames){
        Boolean elseCheck = false;
        Node node = null;
        String dataStr;
        List<String> lines = tree.getValue();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("If")){
                dataStr = StringUtils.substringBetween(line,"(", ")").replaceFirst(" ", "_");
                if(columnNames != null){
                    dataStr = String.join(" ",columnNames.get(Integer.valueOf(dataStr.split("[_ ]")[1])), dataStr.split(" ", 2)[1]);
                }
                if(node == null){
                    node = new Node(dataStr);
                    this.root = node;
                } else if (elseCheck){
                    elseCheck = false;
                    while(node.getRight() != null){
                        node = node.getParent();
                    }
                    node.setRight(new Node(dataStr));
                    node.getRight().setParent(node);
                    node = node.getRight();
                } else {
                    node.setLeft((new Node(dataStr)));
                    node.getLeft().setParent(node);
                    node = node.getLeft();
                }
            } else if(line.startsWith("Else")){
                elseCheck = true;
            } else if(line.startsWith("Predict")){
                if(node == null){
                    node = new Node(Float.valueOf(line.split(" ")[1]));
                    this.root = node;
                } else if(elseCheck){
                    elseCheck = false;
                    while(node.getRight() != null){
                        node = node.getParent();
                    }
                    node.setRight(new Node(Float.valueOf(line.split(" ")[1])));
                    node.getRight().setParent(node);
                    node = node.getParent();
                } else {
                    node.setLeft(new Node(Float.valueOf(line.split(" ")[1])));
                    node.getLeft().setParent(node);
                }
            }
        }
        this.maxDepth = this.getMaxDepth(this.getRoot()) - 1;
        this.maxBreadth = this.getMaxBreadth(this.getMaxDepth());
    }

    public void printInOrder(Node node){
        if(node != null){
            printInOrder(node.getLeft());
            System.out.println(node);
            printInOrder(node.getRight());
        }
    }

    public List<Node> preorder(Node node, List<Node> nodeList){
        if(nodeList == null){
            nodeList = new ArrayList<>();
        }
        if(node != null){
            nodeList.add(node);
            preorder(node.getLeft(), nodeList);
            preorder(node.getRight(), nodeList);
        }
        return nodeList;
    }

    public JSONObject getJsStruct(Node node, JSONObject nodeJson){
        if(nodeJson == null){
            nodeJson = new JSONObject();
            nodeJson.put("name", node.getData());
            nodeJson.put("children", new JSONArray());
        }
        if(node != null){
            if(node.getLeft() != null){
                JSONObject nodeJsonLeft = new JSONObject();
                nodeJsonLeft.put("name", node.getLeft().getData());
                nodeJsonLeft.put("children", new JSONArray());
                nodeJsonLeft.put("type", "yes");
                nodeJson.put("isPrediction", false);
                ((JSONArray)nodeJson.get("children")).add(getJsStruct(node.getLeft(), nodeJsonLeft));
            }
            if(node.getRight() != null){
                JSONObject nodeJsonRight = new JSONObject();
                nodeJsonRight.put("name", node.getRight().getData());
                nodeJsonRight.put("children", new JSONArray());
                nodeJsonRight.put("type", "no");
                nodeJson.put("isPrediction", false);
                ((JSONArray)nodeJson.get("children")).add(getJsStruct(node.getRight(), nodeJsonRight));
            } else {
                nodeJson.put("isPrediction", true);
            }
            if(node.getParent() == null){
                nodeJson.put("type", "root");
            }
        }
        return nodeJson;
    }

    public void printPreorder(Node node){
        if(node != null){
            System.out.println(node.getData());
            printPreorder(node.getLeft());
            printPreorder(node.getRight());
        }
    }

    public void printPostorder(Node node){
        if(node != null){
            printPostorder(node.getLeft());
            printPostorder(node.getRight());
            System.out.println(node.getData());
        }
    }

    public Integer getMaxDepth(Node node){
        Integer res;
        if(node == null){
            res = 0;
        } else {
            Integer leftDepth = getMaxDepth(node.getLeft());
            Integer rightDepth = getMaxDepth(node.getRight());
            if (leftDepth > rightDepth) {
                res = leftDepth + 1;
            } else {
                res = rightDepth + 1;
            }
        }
        return res;
    }

    public Integer getMaxBreadth(Integer maxDepth){
        Integer res;
        if (maxDepth ==null){
            maxDepth = getMaxDepth(this.root);
        }
        res = (int)Math.pow(2.0, maxDepth);
        return res;
    }
}
