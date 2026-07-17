package de.cristelknight.cristellib.config.simple;

import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.FileWriter;
import de.cristelknight.cristellib.platform.Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigHolder<T> {

    private final ConfigSettings<T> spec;
    private volatile T instance;

    public ConfigHolder(ConfigSettings<T> spec) {
        this.spec = spec;
    }

    public T getInstance() {
        T current = instance;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (instance == null) {
                instance = readOrCreate();
            }
            return instance;
        }
    }

    public synchronized void update(T newData) {
        instance = newData;
    }

    public synchronized void updateAndSave(T newData) {
        instance = newData;
        save();
    }

    public synchronized void save() {
        write(instance);
    }

    public ConfigSettings<T> getSettings() {
        return spec;
    }

    private T readOrCreate() {
        Path path = getPath();
        if (!Files.exists(path)) {
            write(spec.getDefault());
        }
        return ConfigManager.readFromJanksonPathWithFix(path, spec.getCodec(), this::write);
    }

    private void write(T data) {
        FileWriter.writeToFile(getPath(), spec.getCodec(), FileWriter.getSafeComments(spec.getComments()), data, ConfigManager.createHeader(spec.getHeader()), spec.isSorted());
    }

    private Path getPath() {
        return Services.PLATFORM.getConfigDirectory().resolve(spec.getSubPath() + ".json5");
    }
}
