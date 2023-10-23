package exqudens.antlr.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import exqudens.antlr.model.Tree;

public interface TreeProcessor {

    static TreeProcessor newInstance() {
        return new TreeProcessor() {};
    }

    default List<Tree> depthFirstSearch(Tree root) {
        Objects.requireNonNull(root);
        List<Tree> result = new ArrayList<>();
        Stack<Tree> container = new Stack<>();
        container.push(root);
        while (!container.isEmpty()) {
            Tree current = container.pop();
            result.add(current);
            container.addAll(current.getChildren());
        }
        Collections.reverse(result);
        return result;
    }

    default List<Tree> breadthFirstSearch(Tree root) {
        List<Tree> result = new ArrayList<>();
        Queue<Tree> container = new LinkedList<>();
        container.add(root);
        while (!container.isEmpty()) {
            Tree current = container.remove();
            result.add(current);
            container.addAll(current.getChildren());
        }
        return result;
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

    default List<Long> getIdPath(Tree tree) {
        return getTreePath(tree).stream().map(Tree::getId).collect(Collectors.toList());
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

    default Tree getTree(List<Long> path, Tree root) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(root);
        if (path.isEmpty()) {
            return null;
        }
        Tree tree = root;
        Tree last = root;
        for (int i = 1; i < path.size(); i++) {
            Long id = path.get(i);
            tree = last.getChildren().get(id.intValue());
            last = tree;
        }
        return tree;
    }

}
