package de.cristelknight.cristellib.api;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

@CristelPlugin
public class BuiltInAPI implements CristelLibAPI {
    public static final StructureConfig MINECRAFT_ED = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftED", ConfigType.ENABLE_DISABLE);
    public static final StructureConfig MINECRAFT_P = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftP", ConfigType.PLACEMENT);

    @Override
    public void registerConfigs(Set<StructureConfig> sets) {
        sets.add(MINECRAFT_ED);
        sets.add(MINECRAFT_P);


        MINECRAFT_ED.setHeader("""
                    This config file makes it possible to switch off any Minecraft structure.
                    To disable a structure, simply set the value of that structure to "false".
                    To change the rarity of a structure category, use the other file in the folder.
                """);
        MINECRAFT_P.setHeader("""
            This config file makes it possible to change the spacing, separation, salt (and frequency) of Minecraft's structure sets.
            	SPACING ---  controls how far a structure can be from others of its kind
            	SEPARATION --- controls how close to each other two structures of the same type can be.
            KEEP IN MIND THAT SPACING ALWAYS NEEDS TO BE HIGHER THAN SEPARATION.
            """);
    }

    @Override
    public void registerStructureSets(CristelLibRegistry registry) {
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace("ancient_cities"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "buried_treasures"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "desert_pyramids"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "end_cities"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "igloos"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "jungle_temples"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "mineshafts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "nether_complexes"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "nether_fossils"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "ocean_monuments"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "ocean_ruins"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "pillager_outposts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "ruined_portals"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "shipwrecks"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "swamp_huts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "villages"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "woodland_mansions"), MINECRAFT_ED, MINECRAFT_P);

        //No support yet for minecraft:concentric_rings (only for minecraft:random_spread)
        registry.registerSetToConfig("minecraft", ResourceLocation.withDefaultNamespace( "strongholds"), MINECRAFT_ED);
    }
}
