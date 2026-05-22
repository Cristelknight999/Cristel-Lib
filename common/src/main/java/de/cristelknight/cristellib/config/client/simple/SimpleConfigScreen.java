package de.cristelknight.cristellib.config.client.simple;

public record SimpleConfigScreen(Class<?> simpleConfig, String screenName, Runnable onScreenSave) {
}
