package exqudens.antlr;

import exqudens.antlr.model.ParsingResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class IntegrationTests {

    @Test
    public void test1() throws Throwable {
        String javaHomeDirString = System.getProperty("java.home");
        String projectDirString = System.getProperty("project.basedir");
        String buildDirString = System.getProperty("project.build.directory");

        String currentMethodName = new Object() {}.getClass().getEnclosingMethod().getName();
        String projectName = System.getProperty("project.artifactId");
        String projectVersion = System.getProperty("project.version");

        Assertions.assertNotNull(javaHomeDirString);
        Assertions.assertFalse(javaHomeDirString.isEmpty());

        Assertions.assertNotNull(projectDirString);
        Assertions.assertFalse(projectDirString.isEmpty());

        Assertions.assertNotNull(buildDirString);
        Assertions.assertFalse(buildDirString.isEmpty());

        Assertions.assertNotNull(currentMethodName);
        Assertions.assertFalse(currentMethodName.isEmpty());

        Assertions.assertNotNull(projectName);
        Assertions.assertFalse(projectName.isEmpty());

        Assertions.assertNotNull(projectVersion);
        Assertions.assertFalse(projectVersion.isEmpty());

        Path projectDir = Paths.get(projectDirString);

        Path resourcesDir = Paths.get(
            projectDir.toFile().toString(),
            String.join(".",
                "src",
                "test",
                "resources",
                getClass().getName(),
                currentMethodName
            ).split("\\.")
        );

        Path testDir = Paths.get(
            System.getProperty("project.build.directory"),
            String.join(".",
                System.getProperty("project.artifactId") + "-tests-output-dir",
                getClass().getName(),
                currentMethodName
            ).split("\\.")
        );

        System.out.println("resourcesDir: '" + resourcesDir + "'");
        System.out.println("testDir: '" + testDir + "'");

        if (Files.exists(testDir)) {
            try (Stream<Path> fileStream = Files.walk(testDir)) {
                fileStream.forEach(p -> {
                    File f = p.toFile();
                    if (!f.delete()) {
                        throw new RuntimeException("Failed to delete: '" + p + "'");
                    }
                });
            }
        }
        Files.createDirectories(testDir);

        String jarFileName = String.join("-",
            projectName,
            projectVersion,
            "executable.jar"
        );
        String javaCommand = Paths.get(javaHomeDirString, "bin", "java").toFile().getAbsolutePath();
        String buildDir = Paths.get(buildDirString).toFile().getAbsolutePath();
        String jarFile = Paths.get(buildDir, jarFileName).toFile().getAbsolutePath();

        System.out.println("jarFile: '" + jarFile + "'");

        Process process = new ProcessBuilder()
            .command(
                "cmd.exe", "/c", javaCommand, "-jar", jarFile,
                //"--" + Application.LONG_OPT_HELP,
                "--" + Application.LONG_OPT_TEMPLATE_NEW_LINE_UNIVERSAL, "true",
                "--" + Application.LONG_OPT_INPUT_FILE, resourcesDir.resolve("input-file.txt").toFile().getAbsolutePath(),
                "--" + Application.LONG_OPT_TEMPLATE_FILE, resourcesDir.resolve("template-file.txt").toFile().getAbsolutePath(),
                "--" + Application.LONG_OPT_OUTPUT_INDENT_FACTOR, "4",
                "--" + Application.LONG_OPT_OUTPUT_DIR, testDir.resolve("output").toFile().getAbsolutePath()
            )
            .directory(projectDir.toFile())
            .start();

        process.waitFor();

        //OutputStream outputStream = process.getOutputStream();
        //InputStream inputStream = process.getInputStream();
        //InputStream errorStream = process.getErrorStream();

        if (process.exitValue() > 0) {
            List<String> errorLines = TestUtils.readLines(process.getErrorStream());
            throw new RuntimeException(String.join(System.lineSeparator(), errorLines));
        } else {
            List<String> outputLines = TestUtils.readLines(process.getInputStream());
            for (String line : outputLines) {
                System.out.println(line);
            }
        }
    }

    @Test
    public void test2() {
        try {
            String text = String.join(System.lineSeparator(),
                "OrderNumber< 12345.ABC",
                "  Items:",
                "    description_1   $1zzz    description___4 $400",
                "    description__2  $20   description__5  $50",
                "    description___3 $300  description_6   $6",
                "OrderNumber< 09876XYZ",
                "  items:",
                "    description_7   $7zzz    description___10 $1000",
                "    description__8  $80   description__11  $110",
                "    description___9 $900  description_12   $12",
                ""
            );

            String template = String.join(
                "<or><area><carriage_return/><new_line/></area><area><carriage_return/></area><area><new_line/></area></or>",

                "<rule>",
                "  <parser name='identifier'>( letter | NUMBER | DASH | UNDER_LINE | DOT )+</parser>",
                "</rule><repeat produce='orders'>OrderNumber\\< <identifier name='order_number' delimiter='-'/>",
                "<or><area>  Items:</area><area>  items:</area></or>",
                "<repeat produce='items'><repeat><spaces/><identifier name='item_description'/><spaces/>$<numbers name='item_price'/><optional>zzz</optional></repeat>",
                "</repeat></repeat><eof/>"
            );

            ParsingResult parsingResult = ExqudensParserAntlr.newInstance().parse(text, template, "Exqudens", "org", "exqudens");
            List<Entry<List<String>, String>> list = parsingResult.getList();
            Map<String, Map<String, String>> configuration = parsingResult.getConfiguration();

            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(text);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(template);
            System.out.println("-------------------------------------------------------------------------------------");
            list.forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------------");
            configuration.entrySet().forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------------");

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    @Test
    public void test3() {
        try {
            String text = String.join(System.lineSeparator(),
                "OrderNumber: 12345.ABC",
                ""
            );

            String template = String.join(
                "<or><area><carriage_return/><new_line/></area><area><carriage_return/></area><area><new_line/></area></or>",

                "<rule>",
                "  <parser name='identifier'>( letter | NUMBER | DASH | UNDER_LINE | DOT )+</parser>",
                "</rule>OrderNumber: <identifier name='order_number' delimiter='-'/>",
                "<eof/>"
            );

            ParsingResult parsingResult = ExqudensParserAntlr.newInstance().parse(text, template, "Exqudens", "org", "exqudens");
            List<Entry<List<String>, String>> list = parsingResult.getList();
            Map<String, Map<String, String>> configuration = parsingResult.getConfiguration();

            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(text);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(template);
            System.out.println("-------------------------------------------------------------------------------------");
            list.forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------------");
            configuration.entrySet().forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------------");

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

}
