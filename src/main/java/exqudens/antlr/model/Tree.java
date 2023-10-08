package exqudens.antlr.model;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

public class Tree {

    private final Integer index;
    private final ParseTree parseTree;

    private final Tree parent;
    private final List<Tree> children;

    public Tree(Integer index, ParseTree parseTree, Tree parent, List<Tree> children) {
        super();
        this.index = index;
        this.parseTree = parseTree;
        this.parent = parent;
        this.children = children;
    }

    public Integer getIndex() {
        return index;
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
