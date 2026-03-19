package de.cristelknight.cristellib.config.client;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.config.client.extension.ConfigScreenExtension;
import de.cristelknight.cristellib.config.client.extension.ExtensionRegistry;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ScreenBuilder {

    private final String modId;

    private final Set<ConfigScreenExtension> extensions = new HashSet<>();

    public ScreenBuilder(String modId) {
        this.modId = modId;
    }

    /**
     * Creates and builds a configuration screen for the specified mod.
     *
     * <p>This method initializes a {@link ConfigBuilder}, sets up its properties,
     * and populates it with config entries for either structure configs,
     * simple configs, or both depending on the provided flags.</p>
     *
     * @param parent    the parent screen to return to when the config screen is closed
     * @param structure if {@code true}, includes structure-related configuration categories
     * @param simple    if {@code true}, includes simple (non-structure) configuration categories
     * @return the fully built {@link Screen} instance representing the mod's configuration screen
     */
    public Screen create(Screen parent, boolean structure, boolean simple) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable("§7" + CristelLibExpectPlatform.getModDisplayName(modId) + " Configuration (via %s§7)", Constants.MOD_COMPONENT));

        builder.setParentScreen(parent);
        builder.setSavingRunnable(this::onConfigSave);

        for(Map.Entry<ExtensionRegistry.ExtensionFactory<?>, Set<String>> entry : ExtensionRegistry.getExtensions().entrySet()){
            if(entry.getValue().isEmpty() || entry.getValue().contains(modId)) {
                ConfigScreenExtension extension = entry.getKey().create(modId);
                extension.addToBuilder(builder, builder.entryBuilder());
                extensions.add(extension);
            }
        }

        //addToBuilder(builder, structure, simple);

        return builder.build();
    }

    // Saving
    private void onConfigSave() {
        extensions.forEach(ConfigScreenExtension::onSave);
    }

    public static Optional<Component[]> tooltip(String name, Map<String, String> comments) {
        String comment = comments.get(name);
        if (comment != null && !comment.isEmpty()) {
            return Optional.of(new MutableComponent[] { Component.literal(comment) });
        }
        return Optional.empty();
    }

    // Get correct screens helpers
    public static Pair<Boolean, Boolean> shouldCreateScreen(String modId, boolean mainStructure) {
        if (modId.equals(Constants.MOD_ID) || modId.equals(Constants.MC_ID)) return new Pair<>(false, false);

        boolean structure = mainStructure &&
                CristelLibRegistry.getConfigs().containsKey(modId) &&
                !ConfigRegistry.get(ACConfig.class).clientExcludedMods().contains(modId);

        return new Pair<>(structure, ClientConfigRegistry.hasScreens(modId));
    }

    public static Set<String> allConfigMods(boolean structure) {
        Set<String> allMods = new HashSet<>(ClientConfigRegistry.getAllConfigsWithScreen().keySet());
        if (structure) allMods.addAll(new HashSet<>(CristelLibRegistry.getConfigs().keySet()));
        return allMods;
    }
}
