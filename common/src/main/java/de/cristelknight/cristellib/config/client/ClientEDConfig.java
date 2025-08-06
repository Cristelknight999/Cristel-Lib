package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.config.serialize.ed.EDConfig;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;

import java.util.Map;
import java.util.stream.Collectors;

public record ClientEDConfig(Map<String, BooleanListEntry> structures) {

    public EDConfig toED() {
        return new EDConfig(structures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
    }

}
