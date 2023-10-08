package exqudens.antlr.rule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParserRule extends Rule {

    public static String toString(Quantifier quantifier, Rule rule, Rule... rules) {
        String string;

        String quantifierValue = quantifier != null ? quantifier.value : "";
        List<String> names = Stream.concat(Stream.of(rule), Stream.of(rules))
        .filter(Objects::nonNull)
        .map(Rule::getName)
        .collect(Collectors.toList());

        if (names.isEmpty()) {
            throw new IllegalStateException("'names' is empty");
        }

        if (names.size() > 1) {
            string = names.stream().collect(Collectors.joining(" | ", "( ", " )" + quantifierValue));
        } else {
            string = names.get(0) + quantifierValue;
        }

        return string;
    }

    public ParserRule(String name, String rule) {
        super(name, rule);
    }

    public ParserRule(String name, Rule rule, Rule... rules) {
        super(name, toString(null, rule, rules));
    }

    public ParserRule(String name, Quantifier quantifier, Rule rule, Rule... rules) {
        super(name, toString(quantifier, rule, rules));
    }

}
