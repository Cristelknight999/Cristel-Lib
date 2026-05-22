package de.cristelknight.cristellib.api;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import de.cristelknight.cristellib.config.ConfigType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.List;
import java.util.Set;

@CristelPlugin
public class BuiltInAPI implements CristelLibAPI {
    public static final StructureConfig MINECRAFT_T = StructureConfig.createWithDefaultConfigPath("vanilla_structures", "toggle_structure_config", ConfigType.TOGGLE);
    public static final StructureConfig MINECRAFT_P = StructureConfig.createWithDefaultConfigPath("vanilla_structures", "placement_structure_config", ConfigType.PLACEMENT);

    @Override
    public void registerConfigs(Set<StructureConfig> sets) {
        sets.add(MINECRAFT_T);
        sets.add(MINECRAFT_P);

        MINECRAFT_T.setHeader("""
                This config makes it possible to switch off any Minecraft structure.
                To disable a structure, simply set the value of that structure to "false".
                To change the rarity of a structure category use the structure placement config.
                
                =====
                Created by Cristel Lib
                """);
        MINECRAFT_P.setHeader("""
                This config makes it possible to change the spacing, separation, salt (and frequency) of Minecraft's structure sets.
                    SPACING ---  controls how far a structure can be from others of its kind
                	SEPARATION --- controls how close to each other two structures of the same type can be.
                KEEP IN MIND THAT SPACING ALWAYS NEEDS TO BE HIGHER THAN SEPARATION.
                
                =====
                Created by Cristel Lib
                """);
    }

    @Override
    public void registerStructureSets(CristelLibRegistry registry) {
        registry.registerSetToConfig(Constants.MC_ID, null, List.of(
                        "ancient_cities", "buried_treasures", "desert_pyramids", "end_cities", "igloos", "jungle_temples", "mineshafts", "nether_complexes", "nether_fossils",
                        "ocean_monuments", "ocean_ruins", "pillager_outposts", "ruined_portals", "shipwrecks", "swamp_huts", "trail_ruins", "trial_chambers", "villages", "woodland_mansions"
                ),
                MINECRAFT_T, MINECRAFT_P);


        // No support yet for minecraft:concentric_rings (only for minecraft:random_spread)
        registry.registerSetToConfig(Constants.MC_ID, ResourceLocation.withDefaultNamespace("strongholds"), MINECRAFT_T);
    }

    @Override
    public void registerBuiltInPacks() {
        BuiltInPackLoader.registerPack(CristelLib.CONFIG_PACK, Component.literal("Cristel Lib Config Pack"), () -> true, PackType.SERVER_DATA);
    }

}
