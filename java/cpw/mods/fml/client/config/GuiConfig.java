/*
 * Forge Mod Loader
 * Copyright (c) 2012-2014 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors (this class):
 *     bspkrs - implementation
 */

package cpw.mods.fml.client.config;

import static cpw.mods.fml.client.config.GuiUtils.RESET_CHAR;
import static cpw.mods.fml.client.config.GuiUtils.UNDO_CHAR;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.config.GuiConfigEntries.IConfigEntry;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * This class is the base GuiScreen for all config GUI screens. It can be extended by mods to provide the top-level config screen
 * that will be called when the Config button is clicked from the Main Menu Mods list.
 * 
 * @author bspkrs
 */
public class GuiConfig extends GuiScreen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    public final GuiScreen parentScreen;
    public String title = "Config GUI";
    public String titleLine2;
    @SuppressWarnings("rawtypes")
    public final List<IConfigElement> configElements;
    @SuppressWarnings("rawtypes")
    public final List<IConfigEntry> initEntries;
    public GuiConfigEntries entryList;
    private GuiButtonExt btnDefaultAll;
    private GuiButtonExt btnUndoAll;
    private GuiCheckBox chkApplyGlobally;
    public final String modID;
    /**
     * When set to a non-null value the OnConfigChanged and PostConfigChanged events will be posted when the Done button is pressed
     * if any configElements were changed (includes child screens). If not defined, the events will be posted if the parent gui is null
     * or if the parent gui is not an instance of GuiConfig.
     */
    public final String configID;
    public final boolean isWorldRunning;
    public final boolean allRequireWorldRestart;
    public final boolean allRequireMcRestart;
    public boolean needsRefresh = true;
    private HoverChecker undoHoverChecker;
    private HoverChecker resetHoverChecker;
    private HoverChecker checkBoxHoverChecker;

    /**
     * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded. If a non-null value is passed for configID,
     * the OnConfigChanged and PostConfigChanged events will be posted when the Done button is pressed if any configElements were changed
     * (includes child screens). If configID is not defined, the events will be posted if the parent gui is null or if the parent gui 
     * is not an instance of GuiConfig.
     *
     * @param parentScreen the parent GuiScreen object
     * @param configElements a List of IConfigProperty objects
     * @param modID the mod ID for the mod whose config settings will be edited
     * @param configID an identifier that will be passed to the OnConfigChanged and PostConfigChanged events. Setting this value will force 
     *            the save action to be called when the Done button is pressed on this screen if any configElements were changed.
     * @param allRequireWorldRestart send true if all configElements on this screen require a world restart
     * @param allRequireMcRestart send true if all configElements on this screen require MC to be restarted
     * @param title the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
     *            edited.
     */
    @SuppressWarnings("rawtypes")
    public GuiConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, String configID, 
            boolean allRequireWorldRestart, boolean allRequireMcRestart, String title)
    {
        this(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    /**
     * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded. This constructor passes null for configID.
     * If configID is not defined, the events will be posted if the parent gui is null or if the parent gui is not an instance of GuiConfig.
     *
     * @param parentScreen the parent GuiScreen object
     * @param configElements a List of IConfigProperty objects
     * @param modID the mod ID for the mod whose config settings will be edited
     * @param allRequireWorldRestart send true if all configElements on this screen require a world restart
     * @param allRequireMcRestart send true if all configElements on this screen require MC to be restarted
     * @param title the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
     *            edited.
     */
    @SuppressWarnings("rawtypes")
    public GuiConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, 
            boolean allRequireWorldRestart, boolean allRequireMcRestart, String title)
    {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, null);
    }

    /**
     * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded. This constructor passes null for configID.
     * If configID is not defined, the events will be posted if the parent gui is null or if the parent gui is not an instance of GuiConfig.
     *
     * @param parentScreen the parent GuiScreen object
     * @param configElements a List of IConfigProperty objects
     * @param modID the mod ID for the mod whose config settings will be edited
     * @param allRequireWorldRestart send true if all configElements on this screen require a world restart
     * @param allRequireMcRestart send true if all configElements on this screen require MC to be restarted
     * @param title the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
     *            edited.
     * @param titleLine2 the desired title second line for this screen. Typically this is used to send the category name of the category
     *            currently being edited.
     */
    @SuppressWarnings("rawtypes")
    public GuiConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, 
            boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2)
    {
        this(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }

    /**
     * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded. titleLine2 is specified in this constructor.
     * If a non-null value is passed for configID, the OnConfigChanged and PostConfigChanged events will be posted when the Done button is 
     * pressed if any configElements were changed (includes child screens). If configID is not defined, the events will be posted if the parent 
     * gui is null or if the parent gui is not an instance of GuiConfig.
     *
     * @param parentScreen the parent GuiScreen object
     * @param configElements a List of IConfigProperty objects
     * @param modID the mod ID for the mod whose config settings will be edited
     * @param configID an identifier that will be passed to the OnConfigChanged and PostConfigChanged events
     * @param allRequireWorldRestart send true if all configElements on this screen require a world restart
     * @param allRequireMcRestart send true if all configElements on this screen require MC to be restarted
     * @param title the desired title for this screen. For consistency it is recommended that you pass the path of the config file being
     *            edited.
     * @param titleLine2 the desired title second line for this screen. Typically this is used to send the category name of the category
     *            currently being edited.
     */
    @SuppressWarnings("rawtypes")
    public GuiConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, String configID, 
            boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2)
    {
        this.mc = Minecraft.getMinecraft();
        this.parentScreen = parentScreen;
        this.configElements = configElements;
        this.entryList = new GuiConfigEntries(this, mc);
        this.initEntries = new ArrayList<IConfigEntry>(entryList.listEntries);
        this.allRequireWorldRestart = allRequireWorldRestart;
        this.allRequireMcRestart = allRequireMcRestart;
        this.modID = modID;
        this.configID = configID;
        this.isWorldRunning = mc.theWorld != null;
        if (title != null)
            this.title = title;
        this.titleLine2 = titleLine2;
        if (this.titleLine2 != null && this.titleLine2.startsWith(" > "))
            this.titleLine2 = this.titleLine2.replaceFirst(" > ", "");
    }

    public static String getAbridgedConfigPath(String path)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.mcDataDir.getAbsolutePath().endsWith("."))
            return path.replace("\\", "/").replace(mc.mcDataDir.getAbsolutePath().replace("\\", "/").substring(0, mc.mcDataDir.getAbsolutePath().length() - 1), "/.minecraft/");
        else
            return path.replace("\\", "/").replace(mc.mcDataDir.getAbsolutePath().replace("\\", "/"), "/.minecraft");
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);

        if (this.entryList == null || this.needsRefresh)
        {
            this.entryList = new GuiConfigEntries(this, mc);
            this.needsRefresh = false;
        }

        int undoGlyphWidth = mc.fontRenderer.getStringWidth(UNDO_CHAR) * 2;
        int resetGlyphWidth = mc.fontRenderer.getStringWidth(RESET_CHAR) * 2;
        int doneWidth = Math.max(mc.fontRenderer.getStringWidth(I18n.format("gui.done")) + 20, 100);
        int undoWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.undoChanges")) + undoGlyphWidth + 20;
        int resetWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.resetToDefault")) + resetGlyphWidth + 20;
        int checkWidth = mc.fontRenderer.getStringWidth(I18n.format("fml.configgui.applyGlobally")) + 13;
        int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth + 5 + checkWidth) / 2;
        this.buttonList.add(new GuiButtonExt(2000, this.width / 2 - buttonWidthHalf, this.height - 29, doneWidth, 20, I18n.format("gui.done")));
        this.buttonList.add(this.btnDefaultAll = new GuiUnicodeGlyphButton(2001, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5,
                this.height - 29, resetWidth, 20, " " + I18n.format("fml.configgui.tooltip.resetToDefault"), RESET_CHAR, 2.0F));
        this.buttonList.add(btnUndoAll = new GuiUnicodeGlyphButton(2002, this.width / 2 - buttonWidthHalf + doneWidth + 5,
                this.height - 29, undoWidth, 20, " " + I18n.format("fml.configgui.tooltip.undoChanges"), UNDO_CHAR, 2.0F));
        this.buttonList.add(chkApplyGlobally = new GuiCheckBox(2003, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5 + resetWidth + 5,
                this.height - 24, I18n.format("fml.configgui.applyGlobally"), false));

        this.undoHoverChecker = new HoverChecker(this.btnUndoAll, 800);
        this.resetHoverChecker = new HoverChecker(this.btnDefaultAll, 800);
        this.checkBoxHoverChecker = new HoverChecker(chkApplyGlobally, 800);
        this.entryList.initGui();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed()
    {
        this.entryList.onGuiClosed();
        
        if (this.configID != null && this.parentScreen instanceof GuiConfig)
        {
            GuiConfig parentGuiConfig = (GuiConfig) this.parentScreen;
            parentGuiConfig.needsRefresh = true;
            parentGuiConfig.initGui();
        }
        
        if (!(this.parentScreen instanceof GuiConfig))
            Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 2000)
        {
            boolean flag = true;
            try
            {
                if ((configID != null || this.parentScreen == null || !(this.parentScreen instanceof GuiConfig)) 
                        && (this.entryList.hasChangedEntry(true)))
                {
                    boolean requiresMcRestart = this.entryList.saveConfigElements();

                    if (Loader.isModLoaded(modID))
                    {
                        ConfigChangedEvent event = new OnConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart);
                        FMLCommonHandler.instance().bus().post(event);
                        if (!event.getResult().equals(Result.DENY))
                            FMLCommonHandler.instance().bus().post(new PostConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart));
                        
                        if (requiresMcRestart)
                        {
                            flag = false;
                            mc.displayGuiScreen(new GuiMessageDialog(parentScreen, "fml.configgui.gameRestartTitle", 
                                    new ChatComponentText(I18n.format("fml.configgui.gameRestartRequired")), "fml.configgui.confirmRestartMessage"));
                        }
                        
                        if (this.parentScreen instanceof GuiConfig)
                            ((GuiConfig) this.parentScreen).needsRefresh = true;
                    }
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            
            if (flag)
                this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 2001)
        {
            this.entryList.setAllToDefault(this.chkApplyGlobally.isChecked());
        }
        else if (button.id == 2002)
        {
            this.entryList.undoAllChanges(this.chkApplyGlobally.isChecked());
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int x, int y, int mouseEvent)
    {
        if (mouseEvent != 0 || !this.entryList.handleMouseClick(x, y, mouseEvent))
        {
            this.entryList.mouseClicked(x, y, mouseEvent);
            super.mouseClicked(x, y, mouseEvent);
        }
    }

    /**
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
     */
    @Override
    protected void mouseMovedOrUp(int x, int y, int mouseEvent)
    {
        if (mouseEvent != 0 || !this.entryList.handleMouseRelease(x, y, mouseEvent))
        {
            super.mouseMovedOrUp(x, y, mouseEvent);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char eventChar, int eventKey)
    {
        if (eventKey == Keyboard.KEY_ESCAPE)
            this.mc.displayGuiScreen(parentScreen);
        else
            this.entryList.keyTyped(eventChar, eventKey);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        this.entryList.updateScreen();
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.entryList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
        String title2 = this.titleLine2;

        if (title2 != null)
        {
            int strWidth = mc.fontRenderer.getStringWidth(title2);
            int elipsisWidth = mc.fontRenderer.getStringWidth("...");
            if (strWidth > width - 6 && strWidth > elipsisWidth)
                title2 = mc.fontRenderer.trimStringToWidth(title2, width - 6 - elipsisWidth).trim() + "...";
            this.drawCenteredString(this.fontRendererObj, title2, this.width / 2, 18, 16777215);
        }

        this.btnUndoAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked()) && this.entryList.hasChangedEntry(this.chkApplyGlobally.isChecked());
        this.btnDefaultAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked()) && !this.entryList.areAllEntriesDefault(this.chkApplyGlobally.isChecked());
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.entryList.drawScreenPost(mouseX, mouseY, partialTicks);
        if (this.undoHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(this.mc.fontRenderer.listFormattedStringToWidth(I18n.format("fml.configgui.tooltip.undoAll"), 300), mouseX, mouseY);
        if (this.resetHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(this.mc.fontRenderer.listFormattedStringToWidth(I18n.format("fml.configgui.tooltip.resetAll"), 300), mouseX, mouseY);
        if (this.checkBoxHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(this.mc.fontRenderer.listFormattedStringToWidth(I18n.format("fml.configgui.tooltip.applyGlobally"), 300), mouseX, mouseY);
    }

    @SuppressWarnings("rawtypes")
    public void drawToolTip(List stringList, int x, int y)
    {
        this.func_146283_a(stringList, x, y);
    }
}