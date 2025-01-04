package net.minecraft.world.chunk.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilSaveConverter extends SaveFormatOld
{
    private static final Logger logger = LogManager.getLogger();
    private static final String __OBFID = "CL_00000582";

    public AnvilSaveConverter(File p_i2144_1_)
    {
        super(p_i2144_1_);
    }

    @SideOnly(Side.CLIENT)
    public String func_154333_a()
    {
        return "Anvil";
    }

    @SideOnly(Side.CLIENT)
    public List<net.minecraft.world.storage.SaveFormatComparator> getSaveList() throws AnvilConverterException
    {
        if (this.savesDirectory != null && this.savesDirectory.exists() && this.savesDirectory.isDirectory())
        {
            ArrayList arraylist = new ArrayList();
            File[] afile = this.savesDirectory.listFiles();
            File[] afile1 = afile;
            int i = afile.length;

            for (int j = 0; j < i; ++j)
            {
                File file1 = afile1[j];

                if (file1.isDirectory())
                {
                    String s = file1.getName();
                    WorldInfo worldinfo = this.getWorldInfo(s);

                    if (worldinfo != null && (worldinfo.getSaveVersion() == 19132 || worldinfo.getSaveVersion() == 19133))
                    {
                        boolean flag = worldinfo.getSaveVersion() != this.getSaveVersion();
                        String s1 = worldinfo.getWorldName();

                        if (s1 == null || MathHelper.stringNullOrLengthZero(s1))
                        {
                            s1 = s;
                        }

                        long k = 0L;
                        arraylist.add(new SaveFormatComparator(s, s1, worldinfo.getLastTimePlayed(), k, worldinfo.getGameType(), flag, worldinfo.isHardcoreModeEnabled(), worldinfo.areCommandsAllowed()));
                    }
                }
            }

            return arraylist;
        }
        else
        {
            throw new AnvilConverterException("Unable to read or access folder where game worlds are saved!");
        }
    }

    protected int getSaveVersion()
    {
        return 19133;
    }

    public void flushCache()
    {
        RegionFileCache.clearRegionFileReferences();
    }

    /**
     * 返回指定保存目录的加载器
     */
    public ISaveHandler getSaveLoader(String p_75804_1_, boolean p_75804_2_)
    {
        return new AnvilSaveHandler(this.savesDirectory, p_75804_1_, p_75804_2_);
    }

    @SideOnly(Side.CLIENT)
    public boolean func_154334_a(String p_154334_1_)
    {
        WorldInfo worldinfo = this.getWorldInfo(p_154334_1_);
        return worldinfo != null && worldinfo.getSaveVersion() == 19132;
    }

    /**
     * 检查保存目录是否使用旧地图格式
     */
    public boolean isOldMapFormat(String p_75801_1_)
    {
        WorldInfo worldinfo = this.getWorldInfo(p_75801_1_);
        return worldinfo != null && worldinfo.getSaveVersion() != this.getSaveVersion();
    }

    /**
     * 将指定的地图转换为新的地图格式。参数：世界名称、加载屏幕
     */
    public boolean convertMapFormat(String s, IProgressUpdate iProgressUpdate)
    {
        // 初始化加载进度为0
        iProgressUpdate.setLoadingProgress(0);

        // 创建ArrayList用于存储区域文件
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        ArrayList arraylist2 = new ArrayList();

        // 定义世界、地狱和天空维度的文件路径
        File file1 = new File(this.savesDirectory, s);
        File file2 = new File(file1, "DIM-1");
        File file3 = new File(file1, "DIM1");

        // 通知日志开始扫描文件夹
        logger.info("Scanning folders...");

        // 将世界维度的区域文件添加到集合中
        this.addRegionFilesToCollection(file1, arraylist);

        // 如果地狱维度的文件夹存在，则将其中的区域文件添加到集合中
        if (file2.exists())
        {
            this.addRegionFilesToCollection(file2, arraylist1);
        }

        // 如果天空维度的文件夹存在，则将其中的区域文件添加到集合中
        if (file3.exists())
        {
            this.addRegionFilesToCollection(file3, arraylist2);
        }

        // 计算总共需要转换的区域文件数量
        int i = arraylist.size() + arraylist1.size() + arraylist2.size();

        // 通知日志总共需要转换的区域文件数量
        logger.info("Total conversion count is " + i);

        // 获取世界信息
        WorldInfo worldinfo = this.getWorldInfo(s);

        // 根据世界类型选择合适的WorldChunkManager
        Object object = null;
        if (worldinfo.getTerrainType() == WorldType.FLAT)
        {
            object = new WorldChunkManagerHell(BiomeGenBase.plains, 0.5F);
        }
        else
        {
            object = new WorldChunkManager(worldinfo.getSeed(), worldinfo.getTerrainType());
        }

        // 转换世界维度的区域文件
        this.convertFile(new File(file1, "region"), arraylist, (WorldChunkManager)object, 0, i, iProgressUpdate);

        // 转换地狱维度的区域文件
        this.convertFile(new File(file2, "region"), arraylist1, new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F), arraylist.size(), i, iProgressUpdate);

        // 转换天空维度的区域文件
        this.convertFile(new File(file3, "region"), arraylist2, new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F), arraylist.size() + arraylist1.size(), i, iProgressUpdate);

        // 更新世界信息的保存版本
        worldinfo.setSaveVersion(19133);

        // 如果世界类型为DEFAULT_1_1，则更新为DEFAULT
        if (worldinfo.getTerrainType() == WorldType.DEFAULT_1_1)
        {
            worldinfo.setTerrainType(WorldType.DEFAULT);
        }

        // 创建世界信息文件
        this.createFile(s);

        // 获取世界保存处理器
        ISaveHandler isavehandler = this.getSaveLoader(s, false);

        // 保存世界信息
        isavehandler.saveWorldInfo(worldinfo);

        // 返回转换成功
        return true;
    }

    /**
     * 创建一个指定名称的备份文件
     * 此方法旨在对level.dat文件进行备份，通过重命名为level.dat_mcr
     *
     * @param fileName 备份文件的名称，通常为level.dat_mcr
     */
    private void createFile(String fileName)
    {
        // 创建备份文件的完整路径
        File file1 = new File(this.savesDirectory, fileName);

        // 检查备份文件是否存在，如果不存在则输出警告信息
        if (!file1.exists())
        {
            logger.warn("Unable to create level.dat_mcr backup");
        }
        else
        {
            // 检查level.dat文件是否存在，这是需要备份的原始文件
            File file2 = new File(file1, "level.dat");

            // 如果level.dat不存在，则输出警告信息
            if (!file2.exists())
            {
                logger.warn("Unable to create level.dat_mcr backup");
            }
            else
            {
                // 创建level.dat_mcr文件的路径，准备进行重命名操作
                File file3 = new File(file1, "level.dat_mcr");

                // 尝试将level.dat重命名为level.dat_mcr，如果失败则输出警告信息
                if (!file2.renameTo(file3))
                {
                    logger.warn("Unable to create level.dat_mcr backup");
                }
            }
        }
    }

    private void convertFile(File p_75813_1_, Iterable p_75813_2_, WorldChunkManager p_75813_3_, int p_75813_4_, int p_75813_5_, IProgressUpdate p_75813_6_)
    {
        Iterator iterator = p_75813_2_.iterator();

        while (iterator.hasNext())
        {
            File file2 = (File)iterator.next();
            this.convertChunks(p_75813_1_, file2, p_75813_3_, p_75813_4_, p_75813_5_, p_75813_6_);
            ++p_75813_4_;
            int k = (int)Math.round(100.0D * (double)p_75813_4_ / (double)p_75813_5_);
            p_75813_6_.setLoadingProgress(k);
        }
    }

    /**
     * 通过 AnvilConverterData 将 32x32 块集从 par2File 复制到 par1File
     */
    private void convertChunks(File file, File file1, WorldChunkManager worldChunkManager, int x, int y, IProgressUpdate iProgressUpdate)
    {
        try
        {
            // 获取源文件名
            String s = file1.getName();
            // 创建源区域文件和目标区域文件对象
            RegionFile regionfile = new RegionFile(file1);
            RegionFile regionfile1 = new RegionFile(new File(file, s.substring(0, s.length() - ".mcr".length()) + ".mca"));

            // 遍历 32x32 的区块
            for (int k = 0; k < 32; ++k)
            {
                int l;

                for (l = 0; l < 32; ++l)
                {
                    // 检查源文件中区块是否已保存且目标文件中区块是否未保存
                    if (regionfile.isChunkSaved(k, l) && !regionfile1.isChunkSaved(k, l))
                    {
                        // 获取源文件中区块的数据输入流
                        DataInputStream datainputstream = regionfile.getChunkDataInputStream(k, l);

                        if (datainputstream == null)
                        {
                            logger.warn("Failed to fetch input stream");
                        }
                        else
                        {
                            // 读取并解析区块数据
                            NBTTagCompound nbttagcompound = CompressedStreamTools.read(datainputstream);
                            datainputstream.close();
                            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Level");
                            ChunkLoader.AnvilConverterData anvilconverterdata = ChunkLoader.load(nbttagcompound1);
                            // 创建新的 NBT 标签复合对象用于存储转换后的数据
                            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
                            nbttagcompound2.setTag("Level", nbttagcompound3);
                            // 将数据转换为 Anvil 格式并写入目标文件
                            ChunkLoader.convertToAnvilFormat(anvilconverterdata, nbttagcompound3, worldChunkManager);
                            DataOutputStream dataoutputstream = regionfile1.getChunkDataOutputStream(k, l);
                            CompressedStreamTools.write(nbttagcompound2, dataoutputstream);
                            dataoutputstream.close();
                        }
                    }
                }

                // 计算并更新转换进度
                l = (int)Math.round(100.0D * (double)(x * 1024) / (double)(y * 1024));
                int i1 = (int)Math.round(100.0D * (double)((k + 1) * 32 + x * 1024) / (double)(y * 1024));

                if (i1 > l)
                {
                    iProgressUpdate.setLoadingProgress(i1);
                }
            }

            // 关闭文件
            regionfile.close();
            regionfile1.close();
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    /**
     * filters the files in the par1 directory, and adds them to the par2 collections
     */
    private void addRegionFilesToCollection(File p_75810_1_, Collection p_75810_2_)
    {
        File file2 = new File(p_75810_1_, "region");
        File[] afile = file2.listFiles(new FilenameFilter()
        {
            private static final String __OBFID = "CL_00000583";
            public boolean accept(File p_accept_1_, String p_accept_2_)
            {
                return p_accept_2_.endsWith(".mcr");
            }
        });

        if (afile != null)
        {
            Collections.addAll(p_75810_2_, afile);
        }
    }
}