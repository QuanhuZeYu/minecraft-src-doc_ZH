package net.minecraft.world.chunk.storage;

public class NibbleArrayReader
{
    public final byte[] data;
    private final int depthBits;
    private final int depthBitsPlusFour;
    private static final String __OBFID = "CL_00000376";

    public NibbleArrayReader(byte[] data, int depthBits)
    {
        this.data = data;
        this.depthBits = depthBits;
        this.depthBitsPlusFour = depthBits + 4;
    }

    public int get(int x, int y, int z)
    {
        int index = x << this.depthBitsPlusFour | z << this.depthBits | y; // x在左移depthBits + 4位后，z在左移depthBits位后，y在左移0位后，拼接在一起
        int byteIndex = index >> 1; // 除以2?
        int nibblePosition = index & 1; // index & 1 的结果是 0 或 1，表示目标 nibble 是字节的低四位（0）还是高四位（1）。
        return nibblePosition == 0 ? this.data[byteIndex] & 15 : this.data[byteIndex] >> 4 & 15; // 等于0则返回低4位，等于1则返回高4位
    }
}