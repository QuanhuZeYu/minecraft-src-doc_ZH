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

public class MapStorage
{
    private ISaveHandler saveHandler;
    /** Map of item data String id to loaded MapDataBases */
    private Map loadedDataMap = new HashMap();
    /** List of loaded MapDataBases. */
    private List loadedDataList = new ArrayList();
    /** MapDataBase id 字符串前缀（'map' 等）到该前缀的最大已知唯一短 id（0 部分等）的映射 */
    private Map idCounts = new HashMap();
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
    public WorldSavedData loadData(Class<? extends net.minecraft.world.WorldSavedData> p_75742_1_, String p_75742_2_)
    {
        WorldSavedData worldsaveddata = (WorldSavedData)this.loadedDataMap.get(p_75742_2_);

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
                    File file1 = this.saveHandler.getMapFileFromName(p_75742_2_);

                    if (file1 != null && file1.exists())
                    {
                        try
                        {
                            worldsaveddata = (WorldSavedData)p_75742_1_.getConstructor(new Class[] {String.class}).newInstance(new Object[] {p_75742_2_});
                        }
                        catch (Exception exception)
                        {
                            throw new RuntimeException("Failed to instantiate " + p_75742_1_.toString(), exception);
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
                this.loadedDataMap.put(p_75742_2_, worldsaveddata);
                this.loadedDataList.add(worldsaveddata);
            }

            return worldsaveddata;
        }
    }

    /**
     * Assigns the given String id to the given MapDataBase, removing any existing ones of the same id.
     */
    public void setData(String p_75745_1_, WorldSavedData p_75745_2_)
    {
        if (p_75745_2_ == null)
        {
            throw new RuntimeException("Can\'t set null data");
        }
        else
        {
            if (this.loadedDataMap.containsKey(p_75745_1_))
            {
                this.loadedDataList.remove(this.loadedDataMap.remove(p_75745_1_));
            }

            this.loadedDataMap.put(p_75745_1_, p_75745_2_);
            this.loadedDataList.add(p_75745_2_);
        }
    }

    /**
     * Saves all dirty loaded MapDataBases to disk.
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
     * Saves the given MapDataBase to disk.
     */
    private void saveData(WorldSavedData p_75747_1_)
    {
        if (this.saveHandler != null)
        {
            try
            {
                File file1 = this.saveHandler.getMapFileFromName(p_75747_1_.mapName);

                if (file1 != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    p_75747_1_.writeToNBT(nbttagcompound);
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
     * Loads the idCounts Map from the 'idcounts' file.
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

            File file1 = this.saveHandler.getMapFileFromName("idcounts");

            if (file1 != null && file1.exists())
            {
                DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));
                NBTTagCompound nbttagcompound = CompressedStreamTools.read(datainputstream);
                datainputstream.close();
                Iterator iterator = nbttagcompound.func_150296_c().iterator();

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
     * Returns an unique new data id for the given prefix and saves the idCounts map to the 'idcounts' file.
     */
    public int getUniqueDataId(String p_75743_1_)
    {
        Short oshort = (Short)this.idCounts.get(p_75743_1_);

        if (oshort == null)
        {
            oshort = Short.valueOf((short)0);
        }
        else
        {
            oshort = Short.valueOf((short)(oshort.shortValue() + 1));
        }

        this.idCounts.put(p_75743_1_, oshort);

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