package exqudens.antlr.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Constants {

    String TEXT   = "TEXT";
    String LEXER  = "Lexer";
    String PARSER = "Parser";

    String CONTROL_NODE_NAME_PROCESS  = "process";
    String CONTROL_NODE_NAME_RULE     = "rule";
    String CONTROL_NODE_NAME_LEXER    = "lexer";
    String CONTROL_NODE_NAME_PARSER   = "parser";
    String CONTROL_NODE_NAME_REPEAT   = "repeat";
    String CONTROL_NODE_NAME_OR       = "or";
    String CONTROL_NODE_NAME_OPTIONAL = "optional";
    String CONTROL_NODE_NAME_AREA     = "area";

    String GRAMMAR_EXTENSION = "g4";

    String[] CONTROL_NODE_NAMES = {
        CONTROL_NODE_NAME_REPEAT,
        CONTROL_NODE_NAME_OR,
        CONTROL_NODE_NAME_OPTIONAL,
        CONTROL_NODE_NAME_AREA
    };

}
