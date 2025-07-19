package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;

public record ClientPlacementConfig(IntegerListEntry spacing, IntegerListEntry separation, DoubleListEntry frequency, IntegerListEntry salt) implements ClientConfig {

    public PlacementConfig toPlacement(){
        return new PlacementConfig(spacing.getValue(), separation.getValue(), frequency.getValue(), salt.getValue());
    }
}
