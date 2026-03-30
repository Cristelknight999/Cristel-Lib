package de.cristelknight.cristellib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.structure.ReadStructureSets;
import de.cristelknight.cristellib.config.structure.ed.ToggleConfig;
import de.cristelknight.cristellib.config.structure.placement.PlacementConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import de.cristelknight.cristellib.util.JsonHelper;
import de.cristelknight.cristellib.util.runtimepack.RuntimePackUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StructureConfigPlacement extends StructureConfig {

    // default values
    private final Supplier<Map<Identifier, List<Identifier>>> structuresForED;

    // current values (structure_set + Config)
    private Map<Identifier, ToggleConfig> enableDisableConfig = null;


    StructureConfigPlacement(Path path, String header, Map<String, String> comments, ConfigType type, List<StructureSetData> structureSetHolders) {
        super.this(path, header, comments, type, structureSetHolders);
        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
    }

    private boolean updatePlacementsInSet(JsonObject structureSet, Identifier setLocation) {
        PlacementConfig placementConfig = this.placementConfig.get(setLocation);
        if(getDefaultStructurePlacement().get(setLocation).equals(placementConfig))
            return false;

        JsonObject p = structureSet.get("placement").getAsJsonObject();

        p.addProperty("salt", placementConfig.salt());
        p.addProperty("spacing", placementConfig.spacing());
        p.addProperty("separation", placementConfig.separation());

        double newF = placementConfig.frequency();

        if ((p.has("frequency") || newF != 1.0) && (!p.has("frequency") || newF != p.get("frequency").getAsDouble()))
            p.addProperty("frequency", newF);

        return true;
    }

    @Override
    public boolean add(String modId, Identifier setLocation) {
        return false;
    }

    private JsonElement getStructureSet(Identifier location, String modId) {
        Identifier structureLocation = RuntimePackUtil.getLocationForStructureSet(location);
        if (CristelLib.CONFIG_PACK.hasData(structureLocation)) {
            return CristelLib.CONFIG_PACK.getResourceAsJson(PackType.SERVER_DATA, structureLocation);
        }
        return JsonHelper.getSetElement(location, modId);
    }

    @Override
    public void readConfig(boolean override) {

    }

    public Map<Identifier, List<Identifier>> getDefaultStructures() {
        return structuresForED.get();
    }

    public Map<Identifier, ToggleConfig> getEnableDisableConfig() {
        return enableDisableConfig;
    }

    public void updateEDConfig(Identifier key, ToggleConfig config) {
        this.enableDisableConfig.put(key, config);
    }
}