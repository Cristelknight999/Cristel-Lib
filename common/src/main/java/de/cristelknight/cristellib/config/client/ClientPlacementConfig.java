package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;

public record ClientPlacementConfig(DoubleListEntry frequency, IntegerListEntry salt,  IntegerListEntry separation, IntegerListEntry spacing) {

    public PlacementConfig toPlacement(){
        return new PlacementConfig(frequency.getValue(), salt.getValue(), separation.getValue(), spacing.getValue());
    }
}
