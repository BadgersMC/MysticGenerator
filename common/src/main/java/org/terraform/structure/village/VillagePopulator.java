package org.terraform.structure.village;

import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.GenUtils;

public class VillagePopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(11111199, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(
                tw.getHashedRand(chunkX, chunkZ, 12422),
                (int) (TConfig.c.STRUCTURES_VILLAGE_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }
        // MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        // int[] coords = mc.getCenterBiomeSectionBlockCoords();// getCoordsFromMegaChunk(tw, mc);
        // If it is below sea level, DON'T SPAWN IT.
        // if (HeightMap.getBlockHeight(tw, coords[0], coords[1]) > TerraformGenerator.seaLevel) {

        // Height no longer checked in the interest of speed.

        if (biome == (BiomeBank.BOGWALKER_LAND)
            || biome == (BiomeBank.GLACIERBORN_LAND)
            || biome == (BiomeBank.LEAFSTRIDER_LAND))
        {

            return rollSpawnRatio(tw, chunkX, chunkZ);
        }
        // }
        return false;
    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        // For now, don't check biomes. There is only plains village.
        //    	EnumSet<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());

        // MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());


        // int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);

        // NO HIGHEST GROUND CHECKS IN POPULATE. If canSpawn is true, the structure MUST spawn.
        // if (GenUtils.getHighestGround(data, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
        //        if (banks.contains(BiomeBank.PLAINS)
        //       		|| banks.contains(BiomeBank.FOREST)
        //       		|| banks.contains(BiomeBank.SAVANNA)
        //       		|| banks.contains(BiomeBank.TAIGA)
        //       		|| banks.contains(BiomeBank.LEAFSTRIDER_LAND)
        //       		|| banks.contains(BiomeBank.GLACIERBORN_LAND)) {

        new PlainsVillagePopulator().populate(tw, data);
        //        }
        // }

    }

    @Override
    public int getChunkBufferDistance() {
        return TConfig.c.STRUCTURES_VILLAGE_CHUNK_EXCLUSION_ZONE;
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled()
               && (BiomeBank.isBiomeEnabled(BiomeBank.LEAFSTRIDER_LAND)
                   || BiomeBank.isBiomeEnabled(BiomeBank.BOGWALKER_LAND)
                   || BiomeBank.isBiomeEnabled(BiomeBank.GLACIERBORN_LAND))
               && TConfig.c.STRUCTURES_PLAINSVILLAGE_ENABLED;
    }
}
