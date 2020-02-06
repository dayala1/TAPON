package utils.RandomForestParsing;

import com.google.common.base.Charsets;
import com.hubspot.jinjava.Jinjava;
import com.sun.source.tree.BinaryTree;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.spark_project.guava.io.Resources;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Based on EurekaTrees for python
 */
public class RandomForestParser {
    /**
     *
     * @param treeFile The file with the debug string dump of the entire random forest classifier.
     * @return A list of pairs. In each pair, the key is the name of the tree (e.g. Tree 23), and the value a list of string with the lines of the tree.
     * @throws FileNotFoundException If the debug string file does cannot be found.
     */
    public static List<Pair<String, List<String>>> separateTrees(File treeFile) throws IOException {
        List<Pair<String, List<String>>> res = new ArrayList<>();
        List<String> treeContent = null;
        String treeName = null;
        BufferedReader reader = new BufferedReader(new FileReader(treeFile));
        for (String line: reader.lines().collect(Collectors.toList())) {
            line = line.trim();
            //Beginning of a tree
            if (line.startsWith("Tree")){
                if(treeName != null){
                    res.add(new Pair<>(treeName, treeContent));
                }
                treeContent = new ArrayList<>();
                treeName = line;
            } else if(treeName != null && !line.isEmpty()) {
                treeContent.add(line);
            }
        }
        reader.close();
        //Line of the tree, checking it is not the header of the file nor a blank line
        if (!treeContent.isEmpty()){
            res.add(new Pair<>(treeName, treeContent));
        }

        return res;
    }

    public static void makeTreeViz(List<Tree> trees, String folderPath) throws IOException {
        Jinjava jinjava = new Jinjava();
        File treesFolder = new File(String.format("%s/trees/", folderPath));
        treesFolder.mkdirs();
        Map<String, Object> context = new HashMap<>();
        String homeTemplate = Resources.toString(Resources.getResource("templates/home_template.jinjava"), Charsets.UTF_8);
        String treeTemplate = Resources.toString(Resources.getResource("templates/tree_template.jinjava"), Charsets.UTF_8);
        String cssString = Resources.toString(Resources.getResource("templates/trees.css"), Charsets.UTF_8);
        List<String> treeLinks = IntStream.range(0, trees.size()).mapToObj(i -> String.format("./trees/tree%s.html", i)).collect(Collectors.toList());
        context.put("trees", treeLinks);
        String homeString = jinjava.render(homeTemplate, context);
        File homeFile = new File(folderPath, "home.html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(homeFile));
        writer.write(homeString);
        writer.close();
        File cssFile = new File(folderPath, "trees.css");
        writer = new BufferedWriter(new FileWriter(cssFile));
        writer.write(cssString);
        writer.close();

        String treeString;
        File treeFile;
        for (int i = 0; i < trees.size(); i++) {
            Tree tree = trees.get(i);
            context = new HashMap<>();
            context.put("max_depth", tree.getMaxDepth()*120);
            context.put("max_breadth", tree.getMaxDepth()*750);
            context.put("tree", tree.getJsStruct(tree.getRoot(),null));
            treeString = jinjava.render(treeTemplate, context);
            treeFile = new File(treesFolder, String.format("tree%s.html", i));
            writer = new BufferedWriter(new FileWriter(treeFile));
            writer.write(treeString);
            writer.close();
        }
    }
}
