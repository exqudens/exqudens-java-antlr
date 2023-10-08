package exqudens.antlr.rule;

public class Rules {

    public static final LexerRule RUSSIAN_LOWER_LETTER = new LexerRule("RUSSIAN_LOWER_LETTER", "[\\u0451\\u0430-\\u044F]");
    public static final LexerRule RUSSIAN_UPPER_LETTER = new LexerRule("RUSSIAN_UPPER_LETTER", "[\\u0401\\u0410-\\u042F]");

    public static final LexerRule ENGLISH_LOWER_LETTER = new LexerRule("ENGLISH_LOWER_LETTER", "[a-z]");
    public static final LexerRule ENGLISH_UPPER_LETTER = new LexerRule("ENGLISH_UPPER_LETTER", "[A-Z]");

    public static final LexerRule NUMBER = new LexerRule("NUMBER", "[0-9]");

    public static final LexerRule DASH = new LexerRule("DASH", "[-]");
    public static final LexerRule UNDER_LINE = new LexerRule("UNDER_LINE", "[_]");
    public static final LexerRule SPACE = new LexerRule("SPACE", "[ ]");
    public static final LexerRule DOT = new LexerRule("DOT", "[.]");
    public static final LexerRule COMMA = new LexerRule("COMMA", "[,]");
    public static final LexerRule COLON = new LexerRule("COLON", "[:]");
    public static final LexerRule SEMICOLON = new LexerRule("SEMICOLON", "[;]");
    public static final LexerRule NEW_LINE = new LexerRule("NEW_LINE", "[\\n]");
    public static final LexerRule TAB = new LexerRule("TAB", "[\\t]");
    public static final LexerRule RETURN = new LexerRule("RETURN", "[\\r]");

    public static LexerRule[] lexerRules() {
        return new LexerRule[] {
            RUSSIAN_LOWER_LETTER,
            RUSSIAN_UPPER_LETTER,

            ENGLISH_LOWER_LETTER,
            ENGLISH_UPPER_LETTER,

            NUMBER,

            DASH,
            UNDER_LINE,
            SPACE,
            DOT,
            COMMA,
            COLON,
            SEMICOLON,
            NEW_LINE,
            TAB,
            RETURN
        };
    }

    public static final ParserRule russian_letter = new ParserRule("russian_letter", RUSSIAN_LOWER_LETTER, RUSSIAN_UPPER_LETTER);
    public static final ParserRule russian_letters = new ParserRule("russian_letters", Quantifier.ONE_OR_MORE, russian_letter);

    public static final ParserRule english_letter = new ParserRule("english_letter", ENGLISH_LOWER_LETTER, ENGLISH_UPPER_LETTER);
    public static final ParserRule english_letters = new ParserRule("english_letters", Quantifier.ONE_OR_MORE, english_letter);

    public static final ParserRule lower_letter = new ParserRule("lower_letter", RUSSIAN_LOWER_LETTER, ENGLISH_LOWER_LETTER);
    public static final ParserRule lower_letters = new ParserRule("lower_letters", Quantifier.ONE_OR_MORE, lower_letter);

    public static final ParserRule upper_letter = new ParserRule("upper_letter", RUSSIAN_UPPER_LETTER, ENGLISH_UPPER_LETTER);
    public static final ParserRule upper_letters = new ParserRule("upper_letters", Quantifier.ONE_OR_MORE, upper_letter);

    public static final ParserRule letter = new ParserRule("letter", lower_letter, upper_letter);
    public static final ParserRule letters = new ParserRule("letters", Quantifier.ONE_OR_MORE, letter);

    public static final ParserRule number = new ParserRule("number", NUMBER);
    public static final ParserRule numbers = new ParserRule("numbers", Quantifier.ONE_OR_MORE, number);

    public static final ParserRule space = new ParserRule("space", SPACE);
    public static final ParserRule spaces = new ParserRule("spaces", Quantifier.ONE_OR_MORE, space);

    public static final ParserRule carriage_return = new ParserRule("carriage_return", RETURN);
    public static final ParserRule carriage_returns = new ParserRule("carriage_returns", Quantifier.ONE_OR_MORE, carriage_return);

    public static final ParserRule new_line = new ParserRule("new_line", NEW_LINE);
    public static final ParserRule new_lines = new ParserRule("new_lines", Quantifier.ONE_OR_MORE, new_line);

    public static final ParserRule tab = new ParserRule("tab", TAB);
    public static final ParserRule tabs = new ParserRule("tabs", Quantifier.ONE_OR_MORE, tab);

    public static final ParserRule eof = new ParserRule("eof", "EOF");
    public static final ParserRule word = new ParserRule("word", Quantifier.ONE_OR_MORE, letter);
    public static final ParserRule identifier = new ParserRule("identifier", Quantifier.ONE_OR_MORE, letter, NUMBER, DASH, UNDER_LINE);

    public static ParserRule[] parserRules() {
        return new ParserRule[] {
            russian_letter,
            russian_letters,

            english_letter,
            english_letters,

            lower_letter,
            lower_letters,

            upper_letter,
            upper_letters,

            letter,
            letters,

            number,
            numbers,

            space,
            spaces,

            carriage_return,
            carriage_returns,

            new_line,
            new_lines,

            tab,
            tabs,

            eof,
            word,
            identifier
        };
    }

}
