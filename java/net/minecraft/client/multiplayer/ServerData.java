package net.minecraft.client.multiplayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

/**
 * 服务器数据管理类
 */
@SideOnly(Side.CLIENT)
public class ServerData {
    public String serverName; // 服务器名称
    public String serverIP; // 服务器IP地址
    /**
     * 服务器状态显示信息（如 "5/20" 表示在线人数/最大容量）
     */
    public String populationInfo;
    /**
     * 服务器描述信息（显示在服务器列表的灰色文本行）
     */
    public String serverMOTD;
    /** 最近一次服务器列表刷新时的延迟 */
    public long pingToServer;
    public int magicNumber; // 未知字段（可能与服务器搜索相关）初始化为5
    /** 服务器游戏版本 */
    public String gameVersion;
    public boolean isShow; // 未知布尔字段
    public String field_147412_i; // 未知字符串字段
    private ServerData.ServerResourceMode serverResMode; // 资源模式
    private String iconBase64; // 服务器图标Base64编码
    private boolean unknowBool; // 是否启用某种特殊模式
    private static final String __OBFID = "CL_00000890"; // 反编译标识符

    public ServerData(String p_i1193_1_, String p_i1193_2_) {
        this.magicNumber = 5;
        this.gameVersion = "1.7.10";
        this.serverResMode = ServerData.ServerResourceMode.PROMPT;
        this.serverName = p_i1193_1_;
        this.serverIP = p_i1193_2_;
    }

    public ServerData(String p_i1055_1_, String p_i1055_2_, boolean p_i1055_3_) {
        this(p_i1055_1_, p_i1055_2_);
        this.unknowBool = p_i1055_3_;
    }

    /**
     * 获取服务器数据的NBT表示形式（用于存档/传输）
     */
    public NBTTagCompound getNBTCompound() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("name", this.serverName); // 保存服务器名称
        nbttagcompound.setString("ip", this.serverIP); // 保存服务器IP

        if (this.iconBase64 != null) {
            nbttagcompound.setString("icon", this.iconBase64);
        }

        // 根据资源模式设置acceptTextures字段
        if (this.serverResMode == ServerData.ServerResourceMode.ENABLED) {
            nbttagcompound.setBoolean("acceptTextures", true);
        } else if (this.serverResMode == ServerData.ServerResourceMode.DISABLED) {
            nbttagcompound.setBoolean("acceptTextures", false);
        }

        return nbttagcompound;
    }

    public ServerData.ServerResourceMode func_152586_b() {
        return this.serverResMode;
    }

    public void func_152584_a(ServerData.ServerResourceMode mode) {
        this.serverResMode = mode;
    }

    /**
     * 从NBT数据解析服务器信息
     */
    public static ServerData getServerDataFromNBTCompound(NBTTagCompound nbtCompound) {
        ServerData serverdata = new ServerData(nbtCompound.getString("name"), nbtCompound.getString("ip"));

        if (nbtCompound.hasKey("icon", 8)) {
            serverdata.getIconBase64(nbtCompound.getString("icon"));
        }

        if (nbtCompound.hasKey("acceptTextures", 1)) {
            if (nbtCompound.getBoolean("acceptTextures")) {
                serverdata.func_152584_a(ServerData.ServerResourceMode.ENABLED);
            } else {
                serverdata.func_152584_a(ServerData.ServerResourceMode.DISABLED);
            }
        } else {
            serverdata.func_152584_a(ServerData.ServerResourceMode.PROMPT);
        }

        return serverdata;
    }

    /**
     * Returns the base-64 encoded representation of the server's icon, or null if
     * not available
     */
    public String getBase64EncodedIconData() {
        return this.iconBase64;
    }

    public void getIconBase64(String icon) {
        this.iconBase64 = icon;
    }

    public void func_152583_a(ServerData serverDataIn) {
        this.serverIP = serverDataIn.serverIP;
        this.serverName = serverDataIn.serverName;
        this.func_152584_a(serverDataIn.func_152586_b());
        this.iconBase64 = serverDataIn.iconBase64;
    }

    // 获取特殊模式标志位
    public boolean func_152585_d() {
        return this.unknowBool;
    }

    @SideOnly(Side.CLIENT)
    public static enum ServerResourceMode {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final IChatComponent field_152594_d;

        private static final String __OBFID = "CL_00001833";

        private ServerResourceMode(String p_i1053_3_) {
            this.field_152594_d = new ChatComponentTranslation("addServer.resourcePack." + p_i1053_3_, new Object[0]);
        }

        public IChatComponent func_152589_a() {
            return this.field_152594_d;
        }
    }
}