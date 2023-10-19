package exqudens.antlr.generator;

import exqudens.antlr.processor.NodeProcessor;
import exqudens.antlr.util.Constants;
import exqudens.antlr.rule.LexerRule;
import exqudens.antlr.rule.ParserRule;
import exqudens.antlr.model.Rule;
import exqudens.antlr.rule.Rules;
import exqudens.antlr.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface GrammarGenerator {

    static GrammarGenerator newInstance() {
        return new GrammarGenerator() {};
    }

    default Entry<String, Map<String, Map<String, String>>> toGrammarEntry(String templateContent, String grammarName) {
        String newStartTag = "START_" + System.nanoTime() + "_START";
        String newEndTag = "END_" + System.nanoTime() + "_END";
        return toGrammarEntry(templateContent, newStartTag, newEndTag, grammarName);
    }

    default Entry<String, Map<String, Map<String, String>>> toGrammarEntry(String templateContent, String newStartTag, String newEndTag, String grammarName) {
        String xmlContent = "<" + Constants.CONTROL_NODE_NAME_PROCESS + ">" + convert(templateContent, newStartTag, newEndTag) + "</" + Constants.CONTROL_NODE_NAME_PROCESS + ">";
        Node node = NodeProcessor.newInstance().createNode(xmlContent);
        Document document = (Document) node;
        NodeList lexerNodes = document.getElementsByTagName(Constants.CONTROL_NODE_NAME_LEXER);
        NodeList parserNodes = document.getElementsByTagName(Constants.CONTROL_NODE_NAME_PARSER);

        Map<String, LexerRule> lexerRuleMap = Stream
        .of(Rules.lexerRules())
        .collect(Collectors.toMap(LexerRule::getName, Function.identity(), Util.throwingMerger(), LinkedHashMap::new));
        Map<String, ParserRule> parserRuleMap = Stream
        .of(Rules.parserRules())
        .collect(Collectors.toMap(ParserRule::getName, Function.identity(), Util.throwingMerger(), LinkedHashMap::new));

        Map<String, LexerRule> overrideLexerRuleMap = IntStream
        .range(0, lexerNodes.getLength())
        .mapToObj(lexerNodes::item)
        .map(n -> new LexerRule(n.getAttributes().getNamedItem("name").getNodeValue(), n.getTextContent()))
        .collect(Collectors.toMap(LexerRule::getName, Function.identity()));

        Map<String, ParserRule> overrideParserRuleMap = IntStream
        .range(0, parserNodes.getLength())
        .mapToObj(parserNodes::item)
        .map(n -> new ParserRule(n.getAttributes().getNamedItem("name").getNodeValue(), n.getTextContent()))
        .collect(Collectors.toMap(ParserRule::getName, Function.identity()));

        lexerRuleMap.putAll(overrideLexerRuleMap);
        parserRuleMap.putAll(overrideParserRuleMap);

        List<LexerRule> lexerRules = new ArrayList<>(lexerRuleMap.values());
        List<ParserRule> parserRules = new ArrayList<>(parserRuleMap.values());

        if (document.getElementsByTagName(Constants.CONTROL_NODE_NAME_RULE).getLength() > 0) {
            node.getFirstChild().removeChild(document.getElementsByTagName(Constants.CONTROL_NODE_NAME_RULE).item(0));
        }

        return toGrammarEntry(document, newStartTag, newEndTag, grammarName, lexerRules, parserRules);
    }

    default Entry<String, Map<String, Map<String, String>>> toGrammarEntry(Node document, String newStartTag, String newEndTag, String grammarName, List<LexerRule> lexerRules, List<ParserRule> parserRules) {
        try {
            NodeProcessor.newInstance().removeEmptyTextNodes(
                document,
                Constants.CONTROL_NODE_NAME_PROCESS,
                Constants.CONTROL_NODE_NAME_REPEAT,
                Constants.CONTROL_NODE_NAME_OPTIONAL,
                Constants.CONTROL_NODE_NAME_AREA
            );

            Entry<Rule, Map<String, Map<String, String>>> entry = toRuleEntry(document.getChildNodes().item(0), newStartTag, newEndTag, parserRules.stream().map(ParserRule::getName).collect(Collectors.toSet()));
            Rule rule = entry.getKey();
            Map<String, Map<String, String>> attributeMap = entry.getValue();

            Map<String, Rule> newLexerRules = new LinkedHashMap<>();
            List<Rule> newParserRules = new ArrayList<>();

            for (Rule descendant : getDescendants(rule)) {
                if (Constants.TEXT.equals(descendant.getName())) {
                    newLexerRules.putIfAbsent(descendant.getFullName(), descendant);
                } else {
                    newParserRules.add(descendant);
                }
            }

            List<String> lines = new ArrayList<>();
            lines.add("grammar " + grammarName + ";");
            lines.add("");

            lines.add("// Parser Rules");
            lines.add("");
            for (Rule r : newParserRules) {
                lines.add(r.getFullName() + " : " + r.getValue() + " ;");
            }
            lines.add("");
            for (ParserRule parserRule : parserRules) {
                lines.add(parserRule.getName() + " : " + parserRule.getRule() + " ;");
            }
            lines.add("");

            lines.add("// Lexer Rules");
            lines.add("");
            for (Rule r : newLexerRules.values()) {
                lines.add(r.getFullName() + " : " + r.getValue() + " ;");
            }
            lines.add("");
            for (LexerRule lexerRule : lexerRules) {
                lines.add(lexerRule.getName() + " : " + lexerRule.getRule() + " ;");
            }
            lines.add("");

            String grammar = String.join("\n", lines);
            return new SimpleEntry<>(grammar, attributeMap);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    default Entry<Rule, Map<String, Map<String, String>>> toRuleEntry(Node node, String newStartTag, String newEndTag, Set<String> parserRuleNames) {
        Map<String, Rule> textRuleMap = new HashMap<>();
        Map<String, AtomicInteger> indexMap = new HashMap<>();
        Map<List<Integer>, Rule> ruleMap = new LinkedHashMap<>();
        Map<String, Map<String, String>> attributeMap = new LinkedHashMap<>();
        NodeProcessor nodeProcessor = NodeProcessor.newInstance();

        for (Node descendant : nodeProcessor.getDescendants(node)) {

            String nodeName = descendant.getNodeName();
            short nodeType = descendant.getNodeType();
            List<Integer> key = nodeProcessor.getIntegerPath(descendant);
            indexMap.putIfAbsent(nodeName, new AtomicInteger(0));

            String name = nodeName;
            String value;
            Integer index;
            Rule r;
            if (Node.TEXT_NODE == nodeType) {
                name = Constants.TEXT;
                value = "'" + prepareLexerRuleValue(descendant.getNodeValue(), newStartTag, newEndTag) + "'";
                if (!textRuleMap.containsKey(value)) {
                    index = indexMap.get(nodeName).getAndIncrement();
                    r = new Rule(index, name, value);
                    textRuleMap.put(value, r);
                } else {
                    r = textRuleMap.get(value);
                }
            } else if (parserRuleNames.contains(nodeName)) {
                value = nodeName;
                index = indexMap.get(nodeName).getAndIncrement();
                r = new Rule(index, name, value);
            } else {
                index = indexMap.get(nodeName).getAndIncrement();
                r = new Rule(index, name, null);
            }

            for (int i = key.size() - 1; i > 0; i--) {
                List<Integer> subKey = key.subList(0, i);
                if (ruleMap.containsKey(subKey)) {
                    ruleMap.get(subKey).getChildren().add(r);
                    break;
                }
            }

            if (descendant.getAttributes() != null && descendant.getAttributes().getLength() > 0) {
                attributeMap.putIfAbsent(r.getFullName(), new LinkedHashMap<>());
                for (int i = 0; i < descendant.getAttributes().getLength(); i++) {
                    String attributeKey = descendant.getAttributes().item(i).getNodeName();
                    String attributeValue = descendant.getAttributes().item(i).getNodeValue();
                    attributeMap.get(r.getFullName()).put(attributeKey, attributeValue);
                }
            }

            ruleMap.putIfAbsent(key, r);
        }

        Rule rule = ruleMap.values().iterator().next();

        return new SimpleEntry<>(rule, attributeMap);
    }

    default String prepareLexerRuleValue(String nodeValue, String newStartTag, String newEndTag) {
        return unconvert(nodeValue, newStartTag, newEndTag)
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
        .replace("\r", "\\r")
        ;
    }

    default String convert(String unconvertedString, String newStartTag, String newEndTag) {
        String escapedStartTag = "\\<";
        String escapedEndTag = "\\>";
        String replace1 = unconvertedString.replace(escapedStartTag, newStartTag);
        return replace1.replace(escapedEndTag, newEndTag);
    }

    default String unconvert(String convertedString, String newStartTag, String newEndTag) {
        String unescapedStartTag = "<";
        String unescapedEndTag = ">";
        String replace1 = convertedString.replace(newStartTag, unescapedStartTag);
        return replace1.replace(newEndTag, unescapedEndTag);
    }

    default List<Rule> getDescendants(Rule rule) {
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        for (int i = 0 ; i < rule.getChildren().size(); i++){
            rules.addAll(getDescendants(rule.getChildren().get(i)));
        }
        return rules;
    }

}
