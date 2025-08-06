package de.cristelknight.cristellib.builtinpacks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.CommentedConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record BuiltInPackConfig(List<String> defaultPacks, List<String> disabledPacks) implements CommentedConfig<BuiltInPackConfig> {

    private static BuiltInPackConfig INSTANCE = null;

    public static final BuiltInPackConfig DEFAULT = new BuiltInPackConfig(BuiltInDataPackLoader.getIDs(), List.of());

    public static final Codec<BuiltInPackConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.list(Codec.STRING).fieldOf("defaultPacks").forGetter(config -> config.defaultPacks),
                    Codec.list(Codec.STRING).fieldOf("disabledPacks").forGetter(config -> config.disabledPacks)
            ).apply(builder, BuiltInPackConfig::new)
    );

    public static void updateConfig() {
        BuiltInPackConfig config = BuiltInPackConfig.DEFAULT.getConfig();

        List<String> modifiableList1 = new ArrayList<>(config.defaultPacks());
        List<String> modifiableList2 = new ArrayList<>(config.disabledPacks());

        boolean bl = false;
        // Remove elements not in the default list
        if(modifiableList1.retainAll(DEFAULT.defaultPacks())) bl = true;
        if(modifiableList2.retainAll(DEFAULT.defaultPacks())) bl = true;

        // Add missing elements from the default list only if both lists miss them
        for (String item : DEFAULT.defaultPacks()) {
            if (!modifiableList1.contains(item) && !modifiableList2.contains(item)) {
                modifiableList1.add(item);
                bl = true;
            }
        }

        if(bl){
            config.setInstance(new BuiltInPackConfig(modifiableList1, modifiableList2));
            config.getConfig(false, true);
        }
    }

    @Override
    public String getSubPath() {
        return CristelLib.MOD_ID + "/built_in_packs";
    }

    @Override
    public BuiltInPackConfig getInstance() {
        return INSTANCE;
    }

    @Override
    public BuiltInPackConfig getDefault() {
        return DEFAULT;
    }

    @Override
    public Codec<BuiltInPackConfig> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable HashMap<String, String> getComments() {
        return null;
    }

    @Override
    public @NotNull String getHeader() {
        return """
                This config file makes it possible to disable built-in packs supplied by Cristel Lib.
                To disable a pack move it from the "defaultPacks" list to the "disabledPacks" list.
                """;
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public void setInstance(BuiltInPackConfig instance) {
        INSTANCE = instance;
    }
}
