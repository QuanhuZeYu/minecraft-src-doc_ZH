package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagByte extends NBTBase.NBTPrimitive
{
    /** 标签的字节值。 */
    private byte data;
    private static final String __OBFID = "CL_00001214";

    NBTTagByte() {}

    public NBTTagByte(byte byteIn)
    {
        this.data = byteIn;
    }

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException
    {
        output.writeByte(this.data);
    }

    void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.func_152450_a(8L);
        this.data = input.readByte();
    }

    /**
     * 获取标签的类型字节。
     */
    public byte getId()
    {
        return (byte)1;
    }

    public String toString()
    {
        return "" + this.data + "b";
    }

    /**
     * 创建标签的克隆。
     */
    public NBTBase copy()
    {
        return new NBTTagByte(this.data);
    }

    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            NBTTagByte nbttagbyte = (NBTTagByte) obj;
            return this.data == nbttagbyte.data;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.data;
    }

    public long func_150291_c()
    {
        return (long)this.data;
    }

    public int func_150287_d()
    {
        return this.data;
    }

    public short func_150289_e()
    {
        return (short)this.data;
    }

    public byte func_150290_f()
    {
        return this.data;
    }

    public double func_150286_g()
    {
        return (double)this.data;
    }

    public float func_150288_h()
    {
        return (float)this.data;
    }
}