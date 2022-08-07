package ch.wipfli.microstreamclientplus.web;

import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

public class Database {

    private static EmbeddedStorageManager storageManager;

    public Database() {
    }

    public static void initialize(String classDefenition, String path) {

        if (storageManager != null && storageManager.isRunning()) {
            storageManager.shutdown();
            storageManager = null;
        }
        try {
            Class<?> clazz = Class.forName(classDefenition);
            Object root = clazz.newInstance();
            storageManager = EmbeddedStorage.Foundation(Paths.get(path))
                .onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(Thread.currentThread().getContextClassLoader())))
                .start(root);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(Object root) {
        final ObjectMapper mapper = JsonMapper.builder() // or different mapper for other format
            .addModule(new ParameterNamesModule())
            .addModule(new Jdk8Module())
            .addModule(new JavaTimeModule())
            .build();
        try {
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static Long store(Object object) {
        if (storageManager != null) {
            return storageManager.store(object);
        }
        return null;
    }

    public static Object root() {
        if (storageManager != null) {
            final Object root = storageManager.root();
            return root;
        }
        return null;
    }

    public static EmbeddedStorageManager getStorage() {
        if (storageManager != null) {
            return storageManager;
        }
        return null;
    }
}
