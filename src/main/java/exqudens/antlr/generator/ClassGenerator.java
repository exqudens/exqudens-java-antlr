package exqudens.antlr.generator;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ClassGenerator {

    static ClassGenerator newInstance() {
        return new ClassGenerator() {};
    }

    default Map<String, byte[]> generateClassFiles(Map<String, String> javaFiles) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(
                diagnostics,
                Locale.ENGLISH,
                StandardCharsets.UTF_8
            );
            Map<String, ByteArrayOutputStream> byteArrayMap = javaFiles.keySet().stream().filter(
                fileName -> fileName.endsWith(Kind.SOURCE.extension)
            ).map(s -> s.split("\\.")[0]).map(s -> s.replace(File.separator, ".")).map(
                s -> new SimpleEntry<>(s, new ByteArrayOutputStream())
            ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(standardFileManager) {

                @Override
                public JavaFileObject getJavaFileForOutput(
                    Location location,
                    String className,
                    Kind kind,
                    FileObject sibling
                ) {
                    byteArrayMap.putIfAbsent(className, new ByteArrayOutputStream());
                    return toJavaFileObject(className, byteArrayMap.get(className));
                }

            };
            List<String> optionList = getOptionList();
            List<JavaFileObject> compilationUnits = javaFiles.entrySet().stream().filter(
                entry -> entry.getKey().endsWith(Kind.SOURCE.extension)
            ).map(entry -> toJavaFileObject(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                optionList,
                null,
                compilationUnits
            );
            if (!task.call()) {
                Diagnostic<?> diagnostic = diagnostics.getDiagnostics().get(0);
                List<String> errors = new ArrayList<>();
                String strCode = "" + diagnostic.getCode();
                String strKind = "" + diagnostic.getKind();
                String strPosition = "" + diagnostic.getPosition();
                String strStartPosition = "" + diagnostic.getStartPosition();
                String strEndPosition = "" + diagnostic.getEndPosition();
                String strSource = "" + diagnostic.getSource();
                String strMessage = "" + diagnostic.getMessage(null);
                String strErrorJavaLine = null;
                if (diagnostic.getSource() != null) {
                    Entry<String, String> javaFile = null;
                    for (Entry<String, String> entry : javaFiles.entrySet()) {
                        String filePath = entry.getKey();
                        String uriString = uriFromFilePath(filePath).toString();
                        if (diagnostic.getSource().toString().startsWith(uriString)) {
                            javaFile = entry;
                            break;
                        }
                    }
                    if (javaFile != null) {
                        String javaString = javaFile.getValue();
                        BufferedReader br = new BufferedReader(new StringReader(javaString));
                        List<String> javaLines = new ArrayList<>();
                        String javaLine;
                        while ((javaLine = br.readLine()) != null) {
                            javaLines.add(javaLine);
                        }
                        if (Diagnostic.NOPOS != diagnostic.getLineNumber()) {
                            strErrorJavaLine = javaLines.get(
                                Long.valueOf(diagnostic.getLineNumber()).intValue() - 1
                            );
                        }
                    }
                }
                errors.add("---------------------------- error-block ----------------------------");
                errors.add(strCode);
                errors.add(strKind);
                errors.add(strPosition);
                errors.add(strStartPosition);
                errors.add(strEndPosition);
                errors.add(strSource);
                errors.add(strMessage);
                errors.add(strErrorJavaLine);
                errors.add("---------------------------------------------------------------------");
                throw new IllegalStateException(
                    errors.stream().filter(Objects::nonNull).collect(Collectors.joining(System.lineSeparator()))
                );
            }
            return byteArrayMap.entrySet().stream().map(
                entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().toByteArray())
            ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    default URI uriFromFilePath(String filePath) {
        String step1 = filePath.replace(File.separatorChar, '/');
        if (!step1.startsWith("/")) {
            step1 = "/" + step1;
        }
        String step2 = step1.split("\\.")[0] + Kind.SOURCE.extension;
        return URI.create("string://" + step2);
    }

    default URI uriFromClassName(String className) {
        return URI.create(className.replace('.', '/'));
    }

    default JavaFileObject toJavaFileObject(String filePath, String content) {
        return new SimpleJavaFileObject(
            uriFromFilePath(filePath),
            Kind.SOURCE
        ) {

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return content;
            }

        };
    }

    default JavaFileObject toJavaFileObject(String className, OutputStream outputStream) {
        return new SimpleJavaFileObject(uriFromClassName(className), Kind.CLASS) {

            @Override
            public OutputStream openOutputStream() {
                return outputStream;
            }

        };
    }

    default List<String> getOptionList() {
        /*List<String> optionList = new ArrayList<>();
        String pathTemplatesJar = Preference.getInstance().getString(Key.SLICE_TEMPLATE_GENERATOR_JAR_PATH);
        String pathAntlrJar = Preference.getInstance().getString(Key.ANTLR_RUNTIME_JAR_PATH);
        optionList.addAll(Arrays.asList("-classpath", pathAntlrJar + File.pathSeparator + pathTemplatesJar));*/
        return Collections.emptyList();
    }
}
