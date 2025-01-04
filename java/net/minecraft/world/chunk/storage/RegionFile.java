package net.minecraft.world.chunk.storage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.server.MinecraftServer;

public class RegionFile
{
    private static final byte[] emptySector = new byte[4096];
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private ArrayList sectorFree;
    /** McRegion sizeDelta */
    private int sizeDelta;
    private long lastModified;
    private static final String __OBFID = "CL_00000381";

    // 构造函数，接受一个文件对象作为参数
    public RegionFile(File file) {
        // 初始化文件名和大小变化
        this.fileName = file;
        this.sizeDelta = 0;

        try {
            // 检查文件是否存在，如果存在则记录最后修改时间
            if (file.exists()) {
                this.lastModified = file.lastModified();
            }

            // 打开文件进行读写操作
            this.dataFile = new RandomAccessFile(file, "rw");

            int count;

            // 如果文件长度小于 4096 字节，初始化文件头
            if (this.dataFile.length() < 4096L) {
                // 写入 1024 个整型偏移值，初始化为 0
                for (count = 0; count < 1024; ++count) {
                    this.dataFile.writeInt(0);
                }

                // 写入 1024 个整型时间戳，初始化为 0
                for (count = 0; count < 1024; ++count) {
                    this.dataFile.writeInt(0);
                }

                // 更新文件大小变化
                this.sizeDelta += 8192;
            }

            // 如果文件长度不是 4096 的整数倍，填充文件至对齐
            if ((this.dataFile.length() & 4095L) != 0L) {
                for (count = 0; (long)count < (this.dataFile.length() & 4095L); ++count) {
                    this.dataFile.write(0);
                }
            }

            // 计算文件的扇区数
            count = (int)this.dataFile.length() / 4096;

            // 初始化空闲扇区标记列表
            this.sectorFree = new ArrayList<Integer>(count);

            int j;

            // 初始化所有扇区为可用状态
            for (j = 0; j < count; ++j) {
                this.sectorFree.add(Boolean.valueOf(true));
            }

            // 前两个扇区保留，不可用（用于存储头信息）
            this.sectorFree.set(0, Boolean.valueOf(false));
            this.sectorFree.set(1, Boolean.valueOf(false));

            // 定位到文件开始位置
            this.dataFile.seek(0L);

            int k;

            // 读取偏移表，初始化偏移数组和扇区状态
            for (j = 0; j < 1024; ++j) {
                k = this.dataFile.readInt();
                this.offsets[j] = k;

                // 如果偏移值不为 0，标记相应扇区为已占用
                if (k != 0 && (k >> 8) + (k & 255) <= this.sectorFree.size()) {
                    for (int l = 0; l < (k & 255); ++l) {
                        this.sectorFree.set((k >> 8) + l, Boolean.valueOf(false));
                    }
                }
            }

            // 读取时间戳表，初始化时间戳数组
            for (j = 0; j < 1024; ++j) {
                k = this.dataFile.readInt();
                this.chunkTimestamps[j] = k;
            }
        } catch (IOException ioexception) {
            // 捕获并打印 I/O 异常
            ioexception.printStackTrace();
        }
    }

    // 检查指定的区块是否存在
    // 注意：此方法与后面的方法功能类似，需确保它们逻辑一致
    public synchronized boolean chunkExists(int x, int z) {
        // 检查区块坐标是否超出允许范围
        if (this.outOfBounds(x, z)) return false;

        try {
            // 获取区块的偏移值（offset）
            int offset = this.getOffset(x, z);

            // 如果偏移值为 0，说明区块不存在
            if (offset == 0) return false;

            // 提取扇区号和扇区数量
            int sectorNumber = offset >> 8;  // 高 24 位表示起始扇区号
            int numSectors = offset & 255;  // 低 8 位表示扇区数量

            // 检查扇区号和扇区数量是否合法
            if (sectorNumber + numSectors > this.sectorFree.size()) return false;

            // 定位到区块数据的起始位置
            this.dataFile.seek((long)(sectorNumber * 4096));

            // 读取区块数据的长度
            int length = this.dataFile.readInt();

            // 检查数据长度是否合法
            if (length > 4096 * numSectors || length <= 0) return false;

            // 读取版本号
            byte version = this.dataFile.readByte();

            // 检查版本号是否有效（1 或 2 是合法版本号）
            if (version == 1 || version == 2) return true;
        } catch (IOException ioexception) {
            // 捕获 I/O 异常并返回 false，表示区块不存在
            return false;
        }

        // 如果任何检查未通过，返回 false
        return false;
    }

