package exqudens.antlr;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface Application {

    String LONG_OPT_CHARSET = "charset";
    String LONG_OPT_TEMPLATE_NEW_LINE_UNIVERSAL = "template-new-line-universal";
    String LONG_OPT_INPUT_STRING = "input-string";
    String LONG_OPT_INPUT_FILE = "input-file";
    String LONG_OPT_TEMPLATE_STRING = "template-string";
    String LONG_OPT_TEMPLATE_FILE = "template-file";
    String LONG_OPT_OUTPUT_INDENT_FACTOR = "output-indent-factor";
    String LONG_OPT_OUTPUT_FILE = "output-file";

    static Application newInstance() {
        return new Application() {};
    }

    default void run(String... args) {
        try {
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
            Option outputFileOption = Option
                .builder()
                .longOpt(LONG_OPT_OUTPUT_FILE)
                .type(String.class)
                .hasArg()
                .build();

            Options options = new Options();

            options.addOption(charsetOption);
            options.addOption(templateNewLineUniversalOption);
            options.addOption(inputStringOption);
            options.addOption(inputFileOption);
            options.addOption(templateStringOption);
            options.addOption(templateFileOption);
            options.addOption(outputIndentFactorOption);
            options.addOption(outputFileOption);

            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            String userCharset = null;
            String userTemplateNewLineUniversal = null;
            String userInputString = null;
            String userInputFile = null;
            String userTemplateString = null;
            String userTemplateFile = null;
            String userOutputIndentFactor = null;
            String userOutputFile = null;

            if (commandLine.hasOption(charsetOption)) {
                userCharset = commandLine.getOptionValue(charsetOption);
            }
            if (commandLine.hasOption(templateNewLineUniversalOption)) {
                userTemplateNewLineUniversal = commandLine.getOptionValue(templateNewLineUniversalOption);
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
            if (commandLine.hasOption(outputFileOption)) {
                userOutputFile = commandLine.getOptionValue(outputFileOption);
            }

            String templateNL = "<or><area><carriage_return/><new_line/></area><area><carriage_return/></area><area><new_line/></area></or>";

            Charset charset = StandardCharsets.UTF_8;
            boolean templateNewLineUniversal = false;
            String text = null;
            String template = null;
            int outputIndentFactor = 0;
            String output = null;

            if (userCharset != null) {
                charset = Charset.forName(userCharset);
            }
            if (userTemplateNewLineUniversal != null) {
                templateNewLineUniversal = userTemplateNewLineUniversal.equalsIgnoreCase("true");
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

            Map.Entry<Map<List<String>, String>, Map<String, Map<String, String>>> parsingResult = ExqudensParserAntlr
                .newInstance()
                .parse(
                    text,
                    template,
                    currentMethodName.substring(0, 1).toUpperCase() + currentMethodName.substring(1),
                    getClass().getPackage().getName().split("\\.")
                );
            Map<String, Map<String, String>> configMap = parsingResult.getValue();
            Map<List<String>, String> resultMap = parsingResult.getKey();
            List<Map<String, Object>> entries = new ArrayList<>();

            for (Map.Entry<List<String>, String> resultMapEntry : resultMap.entrySet()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("key", resultMapEntry.getKey());
                entry.put("value", resultMapEntry.getValue());
                entries.add(entry);
            }

            Map<String, Object> map = new HashMap<>();

            map.put("configuration", configMap);
            map.put("entries", entries);

            if (userOutputFile == null) {
                output = new JSONObject(map).toString(outputIndentFactor);
                System.out.println(output);
            } else {
                output = new JSONObject(map).toString(outputIndentFactor);
                byte[] bytes = output.getBytes(charset);
                Files.write(Paths.get(userOutputFile), bytes);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
