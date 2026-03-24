package de.cristelknight.cristellib.config.client.structure;

import de.cristelknight.cristellib.config.structure.ed.EDConfig;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public record ClientEDConfig(Map<String, BooleanListEntry> structures) {

    public EDConfig toED() {
        return new EDConfig(structures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
    }

}
