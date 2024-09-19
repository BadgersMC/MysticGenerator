package org.terraform.structure.villagehouse;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.villagehouse.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.villagehouse.farmhouse.FarmhousePopulator;
import org.terraform.structure.villagehouse.mountainhouse.MountainhousePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class VillageHousePopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(2291282, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(
                tw.getHashedRand(chunkX, chunkZ, 12422),
                (int) (TConfig.c.STRUCTURES_VILLAGEHOUSE_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {

            // Ensure the biome is one of the custom biomes
            if (biome != BiomeBank.BOGWALKER_LAND && biome != BiomeBank.GLACIERBORN_LAND && biome != BiomeBank.LEAFSTRIDER_LAND) {
                return false;
            }

            // If it is below sea level, DON'T SPAWN IT.
            if (HeightMap.getBlockHeight(tw, coords[0], coords[1]) > TerraformGenerator.seaLevel) {
                return rollSpawnRatio(tw, chunkX, chunkZ); // Control spawn chance
            }
        }
        return false;
    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        // Get the biome from the center of the MegaChunk
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();

        // Populate based on the custom biomes
        if (biome == BiomeBank.BOGWALKER_LAND) {
            if (!TConfig.c.STRUCTURES_ANIMALFARM_ENABLED) {
                return;
            }
            new AnimalFarmPopulator().populate(tw, data);

        } else if (biome == BiomeBank.GLACIERBORN_LAND) {
            if (!TConfig.c.STRUCTURES_FARMHOUSE_ENABLED) {
                return;
            }
            new FarmhousePopulator().populate(tw, data);

        } else if (biome == BiomeBank.LEAFSTRIDER_LAND) {
            if (!TConfig.c.STRUCTURES_MOUNTAINHOUSE_ENABLED) {
                return;
            }
            new MountainhousePopulator().populate(tw, data);
        }
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && (
                BiomeBank.isBiomeEnabled(BiomeBank.BOGWALKER_LAND)
                || BiomeBank.isBiomeEnabled(BiomeBank.GLACIERBORN_LAND)
                || BiomeBank.isBiomeEnabled(BiomeBank.LEAFSTRIDER_LAND)
        ) && (
                       TConfig.c.STRUCTURES_ANIMALFARM_ENABLED
                       || TConfig.c.STRUCTURES_FARMHOUSE_ENABLED
                       || TConfig.c.STRUCTURES_MOUNTAINHOUSE_ENABLED
               );
    }
}
