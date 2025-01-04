package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase
{
    public static final String[] NBTTypes = new String[] {"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};
    private static final String __OBFID = "CL_00001229";

    /**
     * 写入标签的实际数据内容，在NBT扩展类中实现
     */
    abstract void write(DataOutput output) throws IOException;

    abstract void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException;

    public abstract String toString();

    /**
     * 获取标签的类型字节。
     */
    public abstract byte getId();

    /**
     * 根据给定的ID创建并返回对应的NBT标签对象
     * NBT标签类型是根据Minecraft游戏中的NBT格式定义的，每种类型都有一个唯一的ID
     * 此方法用于将这些ID映射到相应的NBT标签实现类
     *
     * @param id NBT标签类型的ID
     * @return 对应ID的NBT标签对象，如果ID不匹配任何已知类型，则返回null
     */
    protected static NBTBase func_150284_a(byte id)
    {
        switch (id)
        {
            case 0:
                // 返回表示NBT标签结束的标签
                return new NBTTagEnd();
            case 1:
                // 返回一个字节标签，用于存储单个字节值
                return new NBTTagByte();
            case 2:
                // 返回一个短整型标签，用于存储短整型数值
                return new NBTTagShort();
            case 3:
                // 返回一个整型标签，用于存储整型数值
                return new NBTTagInt();
            case 4:
                // 返回一个长整型标签，用于存储长整型数值
                return new NBTTagLong();
            case 5:
                // 返回一个浮点型标签，用于存储浮点型数值
                return new NBTTagFloat();
            case 6:
                // 返回一个双精度浮点型标签，用于存储双精度浮点型数值
                return new NBTTagDouble();
            case 7:
                // 返回一个字节数组标签，用于存储字节数组
                return new NBTTagByteArray();
            case 8:
                // 返回一个字符串标签，用于存储字符串
                return new NBTTagString();
            case 9:
                // 返回一个列表标签，用于存储一个NBT标签列表
                return new NBTTagList();
            case 10:
                // 返回一个复合标签，用于存储多个NBT标签的复合结构
                return new NBTTagCompound();
            case 11:
                // 返回一个整型数组标签，用于存储整型数组
                return new NBTTagIntArray();
            default:
                // 如果ID不匹配任何已知的NBT标签类型，返回null
                return null;
        }
    }

    /**
     * 创建标签的克隆。
     */
    public abstract NBTBase copy();

    public boolean equals(Object obj)
    {
        if (!(obj instanceof NBTBase))
        {
            return false;
        }
        else
        {
            NBTBase nbtbase = (NBTBase)obj;
            return this.getId() == nbtbase.getId();
        }
    }

    public int hashCode()
    {
        return this.getId();
    }

    protected String func_150285_a_()
    {
        return this.toString();
    }

    public abstract static class NBTPrimitive extends NBTBase
        {
            private static final String __OBFID = "CL_00001230";

            public abstract long func_150291_c();

            public abstract int func_150287_d();

            public abstract short func_150289_e();

            public abstract byte func_150290_f();

            public abstract double func_150286_g();

            public abstract float func_150288_h();
        }
}