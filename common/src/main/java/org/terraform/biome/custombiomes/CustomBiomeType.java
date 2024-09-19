package org.terraform.biome.custombiomes;

import org.jetbrains.annotations.NotNull;
import org.terraform.utils.version.Version;

import java.util.Locale;

public enum CustomBiomeType {
    NONE,

    // Updated swamp oasis with fall colors
    BOGWALKER_LAND("f7e09c", "0a3D2e", "a6764b", "f89c42", "b08961", "c9a67a"),

    // Icy, cold colors to reflect a frozen landscape
    GLACIERBORN_LAND("cce7ff", "69faff", "b0e0e6", "a7dbf5", "ffffff", "d4f0ff"),

    // Lush, vibrant colors for a jungle-like environment
    LEAFSTRIDER_LAND("85cc33", "a4dded", "66cc66", "add8e6", "33ff33", "99ff99"),

    // Crystal-rich environment with bright and mystical colors
    CRYSTALLINE_CLUSTER("e54fff", "c599ff", "e54fff", "d28fff", "ffffff", "f0b3ff"),
    ;

    private final @NotNull String key;
    private final String fogColor;
    private final String waterColor;
    private final String waterFogColor;
    private final String skyColor;
    private final String grassColor;
    private String foliageColor;
    private float rainFall = 0.8f;
    private boolean isCold = false;

    CustomBiomeType() {
        this.key = "MysticBiomes:" + this.toString().toLowerCase(Locale.ENGLISH);
        this.fogColor = "";
        this.waterColor = "";
        this.waterFogColor = "";
        this.skyColor = "";
        this.foliageColor = "";
        this.grassColor = "";
    }

    CustomBiomeType(String fogColor,
                    String waterColor,
                    String waterFogColor,
                    String skyColor,
                    String foliageColor,
                    String grassColor)
    {
        this.key = "MysticBiomes:" + this.toString().toLowerCase(Locale.ENGLISH);
        this.fogColor = fogColor;
        this.waterColor = waterColor;
        this.waterFogColor = waterFogColor;
        this.skyColor = skyColor;
        this.foliageColor = foliageColor;
        this.grassColor = grassColor;
        this.rainFall = 0.8f;
        this.isCold = false;
        // In 1.20, cherry trees no longer need the pink.
        if (Version.isAtLeast(20) && this.foliageColor.equals("ffa1fc")) {
            this.foliageColor = "acff96";
        }

    }

    public @NotNull String getKey() {
        return key;
    }

    public String getFogColor() {
        return fogColor;
    }

    public String getWaterColor() {
        return waterColor;
    }

    public String getWaterFogColor() {
        return waterFogColor;
    }

    public String getSkyColor() {
        return skyColor;
    }

    public String getFoliageColor() {
        return foliageColor;
    }

    public String getGrassColor() {
        return grassColor;
    }

    public float getRainFall() {
        return rainFall;
    }

    public boolean isCold() {
        return isCold;
    }
}