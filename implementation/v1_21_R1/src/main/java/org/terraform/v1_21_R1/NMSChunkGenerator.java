package org.terraform.v1_21_R1;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.small.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NMSChunkGenerator extends ChunkGenerator {
    private final ChunkGenerator delegate;
    private final TerraformWorld tw;
    private final MapRenderWorldProviderBiome mapRendererBS;
    private final TerraformWorldProviderBiome twBS;

    public NMSChunkGenerator(String worldName, long seed, ChunkGenerator delegate) {
        super(
                delegate.d(), //WorldChunkManager d() is getBiomeSource()
                delegate.d); //Idk what generationSettingsGetter is
        tw = TerraformWorld.get(worldName, seed);
        this.delegate = delegate;

        //Set the long term biome handler to this one. The normal behaving one
        //is initiated inside the cave carver
        mapRendererBS = new MapRenderWorldProviderBiome(tw, delegate.d());
        twBS = new TerraformWorldProviderBiome(TerraformWorld.get(worldName, seed), delegate.d());
    }


    @Override //getBiomeSource
    public WorldChunkManager d() {
        return mapRendererBS;
    }

    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> b() {
        return MapCodec.unit(null);
    }

    @Override //createBiomes
    public CompletableFuture<IChunkAccess> a(RandomState randomstate, Blender blender, StructureManager structuremanager, IChunkAccess ichunkaccess)
    {
        return CompletableFuture.supplyAsync(SystemUtils.a("init_biomes", () -> {
            return ichunkaccess; //Don't do any calculations here, biomes are set in applyCarvers
        }), SystemUtils.g());
    }

    @Override //findNearestMapFeature
    public Pair<BlockPosition, Holder<Structure>> a(WorldServer worldserver, HolderSet<Structure> holderset,
                                                    BlockPosition blockposition, int i, boolean flag) {

        int pX = blockposition.u(); //getX
        int pZ = blockposition.w(); //getZ

        for(Holder<Structure> holder:holderset) {
            Structure feature = holder.a();
            //StructureGenerator<?> structuregenerator = feature.;
            TerraformGeneratorPlugin.logger.info("Vanilla locate for " + feature.getClass().getName() + " invoked.");

            if (holder.a().getClass() == StrongholdStructure.class) { //stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<>(new BlockPosition(coords[0], 20, coords[1]), holder);
            }
            else if(!TConfigOption.DEVSTUFF_VANILLA_LOCATE_DISABLE.getBoolean())
            {
                if (holder.a().getClass() == OceanMonumentStructure.class) { //Monument

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<Structure>>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == WoodlandMansionStructure.class) { //Mansion

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());

                    return new Pair<BlockPosition, Holder<Structure>>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == BuriedTreasureStructure.class) {
                    //Buried Treasure
                    int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfigOption.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS.getInt());
                    if(coords == null) return null;
                    return new Pair<BlockPosition, Holder<Structure>>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                }
            }
        }
        return null;
    }

    @Override //applyBiomeDecoration
    public void a(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) {
        delegate.a(generatoraccessseed, ichunkaccess, structuremanager);
    }

    @Override //applyCarvers
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, long seed,
                  RandomState randomstate, BiomeManager biomemanager,
                  StructureManager structuremanager, IChunkAccess ichunkaccess,
                  WorldGenStage.Features worldgenstage_features)
    {
        //POPULATES BIOMES. IMPORTANT
        //(net.minecraft.world.level.biome.BiomeResolver,net.minecraft.world.level.biome.Climate$Sampler)
        //Use twBS as it is the biome provider that actually calculates biomes.
        //The other one only returns river/plains
        ichunkaccess.a(this.twBS, null); //This can be null as its passed into twBS

        //Call delegate applyCarvers to apply spigot ChunkGenerator;
        delegate.a(regionlimitedworldaccess, seed, randomstate, biomemanager,structuremanager,ichunkaccess,worldgenstage_features);
    }

    @Override //getSeaLevel
    public int e() {
        return delegate.e();
    }

    @Override //createStructures should be empty
    public void a(IRegistryCustom iregistrycustom, ChunkGeneratorStructureState chunkgeneratorstructurestate, StructureManager structuremanager, IChunkAccess ichunkaccess, StructureTemplateManager structuretemplatemanager) {
    }

    @Override //getSpawnHeight
    public int a(LevelHeightAccessor levelheightaccessor) {
        return 64;
    }

    @Override //fillFromNoise
    public CompletableFuture<IChunkAccess> a(Blender blender,
                                             RandomState randomstate, StructureManager structuremanager,
                                             IChunkAccess ichunkaccess) {
        return delegate.a(blender,
                randomstate, structuremanager,
                ichunkaccess);
    }

    @Override //buildSurface. Used to be buildBase
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, StructureManager structuremanager, RandomState randomstate, IChunkAccess ichunkaccess)
    {
        delegate.a(regionlimitedworldaccess, structuremanager, randomstate, ichunkaccess);
    }

    @Override //createReferences. Idk what this is
    public void a(GeneratorAccessSeed gas,StructureManager manager,IChunkAccess ica)
    {
        delegate.a(gas, manager, ica);
    }

    @Override //getBaseColumn
    public BlockColumn a(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.delegate.a(i,j,levelheightaccessor,randomstate);
    }

//	@Override //withSeed
//	public ChunkGenerator a(long seed) {
//		return new NMSChunkGenerator(this.tw.getName(), (int) seed, this.delegate);
//	}

    //spawnOriginalMobs
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess) {
        this.delegate.a(regionlimitedworldaccess);
    }


    //getSeaLevel
    @Override
    public int f() {
        return TerraformGenerator.seaLevel;
    }

    //getMinY
    @Override
    public int g() {
        return this.delegate.g();
    }

    @Override //getFirstFreeHeight
    public int b(int i, int j, HeightMap.Type heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.a(i, j, heightmap_type, levelheightaccessor, randomstate);
    }


    @Override //getFirstOccupiedHeight
    public int c(int i, int j, HeightMap.Type heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.a(i, j, heightmap_type, levelheightaccessor, randomstate) - 1;
    }

    @Override //getBaseHeight
    public int a(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        //return delegate.a(x, z, var2, var3);
        return 100;
        //return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }

    //private static boolean biomeDebug = false;


    @Override //addDebugScreenInfo
    public void a(List<String> list, RandomState randomstate, BlockPosition blockposition) {

    }

}
