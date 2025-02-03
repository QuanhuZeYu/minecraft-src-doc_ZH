package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiCreateWorld extends GuiScreen
{
    /**
     * 父屏幕，通常为世界选择界面。
     */
    private GuiScreen parentScreen; // 建议变量名：parentScreen

    /**
     * 世界名称输入框。
     */
    private GuiTextField worldNameTextField; // 建议变量名：worldNameTextField

    /**
     * 世界种子输入框。
     */
    private GuiTextField seedTextField; // 建议变量名：seedTextField

    /**
     * 生成的世界名称。
     */
    private String generatedWorldName; // 建议变量名：generatedWorldName

    /**
     * 当前选择的游戏模式。
     */
    private String selectedGameMode = "survival"; // 建议变量名：selectedGameMode

    /**
     * 是否启用地图特性（如村庄、地牢等）。
     */
    private boolean mapFeaturesEnabled = true; // 建议变量名：mapFeaturesEnabled

    /**
     * 是否允许作弊。
     */
    private boolean allowCheats; // 建议变量名：allowCheats

    /**
     * 是否为硬核模式。
     */
    private boolean hardcoreMode; // 建议变量名：hardcoreMode

    /**
     * 是否为奖励箱模式。
     */
    private boolean bonusChestEnabled; // 建议变量名：bonusChestEnabled

    /**
     * 是否为创造模式。
     */
    private boolean creativeMode; // 建议变量名：creativeMode

    /**
     * 是否正在创建世界。
     */
    private boolean isCreatingWorld; // 建议变量名：isCreatingWorld

    /**
     * 是否显示更多世界选项。
     */
    private boolean showMoreOptions; // 建议变量名：showMoreOptions

    /**
     * 游戏模式按钮。
     */
    private GuiButton gameModeButton; // 建议变量名：gameModeButton

    /**
     * 更多世界选项按钮。
     */
    private GuiButton moreOptionsButton; // 建议变量名：moreOptionsButton

    /**
     * 地图特性按钮。
     */
    private GuiButton mapFeaturesButton; // 建议变量名：mapFeaturesButton

    /**
     * 奖励箱按钮。
     */
    private GuiButton bonusChestButton; // 建议变量名：bonusChestButton

    /**
     * 地图类型按钮。
     */
    private GuiButton mapTypeButton; // 建议变量名：mapTypeButton

    /**
     * 允许作弊按钮。
     */
    private GuiButton allowCheatsButton; // 建议变量名：allowCheatsButton

    /**
     * 自定义类型按钮。
     */
    private GuiButton customizeTypeButton; // 建议变量名：customizeTypeButton

    /**
     * 游戏模式的第一行描述。
     */
    private String gameModeLine1; // 建议变量名：gameModeLine1

    /**
     * 游戏模式的第二行描述。
     */
    private String gameModeLine2; // 建议变量名：gameModeLine2

    /**
     * 世界种子。
     */
    private String worldSeed; // 建议变量名：worldSeed

    /**
     * 世界名称。
     */
    private String worldName; // 建议变量名：worldName

    /**
     * 当前选择的世界类型索引。
     */
    private int selectedWorldTypeIndex; // 建议变量名：selectedWorldTypeIndex

    /**
     * 生成器选项。
     */
    public String generatorOptions = ""; // 建议变量名：generatorOptions

    /**
     * 不允许的文件名列表。
     */
    private static final String[] denineNames = new String[] {"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private static final String __OBFID = "CL_00000689";

    /**
     * 构造函数，初始化创建世界界面。
     *
     * @param p_i1030_1_ 父屏幕
     */
    public GuiCreateWorld(GuiScreen p_i1030_1_)
    {
        this.parentScreen = p_i1030_1_;
        this.worldSeed = "";
        this.worldName = I18n.format("selectWorld.newWorld", new Object[0]);
    }

    /**
     * 从主游戏循环中调用以更新屏幕。
     */
    public void updateScreen()
    {
        this.worldNameTextField.updateCursorCounter();
        this.seedTextField.updateCursorCounter();
    }

    /**
     * 向屏幕添加按钮和其他控件。
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("selectWorld.create", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.buttonList.add(this.gameModeButton = new GuiButton(2, this.width / 2 - 75, 115, 150, 20, I18n.format("selectWorld.gameMode", new Object[0])));
        this.buttonList.add(this.moreOptionsButton = new GuiButton(3, this.width / 2 - 75, 187, 150, 20, I18n.format("selectWorld.moreWorldOptions", new Object[0])));
        this.buttonList.add(this.mapFeaturesButton = new GuiButton(4, this.width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures", new Object[0])));
        this.mapFeaturesButton.visible = false;
        this.buttonList.add(this.bonusChestButton = new GuiButton(7, this.width / 2 + 5, 151, 150, 20, I18n.format("selectWorld.bonusItems", new Object[0])));
        this.bonusChestButton.visible = false;
        this.buttonList.add(this.mapTypeButton = new GuiButton(5, this.width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType", new Object[0])));
        this.mapTypeButton.visible = false;
        this.buttonList.add(this.allowCheatsButton = new GuiButton(6, this.width / 2 - 155, 151, 150, 20, I18n.format("selectWorld.allowCommands", new Object[0])));
        this.allowCheatsButton.visible = false;
        this.buttonList.add(this.customizeTypeButton = new GuiButton(8, this.width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType", new Object[0])));
        this.customizeTypeButton.visible = false;
        this.worldNameTextField = new GuiTextField(this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.worldNameTextField.setFocused(true);
        this.worldNameTextField.setText(this.worldName);
        this.seedTextField = new GuiTextField(this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.seedTextField.setText(this.worldSeed);
        this.switchMoreState(this.showMoreOptions);
        this.genWorldName();
        this.updateButton();
    }

    /**
     * 生成世界名称，并确保其唯一性。
     */
    private void genWorldName()
    {
        this.generatedWorldName = this.worldNameTextField.getText().trim();
        char[] achar = ChatAllowedCharacters.allowedCharacters;
        int i = achar.length;

        for (int j = 0; j < i; ++j)
        {
            char c0 = achar[j];
            this.generatedWorldName = this.generatedWorldName.replace(c0, '_');
        }

        if (MathHelper.stringNullOrLengthZero(this.generatedWorldName))
        {
            this.generatedWorldName = "World";
        }

        this.generatedWorldName = ensureWorldName(this.mc.getSaveLoader(), this.generatedWorldName);
    }

    /**
     * 更新界面上的按钮和文本显示。
     */
    private void updateButton()
    {
        this.gameModeButton.displayString = I18n.format("selectWorld.gameMode", new Object[0]) + " " + I18n.format("selectWorld.gameMode." + this.selectedGameMode, new Object[0]);
        this.gameModeLine1 = I18n.format("selectWorld.gameMode." + this.selectedGameMode + ".line1", new Object[0]);
        this.gameModeLine2 = I18n.format("selectWorld.gameMode." + this.selectedGameMode + ".line2", new Object[0]);
        this.mapFeaturesButton.displayString = I18n.format("selectWorld.mapFeatures", new Object[0]) + " ";

        if (this.mapFeaturesEnabled)
        {
            this.mapFeaturesButton.displayString = this.mapFeaturesButton.displayString + I18n.format("options.on", new Object[0]);
        }
        else
        {
            this.mapFeaturesButton.displayString = this.mapFeaturesButton.displayString + I18n.format("options.off", new Object[0]);
        }

        this.bonusChestButton.displayString = I18n.format("selectWorld.bonusItems", new Object[0]) + " ";

        if (this.bonusChestEnabled && !this.creativeMode)
        {
            this.bonusChestButton.displayString = this.bonusChestButton.displayString + I18n.format("options.on", new Object[0]);
        }
        else
        {
            this.bonusChestButton.displayString = this.bonusChestButton.displayString + I18n.format("options.off", new Object[0]);
        }

        this.mapTypeButton.displayString = I18n.format("selectWorld.mapType", new Object[0]) + " " + I18n.format(WorldType.worldTypes[this.selectedWorldTypeIndex].getTranslateName(), new Object[0]);
        this.allowCheatsButton.displayString = I18n.format("selectWorld.allowCommands", new Object[0]) + " ";

        if (this.allowCheats && !this.creativeMode)
        {
            this.allowCheatsButton.displayString = this.allowCheatsButton.displayString + I18n.format("options.on", new Object[0]);
        }
        else
        {
            this.allowCheatsButton.displayString = this.allowCheatsButton.displayString + I18n.format("options.off", new Object[0]);
        }
    }

    /**
     * 确保世界名称的唯一性。
     *
     * @param p_146317_0_ 保存格式
     * @param p_146317_1_ 世界名称
     * @return 唯一的世界名称
     */
    public static String ensureWorldName(ISaveFormat p_146317_0_, String p_146317_1_)
    {
        p_146317_1_ = p_146317_1_.replaceAll("[\\./\"]", "_");
        String[] astring = denineNames;
        int i = astring.length;

        for (int j = 0; j < i; ++j)
        {
            String s1 = astring[j];

            if (p_146317_1_.equalsIgnoreCase(s1))
            {
                p_146317_1_ = "_" + p_146317_1_ + "_";
            }
        }

        while (p_146317_0_.getWorldInfo(p_146317_1_) != null)
        {
            p_146317_1_ = p_146317_1_ + "-";
        }

        return p_146317_1_;
    }

    /**
     * 当屏幕卸载时调用。用于禁用键盘重复事件。
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * 当按钮被点击时调用。
     *
     * @param button 被点击的按钮
     */
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            else if (button.id == 0)
            {
                this.mc.displayGuiScreen((GuiScreen)null);

                if (this.isCreatingWorld)
                {
                    return;
                }

                this.isCreatingWorld = true;
                long i = (new Random()).nextLong();
                String s = this.seedTextField.getText();

                if (!MathHelper.stringNullOrLengthZero(s))
                {
                    try
                    {
                        long j = Long.parseLong(s);

                        if (j != 0L)
                        {
                            i = j;
                        }
                    }
                    catch (NumberFormatException numberformatexception)
                    {
                        i = (long)s.hashCode();
                    }
                }

                WorldType.worldTypes[this.selectedWorldTypeIndex].onGUICreateWorldPress();

                WorldSettings.GameType gametype = WorldSettings.GameType.getByName(this.selectedGameMode);
                WorldSettings worldsettings = new WorldSettings(i, gametype, this.mapFeaturesEnabled, this.creativeMode, WorldType.worldTypes[this.selectedWorldTypeIndex]);
                worldsettings.func_82750_a(this.generatorOptions);

                if (this.bonusChestEnabled && !this.creativeMode)
                {
                    worldsettings.enableBonusChest();
                }

                if (this.allowCheats && !this.creativeMode)
                {
                    worldsettings.enableCommands();
                }

                this.mc.launchIntegratedServer(this.generatedWorldName, this.worldNameTextField.getText().trim(), worldsettings);
            }
            else if (button.id == 3)
            {
                this.switchMoreState();
            }
            else if (button.id == 2)
            {
                if (this.selectedGameMode.equals("survival"))
                {
                    if (!this.hardcoreMode)
                    {
                        this.allowCheats = false;
                    }

                    this.creativeMode = false;
                    this.selectedGameMode = "hardcore";
                    this.creativeMode = true;
                    this.allowCheatsButton.enabled = false;
                    this.bonusChestButton.enabled = false;
                    this.updateButton();
                }
                else if (this.selectedGameMode.equals("hardcore"))
                {
                    if (!this.hardcoreMode)
                    {
                        this.allowCheats = true;
                    }

                    this.creativeMode = false;
                    this.selectedGameMode = "creative";
                    this.updateButton();
                    this.creativeMode = false;
                    this.allowCheatsButton.enabled = true;
                    this.bonusChestButton.enabled = true;
                }
                else
                {
                    if (!this.hardcoreMode)
                    {
                        this.allowCheats = false;
                    }

                    this.selectedGameMode = "survival";
                    this.updateButton();
                    this.allowCheatsButton.enabled = true;
                    this.bonusChestButton.enabled = true;
                    this.creativeMode = false;
                }

                this.updateButton();
            }
            else if (button.id == 4)
            {
                this.mapFeaturesEnabled = !this.mapFeaturesEnabled;
                this.updateButton();
            }
            else if (button.id == 7)
            {
                this.bonusChestEnabled = !this.bonusChestEnabled;
                this.updateButton();
            }
            else if (button.id == 5)
            {
                ++this.selectedWorldTypeIndex;

                if (this.selectedWorldTypeIndex >= WorldType.worldTypes.length)
                {
                    this.selectedWorldTypeIndex = 0;
                }

                while (WorldType.worldTypes[this.selectedWorldTypeIndex] == null || !WorldType.worldTypes[this.selectedWorldTypeIndex].getCanBeCreated())
                {
                    ++this.selectedWorldTypeIndex;

                    if (this.selectedWorldTypeIndex >= WorldType.worldTypes.length)
                    {
                        this.selectedWorldTypeIndex = 0;
                    }
                }

                this.generatorOptions = "";
                this.updateButton();
                this.switchMoreState(this.showMoreOptions);
            }
            else if (button.id == 6)
            {
                this.hardcoreMode = true;
                this.allowCheats = !this.allowCheats;
                this.updateButton();
            }
            else if (button.id == 8)
            {
                WorldType.worldTypes[selectedWorldTypeIndex].onCustomizeButton(mc, this);
            }
        }
    }

    /**
     * 切换更多世界选项的显示状态。
     */
    private void switchMoreState()
    {
        this.switchMoreState(!this.showMoreOptions);
    }

    /**
     * 设置更多世界选项的显示状态。
     *
     * @param p_146316_1_ 是否显示更多选项
     */
    private void switchMoreState(boolean p_146316_1_)
    {
        this.showMoreOptions = p_146316_1_;
        this.gameModeButton.visible = !this.showMoreOptions;
        this.mapFeaturesButton.visible = this.showMoreOptions;
        this.bonusChestButton.visible = this.showMoreOptions;
        this.mapTypeButton.visible = this.showMoreOptions;
        this.allowCheatsButton.visible = this.showMoreOptions;
        this.customizeTypeButton.visible = this.showMoreOptions && WorldType.worldTypes[this.selectedWorldTypeIndex].isCustomizable();

        if (this.showMoreOptions)
        {
            this.moreOptionsButton.displayString = I18n.format("gui.done", new Object[0]);
        }
        else
        {
            this.moreOptionsButton.displayString = I18n.format("selectWorld.moreWorldOptions", new Object[0]);
        }
    }

    /**
     * 当键盘按键被按下时调用。
     *
     * @param typedChar 输入的字符
     * @param keyCode   按键代码
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (this.worldNameTextField.isFocused() && !this.showMoreOptions)
        {
            this.worldNameTextField.textboxKeyTyped(typedChar, keyCode);
            this.worldName = this.worldNameTextField.getText();
        }
        else if (this.seedTextField.isFocused() && this.showMoreOptions)
        {
            this.seedTextField.textboxKeyTyped(typedChar, keyCode);
            this.worldSeed = this.seedTextField.getText();
        }

        if (keyCode == 28 || keyCode == 156)
        {
            this.actionPerformed((GuiButton)this.buttonList.get(0));
        }

        ((GuiButton)this.buttonList.get(0)).enabled = this.worldNameTextField.getText().length() > 0;
        this.genWorldName();
    }

    /**
     * 当鼠标点击时调用。
     *
     * @param mouseX      鼠标X坐标
     * @param mouseY      鼠标Y坐标
     * @param mouseButton 鼠标按钮
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.showMoreOptions)
        {
            this.seedTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            this.worldNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * 绘制屏幕及其所有组件。
     *
     * @param mouseX       鼠标X坐标
     * @param mouseY       鼠标Y坐标
     * @param partialTicks 部分刻
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("selectWorld.create", new Object[0]), this.width / 2, 20, -1);

        if (this.showMoreOptions)
        {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterSeed", new Object[0]), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.seedInfo", new Object[0]), this.width / 2 - 100, 85, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.mapFeatures.info", new Object[0]), this.width / 2 - 150, 122, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.allowCommands.info", new Object[0]), this.width / 2 - 150, 172, -6250336);
            this.seedTextField.drawTextBox();

            if (WorldType.worldTypes[this.selectedWorldTypeIndex].showWorldInfoNotice())
            {
                this.fontRendererObj.drawSplitString(I18n.format(WorldType.worldTypes[this.selectedWorldTypeIndex].func_151359_c(), new Object[0]), this.mapTypeButton.xPosition + 2, this.mapTypeButton.yPosition + 22, this.mapTypeButton.getButtonWidth(), 10526880);
            }
        }
        else
        {
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.enterName", new Object[0]), this.width / 2 - 100, 47, -6250336);
            this.drawString(this.fontRendererObj, I18n.format("selectWorld.resultFolder", new Object[0]) + " " + this.generatedWorldName, this.width / 2 - 100, 85, -6250336);
            this.worldNameTextField.drawTextBox();
            this.drawString(this.fontRendererObj, this.gameModeLine1, this.width / 2 - 100, 137, -6250336);
            this.drawString(this.fontRendererObj, this.gameModeLine2, this.width / 2 - 100, 149, -6250336);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 从世界信息中加载设置。
     *
     * @param p_146318_1_ 世界信息
     */
    public void loadFromWorldInfo(WorldInfo p_146318_1_)
    {
        this.worldName = I18n.format("selectWorld.newWorld.copyOf", new Object[] {p_146318_1_.getWorldName()});
        this.worldSeed = p_146318_1_.getSeed() + "";
        this.selectedWorldTypeIndex = p_146318_1_.getTerrainType().getWorldTypeID();
        this.generatorOptions = p_146318_1_.getGeneratorOptions();
        this.mapFeaturesEnabled = p_146318_1_.isMapFeaturesEnabled();
        this.allowCheats = p_146318_1_.areCommandsAllowed();

        if (p_146318_1_.isHardcoreModeEnabled())
        {
            this.selectedGameMode = "hardcore";
        }
        else if (p_146318_1_.getGameType().isSurvivalOrAdventure())
        {
            this.selectedGameMode = "survival";
        }
        else if (p_146318_1_.getGameType().isCreative())
        {
            this.selectedGameMode = "creative";
        }
    }
}