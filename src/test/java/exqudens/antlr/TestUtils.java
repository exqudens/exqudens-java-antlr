package exqudens.antlr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestUtils {

    public static List<String> readLines(InputStream inputStream) {
        try (
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            Objects.requireNonNull(inputStream);
            List<String> result = bufferedReader.lines().collect(Collectors.toList());
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

}