    /**
     * 获取未压缩的块数据输入流
     * 根据给定的区块坐标，从区域文件中读取区块数据并返回一个输入流
     * 如果区块数据不存在或读取过程中发生错误，则返回null
     *
     * @param x 区块的x坐标
     * @param y 区块的z坐标
     * @return DataInputStream 区块数据的输入流，如果无法获取则返回null
     */
    public synchronized DataInputStream getChunkDataInputStream(int x, int y)
    {
        // 检查区块坐标是否超出区域文件的边界
        if (this.outOfBounds(x, y))
        {
            return null;
        }
        else
        {
            try
            {
                // 获取区块数据在文件中的偏移量
                int offset = this.getOffset(x, y);

                // 如果偏移量为0，表示区块数据不存在
                if (offset == 0)
                {
                    return null;
                }
                else
                {
                    // 解析偏移量，获取区块数据所在的扇区和扇区数量
                    int offsetR8 = offset >> 8;
                    int offsetA255 = offset & 255;

                    // 检查区块数据是否超出区域文件的范围
                    if (offsetR8 + offsetA255 > this.sectorFree.size())
                    {
                        return null;
                    }
                    else
                    {
                        // 定位到区块数据的起始位置
                        this.dataFile.seek((long)(offsetR8 * 4096));
                        // 读取区块数据的长度
                        int chunkLength = this.dataFile.readInt();

                        // 检查区块数据长度是否超出扇区范围或无效
                        if (chunkLength > 4096 * offsetA255 || chunkLength <= 0)
                        {
                            return null;
                        }
                        else
                        {
                            // 读取区块数据的压缩方式标志
                            byte b0 = this.dataFile.readByte();
                            // 根据压缩方式读取并解压缩区块数据
                            byte[] abyte;

                            // 使用GZIP压缩
                            if (b0 == 1)
                            {
                                abyte = new byte[chunkLength - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));
                            }
                            // 使用ZLIB压缩
                            else if (b0 == 2)
                            {
                                abyte = new byte[chunkLength - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            }
                            // 不支持的压缩方式
                            else
                            {
                                return null;
                            }
                        }
                    }
                }
            }
            // 捕获IO异常，返回null
            catch (IOException ioexception)
            {
                return null;
            }
        }
    }

