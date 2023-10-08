package exqudens.antlr.generator;

import org.antlr.v4.Tool;
import org.antlr.v4.tool.BuildDependencyGenerator;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.ast.GrammarRootAST;

import java.io.StringWriter;
import java.io.Writer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface JavaGenerator {

    static JavaGenerator newInstance() {
        return new JavaGenerator() {};
    }

    default Map<String, String> generateJavaFiles(String grammarFileName, String grammarContent) {
        return generateJavaFiles(
            Stream.of(new SimpleEntry<>(grammarFileName, grammarContent)).collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
            null,
            false
        );
    }

    default Map<String, String> generateJavaFiles(String grammarFileName, String grammarContent, String genPackage) {
        return generateJavaFiles(
            Stream.of(new SimpleEntry<>(grammarFileName, grammarContent)).collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
            genPackage,
            false
        );
    }

    default Map<String, String> generateJavaFiles(String grammarFileName, String grammarContent, String genPackage, boolean generateListener) {
        return generateJavaFiles(
            Stream.of(new SimpleEntry<>(grammarFileName, grammarContent)).collect(Collectors.toMap(Entry::getKey, Entry::getValue)),
            genPackage,
            generateListener
        );
    }

    default Map<String, String> generateJavaFiles(Map<String, String> grammarFiles, String genPackage, boolean generateListener) {

        Map<String, Writer> writerMap = new HashMap<>();
        Map<String, String> map = grammarFiles;

        Tool antlr;

        try {

            antlr = new Tool() {

                @Override
                public GrammarRootAST parseGrammar(String fileName) {
                    return parseGrammarFromString(map.get(fileName));
                }

                @Override
                public Writer getOutputFileWriter(Grammar g, String fileName) {
                    Writer writer = new StringWriter();
                    writerMap.put(fileName, writer);
                    return writer;
                }

            };

            antlr.genPackage = genPackage;
            antlr.gen_listener = generateListener;

            List<GrammarRootAST> sortedGrammars = antlr.sortGrammarByTokenVocab(
                    new ArrayList<>(map.keySet())
            );

            for (GrammarRootAST t : sortedGrammars) {
                final Grammar g = antlr.createGrammar(t);
                g.fileName = t.fileName;
                if (antlr.gen_dependencies) {
                    BuildDependencyGenerator dep = new BuildDependencyGenerator(antlr, g);
                    System.out.println(dep.getDependencies().render());
                } else if (antlr.errMgr.getNumErrors() == 0) {
                    antlr.process(g, true);
                }
            }

            if (antlr.log) {
                System.out.println(antlr.logMgr.save());
            }

            return writerMap.entrySet().stream().map(
                entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().toString())
            ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            close(writerMap.values());
        }
    }

    default void close(Collection<Writer> writers) {
        try {
            for (Writer writer : writers) {
                writer.close();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
