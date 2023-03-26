package net.cristellib.forge.api;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.api.CristelLibAPI;
import net.cristellib.config.ConfigType;
import net.cristellib.forge.extraapiutil.CristelPlugin;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
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
            This config file makes it possible to change the spacing, separation, salt (and frequency) of Towns and Towers' structure sets. Big thanks to Cristelknight for making this all possible.
            	SPACING ---  controls how far a structure can be from others of its kind
            	SEPARATION --- controls how close to each other two structures of the same type can be.
            By default, the values should be 48/24 for villages and outposts (spacing/separation respectively) - a hypothetical village is going to appear not further than 48 chunks away (ca. 760 blocks), but not closer than 24 chunks (ca. 380 blocks).
                   * If you want a structure to spawn more frequently, decrease those values.
                   * If you want a structure to spawn less frequently, increase those values.
            KEEP IN MIND THAT SPACING ALWAYS NEEDS TO BE HIGHER THAN SEPARATION.
                        
            LINKS
            =====
            Curseforge link: https://www.curseforge.com/minecraft/mc-mods/towns-and-towers
            Modrinth link: https://modrinth.com/mod/towns-and-towers
            PMC link: https://www.planetminecraft.com/data-pack/towns-amp-towers-structure-overhaul/
            GitHub Repository: [NEED TO MAKE ONE]
            """);
        HashMap<String, String> comments = Util.make(new HashMap<>(), map -> {
                    map.put("towers", """
                    Here you can find all outposts.""");
                    map.put("towns", """        
                    Here you can find all villages.""");
                    map.put("other", """        
                   Here you can find all structures that aren't villages or outposts.""");
                });
        MINECRAFT_ED.setComments(comments);
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
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "strongholds"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "swamp_huts"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "villages"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("minecraft", new ResourceLocation("minecraft", "woodland_mansions"), MINECRAFT_ED, MINECRAFT_P);
    }
}
