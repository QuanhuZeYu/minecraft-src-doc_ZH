package net.minecraft.world.chunk.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.NibbleArray;

/**
 * 存储一个16*16*16大小的区域
 */
public class ExtendedBlockStorage
{
    /** 包含由此 ExtendedBlockStorage 表示的最底部的 Y 块。通常是 16 的倍数。*/
    private int yBase;
    /** 此块存储的 Chunk 中非 air 块数量的总数。*/
    private int blockRefCount;
    /**
     * 包含此块存储的父块中需要随机更新的数据块数。用于剔除
     * 出于性能原因，从随机时钟更新中获取块。
     */
    private int tickRefCount;
    /** Contains the least significant 8 bits of each block ID belonging to this block storage's parent Chunk. */
    /** 包含属于此块存储的父 Chunk 的每个块 ID 的最低有效 8 位。*/
    private byte[] blockLSBArray;
    // 一个int中有4个Nibble
    /** 包含属于此块存储的父 Chunk 的每个块 ID 的最高有效 4 位。*/
    private NibbleArray blockMSBArray;
    /** 存储与此 ExtendedBlockStorage 中的块关联的元数据。 max: 15*/
    private NibbleArray blockMetadataArray;
    /** 包含块光数据的 NibbleArray。 max: 15*/
    private NibbleArray blocklightArray;
    /** 包含天空光数据块的 NibbleArray。 max: 15*/
    private NibbleArray skylightArray;
    private static final String __OBFID = "CL_00000375";

    public ExtendedBlockStorage(int yBase, boolean hasSkylight)
    {
        this.yBase = yBase;
        this.blockLSBArray = new byte[4096];
        this.blockMetadataArray = new NibbleArray(this.blockLSBArray.length, 4);
        this.blocklightArray = new NibbleArray(this.blockLSBArray.length, 4);

        if (hasSkylight)
        {
            this.skylightArray = new NibbleArray(this.blockLSBArray.length, 4);
        }
    }

    /**
     * 根据扩展ID获取块，该扩展ID由字节数组和NibbleArray合并形成完整的12位块ID。
     * 由于是在chunk内获取，使用chunk坐标系，大小为16*16*16，所以仅需四位即可表示一个维度
     *
     * @param x 块的x坐标
     * @param y 块的y坐标
     * @param z 块的z坐标
     * @return 位于指定坐标处的Block对象
     */
    public Block getBlockByExtId(int x, int y, int z)
    {
        // 从blockLSBArray中获取块ID的低8位
        int blockID = this.blockLSBArray[y << 8 | z << 4 | x] & 255;

        // 如果blockMSBArray不为空，获取块ID的高4位并合并到l中
        if (this.blockMSBArray != null)
        {
            blockID |= this.blockMSBArray.get(x, y, z) << 8;
        }

        // 根据合并后的块ID获取并返回Block对象
        return Block.getBlockById(blockID);
    }

    /**
     * 更新指定位置的方块信息
     * 此方法主要用于更新世界中某一方块的位置信息，包括方块的类型及其相关的引用计数
     * 它首先获取当前位置的方块ID，然后根据新的方块参数更新位置信息和引用计数
     *
     * @param x 方块的x坐标
     * @param y 方块的y坐标
     * @param z 方块的z坐标
     * @param block 要设置的新方块类型
     */
    public void func_150818_a(int x, int y, int z, Block block)
    {
        // 获取当前位置的方块ID的低8位 低八位包含了 X Z
        int blockIDOld = this.blockLSBArray[y << 8 | z << 4 | x] & 255;

        // 如果存在高8位数组，获取方块ID的高8位并合并到低8位
        if (this.blockMSBArray != null)
        {
            blockIDOld |= this.blockMSBArray.get(x, y, z) << 8;
        }

        // 根据方块ID获取方块实例
        Block block1 = Block.getBlockById(blockIDOld);

        // 如果当前位置的方块不是空气，减少方块引用计数和tick引用计数
        if (block1 != Blocks.air)
        {
            --this.blockRefCount;

            if (block1.getTickRandomly())
            {
                --this.tickRefCount;
            }
        }

        // 如果新方块不是空气，增加方块引用计数和tick引用计数
        if (block != Blocks.air)
        {
            ++this.blockRefCount;

            if (block.getTickRandomly())
            {
                ++this.tickRefCount;
            }
        }

        // 获取新方块的ID并更新低8位数组
        int blockID = Block.getIdFromBlock(block);
        this.blockLSBArray[y << 8 | z << 4 | x] = (byte)(blockID & 255);

        // 如果新方块ID大于255，需要更新高8位数组
        if (blockID > 255)
        {
            if (this.blockMSBArray == null)
            {
                this.blockMSBArray = new NibbleArray(this.blockLSBArray.length, 4);
            }

            this.blockMSBArray.set(x, y, z, (blockID & 3840) >> 8); // val: 1111 0000 0000 右移八位
        }
        // 如果新方块ID不大于255但高8位数组存在，将高8位设置为0
        else if (this.blockMSBArray != null)
        {
            this.blockMSBArray.set(x, y, z, 0);
        }
    }

    /**
     * 返回与此 ExtendedBlockStorage 中给定坐标处的块关联的元数据。
     */
    public int getExtBlockMetadata(int x, int y, int z)
    {
        return this.blockMetadataArray.get(x, y, z);
    }

