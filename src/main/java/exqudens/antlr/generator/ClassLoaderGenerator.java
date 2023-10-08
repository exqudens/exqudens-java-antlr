package exqudens.antlr.generator;

import java.util.Map;

public interface ClassLoaderGenerator {

    static ClassLoaderGenerator newInstance() {
        return new ClassLoaderGenerator() {};
    }

    default ClassLoader generateClassLoader(ClassLoader parent, Map<String, byte[]> classFiles) {
        return new ClassLoader(parent) {

            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    return this.getParent().loadClass(name);
                } catch (ClassNotFoundException e) {
                    return findClass(name);
                }
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] bytes = classFiles.get(name);
                if (bytes != null) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
                return super.findClass(name);
            }

        };
    }

}
