package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.NewFractalTreeBuilder;
import org.terraform.tree.SpookyVineBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SpookyForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.DARK_FOREST;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.SPOOKY_FOREST;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        // Surface layers for the spooky forest
        return new Material[]{
                Material.GRASS_BLOCK, // Custom grass on surface
                Material.DIRT,        // Dirt below grass
                Material.COARSE_DIRT,  // Deeper layer for texture variation
                GenUtils.randChoice(rand, Material.DIRT, Material.GRAVEL),
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
                PlantBuilder.WEEPING_VINES.build(block.getUp());
            }
            // Sugarcane with random heights between 3 and 5
            else if (GenUtils.chance(random, 1, 50)) {
                PlantBuilder.TWISTING_VINES.build(block.getUp(), random, 3, 13);
            }
            // Lilypads in water
            else if (GenUtils.chance(random, 1, 75)) {
                PlantBuilder.CRIMSON_ROOTS.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 75)) {
                PlantBuilder.WARPED_FUNGUS.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 50)) {
                PlantBuilder.WARPED_ROOTS.build(block.getUp());
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

        // Generate large spooky trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);

        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(treeY);

            if (tw.getBiomeBank(sLoc.getX(), sLoc.getZ()) == BiomeBank.SPOOKY_FOREST
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
            {

                // Spawn the custom Spooky Tree
                NewFractalTreeBuilder treeBuilder = new NewFractalTreeBuilder(FractalTypes.Tree.SPOOKY_TREE);

                // Actually generate the tree!
                treeBuilder.build(tw, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));

                // Optionally place spooky vines underneath the leaf blocks
                // Optionally place spooky vines underneath the leaf blocks
                if (GenUtils.chance(random, 1, 3)) { // Increase chance to spawn vines, 1 in 3 for more vines
                    treeBuilder.getFractalLeaves()
                               .setSpookyVineBuilder(new SpookyVineBuilder().setVineMaterials(
                                                                                    Material.DARK_OAK_FENCE, Material.OAK_FENCE, Material.SPRUCE_FENCE)
                                                                            .setVineLengthRange(6, 12)
                                                                            .setDecorationChance(0.05, 0.02)) // Reduce decoration chance
                               .buildSpookyVines(random);
                }
            }
        }

        // Add smaller spooky forest trees or decorations
        SimpleLocation[] decorations = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);

        for (SimpleLocation sLoc : decorations) {
            int decorationY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(decorationY);

            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))) {

                // Add Amethyst Clusters, Warped Fungus, or other decorations
                if (GenUtils.chance(random, 1, 5)) {
                    data.setType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ(), Material.LARGE_AMETHYST_BUD);
                } else if (GenUtils.chance(random, 1, 10)) {
                    data.setType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ(), Material.WARPED_FUNGUS);
                } else if (GenUtils.chance(random, 1, 8)) {
                    data.setType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ(), Material.ALLIUM);
                } else {
                    // Spawn smaller spooky trees
                    new NewFractalTreeBuilder(FractalTypes.Tree.SPOOKY_TREE)
                            .setOriginalTrunkLength(4) // Shorter tree
                            .build(tw, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));
                }

                // Place twisting vines growing from the ground
                if (isAirAbove(data, sLoc.getX(), sLoc.getY(), sLoc.getZ())) {
                    data.setType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ(), Material.TWISTING_VINES);
                }
            }
        }
    }

    // Helper method to check if air is above a block
    private boolean isAirAbove(PopulatorDataAbstract data, int x, int y, int z) {
        return data.getType(x, y + 1, z) == Material.AIR;
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.SPOOKY_BEACH;
    }

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.SPOOKY_FOREST_RIVER;
    }
}
