package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagByteArray extends NBTBase
{
    /** 标签中存储的字节数组。 */
    private byte[] byteArray;
    private static final String __OBFID = "CL_00001213";

    NBTTagByteArray() {}

    public NBTTagByteArray(byte[] p_i45128_1_)
    {
        this.byteArray = p_i45128_1_;
    }

    /**
     * 写入标签的实际数据内容，在NBT扩展类中实现
     */
    void write(DataOutput output) throws IOException
    {
        output.writeInt(this.byteArray.length);
        output.write(this.byteArray);
    }

    /**
     * 从DataInput中读取字节数组并进行大小跟踪
     * 此方法主要用于反序列化过程中，读取存储在NBT中的字节数组
     * 它首先读取字节数组的长度，然后根据该长度读取相应的字节数，并进行大小跟踪
     *
     * @param input DataInput对象，用于读取数据
     * @param depth NBT标签的深度，未在此方法中使用，但可能在扩展或修改方法时有用
     * @param sizeTracker NBTSizeTracker对象，用于跟踪读取数据的大小，以防止过大的数据导致内存溢出
     * @throws IOException 如果读取过程中发生I/O错误
     */
    void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        // 计算并跟踪字节数组长度的大小开销
        sizeTracker.func_152450_a(32); //Forge: Count the length as well
        // 读取字节数组的长度
        int j = input.readInt();
        // 根据字节数组长度计算并跟踪字节数据的大小开销
        sizeTracker.func_152450_a((long)(8 * j));
        // 根据读取的长度创建字节数组
        this.byteArray = new byte[j];
        // 从DataInput中读取字节数据到字节数组中
        input.readFully(this.byteArray);
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId()
    {
        return (byte)7;
    }

    public String toString()
    {
        return "[" + this.byteArray.length + " bytes]";
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTBase copy()
    {
        byte[] abyte = new byte[this.byteArray.length];
        System.arraycopy(this.byteArray, 0, abyte, 0, this.byteArray.length);
        return new NBTTagByteArray(abyte);
    }

    public boolean equals(Object obj)
    {
        return super.equals(obj) ? Arrays.equals(this.byteArray, ((NBTTagByteArray) obj).byteArray) : false;
    }

    public int hashCode()
    {
        return super.hashCode() ^ Arrays.hashCode(this.byteArray);
    }

    public byte[] func_150292_c()
    {
        return this.byteArray;
    }
}