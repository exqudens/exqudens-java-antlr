package exqudens.antlr.test;

import exqudens.antlr.ExqudensParserAntlr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Test1 {

    @Test
    public void test() {
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
