package exqudens.antlr.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ParsingResult {

    private final String grammarFileName;
    private final String grammar;
    private final Map<String, Map<String, String>> configuration;
    private final List<Entry<List<String>, String>> list;
    private final Map<String, Object> map;

    public ParsingResult(
        String grammarFileName,
        String grammar,
        Map<String, Map<String, String>> configuration,
        List<Entry<List<String>, String>> list,
        Map<String, Object> map
    ) {
        this.grammarFileName = grammarFileName;
        this.grammar = grammar;
        this.configuration = configuration;
        this.list = list;
        this.map = map;
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

    public List<Entry<List<String>, String>> getList() {
        return list;
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
