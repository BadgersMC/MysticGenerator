package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.Snow;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.version.OneTwentyBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class CherryGroveHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SNOWY_TAIGA;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.GLACIERBORN_LAND;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data) {
        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);

        // Check if the block is a valid surface for snow
        if (block.getType() == Material.GRASS_BLOCK || block.getType() == Material.DIRT || block.getType() == Material.STONE) {
            // Only place snow in areas that are not under tree cover
            if (!isUnderTree(block)) {
                // Randomly decide how much snow should pile up (1 to 3 layers)
                int snowLayers = GenUtils.randInt(random, 1, 3);

                // Set snow layers based on determined height
                if (block.getUp().getType() == Material.AIR) {
                    block.getUp().setType(Material.SNOW);
                    block.getUp().setBlockData(Material.SNOW.createBlockData(snowData -> ((Snow) snowData).setLayers(snowLayers)));
                }
            }
        }

        // The original grass/plant population logic can stay as is:
        if (block.getType() == Material.GRASS_BLOCK) {
            if (GenUtils.chance(random, 2, 10)) { // Grass
                if (GenUtils.chance(random, 8, 10)) {
                    // Pink petals or tall grass generation
                    if (Version.isAtLeast(20) && TConfig.arePlantsEnabled() && GenUtils.chance(random, 6, 10)) {
                        data.setBlockData(
                                rawX,
                                surfaceY + 1,
                                rawZ,
                                OneTwentyBlockHandler.getPinkPetalData(GenUtils.randInt(1, 4))
                        );
                    } else {
                        PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                } else {
                    if (GenUtils.chance(random, 3, 10)) {
                        PlantBuilder.ALLIUM.build(data, rawX, surfaceY + 1, rawZ);
                    } else {
                        PlantBuilder.PEONY.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    // Helper method to check if a block is under tree coverage (leaves or logs above)
    private boolean isUnderTree(SimpleBlock block) {
        for (int i = 1; i <= 5; i++) {  // Check 5 blocks above for leaves or logs
            Material above = block.getRelative(0, i, 0).getType();
            if (above == Material.DARK_OAK_LEAVES || above == Material.DARK_OAK_LOG) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data) {

        // Large trees, rocks, and other big features
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);

        for (SimpleLocation sLoc : trees) {

            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(treeY);

            if (tw.getBiomeBank(sLoc.getX(), sLoc.getZ()) == BiomeBank.GLACIERBORN_LAND
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))) {

                switch (random.nextInt(35)) { // Adjusted switch case for more options

                    case 34, 33, 32 -> // Rock (3/35)
                            new SphereBuilder(random,
                                    new SimpleBlock(data, sLoc),
                                    Material.COBBLESTONE,
                                    Material.STONE,
                                    Material.STONE,
                                    Material.STONE,
                                    Material.MOSSY_COBBLESTONE
                            ).setRadius(GenUtils.randInt(random, 3, 5)).setRY(GenUtils.randInt(random, 6, 10)).build();

                    case 31, 30 -> // TAIGA_BIG (2/35)
                            new FractalTreeBuilder(FractalTypes.Tree.TAIGA_BIG).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());

                    case 29, 28 -> // TAIGA_SMALL (2/35)
                            new FractalTreeBuilder(FractalTypes.Tree.TAIGA_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());

                    case 27, 26 -> // FROZEN_TREE_BIG (2/35)
                            new FractalTreeBuilder(FractalTypes.Tree.FROZEN_TREE_BIG).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());

                    case 25, 24, 23, 22 -> { // Cherry trees (4/35)
                        if (random.nextBoolean()) {  // Small cherry tree
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                        } else {  // Large cherry tree
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_THICK).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                        }

                        // Optional spore blossom generation if not 1.20
                        if (!Version.isAtLeast(20)) {
                            for (int rX = sLoc.getX() - 6; rX <= sLoc.getX() + 6; rX++) {
                                for (int rZ = sLoc.getZ() - 6; rZ <= sLoc.getZ() + 6; rZ++) {
                                    Wall ceil = new Wall(new SimpleBlock(data, rX, sLoc.getY(), rZ)).findCeiling(15);
                                    if (ceil != null && GenUtils.chance(random, 1, 30)) {
                                        if (ceil.getType() == Material.DARK_OAK_LEAVES) {
                                            PlantBuilder.SPORE_BLOSSOM.build(ceil.getDown());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    default -> // Default case: More Cherry Trees (rest of cases)
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                }
            }
        }
    }

    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.CHERRY_GROVE_BEACH;
    }

    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.CHERRY_GROVE_RIVER;
    }

}
