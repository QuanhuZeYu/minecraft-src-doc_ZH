package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.stream.GuiStreamOptions;
import net.minecraft.client.gui.stream.GuiStreamUnavailable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.IStream;

@SideOnly(Side.CLIENT)
public class GuiOptions extends GuiScreen implements GuiYesNoCallback {
    /**
     * 第一行基础选项（FOV和难度）
     * 建议命名：PRIMARY_OPTIONS
     */
    private static final GameSettings.Options[] PRIMARY_OPTIONS = new GameSettings.Options[] {
            GameSettings.Options.FOV,
            GameSettings.Options.DIFFICULTY
    };

    /**
     * 父级界面（返回时用）
     * 建议命名：parentScreen
     */
    private final GuiScreen parentScreen;

    /**
     * 游戏设置实例
     * 建议命名：gameSettings
     */
    private final GameSettings gameSettings;

    /**
     * 界面标题（初始化为"Options"）
     * 建议命名：screenTitle
     */
    protected String screenTitle = "Options";

    // 混淆标识符（无需修改）
    private static final String __OBFID = "CL_00000700";

    /**
     * 构造方法
     * @param parentScreen 父级界面
     * @param gameSettings 游戏设置实例
     */
    public GuiOptions(GuiScreen parentScreen, GameSettings gameSettings) {
        this.parentScreen = parentScreen;  // parentScreen
        this.gameSettings = gameSettings;  // gameSettings
    }

    /**
     * 初始化界面组件
     */
    public void initGui() {
        int buttonIndex = 0;  // 按钮索引（原变量名i）
        this.screenTitle = I18n.format("options.title");  // 国际化标题

        // 创建基础选项的按钮（FOV和难度）
        for (GameSettings.Options option : PRIMARY_OPTIONS) {  // aoptions改为option
            if (option.getEnumFloat()) {  // 浮点型选项（如FOV）
                this.buttonList.add(new GuiOptionSlider(
                        option.returnEnumOrdinal(),
                        this.width / 2 - 155 + buttonIndex % 2 * 160,
                        this.height / 6 - 12 + 24 * (buttonIndex >> 1),
                        option
                ));
            } else {  // 枚举型选项（如难度）
                GuiOptionButton button = new GuiOptionButton(
                        option.returnEnumOrdinal(),
                        this.width / 2 - 155 + buttonIndex % 2 * 160,
                        this.height / 6 - 12 + 24 * (buttonIndex >> 1),
                        option,
                        this.gameSettings.getKeyBinding(option)
                );

                // 硬核模式下禁用难度按钮
                if (option == GameSettings.Options.DIFFICULTY &&
                        this.mc.theWorld != null &&
                        this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled()
                ) {
                    button.enabled = false;
                    button.displayString = I18n.format("options.difficulty") + ": " +
                            I18n.format("options.difficulty.hardcore");
                }

                this.buttonList.add(button);
            }
            buttonIndex++;
        }

        // --- 添加功能按钮 ---

        // "超级秘密设置"按钮（ID 8675309）
        this.buttonList.add(new GuiButton(8675309, this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, "Super Secret Settings...") {
            private static final String __OBFID = "CL_00000701";

            /**
             * 按钮点击音效（播放随机环境声音）
             * 建议方法名：playClickSound
             */
            public void playClickSound(SoundHandler soundHandler) {  // soundHandlerIn -> soundHandler
                SoundEventAccessorComposite sound = soundHandler.getRandomSoundFromCategories(
                        new SoundCategory[] {  // 随机选择声音类别
                                SoundCategory.ANIMALS,
                                SoundCategory.BLOCKS,
                                SoundCategory.MOBS,
                                SoundCategory.PLAYERS,
                                SoundCategory.WEATHER
                        }
                );
                if (sound != null) {
                    soundHandler.playSound(
                            PositionedSoundRecord.func_147674_a(  // 建议方法名：createPositioned
                                    sound.getSoundEventLocation(),
                                    0.5F
                            )
                    );
                }
            }
        });

        // 其他功能按钮（布局按两列排列）
        this.buttonList.add(new GuiButton(106, this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.sounds")));
        this.buttonList.add(new GuiButton(107, this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.stream")));
        this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.video")));
        this.buttonList.add(new GuiButton(100, this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.controls")));
        this.buttonList.add(new GuiButton(102, this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.language")));
        this.buttonList.add(new GuiButton(103, this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.multiplayer.title")));
        this.buttonList.add(new GuiButton(105, this.width / 2 - 155, this.height / 6 + 144 - 6, 150, 20, I18n.format("options.resourcepack")));
        this.buttonList.add(new GuiButton(104, this.width / 2 + 5, this.height / 6 + 144 - 6, 150, 20, I18n.format("options.snooper.view")));

        // 完成按钮
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
    }

    /**
     * 处理按钮点击事件
     */
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            // 处理基础选项按钮（ID < 100）
            if (button.id < 100 && button instanceof GuiOptionButton) {
                GameSettings.Options option = ((GuiOptionButton) button).returnEnumOptions();
                this.gameSettings.setOptionValue(option, 1);  // 更新设置
                button.displayString = this.gameSettings.getKeyBinding(option);  // 更新按钮文本
            }

            // 处理其他功能按钮
            switch (button.id) {
                case 8675309: // "超级秘密设置" - 切换着色器
                    this.mc.entityRenderer.activateNextShader();
                    break;
                case 101: // 视频设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiVideoSettings(this, this.gameSettings));
                    break;
                case 100: // 控制设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiControls(this, this.gameSettings));
                    break;
                case 102: // 语言设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiLanguage(this, this.gameSettings, this.mc.getLanguageManager()));
                    break;
                case 103: // 多人游戏设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new ScreenChatOptions(this, this.gameSettings));
                    break;
                case 104: // 侦测器设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiSnooper(this, this.gameSettings));
                    break;
                case 200: // 完成按钮
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(this.parentScreen);  // 返回父界面
                    break;
                case 105: // 资源包管理
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiScreenResourcePacks(this));
                    break;
                case 106: // 声音设置
                    this.mc.gameSettings.saveOptions();
                    this.mc.displayGuiScreen(new GuiScreenOptionsSounds(this, this.gameSettings));
                    break;
                case 107: // 流媒体设置（需检查流状态）
                    this.mc.gameSettings.saveOptions();
                    IStream stream = this.mc.func_152346_Z();  // 建议方法名：getStreamManager
                    if (stream.func_152936_l() && stream.func_152928_D()) {  // 建议方法名：isStreamActive & isStreamReady
                        this.mc.displayGuiScreen(new GuiStreamOptions(this, this.gameSettings));
                    } else {
                        GuiStreamUnavailable.func_152321_a(this);  // 显示流不可用界面
                    }
                    break;
            }
        }
    }

    /**
     * 绘制界面
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();  // 绘制默认背景
        // 绘制标题
        this.drawCenteredString(
                this.fontRendererObj,
                this.screenTitle,
                this.width / 2,
                15,
                0xFFFFFF  // 16777215 -> 白色十六进制
        );
        super.drawScreen(mouseX, mouseY, partialTicks);  // 绘制其他组件
    }
}