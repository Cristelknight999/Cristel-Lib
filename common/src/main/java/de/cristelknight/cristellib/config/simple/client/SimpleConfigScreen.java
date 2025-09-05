package de.cristelknight.cristellib.config.simple.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record SimpleConfigScreen(Class<?> simpleConfig, String screenName, Runnable onScreenSave) {
}
