package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;

public class S3DPacketDisplayScoreboard extends Packet
{
    private int field_149374_a;
    private String field_149373_b;
    private static final String __OBFID = "CL_00001325";

    public S3DPacketDisplayScoreboard() {}

    public S3DPacketDisplayScoreboard(int p_i45216_1_, ScoreObjective p_i45216_2_)
    {
        this.field_149374_a = p_i45216_1_;

        if (p_i45216_2_ == null)
        {
            this.field_149373_b = "";
        }
        else
        {
            this.field_149373_b = p_i45216_2_.getName();
        }
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer data) throws IOException
    {
        this.field_149374_a = data.readByte();
        this.field_149373_b = data.readStringFromBuffer(16);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer data) throws IOException
    {
        data.writeByte(this.field_149374_a);
        data.writeStringToBuffer(this.field_149373_b);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleDisplayScoreboard(this);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler handler)
    {
        this.processPacket((INetHandlerPlayClient)handler);
    }

    @SideOnly(Side.CLIENT)
    public int func_149371_c()
    {
        return this.field_149374_a;
    }

    @SideOnly(Side.CLIENT)
    public String func_149370_d()
    {
        return this.field_149373_b;
    }
}