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
import org.terraform.tree.FractalLeaves;
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
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))) {
                // Spawn the custom Spooky Tree
                NewFractalTreeBuilder treeBuilder = new NewFractalTreeBuilder(FractalTypes.Tree.SPOOKY_TREE);

                // Actually generate the tree!
                treeBuilder.build(tw, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));

                // Optionally place spooky vines underneath the leaf blocks
                if (GenUtils.chance(random, 1, 3)) { // Increase chance to spawn vines, 1 in 3 for more vines
                    treeBuilder.getFractalLeaves()
                               .setSpookyVineBuilder(new SpookyVineBuilder().setVineMaterials(
                                                                                    Material.DARK_OAK_FENCE, Material.SPRUCE_FENCE, Material.OAK_FENCE)
                                                                            .setVineLengthRange(6, 12)
                                                                            .setDecorationChance(0.05, 0.02)) // Reduce decoration chance
                               .buildSpookyVines(random);
                }

                // Replace blocks around the tree trunk with mixed materials
                replaceBlocksAroundTree(data, sLoc, random);

                // Generate small warped wart trees beneath the canopy
                generateWarpedWartTrees(tw, random, data, sLoc);
            }
        }

        // Generate Warped Nylium patches after all the trees have been placed
        generateWarpedNyliumPatches(tw, random, data);
    }

    private void generateWarpedWartTrees(TerraformWorld tw, Random random, PopulatorDataAbstract data, SimpleLocation sLoc) {
        int numSmallTrees = GenUtils.randInt(random, 3, 5); // Random number of small trees to spawn

        for (int i = 0; i < numSmallTrees; i++) {
            // Find a random position near the base of the spooky tree
            int offsetX = GenUtils.randInt(random, -5, 5); // Random horizontal offset
            int offsetZ = GenUtils.randInt(random, -5, 5); // Random horizontal offset
            int x = sLoc.getX() + offsetX;
            int z = sLoc.getZ() + offsetZ;
            int y = GenUtils.getHighestGround(data, x, z);

            // Ensure the block is dirt-like and has air above it for tree placement
            if (BlockUtils.isDirtLike(data.getType(x, y, z)) && isAirAbove(data, x, y, z)) {
                // Generate the trunk (warped stems)
                int trunkHeight = GenUtils.randInt(random, 6, 9); // Random trunk height between 6 and 9 blocks
                for (int t = 0; t < trunkHeight; t++) {
                    SimpleBlock trunkBlock = new SimpleBlock(data, x, y + t, z);
                    if (trunkBlock.getType() == Material.AIR || BlockUtils.replacableByTrees.contains(trunkBlock.getType())) {
                        trunkBlock.setType(Material.WARPED_STEM); // Place trunk (stem)
                    }
                }

                // Now build the canopy at the correct height, above the trunk
                FractalLeaves leavesBuilder = new FractalLeaves()
                        .setMaterial(Material.WARPED_WART_BLOCK)
                        .setMinWartHeight(6) // Set the min height to 6 blocks
                        .setMaxWartHeight(9) // Set the max height to 9 blocks
                        .setWartDensity(0.7f) // Adjust density of warped wart blocks
                        .setCanopyRadius(3); // Small canopy radius for small tree

                // Place the canopy above the top of the trunk
                int canopyBaseHeight = y + trunkHeight; // Base height of the canopy should be trunkHeight above ground
                leavesBuilder.placeSmallWarpedWartBlocks(tw, canopyBaseHeight, new SimpleBlock(data, x, canopyBaseHeight, z), 0.1f);
            }
        }
    }




    private void replaceBlocksAroundTree(@NotNull PopulatorDataAbstract data, @NotNull SimpleLocation treeLoc, @NotNull Random random) {
        int radius = GenUtils.randInt(random, 8, 15); // Randomize radius for variation
        SimpleBlock center = new SimpleBlock(data, treeLoc.getX(), treeLoc.getY(), treeLoc.getZ());

        // Materials to be mixed around the tree
        Material[] groundMaterials = {
                Material.PODZOL, Material.COARSE_DIRT, Material.DIRT_PATH, Material.BLUE_TERRACOTTA, Material.MUD
        };

        int materialIndex = 0;  // Start cycling through materials

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                SimpleBlock target = center.getRelative(dx, 0, dz);

                // Only replace dirt-like blocks
                if (BlockUtils.isDirtLike(target.getType())) {

                    // Select the current material from the list
                    Material replacementMaterial = groundMaterials[materialIndex % groundMaterials.length];
                    target.setType(replacementMaterial);

                    // Randomly skip 2 to 3 materials to add variation
                    materialIndex += GenUtils.randInt(random, 2, 3);
                }
            }
        }
    }

    private void generateWarpedNyliumPatches(@NotNull TerraformWorld tw, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        // Define the cluster size and spread of the Warped Nylium patches
        int clusterCount = GenUtils.randInt(random, 2, 3); // Number of patches to generate
        int patchRadius = GenUtils.randInt(random, 3, 5);  // Radius of each patch

        // Loop to generate a certain number of clusters in the chunk
        for (int i = 0; i < clusterCount; i++) {
            // Get random surface coordinates for the center of the patch
            int[] surfaceCoords = GenUtils.randomSurfaceCoordinates(random, data);
            SimpleLocation patchCenter = new SimpleLocation(
                    surfaceCoords[0],
                    GenUtils.getHighestGround(data, surfaceCoords[0], surfaceCoords[2]),
                    surfaceCoords[2]
            );

            // Get Y position for the patch center
            int patchY = patchCenter.getY();

            // Create the Warped Nylium patch
            for (int dx = -patchRadius; dx <= patchRadius; dx++) {
                for (int dz = -patchRadius; dz <= patchRadius; dz++) {
                    SimpleBlock target = new SimpleBlock(
                            data,
                            patchCenter.getX() + dx,
                            patchY,
                            patchCenter.getZ() + dz
                    );

                    // Only replace dirt-like or grass-like blocks
                    if (BlockUtils.isDirtLike(target.getType()) || target.getType() == Material.GRASS_BLOCK) {
                        // Check to ensure patch forms a circle-like shape
                        if (Math.sqrt(dx * dx + dz * dz) <= patchRadius) {
                            target.setType(Material.WARPED_NYLIUM);  // Set Warped Nylium block
                        }
                    }
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
