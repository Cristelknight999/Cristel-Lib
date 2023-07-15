package net.cristellib.api;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.config.ConfigType;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

@CristelPlugin
public class BuiltInAPI implements CristelLibAPI {
    public static StructureConfig MINECRAFT_ED = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftED", ConfigType.ENABLE_DISABLE);
    public static StructureConfig MINECRAFT_P = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftP", ConfigType.PLACEMENT);

    @Override
    public void registerConfigs(Set<StructureConfig> sets) {
        sets.add(MINECRAFT_ED);
        sets.add(MINECRAFT_P);


        MINECRAFT_ED.setHeader("""
                    This config file makes it possible to switch off any Minecraft structure.
                    To disable a structure, simply set the value of that structure to "false".
                    To change the rarity of a structure category, use the other file in the config.
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
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "ancient_cities"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "buried_treasures"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "desert_pyramids"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "end_cities"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "igloos"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "jungle_temples"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "mineshafts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "nether_complexes"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "nether_fossils"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "ocean_monuments"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "ocean_ruins"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "pillager_outposts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "ruined_portals"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "shipwrecks"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "swamp_huts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "villages"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "woodland_mansions"), MINECRAFT_ED, MINECRAFT_P);

        //No support yet for minecraft:concentric_rings (only for minecraft:random_spread)
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "strongholds"), MINECRAFT_ED);
    }
}
