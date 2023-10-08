package exqudens.antlr.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import exqudens.antlr.util.Constants;

public class Rule {

    private final Integer index;
    private final String name;
    private final String fullName;
    private final List<Rule> children;
    private final String value;

    public Rule(Integer index, String name, String value) {
        super();
        this.index = !Constants.CONTROL_NODE_NAME_PROCESS.equals(name) ? index : null;
        this.name = name;
        this.fullName = this.index != null ? this.name + "_" + this.index : this.name;
        this.children = new ArrayList<>();
        this.value = value;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Rule> getChildren() {
        return children;
    }

    public String getValue() {
        if (value != null) {
            return value;
        }

        if (getChildren().isEmpty()) {
             return getFullName();
        } else if (Constants.CONTROL_NODE_NAME_OR.equals(name) && getChildren().size() > 1) {
            String delimiter = " | ";
            String prefix = "( ";
            String suffix = " )";
            String quantifier = "";
            return getChildren()
                    .stream()
                    .map(r -> {
                        if (Constants.CONTROL_NODE_NAME_REPEAT.equals(r.getName())) {
                            return r.getFullName() + "+";
                        } else if (Constants.CONTROL_NODE_NAME_OPTIONAL.equals(r.getName())) {
                            return r.getFullName() + "?";
                        }
                        return r.getFullName();
                    })
                    .collect(Collectors.joining(delimiter, prefix, suffix + quantifier));
        } else {
            String delimiter = " ";
            return getChildren()
            .stream()
            .map(r -> {
                if (Constants.CONTROL_NODE_NAME_REPEAT.equals(r.getName())) {
                    return r.getFullName() + "+";
                } else if (Constants.CONTROL_NODE_NAME_OPTIONAL.equals(r.getName())) {
                    return r.getFullName() + "?";
                }
                return r.getFullName();
            })
            .collect(Collectors.joining(delimiter));
        }
    }

}
