package net.minecraft.nbt;

public class NBTSizeTracker
{
    /**
     * field_152451_a
     */
    public static final NBTSizeTracker NBT_SIZE_TRACKER = new NBTSizeTracker(0L) // field_152451_a
    {
        private static final String __OBFID = "CL_00001902";
        public void func_152450_a(long size) {}
    };
    private final long field_152452_b;
    private long field_152453_c;
    private static final String __OBFID = "CL_00001903";

    public NBTSizeTracker(long worldIn)
    {
        this.field_152452_b = worldIn;
    }

    /**
     * 增加已读取的NBT标签的大小 默认最大2MB
     * <p>
     * 此方法用于累加当前已读取的NBT标签的大小，以确保读取的大小不会超过允许的最大值
     * 它通过将给定的大小除以8来转换为字节，并添加到已读取的总大小中如果累加的大小超过了
     * 最大允许值，方法将抛出RuntimeException
     *
     * @param size 即将读取的NBT标签的大小，单位是比特
     * @throws RuntimeException 如果累加的读取大小超过最大允许大小，抛出此异常
     */
    public void func_152450_a(long size)
    {
        // 累加读取的大小，转换为字节
        this.field_152453_c += size / 8L;

        // 检查累加的读取大小是否超过最大允许大小
        if (this.field_152453_c > this.field_152452_b)
        {
            // 如果超过，抛出异常
            throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.field_152453_c + "bytes where max allowed: " + this.field_152452_b);
        }
    }

    /**
     * 读取UTF-8编码的字符串，并计算其字节长度
     * UTF8不是一个简单的编码系统，每个字符可以是
     * 1、2 或 3 个字节。取决于它的数值落在哪里。
     * 我们必须单独统计每个字符才能看到真实的情况
     * 数据长度。
     *
     * 基本概念是它使用每个字节的 MSB 作为“读取更多”信号。
     * 所以它必须移位每个 7 位段。
     *
     * 这将准确地计算出编码该字符串的正确字节长度，加上其长度前缀的 2 个字节。
     *
     * @param tracker NBTSizeTracker对象，用于跟踪读取数据的大小
     * @param data 要读取的字符串数据
     */
    public static void readUTF(NBTSizeTracker tracker, String data)
    {
        // 跳过头部长度
        tracker.func_152450_a(16); //Header length
        if (data == null)
            return;

        int len = data.length();
        int utflen = 0;

        // 遍历字符串中的每个字符，计算总字节长度
        for (int i = 0; i < len; i++)
        {
            int c = data.charAt(i);
            // 根据字符的Unicode编码范围，确定字符的UTF-8编码字节长度
            if ((c >= 0x0001) && (c <= 0x007F)) utflen += 1;
            else if (c > 0x07FF)                utflen += 3;
            else                                utflen += 2;
        }
        // 更新跟踪器，增加计算出的字节长度
        tracker.func_152450_a(8 * utflen);
    }
}