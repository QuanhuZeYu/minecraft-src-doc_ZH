package net.minecraft.world;

import net.minecraft.nbt.NBTTagCompound;

public abstract class WorldSavedData
{
    /** 地图数据nbt的名称 */
    public final String mapName;
    /** 这个MapDataBase是否需要保存到磁盘。 */
    private boolean dirty;
    private static final String __OBFID = "CL_00000580";

    public WorldSavedData(String p_i2141_1_)
    {
        this.mapName = p_i2141_1_;
    }

    /**
     * 将 NBTTagCompound 中的数据读入此 MapDataBase
     */
    public abstract void readFromNBT(NBTTagCompound p_76184_1_);

    /**
     * 从这个MapDataBase写入数据到NBTTagCompound，类似于Entities和TileEntities
     */
    public abstract void writeToNBT(NBTTagCompound p_76187_1_);

    /**
     * 将此 MapDataBase 标记为脏，以便在下次保存关卡时将其保存到磁盘。
     */
    public void markDirty()
    {
        this.setDirty(true);
    }

    /**
     * 设置此MapDataBase的脏状态，是否需要保存到磁盘。
     */
    public void setDirty(boolean p_76186_1_)
    {
        this.dirty = p_76186_1_;
    }

    /**
     * 该MapDataBase是否需要保存到磁盘。
     */
    public boolean isDirty()
    {
        return this.dirty;
    }
}