    /**
     * args: x, z - get an output stream used to write chunk data, data is on disk when the returned stream is closed
     */
    public DataOutputStream getChunkDataOutputStream(int p_76710_1_, int p_76710_2_)
    {
        return this.outOfBounds(p_76710_1_, p_76710_2_) ? null : new DataOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(p_76710_1_, p_76710_2_)));
    }

    /**
     * 将 (x, z) 处的块数据写入磁盘:
     * @param p_76706_1_ - x 坐标
     * @param p_76706_2_ - z 坐标
     * @param data - 待写入的字节数据
     * @param length - 数据长度（字节数）
     */
    protected synchronized void write(int p_76706_1_, int p_76706_2_, byte[] data, int length) {
        try {
            // 获取块的偏移信息
            int l = this.getOffset(p_76706_1_, p_76706_2_);
            int startPoint = l >> 8;  // 获取起始扇区号
            int sectorCount = l & 255; // 获取占用扇区数量
            int needCount = (length + 5) / 4096 + 1; // 计算写入所需的扇区数，+5 包括头部和版本号

            // 如果需要的扇区数超过 256，直接返回（超出允许范围）
            if (needCount >= 256) {
                return;
            }

            // 如果现有扇区数量正好满足所需数量，直接写入
            if (startPoint != 0 && sectorCount == needCount) {
                this.write(startPoint, data, length);
            } else {
                int sectorCounter;

                // 将现有扇区标记为空闲
                for (sectorCounter = 0; sectorCounter < sectorCount; ++sectorCounter) {
                    this.sectorFree.set(startPoint + sectorCounter, Boolean.valueOf(true));
                }

                // 查找可用的连续空闲扇区
                sectorCounter = this.sectorFree.indexOf(Boolean.valueOf(true));
                int freeSectorCount = 0;
                int j2;

                if (sectorCounter != -1) {
                    for (j2 = sectorCounter; j2 < this.sectorFree.size(); ++j2) {
                        if (freeSectorCount != 0) {
                            if (((Boolean)this.sectorFree.get(j2)).booleanValue()) {
                                ++freeSectorCount;
                            } else {
                                freeSectorCount = 0;
                            }
                        } else if (((Boolean)this.sectorFree.get(j2)).booleanValue()) {
                            sectorCounter = j2;
                            freeSectorCount = 1;
                        }

                        if (freeSectorCount >= needCount) {
                            break;
                        }
                    }
                }

                // 如果找到足够的连续空闲扇区
                if (freeSectorCount >= needCount) {
                    startPoint = sectorCounter;
                    // 设置新的偏移值
                    this.setOffset(p_76706_1_, p_76706_2_, sectorCounter << 8 | needCount);

                    // 将这些扇区标记为已占用
                    for (j2 = 0; j2 < needCount; ++j2) {
                        this.sectorFree.set(startPoint + j2, Boolean.valueOf(false));
                    }

                    // 写入数据
                    this.write(startPoint, data, length);
                } else {
                    // 如果未找到足够的空闲扇区，则追加新扇区
                    this.dataFile.seek(this.dataFile.length()); // 定位到文件末尾
                    startPoint = this.sectorFree.size();

                    // 写入空扇区，并将这些扇区标记为已占用
                    for (j2 = 0; j2 < needCount; ++j2) {
                        this.dataFile.write(emptySector);
                        this.sectorFree.add(Boolean.valueOf(false));
                    }

                    // 更新文件大小变化
                    this.sizeDelta += 4096 * needCount;

                    // 写入数据
                    this.write(startPoint, data, length);

                    // 设置新的偏移值
                    this.setOffset(p_76706_1_, p_76706_2_, startPoint << 8 | needCount);
                }
            }

            // 更新块的时间戳（单位：秒）
            this.setChunkTimestamp(p_76706_1_, p_76706_2_, (int)(MinecraftServer.getSystemTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            // 捕获 I/O 异常并打印错误堆栈
            ioexception.printStackTrace();
        }
    }

    /**
     * args: sectorNumber, data, length - write the chunk data to this RegionFile
     */
    private void write(int p_76712_1_, byte[] p_76712_2_, int p_76712_3_) throws IOException
    {
        this.dataFile.seek((long)(p_76712_1_ * 4096));
        this.dataFile.writeInt(p_76712_3_ + 1);
        this.dataFile.writeByte(2);
        this.dataFile.write(p_76712_2_, 0, p_76712_3_);
    }

    /**
     * args: x, z - check region bounds
     */
    private boolean outOfBounds(int p_76705_1_, int p_76705_2_)
    {
        return p_76705_1_ < 0 || p_76705_1_ >= 32 || p_76705_2_ < 0 || p_76705_2_ >= 32;
    }

    /**
     * args: x, z - get chunk's offset in region file
     */
    private int getOffset(int p_76707_1_, int p_76707_2_)
    {
        return this.offsets[p_76707_1_ + p_76707_2_ * 32];
    }

    /**
     * args: x, z, - true if chunk has been saved / converted
     */
    public boolean isChunkSaved(int p_76709_1_, int p_76709_2_)
    {
        return this.getOffset(p_76709_1_, p_76709_2_) != 0;
    }

    /**
     * args: x, z, offset - sets the chunk's offset in the region file
     */
    private void setOffset(int p_76711_1_, int p_76711_2_, int p_76711_3_) throws IOException
    {
        this.offsets[p_76711_1_ + p_76711_2_ * 32] = p_76711_3_;
        this.dataFile.seek((long)((p_76711_1_ + p_76711_2_ * 32) * 4));
        this.dataFile.writeInt(p_76711_3_);
    }

    /**
     * args: x, z, timestamp - sets the chunk's write timestamp
     */
    private void setChunkTimestamp(int p_76713_1_, int p_76713_2_, int p_76713_3_) throws IOException
    {
        this.chunkTimestamps[p_76713_1_ + p_76713_2_ * 32] = p_76713_3_;
        this.dataFile.seek((long)(4096 + (p_76713_1_ + p_76713_2_ * 32) * 4));
        this.dataFile.writeInt(p_76713_3_);
    }

    /**
     * close this RegionFile and prevent further writes
     */
    public void close() throws IOException
    {
        if (this.dataFile != null)
        {
            this.dataFile.close();
        }
    }

    class ChunkBuffer extends ByteArrayOutputStream
    {
        private int chunkX;
        private int chunkZ;
        private static final String __OBFID = "CL_00000382";

        public ChunkBuffer(int p_i2000_2_, int p_i2000_3_)
        {
            super(8096);
            this.chunkX = p_i2000_2_;
            this.chunkZ = p_i2000_3_;
        }

        public void close() throws IOException
        {
            RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}