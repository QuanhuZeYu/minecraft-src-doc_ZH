package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.chunk.Chunk;

public class S26PacketMapChunkBulk extends Packet
{
    private int[] field_149266_a;
    private int[] field_149264_b;
    private int[] field_149265_c;
    private int[] field_149262_d;
    private byte[] field_149263_e;
    private byte[][] field_149260_f;
    private int field_149261_g;
    private boolean field_149267_h;
    private static byte[] field_149268_i = new byte[0];
    private static final String __OBFID = "CL_00001306";
    private int maxLen = 0;
    private Semaphore deflateGate;

    public S26PacketMapChunkBulk() {}

    public S26PacketMapChunkBulk(List<net.minecraft.world.chunk.Chunk> p_i45197_1_)
    {
        int i = p_i45197_1_.size();
        this.field_149266_a = new int[i];
        this.field_149264_b = new int[i];
        this.field_149265_c = new int[i];
        this.field_149262_d = new int[i];
        this.field_149260_f = new byte[i][];
        this.field_149267_h = !p_i45197_1_.isEmpty() && !((Chunk)p_i45197_1_.get(0)).worldObj.provider.hasNoSky;
        int j = 0;

        for (int k = 0; k < i; ++k)
        {
            Chunk chunk = (Chunk)p_i45197_1_.get(k);
            S21PacketChunkData.Extracted extracted = S21PacketChunkData.func_149269_a(chunk, true, 65535);
            j += extracted.field_150282_a.length;
            this.field_149266_a[k] = chunk.xPosition;
            this.field_149264_b[k] = chunk.zPosition;
            this.field_149265_c[k] = extracted.field_150280_b;
            this.field_149262_d[k] = extracted.field_150281_c;
            this.field_149260_f[k] = extracted.field_150282_a;
        }
        this.deflateGate = new Semaphore(1);
        maxLen = j;
    }

    private void deflate()
    {
        byte[] data = new byte[maxLen];
        int offset = 0;
        for (int x = 0; x < field_149260_f.length; x++)
        {
            System.arraycopy(field_149260_f[x], 0, data, offset, field_149260_f[x].length);
            offset += field_149260_f[x].length;
        }
        Deflater deflater = new Deflater(-1);

        try
        {
            deflater.setInput(data, 0, data.length);
            deflater.finish();
            byte[] deflated = new byte[data.length];
            this.field_149261_g = deflater.deflate(deflated);
            this.field_149263_e = deflated;
        }
        finally
        {
            deflater.end();
        }
    }

    public static int func_149258_c()
    {
        return 5;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer data) throws IOException
    {
        short short1 = data.readShort();
        this.field_149261_g = data.readInt();
        this.field_149267_h = data.readBoolean();
        this.field_149266_a = new int[short1];
        this.field_149264_b = new int[short1];
        this.field_149265_c = new int[short1];
        this.field_149262_d = new int[short1];
        this.field_149260_f = new byte[short1][];

        if (field_149268_i.length < this.field_149261_g)
        {
            field_149268_i = new byte[this.field_149261_g];
        }

        data.readBytes(field_149268_i, 0, this.field_149261_g);
        byte[] abyte = new byte[S21PacketChunkData.func_149275_c() * short1];
        Inflater inflater = new Inflater();
        inflater.setInput(field_149268_i, 0, this.field_149261_g);

        try
        {
            inflater.inflate(abyte);
        }
        catch (DataFormatException dataformatexception)
        {
            throw new IOException("Bad compressed data format");
        }
        finally
        {
            inflater.end();
        }

        int i = 0;

        for (int j = 0; j < short1; ++j)
        {
            this.field_149266_a[j] = data.readInt();
            this.field_149264_b[j] = data.readInt();
            this.field_149265_c[j] = data.readShort();
            this.field_149262_d[j] = data.readShort();
            int k = 0;
            int l = 0;
            int i1;

            for (i1 = 0; i1 < 16; ++i1)
            {
                k += this.field_149265_c[j] >> i1 & 1;
                l += this.field_149262_d[j] >> i1 & 1;
            }

            i1 = 2048 * 4 * k + 256;
            i1 += 2048 * l;

            if (this.field_149267_h)
            {
                i1 += 2048 * k;
            }

            this.field_149260_f[j] = new byte[i1];
            System.arraycopy(abyte, i, this.field_149260_f[j], 0, i1);
            i += i1;
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer data) throws IOException
    {
        if (this.field_149263_e == null)
        {
            deflateGate.acquireUninterruptibly();
            if (this.field_149263_e == null)
            {
                deflate();
            }
            deflateGate.release();
        }

        data.writeShort(this.field_149266_a.length);
        data.writeInt(this.field_149261_g);
        data.writeBoolean(this.field_149267_h);
        data.writeBytes(this.field_149263_e, 0, this.field_149261_g);

        for (int i = 0; i < this.field_149266_a.length; ++i)
        {
            data.writeInt(this.field_149266_a[i]);
            data.writeInt(this.field_149264_b[i]);
            data.writeShort((short)(this.field_149265_c[i] & 65535));
            data.writeShort((short)(this.field_149262_d[i] & 65535));
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleMapChunkBulk(this);
    }

    @SideOnly(Side.CLIENT)
    public int func_149255_a(int p_149255_1_)
    {
        return this.field_149266_a[p_149255_1_];
    }

    @SideOnly(Side.CLIENT)
    public int func_149253_b(int p_149253_1_)
    {
        return this.field_149264_b[p_149253_1_];
    }

    /**
     * Returns a string formatted as comma separated [field]=[value] values. Used by Minecraft for logging purposes.
     */
    public String serialize()
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < this.field_149266_a.length; ++i)
        {
            if (i > 0)
            {
                stringbuilder.append(", ");
            }

            stringbuilder.append(String.format("{x=%d, z=%d, sections=%d, adds=%d, data=%d}", new Object[] {Integer.valueOf(this.field_149266_a[i]), Integer.valueOf(this.field_149264_b[i]), Integer.valueOf(this.field_149265_c[i]), Integer.valueOf(this.field_149262_d[i]), Integer.valueOf(this.field_149260_f[i].length)}));
        }

        return String.format("size=%d, chunks=%d[%s]", new Object[] {Integer.valueOf(this.field_149261_g), Integer.valueOf(this.field_149266_a.length), stringbuilder});
    }

    @SideOnly(Side.CLIENT)
    public int func_149254_d()
    {
        return this.field_149266_a.length;
    }

    @SideOnly(Side.CLIENT)
    public byte[] func_149256_c(int p_149256_1_)
    {
        return this.field_149260_f[p_149256_1_];
    }

    @SideOnly(Side.CLIENT)
    public int[] func_149252_e()
    {
        return this.field_149265_c;
    }

    @SideOnly(Side.CLIENT)
    public int[] func_149257_f()
    {
        return this.field_149262_d;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler handler)
    {
        this.processPacket((INetHandlerPlayClient)handler);
    }
}