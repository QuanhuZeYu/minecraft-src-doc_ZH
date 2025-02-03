package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.Unpooled;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

/**
 * 命令方块GUI界面，允许玩家编辑命令方块的命令和查看之前的输出。
 */
@SideOnly(Side.CLIENT)
public class GuiCommandBlock extends GuiScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    /** 命令输入文本框 */
    private GuiTextField commandInputField;
    /** 前一次输出显示文本框 */
    private GuiTextField previousOutputField;
    /** 关联的命令方块逻辑处理器 */
    private final CommandBlockLogic commandBlockLogic;
    /** 确认按钮 */
    private GuiButton doneBtn;
    /** 取消按钮 */
    private GuiButton cancelBtn;
    // 混淆标识符（Obfuscation ID）
    private static final String __OBFID = "CL_00000748";

    /**
     * 构造一个命令方块GUI界面
     * @param commandBlockLogic 要编辑的命令方块逻辑对象
     */
    public GuiCommandBlock(CommandBlockLogic commandBlockLogic) {
        this.commandBlockLogic = commandBlockLogic;
    }

    /**
     * 每帧更新界面元素状态
     */
    @Override
    public void updateScreen() {
        this.commandInputField.updateCursorCounter();
    }

    /**
     * 初始化GUI组件
     */
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        // 添加确认和取消按钮
        this.buttonList.add(this.doneBtn = new GuiButton(0,
                this.width / 2 - 4 - 150, this.height / 4 + 120 + 12,
                150, 20, I18n.format("gui.done")));
        this.buttonList.add(this.cancelBtn = new GuiButton(1,
                this.width / 2 + 4, this.height / 4 + 120 + 12,
                150, 20, I18n.format("gui.cancel")));

        // 初始化命令输入框
        this.commandInputField = new GuiTextField(
                this.fontRendererObj, this.width / 2 - 150, 50, 300, 20);
        this.commandInputField.setMaxStringLength(32767);
        this.commandInputField.setFocused(true);
        this.commandInputField.setText(this.commandBlockLogic.getCommand());

        // 初始化输出显示框（不可编辑）
        this.previousOutputField = new GuiTextField(
                this.fontRendererObj, this.width / 2 - 150, 135, 300, 20);
        this.previousOutputField.setMaxStringLength(32767);
        this.previousOutputField.setEnabled(false);
        this.previousOutputField.setText(this.commandBlockLogic.getCommand());

        // 如果有前次输出则显示
        if (this.commandBlockLogic.getLastOutput() != null) {
            this.previousOutputField.setText(
                    this.commandBlockLogic.getLastOutput().getUnformattedText());
        }

        this.doneBtn.enabled = !this.commandInputField.getText().trim().isEmpty();
    }

    /**
     * GUI关闭时禁用键盘重复事件
     */
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * 处理按钮点击事件
     * @param button 被点击的按钮对象
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {  // 取消按钮
                this.mc.displayGuiScreen(null);
            } else if (button.id == 0) {  // 确认按钮
                PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
                try {
                    // 构建并发送更新数据包
                    packetBuffer.writeByte(this.commandBlockLogic.getCommandBlockType());
                    this.commandBlockLogic.writeData(packetBuffer);
                    packetBuffer.writeStringToBuffer(this.commandInputField.getText());
                    this.mc.getNetHandler().addToSendQueue(
                            new C17PacketCustomPayload("MC|AdvCdm", packetBuffer));
                } catch (Exception ex) {
                    LOGGER.error("发送命令方块数据失败", ex);
                } finally {
                    packetBuffer.release();
                }
                this.mc.displayGuiScreen(null);
            }
        }
    }

    /**
     * 处理键盘输入事件
     * @param typedChar 输入字符
     * @param keyCode   按键代码
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        this.commandInputField.textboxKeyTyped(typedChar, keyCode);
        this.previousOutputField.textboxKeyTyped(typedChar, keyCode);
        this.doneBtn.enabled = !this.commandInputField.getText().trim().isEmpty();

        // 处理回车和ESC键
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            this.actionPerformed(this.doneBtn);
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            this.actionPerformed(this.cancelBtn);
        }
    }

    /**
     * 处理鼠标点击事件
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.commandInputField.mouseClicked(mouseX, mouseY, mouseButton);
        this.previousOutputField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * 绘制界面元素
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        // 绘制标题
        this.drawCenteredString(this.fontRendererObj,
                I18n.format("advMode.setCommand"),
                this.width / 2, 20, 0xFFFFFF);

        // 绘制输入框标签
        this.drawString(this.fontRendererObj,
                I18n.format("advMode.command"),
                this.width / 2 - 150, 37, 0xA0A0A0);
        this.commandInputField.drawTextBox();

        // 绘制指令提示文本
        int yPos = 75;
        this.drawString(this.fontRendererObj,
                I18n.format("advMode.nearestPlayer"),
                this.width / 2 - 150, yPos, 0xA0A0A0);
        this.drawString(this.fontRendererObj,
                I18n.format("advMode.randomPlayer"),
                this.width / 2 - 150, yPos + this.fontRendererObj.FONT_HEIGHT, 0xA0A0A0);
        this.drawString(this.fontRendererObj,
                I18n.format("advMode.allPlayers"),
                this.width / 2 - 150, yPos + 2 * this.fontRendererObj.FONT_HEIGHT, 0xA0A0A0);

        // 绘制前次输出区域
        if (!this.previousOutputField.getText().isEmpty()) {
            int outputY = yPos + 3 * this.fontRendererObj.FONT_HEIGHT + 20;
            this.drawString(this.fontRendererObj,
                    I18n.format("advMode.previousOutput"),
                    this.width / 2 - 150, outputY, 0xA0A0A0);
            this.previousOutputField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}