package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NBTTagCompound extends NBTBase
{
    private static final Logger logger = LogManager.getLogger();
    /** 标签的键值对。每个键都是一个 UTF 字符串，每个值都是一个标签。 */
    private Map<String, NBTBase> tagMap = new HashMap<>();
    private static final String __OBFID = "CL_00001215";

    /**
     * 写入标签的实际数据内容，在NBT扩展类中实现
     */
    void write(DataOutput output) throws IOException
    {
        // 创建一个迭代器来遍历标签映射中的键
        Iterator<String> iterator = this.tagMap.keySet().iterator();

        // 遍历标签映射
        while (iterator.hasNext())
        {
            // 获取当前标签的键
            String s = (String)iterator.next();
            // 根据键获取对应的NBT标签对象
            NBTBase nbtbase = (NBTBase)this.tagMap.get(s);
            // 调用方法将当前标签的键和对应的NBT标签对象写入输出流
            func_150298_a(s, nbtbase, output);
        }

        // 写入一个表示结束的字节
        output.writeByte(0);
    }

    /**
     * func_152446_a <br>
     * 从DataInput中读取NBT数据
     * <p>
     * 此方法负责解析NBT数据结构，它首先检查深度是否超过最大值（512），以防止过深的解析导致性能问题
     * 然后，它清空当前的tag映射，并根据输入数据重新填充这个映射
     * 它通过循环读取每个标签的类型、名称和值，直到遇到类型为0的标签，表示数据结束
     *
     * @param input 数据输入流，用于读取NBT数据
     * @param depth 当前解析的深度，用于防止过深的解析
     * @param sizeTracker NBT大小跟踪器，用于监控读取的数据量，防止过量读取
     * @throws IOException 如果读取数据时发生I/O错误
     */
    void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        // 检查深度是否超过最大值，如果超过则抛出异常
        if (depth > 512)
        {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        else
        {
            // 清空当前的tag映射，准备读取新的数据
            this.tagMap.clear();
            byte b0;

            // 循环读取NBT标签，直到遇到类型为0的标签
            while ((b0 = func_152447_a(input, sizeTracker)) != 0)
            {
                // 读取标签的名称
                String s = func_152448_b(input, sizeTracker);
                // 读取字符串长度，包括头部，确保正确读取
                NBTSizeTracker.readUTF(sizeTracker, s); // Forge: Correctly read String length including header.
                // 根据标签类型和名称，读取标签的值
                NBTBase nbtbase = func_152449_a(b0, s, input, depth + 1, sizeTracker);
                // 将标签添加到映射中
                this.tagMap.put(s, nbtbase);
            }
        }
    }

    /**
     * func_150296_c
     * @return 所有标签的键的集合
     */
    public Set<String> getKeys()
    {
        return this.tagMap.keySet();
    }

    /**
     * 获取标签的类型字节。
     */
    public byte getId()
    {
        return (byte)10;
    }

    /**
     * 使用给定的字符串键将给定的标签存储到映射中。这主要用于存储标签列表。
     */
    public void setTag(String key, NBTBase value)
    {
        this.tagMap.put(key, value);
    }

    /**
     * 将具有给定字节值的新 NBTTagByte 存储到具有给定字符串键的映射中。
     */
    public void setByte(String key, byte value)
    {
        this.tagMap.put(key, new NBTTagByte(value));
    }

    /**
     * 将具有给定短值的新 NBTTagShort 存储到具有给定字符串键的映射中。
     */
    public void setShort(String key, short value)
    {
        this.tagMap.put(key, new NBTTagShort(value));
    }

    /**
     * 将具有给定整数值的新 NBTTagInt 存储到具有给定字符串键的映射中。
     */
    public void setInteger(String key, int value)
    {
        this.tagMap.put(key, new NBTTagInt(value));
    }

    /**
     * 将具有给定 long 值的新 NBTTagLong 存储到具有给定字符串键的映射中。
     */
    public void setLong(String key, long value)
    {
        this.tagMap.put(key, new NBTTagLong(value));
    }

    /**
     * 将具有给定浮点值的新 NBTTagFloat 存储到具有给定字符串键的映射中。
     */
    public void setFloat(String key, float value)
    {
        this.tagMap.put(key, new NBTTagFloat(value));
    }

    /**
     * 将具有给定双精度值的新 NBTTagDouble 存储到具有给定字符串键的映射中。
     */
    public void setDouble(String key, double value)
    {
        this.tagMap.put(key, new NBTTagDouble(value));
    }

    /**
     * 将具有给定字符串值的新 NBTTagString 存储到具有给定字符串键的映射中。
     */
    public void setString(String key, String value)
    {
        this.tagMap.put(key, new NBTTagString(value));
    }

    /**
     * 将具有给定数组的新 NBTTagByteArray 作为数据存储到具有给定字符串键的映射中。
     */
    public void setByteArray(String key, byte[] value)
    {
        this.tagMap.put(key, new NBTTagByteArray(value));
    }

    /**
     * 将具有给定数组的新 NBTTagIntArray 作为数据存储到具有给定字符串键的映射中。
     */
    public void setIntArray(String key, int[] value)
    {
        this.tagMap.put(key, new NBTTagIntArray(value));
    }

    /**
     * 使用给定的字符串键将给定的布尔值存储为 NBTTagByte，存储 1 表示 true，0 表示 false。
     */
    public void setBoolean(String key, boolean value)
    {
        this.setByte(key, (byte)(value ? 1 : 0));
    }

    /**
     * 获取具有指定名称的通用标签
     */
    public NBTBase getTag(String key)
    {
        return (NBTBase)this.tagMap.get(key);
    }

    /**
     * func_150299_b<br>
     * 根据键获取对应的NBTBase对象类型标识
     *
     * @param key NBT标签的键值，用于唯一标识一个NBT标签
     * @return 如果找到对应的NBTBase对象，则返回其类型标识；否则返回0
     */
    public byte getID(String key)
    {
        // 从tagMap中根据key获取对应的NBTBase对象
        NBTBase nbtbase = (NBTBase)this.tagMap.get(key);
        // 如果nbtbase不为空，则返回其类型标识；否则返回0
        return nbtbase != null ? nbtbase.getId() : 0;
    }

    /**
     * 返回给定字符串之前是否已作为键存储在映射中。
     */
    public boolean hasKey(String key)
    {
        return this.tagMap.containsKey(key);
    }

    public boolean hasKey(String key, int type)
    {
        byte b0 = this.getID(key);
        return b0 == type ? true : (type != 99 ? false : b0 == 1 || b0 == 2 || b0 == 3 || b0 == 4 || b0 == 5 || b0 == 6);
    }

    /**
     * 使用指定的键检索字节值，如果没有存储此类键，则检索 0。
     */
    public byte getByte(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150290_f();
        }
        catch (ClassCastException classcastexception)
        {
            return (byte)0;
        }
    }

    /**
     * 使用指定的键检索短值，如果没有存储此类键，则检索 0。
     */
    public short getShort(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150289_e();
        }
        catch (ClassCastException classcastexception)
        {
            return (short)0;
        }
    }

    /**
     * 使用指定的键检索整数值，如果没有存储此类键，则检索 0。
     */
    public int getInteger(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0 : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150287_d();
        }
        catch (ClassCastException classcastexception)
        {
            return 0;
        }
    }

    /**
     * 使用指定的键检索长整型值，如果没有存储此类键，则检索 0。
     */
    public long getLong(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0L : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150291_c();
        }
        catch (ClassCastException classcastexception)
        {
            return 0L;
        }
    }

    /**
     * 使用指定的键检索浮点值，如果没有存储此类键，则检索 0。
     */
    public float getFloat(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0.0F : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150288_h();
        }
        catch (ClassCastException classcastexception)
        {
            return 0.0F;
        }
    }

    /**
     * 使用指定的键检索双精度值，如果没有存储此类键，则检索 0。
     */
    public double getDouble(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? 0.0D : ((NBTBase.NBTPrimitive)this.tagMap.get(key)).func_150286_g();
        }
        catch (ClassCastException classcastexception)
        {
            return 0.0D;
        }
    }

    /**
     * 使用指定的键检索字符串值，如果没有存储此类键，则检索空字符串。
     */
    public String getString(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? "" : ((NBTBase)this.tagMap.get(key)).func_150285_a_();
        }
        catch (ClassCastException classcastexception)
        {
            return "";
        }
    }

    /**
     * 使用指定的键检索字节数组，如果没有存储此类键，则检索零长度数组。
     */
    public byte[] getByteArray(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? new byte[0] : ((NBTTagByteArray)this.tagMap.get(key)).func_150292_c();
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createCrashReport(key, 7, classcastexception));
        }
    }

    /**
     * 使用指定的键检索 int 数组，如果没有存储此类键，则检索零长度数组。
     */
    public int[] getIntArray(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? new int[0] : ((NBTTagIntArray)this.tagMap.get(key)).func_150302_c();
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createCrashReport(key, 11, classcastexception));
        }
    }

    /**
     * 检索与指定键匹配的 NBTTagCompound 子标签，如果没有这样的键，则检索新的空 NBTTagCompound
     * 已存储。
     */
    public NBTTagCompound getCompoundTag(String key)
    {
        try
        {
            return !this.tagMap.containsKey(key) ? new NBTTagCompound() : (NBTTagCompound)this.tagMap.get(key);
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createCrashReport(key, 10, classcastexception));
        }
    }

    /**
     * 获取具有给定名称的 NBTTagList 对象。参数：名称、NBTBase类型
     */
    public NBTTagList getTagList(String key, int type)
    {
        try
        {
            if (this.getID(key) != 9)
            {
                return new NBTTagList();
            }
            else
            {
                NBTTagList nbttaglist = (NBTTagList)this.tagMap.get(key);
                return nbttaglist.tagCount() > 0 && nbttaglist.func_150303_d() != type ? new NBTTagList() : nbttaglist;
            }
        }
        catch (ClassCastException classcastexception)
        {
            throw new ReportedException(this.createCrashReport(key, 9, classcastexception));
        }
    }

    /**
     * 使用指定的键检索布尔值，如果没有存储此类键，则检索 false。这使用了 getByte
     * 方法。
     */
    public boolean getBoolean(String key)
    {
        return this.getByte(key) != 0;
    }

    /**
     * 删除指定的标签。
     */
    public void removeTag(String key)
    {
        this.tagMap.remove(key);
    }

    public String toString()
    {
        String s = "{";
        String s1;

        for (Iterator iterator = this.tagMap.keySet().iterator(); iterator.hasNext(); s = s + s1 + ':' + this.tagMap.get(s1) + ',')
        {
            s1 = (String)iterator.next();
        }

        return s + "}";
    }

    /**
     * 返回该化合物是否没有标签。
     */
    public boolean hasNoTags()
    {
        return this.tagMap.isEmpty();
    }

    /**
     * 创建指示 NBT 读取错误的崩溃报告。
     */
    private CrashReport createCrashReport(final String p_82581_1_, final int p_82581_2_, ClassCastException p_82581_3_)
    {
        CrashReport crashreport = CrashReport.makeCrashReport(p_82581_3_, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addCrashSectionCallable("Tag type found", new Callable()
        {
            private static final String __OBFID = "CL_00001216";
            public String call()
            {
                return NBTBase.NBTTypes[((NBTBase)NBTTagCompound.this.tagMap.get(p_82581_1_)).getId()];
            }
        });
        crashreportcategory.addCrashSectionCallable("Tag type expected", new Callable()
        {
            private static final String __OBFID = "CL_00001217";
            public String call()
            {
                return NBTBase.NBTTypes[p_82581_2_];
            }
        });
        crashreportcategory.addCrashSection("Tag name", p_82581_1_);
        return crashreport;
    }

    /**
     * 创建标签的克隆。
     */
    public NBTBase copy()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        Iterator iterator = this.tagMap.keySet().iterator();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();
            nbttagcompound.setTag(s, ((NBTBase)this.tagMap.get(s)).copy());
        }

        return nbttagcompound;
    }

    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            NBTTagCompound nbttagcompound = (NBTTagCompound) obj;
            return this.tagMap.entrySet().equals(nbttagcompound.tagMap.entrySet());
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.tagMap.hashCode();
    }

    private static void func_150298_a(String name, NBTBase data, DataOutput output) throws IOException
    {
        output.writeByte(data.getId());

        if (data.getId() != 0)
        {
            output.writeUTF(name);
            data.write(output);
        }
    }

    private static byte func_152447_a(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.func_152450_a(8);
        return input.readByte();
    }

    private static String func_152448_b(DataInput input, NBTSizeTracker sizeTracker) throws IOException
    {
        return input.readUTF();
    }

    static NBTBase func_152449_a(byte id, String key, DataInput input, int depth, NBTSizeTracker sizeTracker)
    {
        sizeTracker.func_152450_a(32); //Forge: 4 extra bytes for the object allocation.
        NBTBase nbtbase = NBTBase.func_150284_a(id);

        try
        {
            nbtbase.readNBT(input, depth, sizeTracker);
            return nbtbase;
        }
        catch (IOException ioexception)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag name", key);
            crashreportcategory.addCrashSection("Tag type", Byte.valueOf(id));
            throw new ReportedException(crashreport);
        }
    }
}