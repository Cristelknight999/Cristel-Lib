package de.cristelknight.cristellib.config;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;

public interface CommentedConfig<T extends Record> {

    String getSubPath();

    T getInstance();

    T getDefault();

    Codec<T> getCodec();

    @Nullable HashMap<String, String> getComments();

    @Nullable String getHeader();

    boolean isSorted();

    void setInstance(T instance);


    default T getConfig() {
        return getConfig(false, false);
    }


    default Path getConfigPath() {
        return CristelLibExpectPlatform.getConfigDirectory().resolve(getSubPath() + ".json5");
    }

    default T getConfig(boolean fromFile, boolean save) {
        if (getInstance() == null || fromFile || save) {
            setInstance(readConfig(save));
        }
        return getInstance();
    }


    default T readConfig(boolean recreate) {
        if (!getConfigPath().toFile().exists() || recreate) {
            createConfig();
        }
        return ConfigManager.readConfig(getConfigPath(), getCodec());
    }

    default void createConfig() {
        T create = getInstance();
        if(create == null) create = getDefault();
        ConfigManager.writeFile(getConfigPath(), getCodec(), getMap(getComments()), create, getCommentedHeader(getHeader()), isSorted());
    }

    private String getCommentedHeader(String header){
        return ConfigManager.createHeader(header);
    }

    private HashMap<String, String> getMap(HashMap<String, String> comments){
        if(comments == null) comments = new HashMap<>();
        return comments;
    }
}
