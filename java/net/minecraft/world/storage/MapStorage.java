package net.minecraft.world.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.WorldSavedData;

/**
 * 存储了哪些区块是需要保存的，以及物品字符串ID与数字ID的map
 */
public class MapStorage
{
    private ISaveHandler saveHandler;
    /** 将项目数据字符串 id 映射到加载的 MapDataBases */
    private Map<String, WorldSavedData> loadedDataMap = new HashMap<>();
    /** 已加载的 MapDataBase 列表。 */
    private List<WorldSavedData> loadedDataList = new ArrayList<>();
    /** MapDataBase id 字符串前缀（'map' 等）到该前缀的最大已知唯一短 id（0 部分等）的映射 */
    private Map<String, Short> idCounts = new HashMap<>();
    private static final String __OBFID = "CL_00000604";

    public MapStorage(ISaveHandler p_i2162_1_)
    {
        this.saveHandler = p_i2162_1_;
        this.loadIdCounts();
    }

    /**
     * 从磁盘加载与给定 String id 对应的现有 MapDataBase，实例化给定类，或者
     * 如果不存在这样的文件则返回 null。 args：要实例化的类，字符串 dataid
     */
    public WorldSavedData loadData(Class<? extends net.minecraft.world.WorldSavedData> worldSaveData, String id)
    {
        WorldSavedData worldsaveddata = (WorldSavedData)this.loadedDataMap.get(id);

        if (worldsaveddata != null)
        {
            return worldsaveddata;
        }
        else
        {
            if (this.saveHandler != null)
            {
                try
                {
                    File file1 = this.saveHandler.getMapFileFromName(id);

                    if (file1 != null && file1.exists())
                    {
                        try
                        {
                            worldsaveddata = (WorldSavedData)worldSaveData.getConstructor(new Class[] {String.class}).newInstance(new Object[] {id});
                        }
                        catch (Exception exception)
                        {
                            throw new RuntimeException("Failed to instantiate " + worldSaveData.toString(), exception);
                        }

                        FileInputStream fileinputstream = new FileInputStream(file1);
                        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                        worldsaveddata.readFromNBT(nbttagcompound.getCompoundTag("data"));
                    }
                }
                catch (Exception exception1)
                {
                    exception1.printStackTrace();
                }
            }

            if (worldsaveddata != null)
            {
                this.loadedDataMap.put(id, worldsaveddata);
                this.loadedDataList.add(worldsaveddata);
            }

            return worldsaveddata;
        }
    }

    /**
     * 将给定的字符串 id 分配给给定的 MapDataBase，删除任何现有的相同 id。
     */
    public void setData(String p_75745_1_, WorldSavedData worldSavedData)
    {
        if (worldSavedData == null)
        {
            throw new RuntimeException("Can\'t set null data");
        }
        else
        {
            if (this.loadedDataMap.containsKey(p_75745_1_))
            {
                this.loadedDataList.remove(this.loadedDataMap.remove(p_75745_1_));
            }

            this.loadedDataMap.put(p_75745_1_, worldSavedData);
            this.loadedDataList.add(worldSavedData);
        }
    }

    /**
     * 将所有脏加载的 MapDataBase 保存到磁盘。
     */
    public void saveAllData()
    {
        for (int i = 0; i < this.loadedDataList.size(); ++i)
        {
            WorldSavedData worldsaveddata = (WorldSavedData)this.loadedDataList.get(i);

            if (worldsaveddata.isDirty())
            {
                this.saveData(worldsaveddata);
                worldsaveddata.setDirty(false);
            }
        }
    }

    /**
     * 将给定的MapDataBase 保存到磁盘。
     */
    private void saveData(WorldSavedData worldSavedData)
    {
        if (this.saveHandler != null)
        {
            try
            {
                File file1 = this.saveHandler.getMapFileFromName(worldSavedData.mapName);

                if (file1 != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    worldSavedData.writeToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("data", nbttagcompound);
                    FileOutputStream fileoutputstream = new FileOutputStream(file1);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                    fileoutputstream.close();
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * 从“idcounts”文件加载 idCounts 映射。
     */
    private void loadIdCounts()
    {
        try
        {
            this.idCounts.clear();

            if (this.saveHandler == null)
            {
                return;
            }

            File idMap = this.saveHandler.getMapFileFromName("idcounts");

            if (idMap != null && idMap.exists())
            {
                DataInputStream idMapStream = new DataInputStream(new FileInputStream(idMap));
                NBTTagCompound nbttagcompound = CompressedStreamTools.read(idMapStream);
                idMapStream.close();
                Iterator<String> iterator = nbttagcompound.func_150296_c().iterator();

                while (iterator.hasNext())
                {
                    String s = (String)iterator.next();
                    NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase instanceof NBTTagShort)
                    {
                        NBTTagShort nbttagshort = (NBTTagShort)nbtbase;
                        short short1 = nbttagshort.func_150289_e();
                        this.idCounts.put(s, Short.valueOf(short1));
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * 返回给定前缀的唯一新数据 ID，并将 idCounts 映射保存到“idcounts”文件中。
     */
    public int getUniqueDataId(String id)
    {
        Short oshort = (Short)this.idCounts.get(id);

        if (oshort == null)
        {
            oshort = Short.valueOf((short)0);
        }
        else
        {
            oshort = Short.valueOf((short)(oshort.shortValue() + 1));
        }

        this.idCounts.put(id, oshort);

        if (this.saveHandler == null)
        {
            return oshort.shortValue();
        }
        else
        {
            try
            {
                File file1 = this.saveHandler.getMapFileFromName("idcounts");

                if (file1 != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    Iterator iterator = this.idCounts.keySet().iterator();

                    while (iterator.hasNext())
                    {
                        String s1 = (String)iterator.next();
                        short short1 = ((Short)this.idCounts.get(s1)).shortValue();
                        nbttagcompound.setShort(s1, short1);
                    }

                    DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));
                    CompressedStreamTools.write(nbttagcompound, dataoutputstream);
                    dataoutputstream.close();
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            return oshort.shortValue();
        }
    }
}