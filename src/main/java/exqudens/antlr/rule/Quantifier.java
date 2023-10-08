package exqudens.antlr.rule;

public enum Quantifier {

    ZERO_OR_ONE('?'),
    ZERO_OR_MORE('*'),
    ONE_OR_MORE('+');

    public final String value;

    private Quantifier(char c) {
        value = new Character(c).toString();
    }

}
