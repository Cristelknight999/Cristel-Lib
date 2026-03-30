package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.client.extension.ConfigScreenExtension;
import de.cristelknight.cristellib.config.client.extension.ExtensionRegistry;
import de.cristelknight.cristellib.platform.Services;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;

public class ScreenBuilder {

    private final String modId;

    private final List<ConfigScreenExtension> extensions = new ArrayList<>();

    public ScreenBuilder(String modId) {
        this.modId = modId;
    }

    public Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setSavingRunnable(this::onConfigSave)
                .setTitle(Component.translatable(
                        "§7" + Services.PLATFORM.getModDisplayName(modId) + " Configuration (via %s§7)", Constants.MOD_COMPONENT
                ));

        addToBuilder(builder);

        if (extensions.isEmpty())
            return null;

        return builder.build();
    }

    public void addToBuilder(ConfigBuilder builder) {
        for (Map.Entry<ExtensionRegistry.ExtensionFactory<?>, ExtensionRegistry.LoadPredicate> entry : ExtensionRegistry.getExtensions().entrySet()) {
            if (entry.getValue().test(modId) && !modId.equals(Constants.MC_ID)) {
                ConfigScreenExtension extension = entry.getKey().create(modId);
                extensions.add(extension);
            }
        }
        extensions.sort(Comparator.comparingInt(ConfigScreenExtension::priority).reversed());
        extensions.forEach(e -> e.addToBuilder(builder, builder.entryBuilder()));
    }

    // Saving
    private void onConfigSave() {
        extensions.forEach(ConfigScreenExtension::onSave);
    }

    // helpers
    public static Optional<Component[]> tooltip(String name, Map<String, String> comments) {
        String comment = comments.get(name);
        if (comment != null && !comment.isEmpty()) {
            return Optional.of(new MutableComponent[]{Component.literal(comment)});
        }
        return Optional.empty();
    }

    public static boolean shouldCreateScreen(String modId) {
        if (modId.equals(Constants.MOD_ID) || modId.equals(Constants.MC_ID))
            return false;

        return ExtensionRegistry.getExtensions().values().stream().anyMatch(loadPredicate -> loadPredicate.test(modId));
    }

    public static Set<String> allModsWithScreen() {
        return Services.MOD_LOADING.getModIds().stream()
                .filter(ScreenBuilder::shouldCreateScreen)
                .collect(Collectors.toSet());
    }
}
