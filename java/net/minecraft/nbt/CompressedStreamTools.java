package net.minecraft.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

/**
 * 压缩流工具
 */
public class CompressedStreamTools
{
    private static final String __OBFID = "CL_00001226";

    /**
     * 从输入流加载 gzip 压缩的复合材料。
     * 此方法主要用于读取经过 gzip 压缩的 NBT 数据，并将其解析为一个 NBTTagCompound 对象。
     *
     * @param inputStream 输入流，包含 gzip 压缩的 NBT 数据。
     * @return 解析后的 NBTTagCompound 对象。
     * @throws IOException 如果在读取输入流或解析 NBT 数据时发生错误。
     */
    public static NBTTagCompound readCompressed(InputStream inputStream) throws IOException
    {
        // 创建一个 DataInputStream，用于读取解压缩后的数据
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(inputStream)));
        NBTTagCompound nbttagcompound;

        try
        {
            // 调用方法 func_152456_a 读取数据并解析为 NBTTagCompound 对象
            nbttagcompound = func_152456_a(datainputstream, NBTSizeTracker.NBT_SIZE_TRACKER);
        }
        finally
        {
            // 关闭数据输入流，释放资源
            datainputstream.close();
        }

        // 返回解析后的 NBTTagCompound 对象
        return nbttagcompound;
    }

    /**
     * 将压缩后的复合内容写入输出流。
     */
    public static void writeCompressed(NBTTagCompound nbtTagCompound, OutputStream outputStream) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));

        try
        {
            write(nbtTagCompound, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }
    }

    public static NBTTagCompound func_152457_a(byte[] p_152457_0_, NBTSizeTracker p_152457_1_) throws IOException
    {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(p_152457_0_))));
        NBTTagCompound nbttagcompound;

        try
        {
            nbttagcompound = func_152456_a(datainputstream, p_152457_1_);
        }
        finally
        {
            datainputstream.close();
        }

        return nbttagcompound;
    }

    /**
     * 压缩NBT标签复合对象
     * 此方法使用GZIP算法将NBTTagCompound对象压缩成字节数组
     * 主要用于减少数据的存储空间或传输体积
     *
     * @param nbtTagCompound 待压缩的NBT标签复合对象
     * @return 压缩后的字节数组
     * @throws IOException 如果在压缩过程中发生I/O错误
     */
    public static byte[] compress(NBTTagCompound nbtTagCompound) throws IOException
    {
        // 创建字节数组输出流，用于存储压缩后的数据
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        // 创建数据输出流，并通过GZIPOutputStream包装，用于压缩数据
        DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));

        try
        {
            // 调用write方法将NBT标签复合对象写入数据输出流，实现压缩
            write(nbtTagCompound, dataoutputstream);
        }
        finally
        {
            // 关闭数据输出流，释放资源
            dataoutputstream.close();
        }

        // 返回压缩后的字节数组
        return bytearrayoutputstream.toByteArray();
    }

    /**
     * 安全地将NBTTagCompound数据写入文件
     * 此方法旨在通过临时文件确保写入操作的安全性，避免直接覆盖原有文件
     * 如果写入过程中发生错误，可以减少数据丢失或损坏的风险
     *
     * @param nbtTagCompound 待写入的NBTTagCompound数据，包含要保存的信息
     * @param file 目标文件路径，指定数据保存的位置
     * @throws IOException 如果文件写入或重命名操作失败，则抛出IOException
     */
    public static void safeWrite(NBTTagCompound nbtTagCompound, File file) throws IOException
    {
        // 创建临时文件路径，用于安全写入操作
        File file2 = new File(file.getAbsolutePath() + "_tmp");

        // 如果临时文件已存在，则先删除它，确保干净的写入环境
        if (file2.exists())
        {
            file2.delete();
        }

        // 将NBTTagCompound数据写入临时文件
        write(nbtTagCompound, file2);

        // 如果目标文件存在，则删除它，为新数据让路
        if (file.exists())
        {
            file.delete();
        }

        // 再次检查目标文件是否存在，如果存在，则抛出异常，因为无法删除旧文件
        if (file.exists())
        {
            throw new IOException("Failed to delete " + file);
        }
        else
        {
            // 如果旧文件成功删除，则将临时文件重命名为目标文件，完成安全写入操作
            file2.renameTo(file);
        }
    }

    /**
     * Reads from a CompressedStream.
     */
    public static NBTTagCompound read(DataInputStream dataInputStream) throws IOException
    {
        return func_152456_a(dataInputStream, NBTSizeTracker.NBT_SIZE_TRACKER);
    }

    public static NBTTagCompound func_152456_a(DataInput dataInput, NBTSizeTracker nbtSizeTracker) throws IOException
    {
        NBTBase nbtbase = func_152455_a(dataInput, 0, nbtSizeTracker);

        if (nbtbase instanceof NBTTagCompound)
        {
            return (NBTTagCompound)nbtbase;
        }
        else
        {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound nbtTagCompound, DataOutput dataOutput) throws IOException
    {
        func_150663_a(nbtTagCompound, dataOutput);
    }

    private static void func_150663_a(NBTBase nbtBase, DataOutput dataOutput) throws IOException
    {
        dataOutput.writeByte(nbtBase.getId());

        if (nbtBase.getId() != 0)
        {
            dataOutput.writeUTF("");
            nbtBase.write(dataOutput);
        }
    }

    private static NBTBase func_152455_a(DataInput dataInput, int i, NBTSizeTracker nbtSizeTracker) throws IOException
    {
        byte b0 = dataInput.readByte();
        nbtSizeTracker.func_152450_a(8); // Forge: Count everything!

        if (b0 == 0)
        {
            return new NBTTagEnd();
        }
        else
        {
            NBTSizeTracker.readUTF(nbtSizeTracker, dataInput.readUTF()); //Forge: Count this string.
            nbtSizeTracker.func_152450_a(32); //Forge: 4 extra bytes for the object allocation.
            NBTBase nbtbase = NBTBase.func_150284_a(b0);

            try
            {
                nbtbase.readNBT(dataInput, i, nbtSizeTracker);
                return nbtbase;
            }
            catch (IOException ioexception)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
                crashreportcategory.addCrashSection("Tag name", "[UNNAMED TAG]");
                crashreportcategory.addCrashSection("Tag type", Byte.valueOf(b0));
                throw new ReportedException(crashreport);
            }
        }
    }

    public static void write(NBTTagCompound p_74795_0_, File p_74795_1_) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(p_74795_1_));

        try
        {
            write(p_74795_0_, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }
    }

    public static NBTTagCompound read(File file) throws IOException
    {
        return func_152458_a(file, NBTSizeTracker.NBT_SIZE_TRACKER);
    }

    public static NBTTagCompound func_152458_a(File file, NBTSizeTracker nbtSizeTracker) throws IOException
    {
        if (!file.exists())
        {
            return null;
        }
        else
        {
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
            NBTTagCompound nbttagcompound;

            try
            {
                nbttagcompound = func_152456_a(datainputstream, nbtSizeTracker);
            }
            finally
            {
                datainputstream.close();
            }

            return nbttagcompound;
        }
    }
}