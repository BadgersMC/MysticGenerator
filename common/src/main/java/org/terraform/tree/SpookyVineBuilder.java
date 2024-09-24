package org.terraform.tree;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SpookyVineBuilder {
    private Material[] vineMaterials;
    private int minLength;
    private int maxLength;
    private double pumpkinChance = 0.1; // 10% chance for a hanging pumpkin
    private double jackOLanternChance = 0.05; // 5% chance for a Jack O Lantern

    // Method to set the materials for the vine (Dark Oak, Spruce, and Oak Fences)
    public SpookyVineBuilder setVineMaterials(Material... vineMaterials) {
        this.vineMaterials = vineMaterials;
        return this;
    }

    // Method to set the length range of the vines
    public SpookyVineBuilder setVineLengthRange(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        return this;
    }

    // Method to set the chance of decorations like pumpkins or jack-o-lanterns
    public SpookyVineBuilder setDecorationChance(double pumpkinChance, double jackOLanternChance) {
        this.pumpkinChance = pumpkinChance;
        this.jackOLanternChance = jackOLanternChance;
        return this;
    }

    // Helper method to check if there is a vine in the neighboring blocks using vineMaterials defined earlier
    private boolean isAdjacentToVine(SimpleBlock block) {
        // Check adjacent blocks (left, right, front, back, and below)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Skip the current block
                SimpleBlock adjacentBlock = block.getRelative(dx, 0, dz);
                if (isMaterialAnyOf(adjacentBlock, vineMaterials)) {
                    return true; // A vine material is nearby
                }
            }
        }

        return false;
    }

    // Helper method to check if a block's material matches any in the provided array
    private boolean isMaterialAnyOf(SimpleBlock block, Material[] materials) {
        Material blockType = block.getType();
        for (Material material : materials) {
            if (blockType == material) {
                return true;
            }
        }
        return false;
    }

    // Method to build the vine based on the provided materials and length range
    public void buildVine(@NotNull SimpleBlock block, Random rand) {
        // If the block is adjacent to a vine, skip placing a vine here
        if (isAdjacentToVine(block)) {
            return;
        }

        int length = GenUtils.randInt(rand, minLength, maxLength);

        for (int i = 0; i < length; i++) {
            SimpleBlock vineBlock = block.getRelative(0, -i, 0);

            if (!vineBlock.isAir()) break;

            // Select the material for this section of the vine based on the current length
            Material material = vineMaterials[Math.min(i, vineMaterials.length - 1)];
            vineBlock.setType(material);

            // If we are at the last section of the vine, we can add decorations
            if (i == length - 1) {
                if (GenUtils.chance(rand, (int) (pumpkinChance * 100), 100)) {
                    vineBlock.setType(Material.PUMPKIN);
                } else if (GenUtils.chance(rand, (int) (jackOLanternChance * 100), 100)) {
                    vineBlock.setType(Material.JACK_O_LANTERN);
                }
            }
        }
    }
}
