package net.minecraft.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiIngameMenu extends GuiScreen
{
    private int buttonYOffset; // 建议命名为：buttonYOffset 或 buttonVerticalSpacing
    private int screenUpdateCounter; // 建议命名为：screenUpdateCounter 或 updateTicks
    private static final String __OBFID = "CL_00000703";

    /**
     * 初始化游戏内菜单界面，添加按钮和其他控件。
     */
    public void initGui()
    {
        this.buttonYOffset = 0; // 重置按钮的垂直偏移量
        this.buttonList.clear(); // 清空按钮列表
        byte b0 = -16; // 按钮垂直间距的固定偏移量
        boolean flag = true;

        // 添加“返回主菜单”按钮
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + b0, I18n.format("menu.returnToMenu", new Object[0])));

        // 如果当前不是在单人游戏中，则显示“断开连接”按钮
        if (!this.mc.isIntegratedServerRunning())
        {
            ((GuiButton)this.buttonList.get(0)).displayString = I18n.format("menu.disconnect", new Object[0]);
        }

        // 添加“返回游戏”按钮
        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + b0, I18n.format("menu.returnToGame", new Object[0])));

        // 添加“选项”按钮
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + b0, 98, 20, I18n.format("menu.options", new Object[0])));

        // 添加“Mod选项”按钮
        this.buttonList.add(new GuiButton(12, this.width / 2 + 2, this.height / 4 + 96 + b0, 98, 20, "Mod Options..."));

        // 添加“分享到局域网”按钮，并设置是否可用
        GuiButton guibutton;
        this.buttonList.add(guibutton = new GuiButton(7, this.width / 2 - 100, this.height / 4 + 72 + b0, 200, 20, I18n.format("menu.shareToLan", new Object[0])));

        // 添加“成就”按钮
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + b0, 98, 20, I18n.format("gui.achievements", new Object[0])));

        // 添加“统计”按钮
        this.buttonList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + b0, 98, 20, I18n.format("gui.stats", new Object[0])));

        // 设置“分享到局域网”按钮是否可用（仅在单人游戏且未公开时可用）
        guibutton.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();
    }

    /**
     * 处理按钮点击事件。
     * @param button 被点击的按钮
     */
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 0: // 点击“选项”按钮
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 1: // 点击“返回主菜单”或“断开连接”按钮
                button.enabled = false;
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld((WorldClient)null);
                this.mc.displayGuiScreen(new GuiMainMenu());
            case 2:
            case 3:
            default:
                break;
            case 4: // 点击“返回游戏”按钮
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;
            case 5: // 点击“成就”按钮
                if (this.mc.thePlayer != null)
                    this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                break;
            case 6: // 点击“统计”按钮
                if (this.mc.thePlayer != null)
                    this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
                break;
            case 7: // 点击“分享到局域网”按钮
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case 12: // 点击“Mod选项”按钮
                FMLClientHandler.instance().showInGameModOptions(this);
                break;
        }
    }

    /**
     * 从主游戏循环中调用，用于更新屏幕。
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++this.screenUpdateCounter; // 更新计数器
    }

    /**
     * 绘制屏幕及其中的所有组件。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分刻（用于平滑动画）
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground(); // 绘制默认背景
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215); // 绘制标题
        super.drawScreen(mouseX, mouseY, partialTicks); // 调用父类绘制方法
    }
}