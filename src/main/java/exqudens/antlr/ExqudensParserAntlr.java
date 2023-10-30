package exqudens.antlr;

import exqudens.antlr.generator.ClassGenerator;
import exqudens.antlr.generator.ClassLoaderGenerator;
import exqudens.antlr.generator.GrammarGenerator;
import exqudens.antlr.generator.JavaGenerator;
import exqudens.antlr.listener.ErrorListener;
import exqudens.antlr.model.ParsingResult;
import exqudens.antlr.processor.ParseTreeProcessor;
import exqudens.antlr.util.Constants;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface ExqudensParserAntlr {

    static ExqudensParserAntlr newInstance() {
        return new ExqudensParserAntlr() {};
    }

    default ParsingResult parse(
        String text,
        String template,
        String simpleClassName,
        String packageName,
        boolean filterTree,
        boolean terminalOnly,
        String... keepControlNames
    ) {
        try {
            Entry<String, Map<String, Map<String, String>>> grammarEntry = GrammarGenerator.newInstance().toGrammarEntry(template, simpleClassName);
            String grammarFileName = simpleClassName + "." + Constants.GRAMMAR_EXTENSION;
            String grammar = grammarEntry.getKey();

            Map<String, String> javaFiles = JavaGenerator.newInstance().generateJavaFiles(grammarFileName, grammar, packageName);
            Map<String, byte[]> classFiles = ClassGenerator.newInstance().generateClassFiles(javaFiles);
            ClassLoader classLoader = ClassLoaderGenerator.newInstance().generateClassLoader(getClass().getClassLoader(), classFiles);
            CharStream charStream = CharStreams.fromString(text);

            Class<?> lexerClass = classLoader.loadClass(packageName + "." + simpleClassName + Constants.LEXER);
            Object lexerObject = lexerClass.getConstructor(CharStream.class).newInstance(charStream);
            Lexer lexer = (Lexer) lexerObject;

            lexer.removeErrorListeners();
            lexer.addErrorListener(ErrorListener.newInstance());

            TokenStream tokenStream = new CommonTokenStream(lexer);

            Class<?> parserClass = classLoader.loadClass(packageName + "." + simpleClassName + Constants.PARSER);
            Object parserObject = parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
            Parser parser = (Parser) parserObject;

            parser.removeErrorListeners();
            parser.addErrorListener(ErrorListener.newInstance());

            Object parseTreeObject = parserClass.getDeclaredMethod(Constants.CONTROL_NODE_NAME_PROCESS).invoke(parser);
            ParseTree parseTree = (ParseTree) parseTreeObject;

            String[] ruleNames = parser.getRuleNames();

            Map<String, Map<String, String>> configuration = grammarEntry.getValue();
            List<Entry<List<String>, String>> list = ParseTreeProcessor.newInstance().toEntryList(
                parseTree,
                ruleNames,
                filterTree,
                terminalOnly,
                configuration.keySet(),
                keepControlNames
            );
            Map<String, Object> map = ParseTreeProcessor.newInstance().toTreeMap(
                parseTree,
                ruleNames,
                filterTree,
                terminalOnly,
                configuration.keySet(),
                keepControlNames
            );

            return new ParsingResult(
                grammarFileName,
                grammar,
                configuration,
                list,
                map
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
