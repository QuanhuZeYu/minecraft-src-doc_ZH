package net.minecraft.world.chunk.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLLog;

public class AnvilChunkLoader implements IChunkLoader, IThreadedFileIO
{
    private static final Logger logger = LogManager.getLogger();
    private List<PendingChunk> chunksToRemove = new ArrayList();
    private Set<ChunkCoordIntPair> pendingAnvilChunksCoordinates = new HashSet();
    private Object syncLockObject = new Object();
    /** 使用 Anvil 格式保存块的目录 */
    public final File chunkSaveLocation;
    private static final String __OBFID = "CL_00000384";

    public AnvilChunkLoader(File file)
    {
        this.chunkSaveLocation = file;
    }

    /**
     * 检查指定的区块是否存在于待处理的区块列表中或在区域文件缓存中存在
     *
     * @param world 世界对象，用于获取区块信息
     * @param x 区块的X坐标
     * @param z 区块的Z坐标
     * @return 如果区块存在，则返回true；否则返回false
     */
    public boolean chunkExists(World world, int x, int z)
    {
        // 创建一个ChunkCoordIntPair对象来表示区块的坐标
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x, z);

        // 同步代码块，以防止多线程环境下的并发问题
        synchronized (this.syncLockObject)
        {
            // 检查待处理的Anvil区块坐标列表中是否包含当前区块坐标
            if (this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair))
            {
                // 遍历待移除的区块列表，查找匹配的区块坐标
                Iterator<PendingChunk> iter = this.chunksToRemove.iterator();
                while (iter.hasNext())
                {
                    PendingChunk pendingChunk = (PendingChunk)iter.next();
                    // 如果找到匹配的区块坐标，则表明区块存在，返回true
                    if (pendingChunk.chunkCoordinate.equals(chunkcoordintpair))
                    {
                        return true;
                    }
                }
            }
        }

        // 如果没有在待处理列表中找到区块，则调用RegionFileCache的方法来检查区块是否在区域文件缓存中存在
        return RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, x, z).chunkExists(x & 31, z & 31);
    }

    /**
     * 将指定的（XZ）块加载到指定的世界中。
     * 此方法首先调用异步加载块的方法获取块数据，如果数据不为空，则进一步加载实体并返回块。
     *
     * @param world 世界对象，用于指定加载块的目标世界。
     * @param x 块的X坐标，用于指定要加载的块位置。
     * @param z 块的Z坐标，用于指定要加载的块位置。
     * @return 如果成功加载块，则返回加载的Chunk对象；否则返回null。
     * @throws IOException 如果在加载块的过程中发生I/O错误。
     */
    public Chunk loadChunk(World world, int x, int z) throws IOException
    {
        // 异步加载块数据
        Object[] data = this.loadChunk__Async(world, x, z);

        // 检查数据是否为空
        if (data != null)
        {
            // 从数据中获取块和NBT标签复合
            Chunk chunk = (Chunk) data[0];
            NBTTagCompound nbttagcompound = (NBTTagCompound) data[1];

            // 加载块中的实体
            this.loadEntities(world, nbttagcompound.getCompoundTag("Level"), chunk);

            // 返回加载的块
            return chunk;
        }

        // 如果数据为空，返回null
        return null;
    }

    /**
     * 异步加载区块数据
     * <p>
     * 此方法用于异步加载特定坐标区域的区块数据它首先尝试从待处理的区块队列中获取数据如果获取失败，
     * 则尝试从磁盘中读取数据如果数据既不在内存中也没有在磁盘中找到，则返回null
     *
     * @param world 世界对象，用于加载区块数据
     * @param x 区块的x坐标
     * @param z 区块的z坐标
     * @return 区块数据数组，如果未找到区块数据则返回null
     * @throws IOException 如果读取磁盘数据时发生错误
     */
    public Object[] loadChunk__Async(World world, int x, int z) throws IOException
    {
        // 初始化NBT标签复合对象，用于存储区块数据
        NBTTagCompound nbttagcompound = null;
        // 创建区块坐标对象，用于唯一标识区块
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x, z);
        // 获取同步锁对象，用于线程安全
        Object object = this.syncLockObject;

        // 同步代码块，确保线程安全
        synchronized (this.syncLockObject)
        {
            // 检查待处理的区块队列中是否包含请求的区块坐标
            if (this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair))
            {
                // 遍历待移除的区块列表，寻找匹配的区块坐标
                Iterator iter = this.chunksToRemove.iterator();
                while (iter.hasNext())
                {
                    PendingChunk pendingChunk = (PendingChunk)iter.next();
                    // 如果找到匹配的区块坐标，则获取其NBT数据并中断循环
                    if (pendingChunk.chunkCoordinate.equals(chunkcoordintpair))
                    {
                        nbttagcompound = pendingChunk.nbtTags;
                        break;
                    }
                }
            }
        }

        // 如果NBT标签复合对象为空，表示未从内存中找到区块数据，尝试从磁盘读取
        if (nbttagcompound == null)
        {
            // 获取区块数据的输入流
            DataInputStream datainputstream = RegionFileCache.getChunkInputStream(this.chunkSaveLocation, x, z);

            // 如果输入流为空，表示没有找到区块数据，返回null
            if (datainputstream == null)
            {
                return null;
            }

            // 从输入流中读取NBT数据
            nbttagcompound = CompressedStreamTools.read(datainputstream);
        }

        // 使用读取到的NBT数据，异步检查并加载区块
        return this.checkedReadChunkFromNBT__Async(world, x, z, nbttagcompound);
    }

    /**
     * 包装 readChunkFromNBT。检查坐标和几个 NBT 标签。
     */
    protected Chunk checkedReadChunkFromNBT(World world, int x, int z, NBTTagCompound nbtTagCompound)
    {
        Object[] data = this.checkedReadChunkFromNBT__Async(world, x, z, nbtTagCompound);

        if (data != null)
        {
            Chunk chunk = (Chunk) data[0];
            return chunk;
        }

        return null;
    }

    /**
 * 异步从NBT标签复合体中读取并验证区块数据。
 * 该方法用于在单独的线程中处理区块数据加载，确保游戏性能不受影响。
 *
 * @param world 世界对象，表示当前的世界环境
 * @param coordX 区块的X坐标
 * @param coordZ 区块的Z坐标
 * @param nbtTagCompound NBT标签复合体，包含区块的数据
 * @return 返回一个包含Chunk对象和NBTTagCompound对象的数组；如果加载失败则返回null
 */
