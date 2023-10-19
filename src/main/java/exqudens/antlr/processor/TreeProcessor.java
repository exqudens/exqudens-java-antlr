package exqudens.antlr.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import exqudens.antlr.model.Tree;

public interface TreeProcessor {

    static TreeProcessor newInstance() {
        return new TreeProcessor() {};
    }

    default List<Tree> getTreePath(Tree tree) {
        List<Tree> path = new ArrayList<>();

        Tree parent = tree;
        do {
            if (parent != null) {
                path.add(0, parent);
                parent = parent.getParent();
            }
        } while (parent != null);

        return path;
    }

    default List<Integer> getIntegerPath(Tree tree) {
        return getTreePath(tree).stream().map(Tree::getIndex).collect(Collectors.toList());
    }

    default List<Tree> getDescendants(Tree tree) {
        List<Tree> trees = new ArrayList<>();
        trees.add(tree);
        List<Tree> children = tree.getChildren();
        int n = children.size();
        for (int i = 0 ; i < n ; i++) {
            trees.addAll(getDescendants(children.get(i)));
        }
        return trees;
    }

    default Tree getTree(List<Integer> path, Tree root) {
        Tree tree = root;
        Tree last = root;
        for (int i = 1; i < path.size(); i++) {
            Integer pathIndex = path.get(i);
            tree = last.getChildren().get(pathIndex);
            last = tree;
        }
        return tree;
    }

}
