package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CherryGroveRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.FROZEN_RIVER; // Set to Frozen River biome to ensure freezing behavior
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(@NotNull TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data) {
        boolean growsKelp = world.getHashedRand(rawX >> 4, rawZ >> 4, 14979813).nextBoolean();

        if (surfaceY >= TerraformGenerator.seaLevel) {
            return;
        }

        // Ice generation
        if (!data.getType(rawX, TerraformGenerator.seaLevel, rawZ).isSolid()) {
            data.setType(rawX, TerraformGenerator.seaLevel, rawZ, Material.ICE); // Freeze water at sea level
        }

        // Set the ground near sea level to gravel
        if (surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        } else if (surfaceY >= TerraformGenerator.seaLevel - 4) {
            if (random.nextBoolean()) {
                data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
            }
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) {
            return;
        }

        // Add river vegetation
        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay deposits
        if (GenUtils.chance(random, TConfig.c.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND, 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // You can implement large structures or trees here if needed
    }
}
