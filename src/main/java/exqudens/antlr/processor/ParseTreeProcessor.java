package exqudens.antlr.processor;

import exqudens.antlr.util.Constants;
import exqudens.antlr.model.Tree;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ParseTreeProcessor {

    static ParseTreeProcessor newInstance() {
        return new ParseTreeProcessor() {};
    }

    default Map<String, Object> toTreeMap(
        ParseTree parseTree,
        String[] ruleNames,
        boolean terminalOnly,
        boolean filterTree,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Tree rootTree = toTree(
            parseTree,
            ruleNames,
            terminalOnly,
            filterTree,
            neededRuleNames,
            keepControlNames
        );
        TreeProcessor treeProcessor = TreeProcessor.newInstance();

        for (Tree tree : treeProcessor.depthFirstSearch(rootTree)) {
            if (!(tree.getParseTree() instanceof TerminalNode)) {
                continue;
            }

            List<Tree> treePath = treeProcessor.getTreePath(tree);

            List<String> namePath = treePath.stream().map(t -> toString(t.getParseTree(), ruleNames)).collect(Collectors.toList());
            namePath.remove(namePath.size() - 1);

            List<Long> idPath = treeProcessor.getIdPath(tree);
            System.out.println(namePath + " (" + idPath + "): '" + toString(tree.getParseTree(), ruleNames) + "'");
        }

        System.out.println("---");

        return null;
    }

    default List<Entry<List<String>, String>> toEntryList(
        ParseTree parseTree,
        String[] ruleNames,
        boolean terminalOnly,
        boolean filterTree,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Map<List<String>, String> map = toOrderedListStringMap(
            parseTree,
            ruleNames,
            terminalOnly,
            filterTree,
            neededRuleNames,
            keepControlNames
        );
        return new ArrayList<>(map.entrySet());
    }

    default Map<List<String>, String> toOrderedListStringMap(
        ParseTree parseTree,
        String[] ruleNames,
        boolean terminalOnly,
        boolean filterTree,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Tree rootTree = toTree(
            parseTree,
            ruleNames,
            terminalOnly,
            filterTree,
            neededRuleNames,
            keepControlNames
        );
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<List<String>, String> map = new LinkedHashMap<>();

        for (Tree tree : treeProcessor.depthFirstSearch(rootTree)) {
            if (!(tree.getParseTree() instanceof TerminalNode)) {
                continue;
            }

            List<Tree> treePath = treeProcessor.getTreePath(tree);
            treePath.remove(treePath.size() - 1);

            List<String> key = new ArrayList<>();

            for (int i = 0; i < treePath.size(); i++) {
                String ruleName = toString(treePath.get(i).getParseTree(), ruleNames);
                Long id;

                if (i == treePath.size() - 1) {
                    id = tree.getId();
                } else {
                    id = treePath.get(i).getId();
                }

                key.add(ruleName);

                if (!ruleName.equals(Constants.CONTROL_NODE_NAME_PROCESS)) {
                    key.add(String.valueOf(id));
                }
            }

            String value = toString(tree.getParseTree(), ruleNames);

            map.putIfAbsent(key, value);
        }

        return map;
    }

    default Tree toTree(
        ParseTree parseTree,
        String[] ruleNames,
        boolean terminalOnly,
        boolean filterTree,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Map<List<Long>, ParseTree> orderedListLongMap = toOrderedListLongMap(
            parseTree,
            ruleNames,
            terminalOnly,
            filterTree,
            neededRuleNames,
            keepControlNames
        );
        Tree rootTree = null;
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<List<Long>, List<Long>> idIndexPathMap = new HashMap<>();

        for (Entry<List<Long>, ParseTree> internalEntry : orderedListLongMap.entrySet()) {
            List<Long> internalKey = internalEntry.getKey();
            ParseTree internalValue = internalEntry.getValue();

            if (idIndexPathMap.containsKey(internalKey)) {
                throw new RuntimeException("'idIndexPathMap' contains key: " + internalKey);
            }

            List<Long> key = new ArrayList<>();

            if (rootTree == null) {
                Long index = 0L;
                rootTree = new Tree(null, index, internalValue, new ArrayList<>());
                key.add(index);
                idIndexPathMap.putIfAbsent(internalKey, key);
                continue;
            }

            List<Long> parentIdPath = new ArrayList<>(internalKey);
            parentIdPath.remove(internalKey.size() - 1);
            List<Long> parentIndexPath = idIndexPathMap.get(parentIdPath);

            Tree parentTree = treeProcessor.getTree(parentIndexPath, rootTree);

            Long index = Integer.valueOf(parentTree.getChildren().size()).longValue();
            parentTree.getChildren().add(new Tree(parentTree, index, internalValue, new ArrayList<>()));
            key.addAll(parentIndexPath);
            key.add(index);

            idIndexPathMap.putIfAbsent(internalKey, key);
        }

        return rootTree;
    }

    default String toString(ParseTree parseTree, String[] ruleNames) {
        if (parseTree instanceof ErrorNode) {
            return parseTree.getText();
        } else if (parseTree instanceof TerminalNode) {
            return parseTree.getText();
        } else if (parseTree instanceof RuleNode) {
            int ruleIndex = ((RuleContext)parseTree).getRuleIndex();
            return ruleNames[ruleIndex];
        } else {
            throw new IllegalArgumentException("Unsupported type: '" + parseTree.getClass().getName() + "'");
        }
    }

    default Tree toTree(Tree parent, AtomicLong increment, ParseTree parseTree) {
        Tree tree = new Tree(parent, increment.getAndIncrement(), parseTree, new ArrayList<>());
        int n = parseTree.getChildCount();
        for (int i = 0 ; i < n ; i++) {
            tree.getChildren().add(toTree(tree, increment, parseTree.getChild(i)));
        }
        return tree;
    }

    default Map<List<Long>, ParseTree> toOrderedListLongMap(
        ParseTree parseTree,
        String[] ruleNames,
        boolean terminalOnly,
        boolean filterTree,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        AtomicLong increment = new AtomicLong(0);
        Tree rootTree = toTree(null, increment, parseTree);
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<List<Long>, ParseTree> map = new LinkedHashMap<>();

        Predicate<Tree> neededControlNodeNameFilter;

        if (filterTree) {
            neededControlNodeNameFilter = t -> {
                if (t.getParseTree() instanceof RuleNode) {
                    String ruleName = toString(t.getParseTree(), ruleNames);
                    if (keepControlNames.length == 0) {
                        if (Stream.of(Constants.CONTROL_NODE_NAMES).anyMatch(ruleName::startsWith)) {
                            return neededRuleNames.contains(ruleName);
                        }
                    } else {
                        return Stream.of(keepControlNames).anyMatch(ruleName::startsWith);
                    }
                }
                return false;
            };
        } else {
            neededControlNodeNameFilter = t -> true;
        }

        for (Tree tree : treeProcessor.depthFirstSearch(rootTree)) {
            if (!(tree.getParseTree() instanceof TerminalNode)) {
                continue;
            }

            List<Tree> treePath = treeProcessor.getTreePath(tree);

            if (filterTree && !terminalOnly) {
                Set<String> neededControlRuleNames = treePath
                    .stream()
                    .filter(neededControlNodeNameFilter)
                    .map(t -> toString(t.getParseTree(), ruleNames))
                    .collect(Collectors.toSet());

                boolean neededTerminalNode;

                if (neededControlRuleNames.isEmpty()) {
                    neededTerminalNode = treePath
                        .stream()
                        .map(Tree::getParseTree)
                        .filter(RuleNode.class::isInstance)
                        .map(pt -> toString(pt, ruleNames))
                        .anyMatch(neededRuleNames::contains);
                } else {
                    neededTerminalNode = treePath
                        .stream()
                        .map(Tree::getParseTree)
                        .filter(RuleNode.class::isInstance)
                        .map(pt -> toString(pt, ruleNames))
                        .anyMatch(neededControlRuleNames::contains);
                }

                if (!neededTerminalNode) {
                    continue;
                }

                Predicate<Tree> predicate = t -> {
                    String name = toString(t.getParseTree(), ruleNames);
                    return name.equals(Constants.CONTROL_NODE_NAME_PROCESS)
                        || neededControlRuleNames.contains(name)
                        || neededRuleNames.contains(name)
                        || t.getParseTree() instanceof TerminalNode;
                };
                treePath = treePath
                    .stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            } else if (!filterTree && terminalOnly) {
                treePath = treePath
                    .stream()
                    .filter(t -> {
                        if (t.getParseTree() instanceof TerminalNode) {
                            return true;
                        } else {
                            String name = toString(t.getParseTree(), ruleNames);
                            return name.equals(Constants.CONTROL_NODE_NAME_PROCESS);
                        }
                    })
                    .collect(Collectors.toList());
            }

            if (treePath.isEmpty()) {
                continue;
            }

            List<Tree> path = new ArrayList<>();

            for (Tree t : treePath) {
                path.add(t);
                List<Long> key = path.stream().map(Tree::getId).collect(Collectors.toList());
                map.putIfAbsent(key, t.getParseTree());
            }
        }

        return map;
    }

}
