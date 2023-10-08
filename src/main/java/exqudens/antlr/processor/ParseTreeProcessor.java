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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public interface ParseTreeProcessor extends TreeProcessor {

    default Map<List<String>, String> toMap(ParseTree parseTree, String[] ruleNames, Set<String> neededRuleNames) {

        Map<String, Map<List<Integer>, Long>> repeatIdMap = new HashMap<>();
        Map<String, AtomicLong> incrementMap = new HashMap<>();
        Tree rootTree = toTree(null, 0, parseTree);
        Map<List<String>, String> map = new LinkedHashMap<>();

        for (Tree descendant : getDescendants(rootTree)) {
            if (descendant.getParseTree() instanceof TerminalNode) {

                List<Tree> treePath = getTreePath(descendant);

                Optional<String> optional = treePath
                .stream()
                .map(Tree::getParseTree)
                .filter(RuleNode.class::isInstance)
                .map(pt -> toString(pt, ruleNames))
                .filter(neededRuleNames::contains)
                .findFirst();

                if (optional.isPresent()) {

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

                    List<String> key = new ArrayList<>();
                    for (Tree t : filterTreePath) {
                        String ruleName = toString(t.getParseTree(), ruleNames);
                        Long id;
                        if (Constants.CONTROL_NODE_NAME_PROCESS.equals(ruleName)) {
                            id = 1L;
                        } else if (ruleName.startsWith(Constants.CONTROL_NODE_NAME_REPEAT)) {
                            repeatIdMap.putIfAbsent(ruleName, new HashMap<>());
                            incrementMap.putIfAbsent(ruleName, new AtomicLong(1));
                            List<Integer> integerPath = getIntegerPath(t);
                            if (!repeatIdMap.get(ruleName).containsKey(integerPath)) {
                                repeatIdMap.get(ruleName).putIfAbsent(integerPath, incrementMap.get(ruleName).getAndIncrement());
                            }
                            id = repeatIdMap.get(ruleName).get(integerPath);
                        } else {
                            incrementMap.putIfAbsent(ruleName, new AtomicLong(1));
                            id = incrementMap.get(ruleName).getAndIncrement();
                        }
                        key.add(ruleName);
                        key.add(String.valueOf(id));
                    }
                    map.putIfAbsent(key, toString(descendant.getParseTree(), ruleNames));

                }

            }
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

}