    /**
     * 将ExtendBlockStorage中给定坐标处的Block的元数据设置为给定的元数据。
     */
    public void setExtBlockMetadata(int x, int y, int z, int meta)
    {
        this.blockMetadataArray.set(x, y, z, meta);
    }

    /**
     * 根据其内部引用计数，返回此块存储的 Chunk 是否完全为空。
     */
    public boolean isEmpty()
    {
        return this.blockRefCount == 0;
    }

    /**
     * 返回此块存储的Chunk是否需要随机勾选，用于避免循环
     * 当没有块会随机滴答时，随机块会滴答。
     */
    public boolean getNeedsRandomTick()
    {
        return this.tickRefCount > 0;
    }

    /**
     * 返回此 ExtendedBlockStorage 的 Y 位置。
     */
    public int getYLocation()
    {
        return this.yBase;
    }

    /**
     * 设置扩展块存储结构中保存的Sky-light值。
     */
    public void setExtSkylightValue(int p_76657_1_, int p_76657_2_, int p_76657_3_, int p_76657_4_)
    {
        this.skylightArray.set(p_76657_1_, p_76657_2_, p_76657_3_, p_76657_4_);
    }

    /**
     * 获取扩展块存储结构中保存的Sky-light值。
     */
    public int getExtSkylightValue(int x, int y, int z)
    {
        return this.skylightArray.get(x, y, z);
    }

    /**
     * 设置扩展块存储结构中保存的Block-light值。
     */
    public void setExtBlocklightValue(int x, int y, int z, int val)
    {
        this.blocklightArray.set(x, y, z, val);
    }

    /**
     * 获取扩展块存储结构中保存的Block-light值。
     */
    public int getExtBlocklightValue(int x, int y, int z)
    {
        return this.blocklightArray.get(x, y, z);
    }

    /**
     * 移除无效的方块并重新计算方块引用计数
     * 此方法遍历一个三维区域内的所有方块，排除空气方块，并计算需要更新的方块和需要随机滴答的方块的数量
     */
    public void removeInvalidBlocks()
    {
        // 重置方块引用计数器
        this.blockRefCount = 0;
        // 重置滴答引用计数器
        this.tickRefCount = 0;

        // 遍历三维区域内的所有方块
        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                for (int k = 0; k < 16; ++k)
                {
                    // 获取当前方块
                    Block block = this.getBlockByExtId(i, j, k);

                    // 如果当前方块不是空气，则进行后续处理
                    if (block != Blocks.air)
                    {
                        // 增加方块引用计数
                        ++this.blockRefCount;

                        // 如果当前方块需要随机滴答，则增加滴答引用计数
                        if (block.getTickRandomly())
                        {
                            ++this.tickRefCount;
                        }
                    }
                }
            }
        }
    }

    public byte[] getBlockLSBArray()
    {
        return this.blockLSBArray;
    }

    @SideOnly(Side.CLIENT)
    public void clearMSBArray()
    {
        this.blockMSBArray = null;
    }

    /**
     * 返回此存储数组的 chunk 的块 ID MSB（位 11..8）数组。高四位
     */
    public NibbleArray getBlockMSBArray()
    {
        return this.blockMSBArray;
    }

    /**
     * 返回此存储数组的 chunk 的元数据数组。
     * @return 元数据数组
     */
    public NibbleArray getMetadataArray()
    {
        return this.blockMetadataArray;
    }

    /**
     * 返回包含 Block-light 数据的 NibbleArray 实例。
     */
    public NibbleArray getBlocklightArray()
    {
        return this.blocklightArray;
    }

    /**
     * 返回包含天光数据的 NibbleArray 实例。
     */
    public NibbleArray getSkylightArray()
    {
        return this.skylightArray;
    }

    /**
     * 设置此 ExtendedBlockStorage 的块 ID 最低有效位数组。
     */
    public void setBlockLSBArray(byte[] low8)
    {
        this.blockLSBArray = low8;
    }

    /**
     * 设置此 ExtendedBlockStorage 的 blockID 最高有效位数组 (blockMSBArray)。
     */
    public void setBlockMSBArray(NibbleArray blockIDex)
    {
        this.blockMSBArray = blockIDex;
    }

    /**
     * 设置此 ExtendedBlockStorage 的块元数据 (blockMetadataArray) 的 NibbleArray。
     */
    public void setBlockMetadataArray(NibbleArray metaArray)
    {
        this.blockMetadataArray = metaArray;
    }

    /**
     * 设置用于此特定存储块中的 Block-light 值的 NibbleArray 实例。
     */
    public void setBlocklightArray(NibbleArray blockLightArray)
    {
        this.blocklightArray = blockLightArray;
    }

    /**
     * 设置用于此特定存储块中的 Sky-light 值的 NibbleArray 实例。
     */
    public void setSkylightArray(NibbleArray skylightArray)
    {
        this.skylightArray = skylightArray;
    }

    /**
     * 如果 getBlockMSBArray 返回 null，则由 Chunk 调用以初始化 MSB 数组。返回新创建的
     * NibbleArray 实例。
     */
    @SideOnly(Side.CLIENT)
    public NibbleArray createBlockMSBArray()
    {
        this.blockMSBArray = new NibbleArray(this.blockLSBArray.length, 4);
        return this.blockMSBArray;
    }
}