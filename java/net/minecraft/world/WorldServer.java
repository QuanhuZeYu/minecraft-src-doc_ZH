package net.minecraft.world;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.ChestGenHooks;
import static net.minecraftforge.common.ChestGenHooks.BONUS_CHEST;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServer extends World
{
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final EntityTracker theEntityTracker;
    private final PlayerManager thePlayerManager;
    private Set pendingTickListEntriesHashSet;
    /** All work to do in future ticks. */
    private TreeSet pendingTickListEntriesTreeSet;
    public ChunkProviderServer theChunkProviderServer;
    /** Whether or not level saving is enabled */
    public boolean levelSaving;
    /** is false if there are no players */
    private boolean allPlayersSleeping;
    private int updateEntityTick;
    /** the teleporter to use when the entity is being transferred into the dimension */
    private final Teleporter worldTeleporter;
    private final SpawnerAnimals animalSpawner = new SpawnerAnimals();
    private WorldServer.ServerBlockEventList[] field_147490_S = new WorldServer.ServerBlockEventList[] {new WorldServer.ServerBlockEventList(null), new WorldServer.ServerBlockEventList(null)};
    private int blockEventCacheIndex;
    public static final WeightedRandomChestContent[] bonusChestContent = new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.stick, 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.planks), 0, 1, 3, 10), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log), 0, 1, 3, 10), new WeightedRandomChestContent(Items.stone_axe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_axe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.stone_pickaxe, 0, 1, 1, 3), new WeightedRandomChestContent(Items.wooden_pickaxe, 0, 1, 1, 5), new WeightedRandomChestContent(Items.apple, 0, 2, 3, 5), new WeightedRandomChestContent(Items.bread, 0, 2, 3, 3), new WeightedRandomChestContent(Item.getItemFromBlock(Blocks.log2), 0, 1, 3, 10)};
    private List pendingTickListEntriesThisTick = new ArrayList();
    /** An IntHashMap of entity IDs (integers) to their Entity objects. */
    private IntHashMap entityIdMap;
    private static final String __OBFID = "CL_00001437";

    /** Stores the recently processed (lighting) chunks */
    protected Set<ChunkCoordIntPair> doneChunks = new HashSet<ChunkCoordIntPair>();
    public List<Teleporter> customTeleporters = new ArrayList<Teleporter>();

    public WorldServer(MinecraftServer p_i45284_1_, ISaveHandler p_i45284_2_, String p_i45284_3_, int p_i45284_4_, WorldSettings p_i45284_5_, Profiler p_i45284_6_)
    {
        super(p_i45284_2_, p_i45284_3_, p_i45284_5_, WorldProvider.getProviderForDimension(p_i45284_4_), p_i45284_6_);
        this.mcServer = p_i45284_1_;
        this.theEntityTracker = new EntityTracker(this);
        this.thePlayerManager = new PlayerManager(this);

        if (this.entityIdMap == null)
        {
            this.entityIdMap = new IntHashMap();
        }

        if (this.pendingTickListEntriesHashSet == null)
        {
            this.pendingTickListEntriesHashSet = new HashSet();
        }

        if (this.pendingTickListEntriesTreeSet == null)
        {
            this.pendingTickListEntriesTreeSet = new TreeSet();
        }

        this.worldTeleporter = new Teleporter(this);
        this.worldScoreboard = new ServerScoreboard(p_i45284_1_);
        ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData)this.mapStorage.loadData(ScoreboardSaveData.class, "scoreboard");

        if (scoreboardsavedata == null)
        {
            scoreboardsavedata = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", scoreboardsavedata);
        }

        if (!(this instanceof WorldServerMulti)) //Forge: We fix the global mapStorage, which causes us to share scoreboards early. So don't associate the save data with the temporary scoreboard
        {
            scoreboardsavedata.func_96499_a(this.worldScoreboard);
        }
        ((ServerScoreboard)this.worldScoreboard).func_96547_a(scoreboardsavedata);
        DimensionManager.setWorld(p_i45284_4_, this);
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();

        if (this.getWorldInfo().isHardcoreModeEnabled() && this.difficultySetting != EnumDifficulty.HARD)
        {
            this.difficultySetting = EnumDifficulty.HARD;
        }

        this.provider.worldChunkMgr.cleanupCache();

        if (this.areAllPlayersAsleep())
        {
            if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
            {
                long i = this.worldInfo.getWorldTime() + 24000L;
                this.worldInfo.setWorldTime(i - i % 24000L);
            }

            this.wakeAllPlayers();
        }

        this.theProfiler.startSection("mobSpawner");

        if (this.getGameRules().getGameRuleBooleanValue("doMobSpawning"))
        {
            this.animalSpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
        }

        this.theProfiler.endStartSection("chunkSource");
        this.chunkProvider.unloadQueuedChunks();
        int j = this.calculateSkylightSubtracted(1.0F);

        if (j != this.skylightSubtracted)
        {
            this.skylightSubtracted = j;
        }

        this.worldInfo.incrementTotalWorldTime(this.worldInfo.getWorldTotalTime() + 1L);

        if (this.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
        {
            this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
        }

        this.theProfiler.endStartSection("tickPending");
        this.tickUpdates(false);
        this.theProfiler.endStartSection("tickBlocks");
        this.func_147456_g();
        this.theProfiler.endStartSection("chunkMap");
        this.thePlayerManager.updatePlayerInstances();
        this.theProfiler.endStartSection("village");
        this.villageCollectionObj.tick();
        this.villageSiegeObj.tick();
        this.theProfiler.endStartSection("portalForcer");
        this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
        for (Teleporter tele : customTeleporters)
        {
            tele.removeStalePortalLocations(getTotalWorldTime());
        }
        this.theProfiler.endSection();
        this.func_147488_Z();
    }

    /**
     * only spawns creatures allowed by the chunkProvider
     */
    public BiomeGenBase.SpawnListEntry spawnRandomCreature(EnumCreatureType p_73057_1_, int p_73057_2_, int p_73057_3_, int p_73057_4_)
    {
        List list = this.getChunkProvider().getPossibleCreatures(p_73057_1_, p_73057_2_, p_73057_3_, p_73057_4_);
        list = ForgeEventFactory.getPotentialSpawns(this, p_73057_1_, p_73057_2_, p_73057_3_, p_73057_4_, list);
        return list != null && !list.isEmpty() ? (BiomeGenBase.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, list) : null;
    }

    /**
     * Updates the flag that indicates whether or not all players in the world are sleeping.
     */
    public void updateAllPlayersSleepingFlag()
    {
        this.allPlayersSleeping = !this.playerEntities.isEmpty();
        Iterator iterator = this.playerEntities.iterator();

        while (iterator.hasNext())
        {
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (!entityplayer.isPlayerSleeping())
            {
                this.allPlayersSleeping = false;
                break;
            }
        }
    }

    protected void wakeAllPlayers()
    {
        this.allPlayersSleeping = false;
        Iterator iterator = this.playerEntities.iterator();

        while (iterator.hasNext())
        {
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (entityplayer.isPlayerSleeping())
            {
                entityplayer.wakeUpPlayer(false, false, true);
            }
        }

        this.resetRainAndThunder();
    }

    private void resetRainAndThunder()
    {
        provider.resetRainAndThunder();
    }

    public boolean areAllPlayersAsleep()
    {
        if (this.allPlayersSleeping && !this.isRemote)
        {
            Iterator iterator = this.playerEntities.iterator();
            EntityPlayer entityplayer;

            do
            {
                if (!iterator.hasNext())
                {
                    return true;
                }

                entityplayer = (EntityPlayer)iterator.next();
            }
            while (entityplayer.isPlayerFullyAsleep());

            return false;
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets a new spawn location by finding an uncovered block at a random (x,z) location in the chunk.
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnLocation()
    {
        if (this.worldInfo.getSpawnY() <= 0)
        {
            this.worldInfo.setSpawnY(64);
        }

        int i = this.worldInfo.getSpawnX();
        int j = this.worldInfo.getSpawnZ();
        int k = 0;

        while (this.getTopBlock(i, j).getMaterial() == Material.air)
        {
            i += this.rand.nextInt(8) - this.rand.nextInt(8);
            j += this.rand.nextInt(8) - this.rand.nextInt(8);
            ++k;

            if (k == 10000)
            {
                break;
            }
        }

        this.worldInfo.setSpawnX(i);
        this.worldInfo.setSpawnZ(j);
    }

    protected void func_147456_g() {
        // 调用父类的方法，可能包含基本的定时更新逻辑。
        super.func_147456_g();

        // 统计变量，记录触发随机 tick 的方块数量。
        int blockTickCounter = 0; // 随机更新的方块数量
        int tickCounter = 0; // 总的随机 tick 操作次数

        // 遍历所有需要处理的活动区块集。
        Iterator<ChunkCoordIntPair> iterator = this.activeChunkSet.iterator();

        while (iterator.hasNext()) {
            // 获取当前区块的坐标对。
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) iterator.next();
            int worldX = chunkcoordintpair.chunkXPos * 16; // 区块 X 坐标转换为世界坐标。
            int worldZ = chunkcoordintpair.chunkZPos * 16; // 区块 Z 坐标转换为世界坐标。

            // 性能分析器开始记录“获取区块”部分的性能。
            this.theProfiler.startSection("getChunk");

            // 根据区块坐标获取具体的区块对象。
            Chunk chunk = this.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);

            // 对该区块执行自定义操作。
            this.func_147467_a(worldX, worldZ, chunk);

            // 性能分析器切换到“更新区块”部分。
            this.theProfiler.endStartSection("tickChunk");

            // 对区块执行 tick 操作（如更新实体状态）。
            chunk.func_150804_b(false);

            // 性能分析器切换到“雷暴”部分。
            this.theProfiler.endStartSection("thunder");

            // 雷暴逻辑：在当前区块可能随机生成闪电。
            if (provider.canDoLightning(chunk) && this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
                this.updateLCG = this.updateLCG * 3 + 1013904223; // 更新伪随机数生成器。
                int randomInt = this.updateLCG >> 2; // 随机数。
                int randX = worldX + (randomInt & 15); // 确定闪电的 X 坐标。
                int randZ = worldZ + (randomInt >> 8 & 15); // 确定闪电的 Z 坐标。
                int rainHeight = this.getPrecipitationHeight(randX, randZ); // 获取降水高度。

                // 如果当前位置可以生成闪电，则创建闪电实体。
                if (this.canLightningStrikeAt(randX, rainHeight, randZ)) {
                    this.addWeatherEffect(new EntityLightningBolt(this, (double) randX, (double) rainHeight, (double) randZ));
                }
            }

            // 性能分析器切换到“冰雪”部分。
            this.theProfiler.endStartSection("iceandsnow");

            // 冰雪逻辑：在当前区块可能生成冰块或雪层。
            if (provider.canDoRainSnowIce(chunk) && this.rand.nextInt(16) == 0) {
                this.updateLCG = this.updateLCG * 3 + 1013904223; // 更新伪随机数生成器。
                int randomInt = this.updateLCG >> 2; // 随机数。
                int randX = randomInt & 15; // 随机选择 X 坐标（区块内）。
                int randZ = randomInt >> 8 & 15; // 随机选择 Z 坐标（区块内）。
                int rainHeight = this.getPrecipitationHeight(randX + worldX, randZ + worldZ); // 获取降水高度。

                // 如果该方块可以冻结，则设置为冰块。
                if (this.isBlockFreezableNaturally(randX + worldX, rainHeight - 1, randZ + worldZ)) {
                    this.setBlock(randX + worldX, rainHeight - 1, randZ + worldZ, Blocks.ice);
                }

                // 如果正在下雨并且可以生成雪层，则设置为雪层。
                if (this.isRaining() && this.func_147478_e(randX + worldX, rainHeight, randZ + worldZ, true)) {
                    this.setBlock(randX + worldX, rainHeight, randZ + worldZ, Blocks.snow_layer);
                }

                // 如果正在下雨，则更新方块的降雨行为。
                if (this.isRaining()) {
                    BiomeGenBase biomegenbase = this.getBiomeGenForCoords(randX + worldX, randZ + worldZ);

                    // 如果生物群系支持闪电，则触发降雨影响。
                    if (biomegenbase.canSpawnLightningBolt()) {
                        this.getBlock(randX + worldX, rainHeight - 1, randZ + worldZ).fillWithRain(this, randX + worldX, rainHeight - 1, randZ + worldZ);
                    }
                }
            }

            // 性能分析器切换到“方块 tick”部分。
            this.theProfiler.endStartSection("tickBlocks");

            // 获取区块中的方块存储数组（按高度分块）。
            ExtendedBlockStorage[] aextendedblockstorage = chunk.getBlockStorageArray();
            int storageCount = aextendedblockstorage.length;

            // 遍历每个存储单元。
            for (int curStorage = 0; curStorage < storageCount; ++curStorage) {
                ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[curStorage];

                // 如果存储单元需要随机 tick，则执行。
                if (extendedblockstorage != null && extendedblockstorage.getNeedsRandomTick()) {
                    for (int rand3 = 0; rand3 < 3; ++rand3) { // 每单元随机 tick 3 次。
                        this.updateLCG = this.updateLCG * 3 + 1013904223; // 更新伪随机数生成器。
                        int randomInt = this.updateLCG >> 2; // 随机数。
                        // 一个chunk X Z 的范围只有16 单个ChunkStorage大小为16*16*16
                        int randX = randomInt & 15; // 随机选择 X 坐标（区块内）。
                        int randZ = randomInt >> 8 & 15; // 随机选择 Z 坐标（区块内）。
                        int randY = randomInt >> 16 & 15; // 随机选择 Y 坐标（存储单元内）。
                        ++tickCounter; // 总 tick 计数。

                        // 获取目标方块。
                        Block block = extendedblockstorage.getBlockByExtId(randX, randY, randZ);

                        // 如果方块需要随机 tick，则更新。
                        if (block.getTickRandomly()) {
                            ++blockTickCounter; // 统计随机更新的方块数量。
                            block.updateTick(this, randX + worldX, randY + extendedblockstorage.getYLocation(), randZ + worldZ, this.rand);
                        }
                    }
                }
            }

            // 性能分析器结束当前区块的处理。
            this.theProfiler.endSection();
        }
    }

    /**
     * Returns true if the given block will receive a scheduled tick in this tick. Args: X, Y, Z, Block
     */
    public boolean isBlockTickScheduledThisTick(int p_147477_1_, int p_147477_2_, int p_147477_3_, Block p_147477_4_)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(p_147477_1_, p_147477_2_, p_147477_3_, p_147477_4_);
        return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
    }

    /**
     * Schedules a tick to a block with a delay (Most commonly the tick rate)
     */
    public void scheduleBlockUpdate(int p_147464_1_, int p_147464_2_, int p_147464_3_, Block p_147464_4_, int p_147464_5_)
    {
        this.scheduleBlockUpdateWithPriority(p_147464_1_, p_147464_2_, p_147464_3_, p_147464_4_, p_147464_5_, 0);
    }

    public void scheduleBlockUpdateWithPriority(int p_147454_1_, int p_147454_2_, int p_147454_3_, Block p_147454_4_, int p_147454_5_, int p_147454_6_)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(p_147454_1_, p_147454_2_, p_147454_3_, p_147454_4_);
        //Keeping here as a note for future when it may be restored.
        //boolean isForced = getPersistentChunks().containsKey(new ChunkCoordIntPair(nextticklistentry.xCoord >> 4, nextticklistentry.zCoord >> 4));
        //byte b0 = isForced ? 0 : 8;
        byte b0 = 0;

        if (this.scheduledUpdatesAreImmediate && p_147454_4_.getMaterial() != Material.air)
        {
            if (p_147454_4_.func_149698_L())
            {
                b0 = 8;

                if (this.checkChunksExist(nextticklistentry.xCoord - b0, nextticklistentry.yCoord - b0, nextticklistentry.zCoord - b0, nextticklistentry.xCoord + b0, nextticklistentry.yCoord + b0, nextticklistentry.zCoord + b0))
                {
                    Block block1 = this.getBlock(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);

                    if (block1.getMaterial() != Material.air && block1 == nextticklistentry.func_151351_a())
                    {
                        block1.updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, this.rand);
                    }
                }

                return;
            }

            p_147454_5_ = 1;
        }

        if (this.checkChunksExist(p_147454_1_ - b0, p_147454_2_ - b0, p_147454_3_ - b0, p_147454_1_ + b0, p_147454_2_ + b0, p_147454_3_ + b0))
        {
            if (p_147454_4_.getMaterial() != Material.air)
            {
                nextticklistentry.setScheduledTime((long)p_147454_5_ + this.worldInfo.getWorldTotalTime());
                nextticklistentry.setPriority(p_147454_6_);
            }

            if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry))
            {
                this.pendingTickListEntriesHashSet.add(nextticklistentry);
                this.pendingTickListEntriesTreeSet.add(nextticklistentry);
            }
        }
    }

    public void func_147446_b(int p_147446_1_, int p_147446_2_, int p_147446_3_, Block p_147446_4_, int p_147446_5_, int p_147446_6_)
    {
        NextTickListEntry nextticklistentry = new NextTickListEntry(p_147446_1_, p_147446_2_, p_147446_3_, p_147446_4_);
        nextticklistentry.setPriority(p_147446_6_);

        if (p_147446_4_.getMaterial() != Material.air)
        {
            nextticklistentry.setScheduledTime((long)p_147446_5_ + this.worldInfo.getWorldTotalTime());
        }

        if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry))
        {
            this.pendingTickListEntriesHashSet.add(nextticklistentry);
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
        }
    }

    /**
     * Updates (and cleans up) entities and tile entities
     */
    public void updateEntities()
    {
        if (this.playerEntities.isEmpty() && getPersistentChunks().isEmpty())
        {
            if (this.updateEntityTick++ >= 1200)
            {
                return;
            }
        }
        else
        {
            this.resetUpdateEntityTick();
        }

        super.updateEntities();
    }

    /**
     * Resets the updateEntityTick field to 0
     */
    public void resetUpdateEntityTick()
    {
        this.updateEntityTick = 0;
    }

    /**
     * Runs through the list of updates to run and ticks them
     */
    public boolean tickUpdates(boolean p_72955_1_)
    {
        int i = this.pendingTickListEntriesTreeSet.size();

        if (i != this.pendingTickListEntriesHashSet.size())
        {
            throw new IllegalStateException("TickNextTick list out of synch");
        }
        else
        {
            if (i > 1000)
            {
                i = 1000;
            }

            this.theProfiler.startSection("cleaning");
            NextTickListEntry nextticklistentry;

            for (int j = 0; j < i; ++j)
            {
                nextticklistentry = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();

                if (!p_72955_1_ && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime())
                {
                    break;
                }

                this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
                this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                this.pendingTickListEntriesThisTick.add(nextticklistentry);
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("ticking");
            Iterator iterator = this.pendingTickListEntriesThisTick.iterator();

            while (iterator.hasNext())
            {
                nextticklistentry = (NextTickListEntry)iterator.next();
                iterator.remove();
                //Keeping here as a note for future when it may be restored.
                //boolean isForced = getPersistentChunks().containsKey(new ChunkCoordIntPair(nextticklistentry.xCoord >> 4, nextticklistentry.zCoord >> 4));
                //byte b0 = isForced ? 0 : 8;
                byte b0 = 0;

                if (this.checkChunksExist(nextticklistentry.xCoord - b0, nextticklistentry.yCoord - b0, nextticklistentry.zCoord - b0, nextticklistentry.xCoord + b0, nextticklistentry.yCoord + b0, nextticklistentry.zCoord + b0))
                {
                    Block block = this.getBlock(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);

                    if (block.getMaterial() != Material.air && Block.isEqualTo(block, nextticklistentry.func_151351_a()))
                    {
                        try
                        {
                            block.updateTick(this, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, this.rand);
                        }
                        catch (Throwable throwable1)
                        {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception while ticking a block");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                            int k;

                            try
                            {
                                k = this.getBlockMetadata(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord);
                            }
                            catch (Throwable throwable)
                            {
                                k = -1;
                            }

                            CrashReportCategory.func_147153_a(crashreportcategory, nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, block, k);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
                else
                {
                    this.scheduleBlockUpdate(nextticklistentry.xCoord, nextticklistentry.yCoord, nextticklistentry.zCoord, nextticklistentry.func_151351_a(), 0);
                }
            }

            this.theProfiler.endSection();
            this.pendingTickListEntriesThisTick.clear();
            return !this.pendingTickListEntriesTreeSet.isEmpty();
        }
    }

    public List<net.minecraft.world.NextTickListEntry> getPendingBlockUpdates(Chunk p_72920_1_, boolean p_72920_2_)
    {
        ArrayList arraylist = null;
        ChunkCoordIntPair chunkcoordintpair = p_72920_1_.getChunkCoordIntPair();
        int i = (chunkcoordintpair.chunkXPos << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.chunkZPos << 4) - 2;
        int l = k + 16 + 2;

        for (int i1 = 0; i1 < 2; ++i1)
        {
            Iterator iterator;

            if (i1 == 0)
            {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            }
            else
            {
                iterator = this.pendingTickListEntriesThisTick.iterator();

                if (!this.pendingTickListEntriesThisTick.isEmpty())
                {
                    logger.debug("toBeTicked = " + this.pendingTickListEntriesThisTick.size());
                }
            }

            while (iterator.hasNext())
            {
                NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();

                if (nextticklistentry.xCoord >= i && nextticklistentry.xCoord < j && nextticklistentry.zCoord >= k && nextticklistentry.zCoord < l)
                {
                    if (p_72920_2_)
                    {
                        this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                        iterator.remove();
                    }

                    if (arraylist == null)
                    {
                        arraylist = new ArrayList();
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }

    /**
     * Will update the entity in the world if the chunk the entity is in is currently loaded or its forced to update.
     * Args: entity, forceUpdate
     */
    public void updateEntityWithOptionalForce(Entity p_72866_1_, boolean p_72866_2_)
    {
        if (!this.mcServer.getCanSpawnAnimals() && (p_72866_1_ instanceof EntityAnimal || p_72866_1_ instanceof EntityWaterMob))
        {
            p_72866_1_.setDead();
        }

        if (!this.mcServer.getCanSpawnNPCs() && p_72866_1_ instanceof INpc)
        {
            p_72866_1_.setDead();
        }

        super.updateEntityWithOptionalForce(p_72866_1_, p_72866_2_);
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
        this.theChunkProviderServer = new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
        return this.theChunkProviderServer;
    }

    public List<net.minecraft.tileentity.TileEntity> func_147486_a(int p_147486_1_, int p_147486_2_, int p_147486_3_, int p_147486_4_, int p_147486_5_, int p_147486_6_)
    {
        ArrayList arraylist = new ArrayList();

        for(int x = (p_147486_1_ >> 4); x <= (p_147486_4_ >> 4); x++)
        {
            for(int z = (p_147486_3_ >> 4); z <= (p_147486_6_ >> 4); z++)
            {
                Chunk chunk = getChunkFromChunkCoords(x, z);
                if (chunk != null)
                {
                    for(Object obj : chunk.chunkTileEntityMap.values())
                    {
                        TileEntity entity = (TileEntity)obj;
                        if (!entity.isInvalid())
                        {
                            if (entity.xCoord >= p_147486_1_ && entity.yCoord >= p_147486_2_ && entity.zCoord >= p_147486_3_ &&
                                entity.xCoord <= p_147486_4_ && entity.yCoord <= p_147486_5_ && entity.zCoord <= p_147486_6_)
                            {
                                arraylist.add(entity);
                            }
                        }
                    }
                }
            }
        }

        return arraylist;
    }

    /**
     * Called when checking if a certain block can be mined or not. The 'spawn safe zone' check is located here.
     */
    public boolean canMineBlock(EntityPlayer player, int x, int y, int z)
    {
        return super.canMineBlock(player, x, y, z);
    }

    public boolean canMineBlockBody(EntityPlayer par1EntityPlayer, int par2, int par3, int par4)
    {
        return !this.mcServer.isBlockProtected(this, par2, par3, par4, par1EntityPlayer);
    }

    protected void initialize(WorldSettings p_72963_1_)
    {
        if (this.entityIdMap == null)
        {
            this.entityIdMap = new IntHashMap();
        }

        if (this.pendingTickListEntriesHashSet == null)
        {
            this.pendingTickListEntriesHashSet = new HashSet();
        }

        if (this.pendingTickListEntriesTreeSet == null)
        {
            this.pendingTickListEntriesTreeSet = new TreeSet();
        }

        this.createSpawnPosition(p_72963_1_);
        super.initialize(p_72963_1_);
    }

    /**
     * creates a spawn position at random within 256 blocks of 0,0
     */
    protected void createSpawnPosition(WorldSettings p_73052_1_)
    {
        if (!this.provider.canRespawnHere())
        {
            this.worldInfo.setSpawnPosition(0, this.provider.getAverageGroundLevel(), 0);
        }
        else
        {
            if (net.minecraftforge.event.ForgeEventFactory.onCreateWorldSpawn(this, p_73052_1_)) return;
            this.findingSpawnPoint = true;
            WorldChunkManager worldchunkmanager = this.provider.worldChunkMgr;
            List list = worldchunkmanager.getBiomesToSpawnIn();
            Random random = new Random(this.getSeed());
            ChunkPosition chunkposition = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
            int i = 0;
            int j = this.provider.getAverageGroundLevel();
            int k = 0;

            if (chunkposition != null)
            {
                i = chunkposition.chunkPosX;
                k = chunkposition.chunkPosZ;
            }
            else
            {
                logger.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!this.provider.canCoordinateBeSpawn(i, k))
            {
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;

                if (l == 1000)
                {
                    break;
                }
            }

            this.worldInfo.setSpawnPosition(i, j, k);
            this.findingSpawnPoint = false;

            if (p_73052_1_.isBonusChestEnabled())
            {
                this.createBonusChest();
            }
        }
    }

    /**
     * Creates the bonus chest in the world.
     */
    protected void createBonusChest()
    {
        WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest(ChestGenHooks.getItems(BONUS_CHEST, rand), ChestGenHooks.getCount(BONUS_CHEST, rand));

        for (int i = 0; i < 10; ++i)
        {
            int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
            int l = this.getTopSolidOrLiquidBlock(j, k) + 1;

            if (worldgeneratorbonuschest.generate(this, this.rand, j, l, k))
            {
                break;
            }
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension.
     */
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return this.provider.getEntrancePortalLocation();
    }

    /**
     * Saves all chunks to disk while updating progress bar.
     */
    public void saveAllChunks(boolean p_73044_1_, IProgressUpdate p_73044_2_) throws MinecraftException
    {
        if (this.chunkProvider.canSave())
        {
            if (p_73044_2_ != null)
            {
                p_73044_2_.displayProgressMessage("Saving level");
            }

            this.saveLevel();

            if (p_73044_2_ != null)
            {
                p_73044_2_.resetProgresAndWorkingMessage("Saving chunks");
            }

            this.chunkProvider.saveChunks(p_73044_1_, p_73044_2_);
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Save(this));
            ArrayList arraylist = Lists.newArrayList(this.theChunkProviderServer.func_152380_a());
            Iterator iterator = arraylist.iterator();

            while (iterator.hasNext())
            {
                Chunk chunk = (Chunk)iterator.next();

                if (chunk != null && !this.thePlayerManager.func_152621_a(chunk.xPosition, chunk.zPosition))
                {
                    this.theChunkProviderServer.unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
                }
            }
        }
    }

    /**
     * saves chunk data - currently only called during execution of the Save All command
     */
    public void saveChunkData()
    {
        if (this.chunkProvider.canSave())
        {
            this.chunkProvider.saveExtraData();
        }
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException
    {
        this.checkSessionLock();
        this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getConfigurationManager().getHostPlayerData());
        this.mapStorage.saveAllData();
        this.perWorldStorage.saveAllData();
    }

    public void onEntityAdded(Entity p_72923_1_)
    {
        super.onEntityAdded(p_72923_1_);
        this.entityIdMap.addKey(p_72923_1_.getEntityId(), p_72923_1_);
        Entity[] aentity = p_72923_1_.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; ++i)
            {
                this.entityIdMap.addKey(aentity[i].getEntityId(), aentity[i]);
            }
        }
    }

    public void onEntityRemoved(Entity p_72847_1_)
    {
        super.onEntityRemoved(p_72847_1_);
        this.entityIdMap.removeObject(p_72847_1_.getEntityId());
        Entity[] aentity = p_72847_1_.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; ++i)
            {
                this.entityIdMap.removeObject(aentity[i].getEntityId());
            }
        }
    }

    /**
     * Returns the Entity with the given ID, or null if it doesn't exist in this World.
     */
    public Entity getEntityByID(int p_73045_1_)
    {
        return (Entity)this.entityIdMap.lookup(p_73045_1_);
    }

    /**
     * 在这个世界的闪电列表中添加了一个闪电。
     */
    public boolean addWeatherEffect(Entity p_72942_1_)
    {
        if (super.addWeatherEffect(p_72942_1_))
        {
            this.mcServer.getConfigurationManager().sendToAllNear(p_72942_1_.posX, p_72942_1_.posY, p_72942_1_.posZ, 512.0D, this.provider.dimensionId, new S2CPacketSpawnGlobalEntity(p_72942_1_));
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    public void setEntityState(Entity entityIn, byte p_72960_2_)
    {
        this.getEntityTracker().func_151248_b(entityIn, new S19PacketEntityStatus(entityIn, p_72960_2_));
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    public Explosion newExplosion(Entity p_72885_1_, double p_72885_2_, double p_72885_4_, double p_72885_6_, float p_72885_8_, boolean p_72885_9_, boolean p_72885_10_)
    {
        Explosion explosion = new Explosion(this, p_72885_1_, p_72885_2_, p_72885_4_, p_72885_6_, p_72885_8_);
        explosion.isFlaming = p_72885_9_;
        explosion.isSmoking = p_72885_10_;
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this, explosion)) return explosion;
        explosion.doExplosionA();
        explosion.doExplosionB(false);

        if (!p_72885_10_)
        {
            explosion.affectedBlockPositions.clear();
        }

        Iterator iterator = this.playerEntities.iterator();

        while (iterator.hasNext())
        {
            EntityPlayer entityplayer = (EntityPlayer)iterator.next();

            if (entityplayer.getDistanceSq(p_72885_2_, p_72885_4_, p_72885_6_) < 4096.0D)
            {
                ((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(p_72885_2_, p_72885_4_, p_72885_6_, p_72885_8_, explosion.affectedBlockPositions, (Vec3)explosion.func_77277_b().get(entityplayer)));
            }
        }

        return explosion;
    }

    /**
     * Adds a block event with the given Args to the blockEventCache. During the next tick(), the block specified will
     * have its onBlockEvent handler called with the given parameters. Args: X,Y,Z, Block, EventID, EventParameter
     */
    public void addBlockEvent(int x, int y, int z, Block blockIn, int eventId, int eventParameter)
    {
        BlockEventData blockeventdata = new BlockEventData(x, y, z, blockIn, eventId, eventParameter);
        Iterator iterator = this.field_147490_S[this.blockEventCacheIndex].iterator();
        BlockEventData blockeventdata1;

        do
        {
            if (!iterator.hasNext())
            {
                this.field_147490_S[this.blockEventCacheIndex].add(blockeventdata);
                return;
            }

            blockeventdata1 = (BlockEventData)iterator.next();
        }
        while (!blockeventdata1.equals(blockeventdata));
    }

    private void func_147488_Z()
    {
        while (!this.field_147490_S[this.blockEventCacheIndex].isEmpty())
        {
            int i = this.blockEventCacheIndex;
            this.blockEventCacheIndex ^= 1;
            Iterator iterator = this.field_147490_S[i].iterator();

            while (iterator.hasNext())
            {
                BlockEventData blockeventdata = (BlockEventData)iterator.next();

                if (this.func_147485_a(blockeventdata))
                {
                    this.mcServer.getConfigurationManager().sendToAllNear((double)blockeventdata.func_151340_a(), (double)blockeventdata.func_151342_b(), (double)blockeventdata.func_151341_c(), 64.0D, this.provider.dimensionId, new S24PacketBlockAction(blockeventdata.func_151340_a(), blockeventdata.func_151342_b(), blockeventdata.func_151341_c(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
                }
            }

            this.field_147490_S[i].clear();
        }
    }

    private boolean func_147485_a(BlockEventData p_147485_1_)
    {
        Block block = this.getBlock(p_147485_1_.func_151340_a(), p_147485_1_.func_151342_b(), p_147485_1_.func_151341_c());
        return block == p_147485_1_.getBlock() ? block.onBlockEventReceived(this, p_147485_1_.func_151340_a(), p_147485_1_.func_151342_b(), p_147485_1_.func_151341_c(), p_147485_1_.getEventID(), p_147485_1_.getEventParameter()) : false;
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    public void flush()
    {
        this.saveHandler.flush();
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
        boolean flag = this.isRaining();
        super.updateWeather();

        if (this.prevRainingStrength != this.rainingStrength)
        {
            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, this.rainingStrength), this.provider.dimensionId);
        }

        if (this.prevThunderingStrength != this.thunderingStrength)
        {
            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, this.thunderingStrength), this.provider.dimensionId);
        }

        /*The function in use here has been replaced in order to only send the weather info to players in the correct dimension,
        rather than to all players on the server. This is what causes the client-side rain, as the
        client believes that it has started raining locally, rather than in another dimension.
        */
        if (flag != this.isRaining())
        {
            if (flag)
            {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(2, 0.0F), this.provider.dimensionId);
            }
            else
            {
                this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(1, 0.0F), this.provider.dimensionId);
            }

            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(7, this.rainingStrength), this.provider.dimensionId);
            this.mcServer.getConfigurationManager().sendPacketToAllPlayersInDimension(new S2BPacketChangeGameState(8, this.thunderingStrength), this.provider.dimensionId);
        }
    }

    protected int func_152379_p()
    {
        return this.mcServer.getConfigurationManager().getViewDistance();
    }

    public MinecraftServer func_73046_m()
    {
        return this.mcServer;
    }

    /**
     * Gets the EntityTracker
     */
    public EntityTracker getEntityTracker()
    {
        return this.theEntityTracker;
    }

    public PlayerManager getPlayerManager()
    {
        return this.thePlayerManager;
    }

    public Teleporter getDefaultTeleporter()
    {
        return this.worldTeleporter;
    }

    public void func_147487_a(String p_147487_1_, double p_147487_2_, double p_147487_4_, double p_147487_6_, int p_147487_8_, double p_147487_9_, double p_147487_11_, double p_147487_13_, double p_147487_15_)
    {
        S2APacketParticles s2apacketparticles = new S2APacketParticles(p_147487_1_, (float)p_147487_2_, (float)p_147487_4_, (float)p_147487_6_, (float)p_147487_9_, (float)p_147487_11_, (float)p_147487_13_, (float)p_147487_15_, p_147487_8_);

        for (int j = 0; j < this.playerEntities.size(); ++j)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntities.get(j);
            ChunkCoordinates chunkcoordinates = entityplayermp.getPlayerCoordinates();
            double d7 = p_147487_2_ - (double)chunkcoordinates.posX;
            double d8 = p_147487_4_ - (double)chunkcoordinates.posY;
            double d9 = p_147487_6_ - (double)chunkcoordinates.posZ;
            double d10 = d7 * d7 + d8 * d8 + d9 * d9;

            if (d10 <= 256.0D)
            {
                entityplayermp.playerNetServerHandler.sendPacket(s2apacketparticles);
            }
        }
    }

    public File getChunkSaveLocation()
    {
        return ((AnvilChunkLoader)theChunkProviderServer.currentChunkLoader).chunkSaveLocation;
    }

    static class ServerBlockEventList extends ArrayList
        {
            private static final String __OBFID = "CL_00001439";

            private ServerBlockEventList() {}

            ServerBlockEventList(Object p_i1521_1_)
            {
                this();
            }
        }
}