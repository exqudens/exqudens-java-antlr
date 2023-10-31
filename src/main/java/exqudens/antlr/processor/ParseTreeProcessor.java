package exqudens.antlr.processor;

import exqudens.antlr.model.Entity;
import exqudens.antlr.util.Constants;
import exqudens.antlr.model.Tree;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
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
        boolean filterTree,
        boolean terminalOnly,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Tree rootTree = toTree(
            parseTree,
            ruleNames,
            filterTree,
            terminalOnly,
            neededRuleNames,
            keepControlNames
        );
        Entity rootEntity = toEntity(null, rootTree, ruleNames);
        Map<String, Object> map = toMap(rootEntity);
        Set<List<Integer>> terminalPaths = new LinkedHashSet<>();

        for (Entity entity : depthFirstSearch(rootEntity)) {
            if (entity.children != null) {
                continue;
            }

            List<Entity> entityPath = getEntityPath(entity);
            List<Integer> indexPath = entityPath.stream().map(o -> o.index).collect(Collectors.toList());
            indexPath.remove(0);

            terminalPaths.add(indexPath);
        }

        List<List<Integer>> terminalPathList = new ArrayList<>(terminalPaths);
        List<Map<String, List<Integer>>> mapTerminalPaths = new ArrayList<>();

        for (int i = 0; i < terminalPathList.size(); i++) {
            Map<String, List<Integer>> mapTerminalPath = new HashMap<>();
            mapTerminalPath.put("path_" + i, terminalPathList.get(i));
            mapTerminalPaths.add(mapTerminalPath);
        }

        map.put("terminal_paths", mapTerminalPaths);

        Set<List<Integer>> intermediatePaths = new LinkedHashSet<>();

        for (List<Integer> terminalPath : terminalPaths) {
            List<Integer> intermediatePath = new ArrayList<>();
            int max = terminalPath.size() - 1;
            for (int i = 0; i < max; i++) {
                intermediatePath.add(terminalPath.get(i));
                intermediatePaths.add(new ArrayList<>(intermediatePath));
            }
        }

        List<List<Integer>> intermediatePathList = new ArrayList<>(intermediatePaths);
        List<Map<String, List<Integer>>> mapIntermediatePaths = new ArrayList<>();

        for (int i = 0; i < intermediatePathList.size(); i++) {
            Map<String, List<Integer>> mapIntermediatePath = new HashMap<>();
            mapIntermediatePath.put("path_" + i, intermediatePathList.get(i));
            mapIntermediatePaths.add(mapIntermediatePath);
        }

        map.put("intermediate_paths", mapIntermediatePaths);

        return map;
    }

    default Map<String, Object> toMap(Entity object) {
        Map<String, Object> map = new HashMap<>();

        map.put("index", object.index);

        if (object.name != null) {
            map.put("name", object.name);
        }

        if (object.value != null) {
            map.put("value", object.value);
        }

        if (object.children != null) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (Entity o : object.children) {
                children.add(toMap(o));
            }
            map.put("children", children);
        }

        return map;
    }

    default Entity toEntity(Entity parent, Tree tree, String[] ruleNames) {
        Entity entity = new Entity();

        entity.parent = parent;
        entity.index = tree.getId().intValue();
        entity.name = null;
        entity.value = null;
        entity.children = null;

        if (tree.getParseTree() instanceof  RuleNode) {
            entity.name = toString(tree.getParseTree(), ruleNames);
            entity.children = new ArrayList<>();
        } else {
            entity.value = toString(tree.getParseTree(), ruleNames);
        }

        if (entity.children != null) {
            for (Tree t : tree.getChildren()) {
                entity.children.add(toEntity(entity, t, ruleNames));
            }
        }

        return entity;
    }

    default List<Entity> depthFirstSearch(Entity root) {
        Objects.requireNonNull(root);
        List<Entity> result = new ArrayList<>();
        Stack<Entity> container = new Stack<>();
        container.push(root);
        while (!container.isEmpty()) {
            Entity current = container.pop();
            result.add(current);
            if (current.children != null) {
                container.addAll(current.children);
            }
        }
        Collections.reverse(result);
        return result;
    }

    default List<Entity> breadthFirstSearch(Entity root) {
        Objects.requireNonNull(root);
        List<Entity> result = new ArrayList<>();
        Queue<Entity> container = new LinkedList<>();
        container.add(root);
        while (!container.isEmpty()) {
            Entity current = container.remove();
            result.add(current);
            if (current.children != null) {
                container.addAll(current.children);
            }
        }
        return result;
    }

    default List<Entity> getEntityPath(Entity entity) {
        List<Entity> path = new ArrayList<>();

        Entity parent = entity;
        do {
            if (parent != null) {
                path.add(0, parent);
                parent = parent.parent;
            }
        } while (parent != null);

        return path;
    }

    default List<Entry<List<String>, String>> toEntryList(
        ParseTree parseTree,
        String[] ruleNames,
        boolean filterTree,
        boolean terminalOnly,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Tree rootTree = toTree(
            parseTree,
            ruleNames,
            filterTree,
            terminalOnly,
            neededRuleNames,
            keepControlNames
        );
        Map<List<String>, String> map = toOrderedListStringMap(rootTree, ruleNames);
        return new ArrayList<>(map.entrySet());
    }

    default Map<List<String>, String> toOrderedListStringMap(Tree rootTree, String[] ruleNames) {
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
        boolean filterTree,
        boolean terminalOnly,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        Map<List<Long>, ParseTree> orderedListLongMap = toOrderedListLongMap(
            parseTree,
            ruleNames,
            filterTree,
            terminalOnly,
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
        boolean filterTree,
        boolean terminalOnly,
        Set<String> neededRuleNames,
        String... keepControlNames
    ) {
        AtomicLong increment = new AtomicLong(0);
        Tree rootTree = toTree(null, increment, parseTree);
        TreeProcessor treeProcessor = TreeProcessor.newInstance();
        Map<List<Long>, ParseTree> map = new LinkedHashMap<>();

        Predicate<Tree> neededControlNodeNameFilter;

        if (filterTree && !terminalOnly) {
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

            if (filterTree) {
                if (terminalOnly) {
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
                } else {
                    boolean neededTerminalNode;

                    if (neededRuleNames.isEmpty()) {
                        neededTerminalNode = true;
                    } else {
                        neededTerminalNode = treePath
                            .stream()
                            .map(Tree::getParseTree)
                            .filter(RuleNode.class::isInstance)
                            .map(pt -> toString(pt, ruleNames))
                            .anyMatch(neededRuleNames::contains);
                    }

                    if (!neededTerminalNode) {
                        continue;
                    }

                    Set<String> neededControlRuleNames = treePath
                        .stream()
                        .filter(neededControlNodeNameFilter)
                        .map(t -> toString(t.getParseTree(), ruleNames))
                        .collect(Collectors.toSet());

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
                }
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
