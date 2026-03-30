package de.cristelknight.cristellib.config.client.structure;

import de.cristelknight.cristellib.config.structure.toggle.ToggleConfig;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;

import java.util.Map;
import java.util.stream.Collectors;

public record ClientEDConfig(Map<String, BooleanListEntry> structures) {

    public ToggleConfig toED() {
        return new ToggleConfig(structures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
    }

}
