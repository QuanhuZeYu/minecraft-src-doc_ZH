package net.minecraft.world.chunk;

public class NibbleArray
{
    /**
     * 存储实际数据的字节数组，每个字节包含两个 4 位的 nibble 数据。
     * 例如：一个字节的二进制表示为 10101100，其中 1010 和 1100 是两个 nibble。
     */
    public final byte[] data;
    /**
     * Z 轴方向的位宽（深度位数），表示 Z 轴上的坐标占用的位数。
     * 通常用于计算 Z 轴坐标在数组中的索引偏移量。
     */
    private final int depthBits;
    /**
     * X 和 Z 轴的位宽之和，等于 depthBits + 4。
     * 4 是因为 X 轴的宽度通常为 16（2^4），这是固定值。
     * 用于加速索引的计算。
     */
    private final int depthBitsPlusFour;
    private static final String __OBFID = "CL_00000371";

    /**
     * 构造一个 NibbleArray 对象并初始化数据数组。
     *
     * @param size 数据总大小（以位为单位），需要除以 2 得到字节数组的实际大小。
     * @param depth Z 轴的位宽（depthBits），通常与数据在三维空间的布局相关。
     */
    public NibbleArray(int size, int depth)
    {
        // 根据传入的大小参数初始化字节数组，每个字节包含两个半字节
        this.data = new byte[size / 2];
        // 设置深度位数，即每个元素的位数
        this.depthBits = depth;
        // 计算深度位数加四，用于后续的位操作
        this.depthBitsPlusFour = depth + 4;
    }

    /**
     * 构造一个 NibbleArray 对象，使用现有的数据数组初始化。
     *
     * @param data 已有的字节数组数据。
     * @param depthBits Z 轴的位宽（depthBits）。
     */
    public NibbleArray(byte[] data, int depthBits)
    {
        this.data = data;
        this.depthBits = depthBits;
        this.depthBitsPlusFour = depthBits + 4;
    }

    /**
     * 获取指定坐标 (x, y, z) 的 nibble 数据。
     *
     * @param x X 坐标。
     * @param y Y 坐标。
     * @param z Z 坐标。
     * @return 指定坐标的 nibble 数据（4 位无符号整数）。
     */
    public int get(int x, int y, int z)
    {
        // l 的最低位用于决定取高半字节还是低半字节
        int l = y << this.depthBitsPlusFour | z << this.depthBits | x; // 偶数低四位 奇数高四位

        // 将计算出的索引值右移 1 位，以获取数据数组的实际索引 i1
        // 因为每个字节包含两个半字节，所以需要除以 2
        int i1 = l >> 1;

        // 通过与操作获取索引值的最低位 j1，用于决定是取高半字节还是低半字节
        int j1 = l & 1;

        // 根据最低位的值决定返回数据数组中索引 i1 位置的低半字节还是高半字节
        return j1 == 0 ? this.data[i1] & 15 : this.data[i1] >> 4 & 15;
    }


    /**
     * 参数为 x、y、z、val。将数据的半字节设置为 x << 11 | z << 7 | y 到 val。
     */
    public void set(int x, int y, int z, int val)
    {
        int i1 = y << this.depthBitsPlusFour | z << this.depthBits | x;
        int j1 = i1 >> 1;
        int k1 = i1 & 1;

        if (k1 == 0)
        {
            this.data[j1] = (byte)(this.data[j1] & 240 | val & 15);
        }
        else
        {
            this.data[j1] = (byte)(this.data[j1] & 15 | (val & 15) << 4);
        }
    }
}