package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

/**
 * 游戏控制设置界面，用于管理按键绑定和部分游戏选项。
 */
@SideOnly(Side.CLIENT)
public class GuiControls extends GuiScreen {
    // 预定义的常用游戏选项数组（鼠标反转、灵敏度、触摸屏）
    private static final GameSettings.Options[] COMMON_OPTIONS = new GameSettings.Options[] {
            GameSettings.Options.INVERT_MOUSE,
            GameSettings.Options.SENSITIVITY,
            GameSettings.Options.TOUCHSCREEN
    };

    /** 父级界面，用于返回时使用 */
    private GuiScreen parentScreen;
    /** 界面标题文本 */
    protected String screenTitle = "Controls";
    /** 游戏设置对象引用 */
    private GameSettings gameSettings;
    /** 当前选中的按键绑定项 */
    public KeyBinding selectedKeyBinding = null;
    /** 最后一次按键绑定的时间戳（用于防抖处理） */
    public long lastKeyBindingTime;
    /** 按键绑定列表组件 */
    private GuiKeyBindingList keyBindingList;
    /** 重置所有按钮组件 */
    private GuiButton resetAllButton;
    /** 混淆标识符（代码混淆工具使用） */
    private static final String __OBFID = "CL_00000736";

    /**
     * 构造控制设置界面
     * @param parentScreen 父级界面
     * @param gameSettings 游戏设置对象
     */
    public GuiControls(GuiScreen parentScreen, GameSettings gameSettings) {
        this.parentScreen = parentScreen;
        this.gameSettings = gameSettings;
    }

    /**
     * 初始化界面组件
     */
    @Override
    public void initGui() {
        // 初始化按键绑定列表
        this.keyBindingList = new GuiKeyBindingList(this, this.mc);

        // 添加底部操作按钮
        this.buttonList.add(new GuiButton(
                200,
                this.width / 2 - 155,
                this.height - 29,
                150, 20,
                I18n.format("gui.done")
        ));

        this.buttonList.add(this.resetAllButton = new GuiButton(
                201,
                this.width / 2 - 155 + 160,
                this.height - 29,
                150, 20,
                I18n.format("controls.resetAll")
        ));

        // 设置本地化标题
        this.screenTitle = I18n.format("controls.title");

        // 创建选项按钮（两列布局）
        int optionIndex = 0;
        for (GameSettings.Options option : COMMON_OPTIONS) {
            int xPos = this.width / 2 - 155 + (optionIndex % 2) * 160;
            int yPos = 18 + 24 * (optionIndex / 2);

            if (option.isFloatOption()) {
                this.buttonList.add(new GuiOptionSlider(
                        option.ordinal(),
                        xPos, yPos,
                        option
                ));
            } else {
                this.buttonList.add(new GuiOptionButton(
                        option.ordinal(),
                        xPos, yPos,
                        option,
                        this.gameSettings.getKeyBinding(option)
                ));
            }
            optionIndex++;
        }
    }

    /**
     * 处理按钮点击事件
     * @param button 被点击的按钮对象
     */
    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 201)
        {
            KeyBinding[] akeybinding = this.mc.gameSettings.keyBindings;
            int i = akeybinding.length;

            for (int j = 0; j < i; ++j)
            {
                KeyBinding keybinding = akeybinding[j];
                keybinding.setKeyCode(keybinding.getKeyCodeDefault());
            }

            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else if (button.id < 100 && button instanceof GuiOptionButton)
        {
            this.gameSettings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
            button.displayString = this.gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        }
    }

    /**
     * 处理鼠标点击事件
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param mouseButton 鼠标按钮
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.selectedKeyBinding != null) {
            // 设置鼠标按键绑定（左键-100，右键-99）
            this.gameSettings.setOptionKeyBinding(this.selectedKeyBinding, -100 + mouseButton);
            this.selectedKeyBinding = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        } else {
            // 尝试处理列表项点击
            if (!this.keyBindingList.handleMouseClick(mouseX, mouseY, mouseButton)) {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    /**
     * 处理鼠标释放事件
     */
    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (!this.keyBindingList.handleMouseRelease(mouseX, mouseY, state)) {
            super.mouseMovedOrUp(mouseX, mouseY, state);
        }
    }

    /**
     * 处理键盘输入事件
     * @param typedChar 输入的字符
     * @param keyCode 按键代码
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.selectedKeyBinding != null) {
            if (keyCode == 1) { // ESC键
                this.gameSettings.setOptionKeyBinding(this.selectedKeyBinding, 0);
            } else {
                this.gameSettings.setOptionKeyBinding(this.selectedKeyBinding, keyCode);
            }
            this.selectedKeyBinding = null;
            this.lastKeyBindingTime = Minecraft.getSystemTime();
            KeyBinding.resetKeyBindingArrayAndHash();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * 绘制界面
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分时钟周期（用于动画）
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 绘制默认背景
        this.drawDefaultBackground();

        // 绘制按键绑定列表
        this.keyBindingList.drawScreen(mouseX, mouseY, partialTicks);

        // 绘制标题
        this.drawCenteredString(
                this.fontRendererObj,
                this.screenTitle,
                this.width / 2, 8,
                0xFFFFFF
        );

        // 检查是否需要禁用重置按钮
        boolean isDefaultSettings = true;
        for (KeyBinding keyBinding : this.gameSettings.keyBindings) {
            if (keyBinding.getKeyCode() != keyBinding.getKeyCodeDefault()) {
                isDefaultSettings = false;
                break;
            }
        }
        this.resetAllButton.enabled = !isDefaultSettings;

        // 绘制其他组件
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}