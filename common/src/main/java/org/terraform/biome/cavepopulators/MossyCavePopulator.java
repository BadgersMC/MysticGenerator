package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;

import java.util.Random;

public class MossyCavePopulator extends AbstractCavePopulator {

    @Override
    public void populate(TerraformWorld tw,
                         @NotNull Random random,
                         @NotNull SimpleBlock ceil,
                         @NotNull SimpleBlock floor)
    {

        int caveHeight = ceil.getY() - floor.getY();

        // Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
            return;
        }

        // =========================
        // Upper decorations
        // =========================

        // Stalactites
        if (GenUtils.chance(random, 1, 35)) {
            Wall w = new Wall(ceil);
            if (w.getUp().getType() == Material.SAND || w.getUp().getType() == Material.SANDSTONE) {
                new StalactiteBuilder(Material.SANDSTONE_WALL).setSolidBlockType(Material.SANDSTONE)
                                                              .setFacingUp(false)
                                                              .setVerticalSpace(caveHeight)
                                                              .build(random, w);
            }
            else if (w.getUp().getType() == Material.DEEPSLATE) {
                new StalactiteBuilder(Material.COBBLED_DEEPSLATE_WALL).setSolidBlockType(Material.DEEPSLATE)
                                                                      .setFacingUp(false)
                                                                      .setVerticalSpace(caveHeight)
                                                                      .build(random, w);
            }
            else if (BlockUtils.isStoneLike(w.getUp().getType())) {
                new StalactiteBuilder(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL).setSolidBlockType(Material.COBBLESTONE,
                                                                                                         Material.MOSSY_COBBLESTONE
                                                                                                 )
                                                                                                 .setFacingUp(false)
                                                                                                 .setVerticalSpace(
                                                                                                         caveHeight)
                                                                                                 .build(random, w);
            }
        }

        // =========================
        // Lower decorations
        // =========================

        // Stalagmites
        if (GenUtils.chance(random, 1, 35)) {
            int h = caveHeight / 4;
            if (h < 1) {
                h = 1;
            }
            Wall w = new Wall(floor.getUp(), BlockFace.NORTH);
            if (BlockUtils.isAir(w.getType())) {
                if (w.getDown().getType() == Material.SAND || w.getUp().getType() == Material.SANDSTONE) {
                    new StalactiteBuilder(Material.SANDSTONE_WALL).setSolidBlockType(Material.SANDSTONE)
                                                                  .setFacingUp(true)
                                                                  .setVerticalSpace(caveHeight)
                                                                  .build(random, w);
                }
                else if (w.getDown().getType() == Material.DEEPSLATE) {
                    new StalactiteBuilder(Material.COBBLED_DEEPSLATE_WALL).setSolidBlockType(Material.DEEPSLATE)
                                                                          .setFacingUp(true)
                                                                          .setVerticalSpace(caveHeight)
                                                                          .build(random, w);
                }
                else if (BlockUtils.isStoneLike(w.getDown().getType())) {
                    new StalactiteBuilder(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL).setSolidBlockType(Material.COBBLESTONE,
                                                                                                             Material.MOSSY_COBBLESTONE
                                                                                                     )
                                                                                                     .setFacingUp(true)
                                                                                                     .setVerticalSpace(
                                                                                                             caveHeight)
                                                                                                     .build(random, w);
                }
            }

        }
        else if (GenUtils.chance(random, 1, 25) && BlockUtils.isStoneLike(floor.getUp().getType())) { // Slabbing
            SimpleBlock base = floor.getUp();
            if (BlockUtils.isAir(base.getType())) {
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (base.getRelative(face).isSolid()) {
                        if (base.getDown().getType() == Material.DEEPSLATE) {
                            base.setType(Material.COBBLED_DEEPSLATE_SLAB);
                        }
                        else {
                            base.setType(Material.STONE_SLAB);
                        }
                        break;
                    }
                }
            }
        }
        else if (GenUtils.chance(random, 1, 35) && BlockUtils.isStoneLike(floor.getUp().getType())) { // Shrooms
            if (BlockUtils.isAir(floor.getUp().getType())) {
                PlantBuilder.build(floor.getUp(), PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
            }
        }
        // =========================
        // Light placement logic
        // =========================
        if (GenUtils.chance(random, 1, 50)) {  // 2% chance for light placement
            Material lightBlock = Material.GLOWSTONE; // Default to glowstone

            // Try to place on walls if air is present
            boolean lightPlaced = false;
            for (BlockFace face : BlockUtils.directBlockFaces) {
                Wall wall = new Wall(floor).getRelative(face);

                // Ensure the wall block is air and the opposite face is solid
                if (wall.getType().isAir() && wall.getRelative(face.getOppositeFace()).getType().isSolid()) {

                    // Check if only one face is exposed to air
                    int airFaces = 0;
                    for (BlockFace checkFace : BlockUtils.directBlockFaces) {
                        if (BlockUtils.isAir(wall.getRelative(checkFace).getType())) {
                            airFaces++;
                        }
                    }

                    // Place the light only if there's exactly one face exposed to air
                    if (airFaces == 1) {
                        wall.setType(lightBlock);
                        lightPlaced = true;
                        break;
                    }
                }
            }

            // If no suitable walls, place on ceiling if valid
            if (!lightPlaced && BlockUtils.isAir(ceil.getRelative(BlockFace.DOWN).getType())
                && ceil.getType().isSolid()) { // Ensure the ceiling is solid

                // Check if only one face of the block below the ceiling is exposed to air
                SimpleBlock lightPos = ceil.getRelative(BlockFace.DOWN);
                int airFaces = 0;
                for (BlockFace checkFace : BlockUtils.directBlockFaces) {
                    if (BlockUtils.isAir(lightPos.getRelative(checkFace).getType())) {
                        airFaces++;
                    }
                }

                // Place the light only if there's exactly one face exposed to air
                if (airFaces == 1) {
                    lightPos.setType(lightBlock);
                }
            }
        }



        else if (GenUtils.chance(random, 1, 35) && BlockUtils.isStoneLike(floor.getUp().getType())) { // Shrooms
            if (BlockUtils.isAir(floor.getUp().getType())) {
                PlantBuilder.build(floor.getUp(), PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
            }
        }

    }
}
