package net.minecraft.world.storage;

import java.io.File;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

/**
 * 继承者:<br>
 * 1.AnvilSaveHandler<br>
 * 2.SaveHandler<br>
 * 3.SaveHandlerMP<br>
 * 4.WorldSpecificSaveHandler<br>
 * 接口功能:<br>
 * 读取世界信息，保存世界信息，加载区块，直接获取磁盘保存文件
 */
public interface ISaveHandler
{
    /**
     * 加载并返回世界信息
     */
    WorldInfo loadWorldInfo();

    /**
     * 检查会话锁以防止保存冲突
     */
    void checkSessionLock() throws MinecraftException;

    /**
     * 返回带有提供的世界提供者的块加载器
     */
    IChunkLoader getChunkLoader(WorldProvider p_75763_1_);

    /**
     * 使用给定的 NBTTagCompound 作为播放器保存给定的世界信息。
     */
    void saveWorldInfoWithPlayer(WorldInfo p_75755_1_, NBTTagCompound p_75755_2_);

    /**
     * 保存传入的世界信息。
     */
    void saveWorldInfo(WorldInfo p_75761_1_);

    /**
     * 如果没有相关的 saveHandler（例如 SMP），则返回 null
     */
    IPlayerFileData getSaveHandler();

    /**
     * 调用将所有更改刷新到磁盘，等待它们完成。
     */
    void flush();

    /**
     * 获取这个世界的根目录对应的File对象。
     */
    File getWorldDirectory();

    /**
     * 获取给定映射的文件位置
     */
    File getMapFileFromName(String p_75758_1_);

    /**
     * 返回保存世界信息的目录名称。
     */
    String getWorldDirectoryName();
}