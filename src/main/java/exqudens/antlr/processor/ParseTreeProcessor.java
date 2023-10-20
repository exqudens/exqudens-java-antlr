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
import java.util.stream.Collectors;

public interface ParseTreeProcessor {

    static ParseTreeProcessor newInstance() {
        return new ParseTreeProcessor() {};
    }

    default Map<String, Object> toTreeMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        return null;
    }

    default List<Entry<List<String>, String>> toEntryList(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        Map<List<String>, String> map = toOrderedMap(parseTree, ruleNames, neededRuleNames);
        return new ArrayList<>(map.entrySet());
    }

    default Map<List<String>, String> toOrderedMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {
        Map<String, Map<List<Integer>, Long>> repeatIdMap = new HashMap<>();
        Map<String, AtomicLong> incrementMap = new HashMap<>();
        Tree rootTree = toTree(null, 0, parseTree);
        Map<List<String>, String> map = new LinkedHashMap<>();
        TreeProcessor treeProcessor = TreeProcessor.newInstance();

        for (Tree tree : treeProcessor.getDescendants(rootTree)) {
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
                    if (name.equals(Constants.CONTROL_NODE_NAME_PROCESS) || name.startsWith(Constants.CONTROL_NODE_NAME_REPEAT)) {
                        return true;
                    } else {
                        return neededRuleNames.contains(name);
                    }
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
                    List<Integer> integerPath = treeProcessor.getIntegerPath(t);
                    if (!repeatIdMap.get(ruleName).containsKey(integerPath)) {
                        repeatIdMap.get(ruleName).putIfAbsent(integerPath, incrementMap.get(ruleName).getAndIncrement());
                    }
                    id = repeatIdMap.get(ruleName).get(integerPath);
                } else {
                    incrementMap.putIfAbsent(ruleName, new AtomicLong(0));
                    id = incrementMap.get(ruleName).getAndIncrement();
                }

                if (Constants.CONTROL_NODE_NAME_PROCESS.equals(ruleName)) {
                    key.add(ruleName);
                } else {
                    key.add(ruleName);
                    key.add(String.valueOf(id));
                }
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

    default Tree toTree(Tree parent, Integer index, ParseTree parseTree) {
        Tree tree = new Tree(index, parseTree, parent, new ArrayList<>());
        int n = parseTree.getChildCount();
        for (int i = 0 ; i < n ; i++) {
            tree.getChildren().add(toTree(tree, i, parseTree.getChild(i)));
        }
        return tree;
    }

}
