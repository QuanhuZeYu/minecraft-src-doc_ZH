package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S2DPacketOpenWindow extends Packet
{
    private int windowId;
    private int inventoryType;
    private String windowTitle;
    private int slotCount;
    private boolean useCustomTitle;
    private int additionalField;
    private static final String __OBFID = "CL_00001293";

    public S2DPacketOpenWindow() {}

    /**
     * 构造一个打开窗口的数据包实例。
     *
     * @param windowId 窗口的唯一标识符，用于区分不同的窗口类型。
     * @param inventoryType 窗口的槽位数量，表示该窗口可以容纳的物品栏格子数量。
     * @param windowTitle 窗口的标题，用于显示在窗口顶部的文字描述。
     * @param slotCount 窗口的实体ID（如果适用），例如：当窗口与特定实体（如：箱子、村民等）相关时，此字段将存储该实体的ID。
     * @param useCustomTitle 标识窗口是否使用自定义标题的布尔值，如果为true，则使用windowTitle作为窗口标题；如果为false，则使用默认标题。
     */
    public S2DPacketOpenWindow(int windowId, int inventoryType, String windowTitle, int slotCount, boolean useCustomTitle) {
        this.windowId = windowId;
        this.inventoryType = inventoryType;
        this.windowTitle = windowTitle;
        this.slotCount = slotCount;
        this.useCustomTitle = useCustomTitle;
    }

    /**
     * 构造一个打开窗口的数据包实例，包含额外的参数。
     *
     * @param windowId 窗口的唯一标识符，用于区分不同的窗口类型。
     * @param inventoryType 窗口的槽位数量，表示该窗口可以容纳的物品栏格子数量。
     * @param windowTitle 窗口的标题，用于显示在窗口顶部的文字描述。
     * @param slotCount 窗口相关的实体ID（如果适用），例如：当窗口与特定实体（如：箱子、村民等）相关时，此字段将存储该实体的ID。
     * @param useCustomTitle 标识窗口是否使用自定义标题的布尔值，如果为true，则使用windowTitle作为窗口标题；如果为false，则使用默认标题。
     * @param additionalField 额外的字段，具体用途根据上下文确定，例如：某些窗口可能需要额外的状态信息。
     */
    public S2DPacketOpenWindow(int windowId, int inventoryType, String windowTitle, int slotCount, boolean useCustomTitle, int additionalField) {
        this(windowId, inventoryType, windowTitle, slotCount, useCustomTitle);
        this.additionalField = additionalField;
    }

    /**
     * 将此数据包传递给 NetHandler 进行处理。
     */
    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleOpenWindow(this);
    }

    /**
     * 从数据流中读取原始数据包数据。
     */
    public void readPacketData(PacketBuffer data) throws IOException
    {
        this.windowId = data.readUnsignedByte();
        this.inventoryType = data.readUnsignedByte();
        this.windowTitle = data.readStringFromBuffer(32);
        this.slotCount = data.readUnsignedByte();
        this.useCustomTitle = data.readBoolean();

        if (this.inventoryType == 11)
        {
            this.additionalField = data.readInt();
        }
    }

    /**
     * 将原始数据包数据写入数据流。
     *
     * @param data 数据流对象，类型为PacketBuffer，用于存储和传输数据包的数据。
     * @throws IOException 如果在写入数据时发生IO异常，则抛出该异常。
     */
    public void writePacketData(PacketBuffer data) throws IOException {
        data.writeByte(this.windowId); // 写入窗口的唯一标识符
        data.writeByte(this.inventoryType); // 写入窗口的槽位数量
        data.writeStringToBuffer(this.windowTitle); // 写入窗口的标题
        data.writeByte(this.slotCount); // 写入窗口相关的实体ID
        data.writeBoolean(this.useCustomTitle); // 写入是否使用自定义标题的标志

        if (this.inventoryType == 11) {
            data.writeInt(this.additionalField); // 如果槽位数量为11，则写入额外的字段
        }
    }


    @SideOnly(Side.CLIENT)
    public int func_148901_c()
    {
        return this.windowId;
    }

    @SideOnly(Side.CLIENT)
    public int func_148899_d()
    {
        return this.inventoryType;
    }

    @SideOnly(Side.CLIENT)
    public String func_148902_e()
    {
        return this.windowTitle;
    }

    @SideOnly(Side.CLIENT)
    public int func_148898_f()
    {
        return this.slotCount;
    }

    @SideOnly(Side.CLIENT)
    public boolean func_148900_g()
    {
        return this.useCustomTitle;
    }

    @SideOnly(Side.CLIENT)
    public int func_148897_h()
    {
        return this.additionalField;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler handler)
    {
        this.processPacket((INetHandlerPlayClient)handler);
    }
}