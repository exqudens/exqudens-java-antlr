package exqudens.antlr.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParsingResult {

    private final String grammarFileName;
    private final String grammar;
    private final Map<String, Map<String, String>> configuration;
    private final List<Entry<List<String>, String>> entries;

    public ParsingResult(
        String grammarFileName,
        String grammar,
        Map<String, Map<String, String>> configuration,
        List<Entry<List<String>, String>> entries
    ) {
        this.grammarFileName = grammarFileName;
        this.grammar = grammar;
        this.configuration = configuration;
        this.entries = entries;
    }

    public String getGrammarFileName() {
        return grammarFileName;
    }

    public String getGrammar() {
        return grammar;
    }

    public Map<String, Map<String, String>> getConfiguration() {
        return configuration;
    }

    public List<Entry<List<String>, String>> getEntries() {
        return entries;
    }

}
