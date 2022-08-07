package ch.wipfli.microstreamclientplus.web.services;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import ch.wipfli.microstreamclientplus.web.models.Settings;
import one.microstream.reflect.ClassLoaderProvider;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

@Service
@Singleton
public class SettingsServiceImpl implements SettingsService {

    private EmbeddedStorageManager storageManager;

    @PostConstruct
    public void initialize() {
        final Settings root = new Settings();
        storageManager = EmbeddedStorage.Foundation(FileUtils.getFile(FileUtils.getUserDirectory(), ".microstreamclientplus").toPath())
            .onConnectionFoundation(cf -> cf.setClassLoaderProvider(ClassLoaderProvider.New(Thread.currentThread().getContextClassLoader())))
            .start(root);
    }

    @Override
    public Settings findSettings() {
        return (Settings) storageManager.root();
    }

    @Override
    public void storeSettings(Settings settings) {
        storageManager.store(settings.getImports());
        storageManager.store(settings.getClassPaths());
        storageManager.store(settings);
    }
}
