package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagIntArray extends NBTBase
{
    /** The array of saved integers */
    private int[] intArray;
    private static final String __OBFID = "CL_00001221";

    NBTTagIntArray() {}

    public NBTTagIntArray(int[] p_i45132_1_)
    {
        this.intArray = p_i45132_1_;
    }

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException
    {
        output.writeInt(this.intArray.length);

        for (int i = 0; i < this.intArray.length; ++i)
        {
            output.writeInt(this.intArray[i]);
        }
    }

    void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.func_152450_a(32); //Forge: Count the length as well
        int j = input.readInt();
        sizeTracker.func_152450_a((long)(32 * j));
        this.intArray = new int[j];

        for (int k = 0; k < j; ++k)
        {
            this.intArray[k] = input.readInt();
        }
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId()
    {
        return (byte)11;
    }

    public String toString()
    {
        String s = "[";
        int[] aint = this.intArray;
        int i = aint.length;

        for (int j = 0; j < i; ++j)
        {
            int k = aint[j];
            s = s + k + ",";
        }

        return s + "]";
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTBase copy()
    {
        int[] aint = new int[this.intArray.length];
        System.arraycopy(this.intArray, 0, aint, 0, this.intArray.length);
        return new NBTTagIntArray(aint);
    }

    public boolean equals(Object obj)
    {
        return super.equals(obj) ? Arrays.equals(this.intArray, ((NBTTagIntArray) obj).intArray) : false;
    }

    public int hashCode()
    {
        return super.hashCode() ^ Arrays.hashCode(this.intArray);
    }

    public int[] func_150302_c()
    {
        return this.intArray;
    }
}