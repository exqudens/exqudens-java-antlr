package exqudens.antlr;

import exqudens.antlr.model.ParsingResult;
import exqudens.antlr.util.Constants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

public interface Application {

    String LONG_OPT_HELP = "help";
    String LONG_OPT_CHARSET = "charset";
    String LONG_OPT_TEMPLATE_NEW_LINE_UNIVERSAL = "template-new-line-universal";
    String LONG_OPT_TERMINAL_ONLY = "terminal-only";
    String LONG_OPT_FILTER_TREE = "filter-tree";
    String LONG_OPT_KEEP_CONTROL_NAMES = "keep-control-names";
    String LONG_OPT_INPUT_STRING = "input-string";
    String LONG_OPT_INPUT_FILE = "input-file";
    String LONG_OPT_TEMPLATE_STRING = "template-string";
    String LONG_OPT_TEMPLATE_FILE = "template-file";
    String LONG_OPT_OUTPUT_INDENT_FACTOR = "output-indent-factor";
    String LONG_OPT_OUTPUT_DIR = "output-dir";

    static Application newInstance() {
        return new Application() {};
    }

    default void run(String... args) {
        try {
            Option helpOption = Option
                .builder()
                .longOpt(LONG_OPT_HELP)
                .build();
            Option charsetOption = Option
                .builder()
                .longOpt(LONG_OPT_CHARSET)
                .type(String.class)
                .hasArg()
                .build();
            Option templateNewLineUniversalOption = Option
                .builder()
                .longOpt(LONG_OPT_TEMPLATE_NEW_LINE_UNIVERSAL)
                .type(String.class)
                .hasArg()
                .build();
            Option terminalOnlyOption = Option
                .builder()
                .longOpt(LONG_OPT_TERMINAL_ONLY)
                .type(String.class)
                .hasArg()
                .build();
            Option filterTreeOption = Option
                .builder()
                .longOpt(LONG_OPT_FILTER_TREE)
                .type(String.class)
                .hasArg()
                .build();
            Option keepControlNamesOption = Option.builder()
                .longOpt(LONG_OPT_KEEP_CONTROL_NAMES)
                .type(String.class)
                .hasArgs()
                .build();
            Option inputStringOption = Option
                .builder()
                .longOpt(LONG_OPT_INPUT_STRING)
                .type(String.class)
                .hasArg()
                .build();
            Option inputFileOption = Option
                .builder()
                .longOpt(LONG_OPT_INPUT_FILE)
                .type(String.class)
                .hasArg()
                .build();
            Option templateStringOption = Option
                .builder()
                .longOpt(LONG_OPT_TEMPLATE_STRING)
                .type(String.class)
                .hasArg()
                .build();
            Option templateFileOption = Option
                .builder()
                .longOpt(LONG_OPT_TEMPLATE_FILE)
                .type(String.class)
                .hasArg()
                .build();
            Option outputIndentFactorOption = Option
                .builder()
                .longOpt(LONG_OPT_OUTPUT_INDENT_FACTOR)
                .type(String.class)
                .hasArg()
                .build();
            Option outputDirOption = Option
                .builder()
                .longOpt(LONG_OPT_OUTPUT_DIR)
                .type(String.class)
                .hasArg()
                .build();

            Options options = new Options();

            options.addOption(helpOption);
            options.addOption(charsetOption);
            options.addOption(templateNewLineUniversalOption);
            options.addOption(terminalOnlyOption);
            options.addOption(filterTreeOption);
            options.addOption(keepControlNamesOption);
            options.addOption(inputStringOption);
            options.addOption(inputFileOption);
            options.addOption(templateStringOption);
            options.addOption(templateFileOption);
            options.addOption(outputIndentFactorOption);
            options.addOption(outputDirOption);

            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.getOptions().length == 0 || commandLine.hasOption(helpOption)) {
                HelpFormatter formatter = new HelpFormatter();
                Properties properties = new Properties();
                properties.load(Application.class.getResourceAsStream("/main.properties"));
                String cmdLineSyntax = properties.get("project.artifactId") + "-" + properties.get("project.version");
                formatter.printHelp(cmdLineSyntax, options);
                return;
            }

            String userCharset = null;
            String userTemplateNewLineUniversal = null;
            String userTerminalOnly = null;
            String userFilterTree = null;
            String[] userKeepControlNames = null;
            String userInputString = null;
            String userInputFile = null;
            String userTemplateString = null;
            String userTemplateFile = null;
            String userOutputIndentFactor = null;
            String userOutputDir = null;

            if (commandLine.hasOption(charsetOption)) {
                userCharset = commandLine.getOptionValue(charsetOption);
            }
            if (commandLine.hasOption(templateNewLineUniversalOption)) {
                userTemplateNewLineUniversal = commandLine.getOptionValue(templateNewLineUniversalOption);
            }
            if (commandLine.hasOption(terminalOnlyOption)) {
                userTerminalOnly = commandLine.getOptionValue(terminalOnlyOption);
            }
            if (commandLine.hasOption(filterTreeOption)) {
                userFilterTree = commandLine.getOptionValue(filterTreeOption);
            }
            if (commandLine.hasOption(keepControlNamesOption)) {
                userKeepControlNames = commandLine.getOptionValues(keepControlNamesOption);
            }
            if (commandLine.hasOption(inputStringOption)) {
                userInputString = commandLine.getOptionValue(inputStringOption);
            }
            if (commandLine.hasOption(inputFileOption)) {
                userInputFile = commandLine.getOptionValue(inputFileOption);
            }
            if (commandLine.hasOption(templateStringOption)) {
                userTemplateString = commandLine.getOptionValue(templateStringOption);
            }
            if (commandLine.hasOption(templateFileOption)) {
                userTemplateFile = commandLine.getOptionValue(templateFileOption);
            }
            if (commandLine.hasOption(outputIndentFactorOption)) {
                userOutputIndentFactor = commandLine.getOptionValue(outputIndentFactorOption);
            }
            if (commandLine.hasOption(outputDirOption)) {
                userOutputDir = commandLine.getOptionValue(outputDirOption);
            }

            String templateNL = "<or><area><carriage_return/><new_line/></area><area><carriage_return/></area><area><new_line/></area></or>";

            Charset charset = StandardCharsets.UTF_8;
            boolean templateNewLineUniversal = false;
            boolean terminalOnly = false;
            boolean filterTree = false;
            String[] keepControlNames = {};
            String text = null;
            String template = null;
            int outputIndentFactor = 0;

            if (userCharset != null) {
                charset = Charset.forName(userCharset);
            }
            if (userTemplateNewLineUniversal != null) {
                templateNewLineUniversal = userTemplateNewLineUniversal.equalsIgnoreCase("true");
            }
            if (userTerminalOnly != null) {
                terminalOnly = userTerminalOnly.equalsIgnoreCase("true");
            }
            if (userFilterTree != null) {
                filterTree = userFilterTree.equalsIgnoreCase("true");
            }
            if (userKeepControlNames != null) {
                keepControlNames = userKeepControlNames;
            }
            if (userInputString != null) {
                text = userInputString;
            }
            if (userInputFile != null) {
                byte[] bytes = Files.readAllBytes(Paths.get(userInputFile));
                text = new String(bytes, charset);
            }
            if (userTemplateString != null) {
                template = userTemplateString;
            }
            if (userTemplateFile != null) {
                byte[] bytes = Files.readAllBytes(Paths.get(userTemplateFile));
                template = new String(bytes, charset);
            }
            if (userOutputIndentFactor != null) {
                outputIndentFactor = Integer.parseInt(userOutputIndentFactor);
            }

            Objects.requireNonNull(text);
            Objects.requireNonNull(template);

            if (templateNewLineUniversal) {
                template = template.replace(
                    System.lineSeparator(),
                    templateNL
                );
            }

            String currentMethodName = new Object() {}.getClass().getEnclosingMethod().getName();

            ParsingResult parsingResult = ExqudensParserAntlr.newInstance().parse(
                text,
                template,
                currentMethodName.substring(0, 1).toUpperCase() + currentMethodName.substring(1),
                getClass().getPackage().getName(),
                terminalOnly,
                filterTree,
                keepControlNames
            );

            Map<String, Map<String, String>> configuration = parsingResult.getConfiguration();
            List<Map<String, Object>> list = new ArrayList<>();

            for (Entry<List<String>, String> entry : parsingResult.getList()) {
                Map<String, Object> listEntry = new HashMap<>();
                listEntry.put("key", entry.getKey());
                listEntry.put("value", entry.getValue());
                list.add(listEntry);
            }

            Map<String, Object> map = new HashMap<>();

            map.put("configuration", configuration);
            map.put("list", list);

            String json = new JSONObject(map).toString(outputIndentFactor);

            if (userOutputDir == null) {
                System.out.println(json);
            } else {
                Path outputDir = Paths.get(userOutputDir);
                if (!Files.exists(outputDir)) {
                    Files.createDirectories(outputDir);

                    String grammar = parsingResult.getGrammar();
                    Path grammarFile = outputDir.resolve(parsingResult.getGrammarFileName());
                    byte[] grammarBytes = grammar.getBytes(charset);
                    Files.write(grammarFile, grammarBytes);

                    Path jsonFile = outputDir.resolve("parsing-result.json");
                    byte[] jsonBytes = json.getBytes(charset);
                    Files.write(jsonFile, jsonBytes);

                    System.out.println("outputDir: '" + outputDir.toFile().getAbsolutePath() + "'");
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
