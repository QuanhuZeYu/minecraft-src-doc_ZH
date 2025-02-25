package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.MathHelper;

public class NBTTagDouble extends NBTBase.NBTPrimitive
{
    /** The double value for the tag. */
    private double data;
    private static final String __OBFID = "CL_00001218";

    NBTTagDouble() {}

    public NBTTagDouble(double p_i45130_1_)
    {
        this.data = p_i45130_1_;
    }

    /**
     * Write the actual data contents of the tag, implemented in NBT extension classes
     */
    void write(DataOutput output) throws IOException
    {
        output.writeDouble(this.data);
    }

    void readNBT(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        sizeTracker.func_152450_a(64L);
        this.data = input.readDouble();
    }

    /**
     * Gets the type byte for the tag.
     */
    public byte getId()
    {
        return (byte)6;
    }

    public String toString()
    {
        return "" + this.data + "d";
    }

    /**
     * Creates a clone of the tag.
     */
    public NBTBase copy()
    {
        return new NBTTagDouble(this.data);
    }

    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            NBTTagDouble nbttagdouble = (NBTTagDouble) obj;
            return this.data == nbttagdouble.data;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        long i = Double.doubleToLongBits(this.data);
        return super.hashCode() ^ (int)(i ^ i >>> 32);
    }

    public long func_150291_c()
    {
        return (long)Math.floor(this.data);
    }

    public int func_150287_d()
    {
        return MathHelper.floor_double(this.data);
    }

    public short func_150289_e()
    {
        return (short)(MathHelper.floor_double(this.data) & 65535);
    }

    public byte func_150290_f()
    {
        return (byte)(MathHelper.floor_double(this.data) & 255);
    }

    public double func_150286_g()
    {
        return this.data;
    }

    public float func_150288_h()
    {
        return (float)this.data;
    }
}