protected Object[] checkedReadChunkFromNBT__Async(World world, int coordX, int coordZ, NBTTagCompound nbtTagCompound)
{
    // 检查NBT标签复合体是否包含Level键，且其类型为10（Compound Tag）
    if (!nbtTagCompound.hasKey("Level", 10))
    {
        logger.error("区块文件在 " + coordX + "," + coordZ + " 缺少层级数据，跳过");
        return null;
    }
    else if (!nbtTagCompound.getCompoundTag("Level").hasKey("Sections", 9))
    {
        logger.error("区块文件在 " + coordX + "," + coordZ + " 缺少区块数据，跳过");
        return null;
    }
    else
    {
        // 从NBT标签复合体中读取区块数据
        Chunk chunk = this.readChunkFromNBT(world, nbtTagCompound.getCompoundTag("Level"));

        // 检查区块位置是否正确
        if (!chunk.isAtLocation(coordX, coordZ))
        {
            logger.error("区块文件在 " + coordX + "," + coordZ + " 位置错误，需要重新定位。 (期望 " + coordX + ", " + coordZ + ", 实际 " + chunk.xPosition + ", " + chunk.zPosition + ")");
            nbtTagCompound.setInteger("xPos", coordX);
            nbtTagCompound.setInteger("zPos", coordZ);

            // 移动方块实体，因为此时我们还没有加载它们
            NBTTagList tileEntities = nbtTagCompound.getCompoundTag("Level").getTagList("TileEntities", 10);

            if (tileEntities != null)
            {
                // 遍历所有方块实体并更新其坐标
                for (int te = 0; te < tileEntities.tagCount(); te++)
                {
                    NBTTagCompound tileEntity = (NBTTagCompound) tileEntities.getCompoundTagAt(te);
                    int x = tileEntity.getInteger("x") - chunk.xPosition * 16;
                    int z = tileEntity.getInteger("z") - chunk.zPosition * 16;
                    tileEntity.setInteger("x", coordX * 16 + x);
                    tileEntity.setInteger("z", coordZ * 16 + z);
                }
            }

            // 重新读取区块数据以确保位置正确
            chunk = this.readChunkFromNBT(world, nbtTagCompound.getCompoundTag("Level"));
        }

        // 准备返回值，包含Chunk对象和NBTTagCompound对象
        Object[] data = new Object[2];
        data[0] = chunk;
        data[1] = nbtTagCompound;

        // 事件将在ChunkIOProvider.callStage2中触发，因为它必须在TE加载后触发
        // MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, par4NBTTagCompound));

        return data;
    }
}


    /**
     * 将指定的Chunk数据保存到磁盘中
     * 此方法负责将Chunk的数据序列化到NBT标签复合体中，并将其添加到待处理队列中以进行最终的磁盘写入操作
     *
     * @param world 世界对象，用于访问世界特定的数据和方法
     * @param chunk 需要保存的Chunk对象
     * @throws MinecraftException 如果保存过程中发生特定的Minecraft异常
     * @throws IOException 如果保存过程中发生I/O错误
     */
    public void saveChunk(World world, Chunk chunk) throws MinecraftException, IOException
    {
        // 检查世界会话锁，以确保数据一致性
        world.checkSessionLock();

        try
        {
            // 创建NBT标签复合体以存储Chunk数据
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.setTag("Level", nbttagcompound1);

            // 将Chunk数据写入到NBT标签复合体中
            this.writeChunkToNBT(chunk, world, nbttagcompound1);

            // 触发Chunk数据保存事件，允许其他模组修改保存的数据
            MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, nbttagcompound));

            // 将Chunk坐标和其对应的NBT数据添加到待处理队列中
            this.addChunkToPending(chunk.getChunkCoordIntPair(), nbttagcompound);
        }
        catch (Exception exception)
        {
            // 打印异常堆栈跟踪信息，以便于调试和错误追踪
            exception.printStackTrace();
        }
    }

    /**
     * 将区块添加到待处理列表中，以便稍后进行加载或卸载
     * 此方法同步处理区块数据，确保数据一致性
     *
     * @param chunkCoordIntPair 区块的坐标，用于标识区块
     * @param nbtTagCompound 区块的NBT标签复合，包含区块数据
     */
    protected void addChunkToPending(ChunkCoordIntPair chunkCoordIntPair, NBTTagCompound nbtTagCompound)
    {
        // 获取同步锁对象，以确保线程安全
        Object object = this.syncLockObject;

        synchronized (this.syncLockObject)
        {
            // 检查待处理的区块坐标列表中是否已存在该区块
            if (this.pendingAnvilChunksCoordinates.contains(chunkCoordIntPair))
            {
                // 遍历待移除的区块列表，查找并更新已存在的区块数据
                for (int i = 0; i < this.chunksToRemove.size(); ++i)
                {
                    // 如果找到匹配的区块坐标，则更新该区块的数据
                    if (((AnvilChunkLoader.PendingChunk)this.chunksToRemove.get(i)).chunkCoordinate.equals(chunkCoordIntPair))
                    {
                        this.chunksToRemove.set(i, new AnvilChunkLoader.PendingChunk(chunkCoordIntPair, nbtTagCompound));
                        return;
                    }
                }
            }

            // 如果待处理列表中不存在该区块，则将其添加到待移除的区块列表和待处理的区块坐标列表中
            this.chunksToRemove.add(new AnvilChunkLoader.PendingChunk(chunkCoordIntPair, nbtTagCompound));
            this.pendingAnvilChunksCoordinates.add(chunkCoordIntPair);

            // 将当前加载器实例添加到线程IO队列中，以进行异步IO操作
            ThreadedFileIOBase.threadedIOInstance.queueIO(this);
        }
    }

    /**
     * 返回一个布尔值，表明写入是否成功。
     */
    public boolean writeNextIO()
    {
        // 定义一个待处理的区块变量
        AnvilChunkLoader.PendingChunk pendingchunk = null;
        // 定义一个对象，用于同步锁
        Object object = this.syncLockObject;

        // 同步代码块，确保线程安全
        synchronized (this.syncLockObject)
        {
            // 如果待移除的区块列表为空，则返回false
            if (this.chunksToRemove.isEmpty())
            {
                return false;
            }

            // 从待移除的区块列表中取出第一个区块，并移除它
            pendingchunk = (AnvilChunkLoader.PendingChunk)this.chunksToRemove.remove(0);
            // 从待处理的区块坐标列表中移除对应的坐标
            this.pendingAnvilChunksCoordinates.remove(pendingchunk.chunkCoordinate);
        }

        // 如果取出的区块不为空，则尝试写入区块的NBT标签
        if (pendingchunk != null)
        {
            try
            {
                this.writeChunkNBTTags(pendingchunk);
            }
            catch (Exception exception)
            {
                // 如果写入过程中发生异常，则打印异常栈跟踪信息
                exception.printStackTrace();
            }
        }

        // 返回true，表示写入成功
        return true;
    }

    /**
     * 将Chunk的NBT标签写入到文件中
     * <p>
     * 此方法负责将一个待处理的Chunk所包含的NBT标签数据压缩并写入到对应的Chunk文件中
     * 它首先获取到Chunk的保存位置，然后使用压缩流工具将NBT标签数据写入到数据输出流中，
     * 最后关闭输出流以释放资源
     *
     * @param pendingChunk 待写入的Chunk对象，包含待处理的Chunk的坐标和NBT标签数据
     * @throws IOException 如果在写入过程中发生I/O错误
     */
    private void writeChunkNBTTags(AnvilChunkLoader.PendingChunk pendingChunk) throws IOException
    {
        // 获取Chunk的输出流
        DataOutputStream dataoutputstream = RegionFileCache.getChunkOutputStream(this.chunkSaveLocation, pendingChunk.chunkCoordinate.chunkXPos, pendingChunk.chunkCoordinate.chunkZPos);

        // 将NBT标签数据写入到输出流中
        CompressedStreamTools.write(pendingChunk.nbtTags, dataoutputstream);

        // 关闭输出流
        dataoutputstream.close();
    }

    /**
     * 保存与该块相关的额外数据，通常在自动保存期间不保存，仅在块卸载期间保存。
     * 目前未使用。
     */
    public void saveExtraChunkData(World p_75819_1_, Chunk p_75819_2_) {}

    /**
     * 调用每个 World.tick()
     */
    public void chunkTick() {}

    /**
     * 保存与任何Chunk无关的额外数据。  自动保存期间不保存，仅在世界卸载期间保存。  现在
     * 未使用。
     */
    public void saveExtraData()
    {
        while (this.writeNextIO())
        {
            ;
        }
    }

    /**
     * 将作为参数传递的 Chunk 写入同样传递的 NBTTagCompound，使用 World 参数进行检索
     * Chunk的最后更新时间。
     */
    /**
     * 将区块数据写入NBT标签复合对象中
     *
     * @param chunk 待写入NBT的区块对象
     * @param world 区块所属的世界对象
     * @param nbtTagCompound 将要写入区块数据的NBT标签复合对象
     */
    private void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound nbtTagCompound)
    {
        // 设置区块数据版本
        nbtTagCompound.setByte("V", (byte)1);
        // 设置区块的X轴位置
        nbtTagCompound.setInteger("xPos", chunk.xPosition);
        // 设置区块的Z轴位置
        nbtTagCompound.setInteger("zPos", chunk.zPosition);
        // 设置最后更新时间
        nbtTagCompound.setLong("LastUpdate", world.getTotalWorldTime());
        // 设置高度图
        nbtTagCompound.setIntArray("HeightMap", chunk.heightMap);
        // 设置地形是否已生成
        nbtTagCompound.setBoolean("TerrainPopulated", chunk.isTerrainPopulated);
        // 设置光线是否已生成
        nbtTagCompound.setBoolean("LightPopulated", chunk.isLightPopulated);
        // 设置居住时间
        nbtTagCompound.setLong("InhabitedTime", chunk.inhabitedTime);
        // 获取区块中的扩展块存储数组
        ExtendedBlockStorage[] aextendedblockstorage = chunk.getBlockStorageArray();
        // 创建用于存储区块数据的NBT标签列表
        NBTTagList nbttaglist = new NBTTagList();
        // 判断世界是否有天空
        boolean flag = !world.provider.hasNoSky;
        // 遍历扩展块存储数组
        ExtendedBlockStorage[] aextendedblockstorage1 = aextendedblockstorage;
        int i = aextendedblockstorage.length;
        NBTTagCompound nbttagcompound1;

        for (int j = 0; j < i; ++j)
        {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage1[j];

            if (extendedblockstorage != null)
            {
                nbttagcompound1 = new NBTTagCompound();
                // 设置当前扩展块存储的Y轴位置
                nbttagcompound1.setByte("Y", (byte)(extendedblockstorage.getYLocation() >> 4 & 255));
                // 设置Blocks数据
                nbttagcompound1.setByteArray("Blocks", extendedblockstorage.getBlockLSBArray());

                if (extendedblockstorage.getBlockMSBArray() != null)
                {
                    // 设置Add数据
                    nbttagcompound1.setByteArray("Add", extendedblockstorage.getBlockMSBArray().data);
                }

                // 设置Data数据
                nbttagcompound1.setByteArray("Data", extendedblockstorage.getMetadataArray().data);
                // 设置BlockLight数据
                nbttagcompound1.setByteArray("BlockLight", extendedblockstorage.getBlocklightArray().data);

                if (flag)
                {
                    // 如果有天空，则设置SkyLight数据
                    nbttagcompound1.setByteArray("SkyLight", extendedblockstorage.getSkylightArray().data);
                }
                else
                {
                    // 如果没有天空，则设置SkyLight为全黑
                    nbttagcompound1.setByteArray("SkyLight", new byte[extendedblockstorage.getBlocklightArray().data.length]);
                }

                // 将当前扩展块存储的数据添加到NBT标签列表中
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        // 将区块数据列表添加到NBT标签复合对象中
        nbtTagCompound.setTag("Sections", nbttaglist);
        // 设置Biomes数据
        nbtTagCompound.setByteArray("Biomes", chunk.getBiomeArray());
        // 初始化hasEntities标志为false
        chunk.hasEntities = false;
        // 创建用于存储实体数据的NBT标签列表
        NBTTagList nbttaglist2 = new NBTTagList();
        Iterator iterator1;

        for (i = 0; i < chunk.entityLists.length; ++i)
        {
            iterator1 = chunk.entityLists[i].iterator();

            while (iterator1.hasNext())
            {
                Entity entity = (Entity)iterator1.next();
                nbttagcompound1 = new NBTTagCompound();

                try
                {
                    // 尝试将实体数据写入NBT
                    if (entity.writeToNBTOptional(nbttagcompound1))
                    {
                        // 如果写入成功，设置hasEntities标志为true，并将实体数据添加到NBT标签列表中
                        chunk.hasEntities = true;
                        nbttaglist2.appendTag(nbttagcompound1);
                    }
                }
                catch (Exception e)
                {
                    // 如果写入过程中出现异常，记录错误日志
                    FMLLog.log(Level.ERROR, e,
                            "An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author",
                            entity.getClass().getName());
                }
            }
        }

        // 将实体数据列表添加到NBT标签复合对象中
        nbtTagCompound.setTag("Entities", nbttaglist2);
        // 创建用于存储方块实体数据的NBT标签列表
        NBTTagList nbttaglist3 = new NBTTagList();
        iterator1 = chunk.chunkTileEntityMap.values().iterator();

        while (iterator1.hasNext())
        {
            TileEntity tileentity = (TileEntity)iterator1.next();
            nbttagcompound1 = new NBTTagCompound();
            try {
            // 将方块实体数据写入NBT
            tileentity.writeToNBT(nbttagcompound1);
            nbttaglist3.appendTag(nbttagcompound1);
            }
            catch (Exception e)
            {
                // 如果写入过程中出现异常，记录错误日志
                FMLLog.log(Level.ERROR, e,
                        "A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author",
                        tileentity.getClass().getName());
            }
        }

        // 将方块实体数据列表添加到NBT标签复合对象中
        nbtTagCompound.setTag("TileEntities", nbttaglist3);
        // 获取世界中待更新的方块列表
        List list = world.getPendingBlockUpdates(chunk, false);

        if (list != null)
        {
            // 获取当前世界总时间
            long k = world.getTotalWorldTime();
            // 创建用于存储待更新方块数据的NBT标签列表
            NBTTagList nbttaglist1 = new NBTTagList();
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
                NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                // 设置方块ID
                nbttagcompound2.setInteger("i", Block.getIdFromBlock(nextticklistentry.func_151351_a()));
                // 设置方块坐标
                nbttagcompound2.setInteger("x", nextticklistentry.xCoord);
                nbttagcompound2.setInteger("y", nextticklistentry.yCoord);
                nbttagcompound2.setInteger("z", nextticklistentry.zCoord);
                // 设置方块更新时间
                nbttagcompound2.setInteger("t", (int)(nextticklistentry.scheduledTime - k));
                // 设置方块更新优先级
                nbttagcompound2.setInteger("p", nextticklistentry.priority);
                // 将方块更新数据添加到NBT标签列表中
                nbttaglist1.appendTag(nbttagcompound2);
            }

            // 将方块更新数据列表添加到NBT标签复合对象中
            nbtTagCompound.setTag("TileTicks", nbttaglist1);
        }
    }

    /**
     * Reads the data stored in the passed NBTTagCompound and creates a Chunk with that data in the passed World.
     * Returns the created Chunk.
     */
    private Chunk readChunkFromNBT(World p_75823_1_, NBTTagCompound p_75823_2_)
    {
        int i = p_75823_2_.getInteger("xPos");
        int j = p_75823_2_.getInteger("zPos");
        Chunk chunk = new Chunk(p_75823_1_, i, j);
        chunk.heightMap = p_75823_2_.getIntArray("HeightMap");
        chunk.isTerrainPopulated = p_75823_2_.getBoolean("TerrainPopulated");
        chunk.isLightPopulated = p_75823_2_.getBoolean("LightPopulated");
        chunk.inhabitedTime = p_75823_2_.getLong("InhabitedTime");
        NBTTagList nbttaglist = p_75823_2_.getTagList("Sections", 10);
        byte b0 = 16;
        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[b0];
        boolean flag = !p_75823_1_.provider.hasNoSky;

        for (int k = 0; k < nbttaglist.tagCount(); ++k)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
            byte b1 = nbttagcompound1.getByte("Y");
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(b1 << 4, flag);
            extendedblockstorage.setBlockLSBArray(nbttagcompound1.getByteArray("Blocks"));

            if (nbttagcompound1.hasKey("Add", 7))
            {
                extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
            }

            extendedblockstorage.setBlockMetadataArray(new NibbleArray(nbttagcompound1.getByteArray("Data"), 4));
            extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4));

            if (flag)
            {
                extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4));
            }

            extendedblockstorage.removeInvalidBlocks();
            aextendedblockstorage[b1] = extendedblockstorage;
        }

        chunk.setStorageArrays(aextendedblockstorage);

        if (p_75823_2_.hasKey("Biomes", 7))
        {
            chunk.setBiomeArray(p_75823_2_.getByteArray("Biomes"));
        }

        // End this method here and split off entity loading to another method
        return chunk;
    }

    public void loadEntities(World p_75823_1_, NBTTagCompound p_75823_2_, Chunk chunk)
    {
        NBTTagList nbttaglist1 = p_75823_2_.getTagList("Entities", 10);

        if (nbttaglist1 != null)
        {
            for (int l = 0; l < nbttaglist1.tagCount(); ++l)
            {
                NBTTagCompound nbttagcompound3 = nbttaglist1.getCompoundTagAt(l);
                Entity entity2 = EntityList.createEntityFromNBT(nbttagcompound3, p_75823_1_);
                chunk.hasEntities = true;

                if (entity2 != null)
                {
                    chunk.addEntity(entity2);
                    Entity entity = entity2;

                    for (NBTTagCompound nbttagcompound2 = nbttagcompound3; nbttagcompound2.hasKey("Riding", 10); nbttagcompound2 = nbttagcompound2.getCompoundTag("Riding"))
                    {
                        Entity entity1 = EntityList.createEntityFromNBT(nbttagcompound2.getCompoundTag("Riding"), p_75823_1_);

                        if (entity1 != null)
                        {
                            chunk.addEntity(entity1);
                            entity.mountEntity(entity1);
                        }

                        entity = entity1;
                    }
                }
            }
        }

        NBTTagList nbttaglist2 = p_75823_2_.getTagList("TileEntities", 10);

        if (nbttaglist2 != null)
        {
            for (int i1 = 0; i1 < nbttaglist2.tagCount(); ++i1)
            {
                NBTTagCompound nbttagcompound4 = nbttaglist2.getCompoundTagAt(i1);
                TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound4);

                if (tileentity != null)
                {
                    chunk.addTileEntity(tileentity);
                }
            }
        }

        if (p_75823_2_.hasKey("TileTicks", 9))
        {
            NBTTagList nbttaglist3 = p_75823_2_.getTagList("TileTicks", 10);

            if (nbttaglist3 != null)
            {
                for (int j1 = 0; j1 < nbttaglist3.tagCount(); ++j1)
                {
                    NBTTagCompound nbttagcompound5 = nbttaglist3.getCompoundTagAt(j1);
                    p_75823_1_.func_147446_b(nbttagcompound5.getInteger("x"), nbttagcompound5.getInteger("y"), nbttagcompound5.getInteger("z"), Block.getBlockById(nbttagcompound5.getInteger("i")), nbttagcompound5.getInteger("t"), nbttagcompound5.getInteger("p"));
                }
            }
        }

        // return chunk;
    }

    static class PendingChunk
        {
            public final ChunkCoordIntPair chunkCoordinate;
            public final NBTTagCompound nbtTags;
            private static final String __OBFID = "CL_00000385";

            public PendingChunk(ChunkCoordIntPair p_i2002_1_, NBTTagCompound p_i2002_2_)
            {
                this.chunkCoordinate = p_i2002_1_;
                this.nbtTags = p_i2002_2_;
            }
        }
}