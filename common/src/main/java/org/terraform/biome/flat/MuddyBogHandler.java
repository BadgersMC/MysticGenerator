package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class MuddyBogHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.BOGWALKER_LAND;
    }

    // Beach type. This will be used instead if the height is too close to sea level.
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.BOG_BEACH;
    }

    // River type. This will be used instead if the heightmap got carved into a river.
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.BOG_RIVER;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[]{
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data) {
        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);
        if (block.getUp().getType() == Material.AIR && block.getType() == Material.GRASS_BLOCK) {
            // Sunflowers
            if (GenUtils.chance(random, 1, 100)) {
                PlantBuilder.SUNFLOWER.build(block.getUp());
            }
            // Sugarcane with random heights between 3 and 5
            else if (GenUtils.chance(random, 1, 50)) {
                PlantBuilder.SUGAR_CANE.build(block.getUp(), random, 3, 5);
            }
            // Lilypads in water
            else if (block.getType() == Material.WATER && GenUtils.chance(random, 1, 50)) {
                block.getUp().setType(Material.LILY_PAD);
            }
            // Other vegetation
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.GRASS.build(block.getUp());
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data) {
        // Generating various trees and features
        SimpleLocation[] features = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 18);

        for (SimpleLocation sLoc : features) {
            int featureY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(featureY);

            if (isRightBiome(tw.getBiomeBank(sLoc.getX(), sLoc.getZ()))
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))) {

                // Random chance for each large item
                switch (random.nextInt(25)) {
                    case 24, 23 -> // AZALEA_TOP (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.AZALEA_TOP).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    case 22, 21 -> // SWAMP_TOP (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.SWAMP_TOP).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    case 20, 19 -> // GIANT_PUMPKIN (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.GIANT_PUMPKIN).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    case 18, 17 -> // ANDESITE_PETRIFIED_SMALL (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.ANDESITE_PETRIFIED_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    case 16, 15 -> // GRANITE_PETRIFIED_SMALL (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.GRANITE_PETRIFIED_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    case 14, 13 -> // DIORITE_PETRIFIED_SMALL (2/25)
                            new FractalTreeBuilder(FractalTypes.Tree.DIORITE_PETRIFIED_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                    default -> { // Fewer mushrooms (11/25 chance now)
                        if (GenUtils.chance(random, 1, 5)) {
                            new MushroomBuilder(FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                        }
                    }
                }

                // Replace some of the ground with Podzol after tree generation
                TaigaHandler.replacePodzol(tw.getHashedRand(sLoc.getX(), sLoc.getY(), sLoc.getZ()).nextInt(9999),
                        7f,
                        new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));
            }
        }
    }

    private boolean isRightBiome(BiomeBank bank) {
        return bank == BiomeBank.BOGWALKER_LAND || bank == BiomeBank.BOG_BEACH;
    }

    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {

        double height = super.calculateHeight(tw, x, z) - 5;

        FastNoise sinkin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUDDYBOG_HEIGHTMAP, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(4);
            n.SetFrequency(0.08f);
            return n;
        });

        if (sinkin.GetNoise(x, z) < -0.15) {
            if (height > TerraformGenerator.seaLevel) {
                height -= (height - TerraformGenerator.seaLevel) + 2;
            }
        }

        return height;
    }
}
