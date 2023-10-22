package exqudens.antlr.model;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public class Tree {

    private final Tree parent;
    private final Long id;
    private final ParseTree parseTree;
    private final List<Tree> children;

    public Tree(Tree parent, Long id, ParseTree parseTree, List<Tree> children) {
        this.parent = parent;
        this.id = id;
        this.parseTree = parseTree;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public Tree getParent() {
        return parent;
    }

    public List<Tree> getChildren() {
        return children;
    }

}
