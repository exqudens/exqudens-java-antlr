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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface ParseTreeProcessor {

    static ParseTreeProcessor newInstance() {
        return new ParseTreeProcessor() {};
    }

    default Map<String, Object> toTreeMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        Map<List<Long>, ParseTree> orderedListLongMap = toOrderedListLongMap(parseTree, ruleNames, neededRuleNames, true);
        Tree rootIntermediateTree = toResultTree(orderedListLongMap, ruleNames);

        // TODO

        return null;
    }

    default Tree toResultTree(Map<List<Long>, ParseTree> orderedListLongMap, String[] ruleNames) {
        Tree rootTree = null;

        for (Entry<List<Long>, ParseTree> internalEntry : orderedListLongMap.entrySet()) {
            List<Long> internalKey = internalEntry.getKey();
            String internalValue = toString(internalEntry.getValue(), ruleNames);

            System.out.println(internalKey + ": '" + internalValue + "'");
        }

        return rootTree;
    }

    default Map<List<Long>, ParseTree> toOrderedListLongMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames, boolean includeParents) {
        AtomicLong increment = new AtomicLong(0);
        Tree rootTree = toTree(null, increment, parseTree);
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<List<Long>, ParseTree> map = new LinkedHashMap<>();

        for (Tree tree : treeProcessor.depthFirstSearch(rootTree)) {
            if (!(tree.getParseTree() instanceof TerminalNode)) {
                continue;
            }

            List<Tree> treePath = treeProcessor.getTreePath(tree);
            Optional<String> optional = treePath
                .stream()
                .map(Tree::getParseTree)
                .filter(RuleNode.class::isInstance)
                .map(pt -> toString(pt, ruleNames))
                .filter(neededRuleNames::contains)
                .findFirst();

            if (!optional.isPresent()) {
                continue;
            }

            Predicate<Tree> predicate = t -> {
                String name = toString(t.getParseTree(), ruleNames);
                return name.equals(Constants.CONTROL_NODE_NAME_PROCESS)
                    || name.startsWith(Constants.CONTROL_NODE_NAME_REPEAT)
                    || neededRuleNames.contains(name)
                    || t.getParseTree() instanceof TerminalNode;
            };
            List<Tree> filteredTreePath = treePath
                .stream()
                .filter(predicate)
                .collect(Collectors.toList());

            if (includeParents) {
                List<Tree> path = new ArrayList<>();

                for (Tree t : filteredTreePath) {
                    path.add(t);
                    List<Long> key = path.stream().map(Tree::getId).collect(Collectors.toList());
                    map.putIfAbsent(key, t.getParseTree());
                }
            } else {
                List<Long> key = filteredTreePath.stream().map(Tree::getId).collect(Collectors.toList());
                ParseTree value = tree.getParseTree();
                map.putIfAbsent(key, value);
            }
        }

        return map;
    }

    default Tree toTree(Tree parent, AtomicLong increment, ParseTree parseTree) {
        Tree tree = new Tree(parent, increment.getAndIncrement(), parseTree, new ArrayList<>());
        int n = parseTree.getChildCount();
        for (int i = 0 ; i < n ; i++) {
            tree.getChildren().add(toTree(tree, increment, parseTree.getChild(i)));
        }
        return tree;
    }

    default List<Entry<List<String>, String>> toEntryList(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        Map<List<String>, String> map = toOrderedListStringMap(parseTree, ruleNames, neededRuleNames);
        return new ArrayList<>(map.entrySet());
    }

    default Map<List<String>, String> toOrderedListStringMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        Tree rootTree = toTree(null, 0, parseTree);
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<String, Map<List<Long>, Long>> repeatIdMap = new HashMap<>();
        Map<String, AtomicLong> incrementMap = new HashMap<>();
        Map<List<String>, String> map = new LinkedHashMap<>();

        for (Tree tree : treeProcessor.depthFirstSearch(rootTree)) {
            if (!(tree.getParseTree() instanceof TerminalNode)) {
                continue;
            }

            List<Tree> treePath = treeProcessor.getTreePath(tree);
            Optional<String> optional = treePath
                .stream()
                .map(Tree::getParseTree)
                .filter(RuleNode.class::isInstance)
                .map(pt -> toString(pt, ruleNames))
                .filter(neededRuleNames::contains)
                .findFirst();

            if (!optional.isPresent()) {
                continue;
            }

            List<Tree> filterTreePath = treePath
                .stream()
                .filter(t -> {
                    String name = toString(t.getParseTree(), ruleNames);
                    return name.equals(Constants.CONTROL_NODE_NAME_PROCESS)
                        || name.startsWith(Constants.CONTROL_NODE_NAME_REPEAT)
                        || neededRuleNames.contains(name);
                }).collect(Collectors.toList());

            if (filterTreePath.isEmpty()) {
                continue;
            }

            List<String> key = new ArrayList<>();

            for (Tree t : filterTreePath) {
                String ruleName = toString(t.getParseTree(), ruleNames);
                Long id;
                if (Constants.CONTROL_NODE_NAME_PROCESS.equals(ruleName)) {
                    id = 0L;
                } else if (ruleName.startsWith(Constants.CONTROL_NODE_NAME_REPEAT)) {
                    repeatIdMap.putIfAbsent(ruleName, new HashMap<>());
                    incrementMap.putIfAbsent(ruleName, new AtomicLong(0));
                    List<Long> integerPath = treeProcessor.getIdPath(t);
                    if (!repeatIdMap.get(ruleName).containsKey(integerPath)) {
                        repeatIdMap.get(ruleName).putIfAbsent(integerPath, incrementMap.get(ruleName).getAndIncrement());
                    }
                    id = repeatIdMap.get(ruleName).get(integerPath);
                } else {
                    incrementMap.putIfAbsent(ruleName, new AtomicLong(0));
                    id = incrementMap.get(ruleName).getAndIncrement();
                }

                key.add(ruleName);
                key.add(String.valueOf(id));
            }

            String value = toString(tree.getParseTree(), ruleNames);

            map.putIfAbsent(key, value);
        }

        return map;
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

    default Tree toTree(Tree parent, long index, ParseTree parseTree) {
        Tree tree = new Tree(parent, index, parseTree, new ArrayList<>());
        int n = parseTree.getChildCount();
        for (int i = 0 ; i < n ; i++) {
            tree.getChildren().add(toTree(tree, i, parseTree.getChild(i)));
        }
        return tree;
    }

}
