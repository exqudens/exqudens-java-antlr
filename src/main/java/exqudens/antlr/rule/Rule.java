package exqudens.antlr.rule;

import java.util.Objects;

public abstract class Rule {

    private final String name;
    private final String rule;

    public Rule(String name, String rule) {
        super();
        this.name = name;
        this.rule = rule;
    }

    public String getName() {
        return name;
    }

    public String getRule() {
        return rule;
    }

    public String toXMLString() {
        Objects.requireNonNull(name, "'name' is null");
        Objects.requireNonNull(rule, "'rule' is null");
        String tagName = getClass().getSimpleName().replace(Rule.class.getSimpleName(), "").toLowerCase();
        return "<" + tagName + " name='" + getName() + "'>" + getRule().replace("<", "\\<").replace(">", "\\>") + "</" + tagName + ">";
    }

}
