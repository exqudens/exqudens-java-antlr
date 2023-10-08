package exqudens.antlr;

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

public class UnitTests {

    @Test
    public void test1() {
        try {
            String currentMethodName = new Object() {}.getClass().getEnclosingMethod().getName();

            Assertions.assertNotNull(currentMethodName);
            Assertions.assertFalse(currentMethodName.isEmpty());

            Assertions.assertNotNull(System.getProperty("project.basedir"));
            Assertions.assertFalse(System.getProperty("project.basedir").isEmpty());

            Assertions.assertNotNull(System.getProperty("project.artifactId"));
            Assertions.assertFalse(System.getProperty("project.artifactId").isEmpty());

            Assertions.assertNotNull(System.getProperty("project.build.directory"));
            Assertions.assertFalse(System.getProperty("project.build.directory").isEmpty());

            Path resourcesDir = Paths.get(
                System.getProperty("project.basedir"),
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

            if (Files.exists(testDir)) {
                try (Stream<Path> fileStream = Files.walk(testDir)) {
                    fileStream.forEach(p -> {
                        File f = p.toFile();
                        if (!f.delete()) {
                            throw new RuntimeException("Failed to delete: '" + p + "'");
                        }
                    });
                }
                Files.createDirectories(testDir);
            }

            System.out.println("resourcesDir: '" + resourcesDir + "'");
            System.out.println("testDir: '" + testDir + "'");

            String[] args = new String[] {
                "--" + Application.LONG_OPT_HELP,
                "--" + Application.LONG_OPT_INPUT_STRING, "Order: 123",
                "--" + Application.LONG_OPT_TEMPLATE_STRING, "Order: <identifier name='order_number'/>",
                "--" + Application.LONG_OPT_OUTPUT_INDENT_FACTOR, "4"
            };
            Application.newInstance().run(args);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
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
                "</rule><repeat>OrderNumber\\< <identifier name='order_number' delimiter='-'/>",
                "<or><area>  Items:</area><area>  items:</area></or>",
                "<repeat><repeat><spaces/><identifier name='item_description'/><spaces/>$<numbers name='item_price'/><optional>zzz</optional></repeat>",
                "</repeat></repeat><eof/>"
            );

            Entry<Map<List<String>, String>, Map<String, Map<String, String>>> parseResult = ExqudensParserAntlr.newInstance().parse(text, template, "Exqudens", "org", "exqudens");
            Map<List<String>, String> map = parseResult.getKey();
            Map<String, Map<String, String>> configMap = parseResult.getValue();

            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(text);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(template);
            System.out.println("-------------------------------------------------------------------------------------");
            map.entrySet().forEach(System.out::println);
            System.out.println("-------------------------------------------------------------------------------------");
            configMap.entrySet().forEach(System.out::println);
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
