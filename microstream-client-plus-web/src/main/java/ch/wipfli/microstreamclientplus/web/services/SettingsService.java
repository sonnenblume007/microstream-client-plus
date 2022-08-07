package ch.wipfli.microstreamclientplus.web.services;

import ch.wipfli.microstreamclientplus.web.models.Settings;

public interface SettingsService {
    Settings findSettings();

    void storeSettings(Settings settings);
}
