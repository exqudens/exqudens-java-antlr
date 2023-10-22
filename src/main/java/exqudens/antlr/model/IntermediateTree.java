package exqudens.antlr.model;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class IntermediateTree {

    private final IntermediateTree parent;
    private final Integer index;
    private final ParseTree parseTree;
    private final List<IntermediateTree> children;

    public IntermediateTree(IntermediateTree parent, Integer index, ParseTree parseTree, List<IntermediateTree> children) {
        this.parent = parent;
        this.index = index;
        this.parseTree = parseTree;
        this.children = children;
    }

    public IntermediateTree getParent() {
        return parent;
    }

    public Integer getIndex() {
        return index;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public List<IntermediateTree> getChildren() {
        return children;
    }
}
