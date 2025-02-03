package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * GuiScreen 是 Minecraft 中所有 GUI 屏幕的基类。
 * 它提供了绘制屏幕、处理输入、管理按钮和标签等基本功能。
 */
@SideOnly(Side.CLIENT)
public class GuiScreen extends Gui
{
    /** 用于在屏幕上绘制成就图标的 RenderItem 实例（基于 ItemStack）。 */
    protected static RenderItem itemRender = new RenderItem();
    /** Minecraft 实例的引用。 */
    public Minecraft mc;
    /** 屏幕的宽度。 */
    public int width;
    /** 屏幕的高度。 */
    public int height;
    /** 此容器中所有按钮的列表。 */
    protected List<net.minecraft.client.gui.GuiButton> buttonList = new ArrayList();
    /** 此容器中所有标签的列表。 */
    protected List<net.minecraft.client.gui.GuiLabel> labelList = new ArrayList();
    /** 是否允许用户输入。 */
    public boolean allowUserInput;
    /** GuiScreen 使用的字体渲染器。 */
    protected FontRenderer fontRendererObj;
    /** 刚刚按下的按钮。 */
    private GuiButton selectedButton;
    /** 鼠标事件的按钮。 */
    private int eventButton;
    /** 上次鼠标事件的时间。 */
    private long lastMouseEvent;
    /** 用于触摸屏的计数器。 */
    private int touchScreenCounter;
    private static final String __OBFID = "CL_00000710";

    /**
     * 绘制屏幕及其所有组件。
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     * @param partialTicks 部分刻数，用于平滑动画
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int k;

        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        }

        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).drawLabel(this.mc, mouseX, mouseY);
        }
    }

    /**
     * 当按键被键入时触发。相当于 KeyListener.keyTyped(KeyEvent e)。
     * @param typedChar 键入的字符
     * @param keyCode 按键的代码
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == 1) // ESC 键
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
    }

    /**
     * 从系统剪贴板获取字符串。
     * @return 剪贴板中的字符串，如果获取失败则返回空字符串
     */
    public static String getClipboardString()
    {
        try
        {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                return (String)transferable.getTransferData(DataFlavor.stringFlavor);
            }
        }
        catch (Exception exception)
        {
            ;
        }

        return "";
    }

    /**
     * 将给定字符串存储到系统剪贴板。
     * @param copyText 要存储到剪贴板的字符串
     */
    public static void setClipboardString(String copyText)
    {
        try
        {
            StringSelection stringselection = new StringSelection(copyText);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, (ClipboardOwner)null);
        }
        catch (Exception exception)
        {
            ;
        }
    }

    /**
     * 渲染物品的工具提示。
     * @param itemIn 要渲染工具提示的物品
     * @param x 工具提示的 X 坐标
     * @param y 工具提示的 Y 坐标
     */
    protected void renderToolTip(ItemStack itemIn, int x, int y)
    {
        List list = itemIn.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

        for (int k = 0; k < list.size(); ++k)
        {
            if (k == 0)
            {
                list.set(k, itemIn.getRarity().rarityColor + (String)list.get(k));
            }
            else
            {
                list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
            }
        }

        FontRenderer font = itemIn.getItem().getFontRenderer(itemIn);
        drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));
    }

    /**
     * 当鼠标悬停在创造模式库存标签上时绘制文本。
     * @param tabName 标签名称
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     */
    protected void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY)
    {
        this.func_146283_a(Arrays.asList(new String[] {tabName}), mouseX, mouseY);
    }

    /**
     * 绘制悬停文本。
     * @param textLines 文本行列表
     * @param x 文本的 X 坐标
     * @param y 文本的 Y 坐标
     */
    protected void func_146283_a(List<String> textLines, int x, int y)
    {
        drawHoveringText(textLines, x, y, fontRendererObj);
    }

    /**
     * 绘制悬停文本。
     * @param textLines 文本行列表
     * @param x 文本的 X 坐标
     * @param y 文本的 Y 坐标
     * @param font 字体渲染器
     */
    protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font)
    {
        if (!textLines.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int maxTextWidth = 0;
            Iterator iterator = textLines.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int textWidth = font.getStringWidth(s);

                if (textWidth > maxTextWidth)
                {
                    maxTextWidth = textWidth;
                }
            }

            int textX = x + 12;
            int textY = y - 12;
            int textHeight = 8;

            if (textLines.size() > 1)
            {
                textHeight += 2 + (textLines.size() - 1) * 10;
            }

            if (textX + maxTextWidth > this.width)
            {
                textX -= 28 + maxTextWidth;
            }

            if (textY + textHeight + 6 > this.height)
            {
                textY = this.height - textHeight - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int backgroundColor = -267386864;
            this.drawGradientRect(textX - 3, textY - 4, textX + maxTextWidth + 3, textY - 3, backgroundColor, backgroundColor);
            this.drawGradientRect(textX - 3, textY + textHeight + 3, textX + maxTextWidth + 3, textY + textHeight + 4, backgroundColor, backgroundColor);
            this.drawGradientRect(textX - 3, textY - 3, textX + maxTextWidth + 3, textY + textHeight + 3, backgroundColor, backgroundColor);
            this.drawGradientRect(textX - 4, textY - 3, textX - 3, textY + textHeight + 3, backgroundColor, backgroundColor);
            this.drawGradientRect(textX + maxTextWidth + 3, textY - 3, textX + maxTextWidth + 4, textY + textHeight + 3, backgroundColor, backgroundColor);
            int borderColor = 1347420415;
            int borderColor2 = (borderColor & 16711422) >> 1 | borderColor & -16777216;
            this.drawGradientRect(textX - 3, textY - 3 + 1, textX - 3 + 1, textY + textHeight + 3 - 1, borderColor, borderColor2);
            this.drawGradientRect(textX + maxTextWidth + 2, textY - 3 + 1, textX + maxTextWidth + 3, textY + textHeight + 3 - 1, borderColor, borderColor2);
            this.drawGradientRect(textX - 3, textY - 3, textX + maxTextWidth + 3, textY - 3 + 1, borderColor, borderColor);
            this.drawGradientRect(textX - 3, textY + textHeight + 2, textX + maxTextWidth + 3, textY + textHeight + 3, borderColor2, borderColor2);

            for (int i = 0; i < textLines.size(); ++i)
            {
                String s1 = (String)textLines.get(i);
                font.drawStringWithShadow(s1, textX, textY, -1);

                if (i == 0)
                {
                    textY += 2;
                }

                textY += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    /**
     * 当鼠标被点击时调用。
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     * @param mouseButton 鼠标按钮
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0) // 左键
        {
            for (int l = 0; l < this.buttonList.size(); ++l)
            {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY))
                {
                    ActionPerformedEvent.Pre event = new ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        break;
                    this.selectedButton = event.button;
                    event.button.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(event.button);
                    if (this.equals(this.mc.currentScreen))
                        MinecraftForge.EVENT_BUS.post(new ActionPerformedEvent.Post(this, event.button, this.buttonList));
                }
            }
        }
    }

    /**
     * 当鼠标移动或鼠标按钮释放时调用。
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     * @param state 鼠标状态，-1 表示鼠标移动，0 或 1 表示鼠标释放
     */
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state)
    {
        if (this.selectedButton != null && state == 0)
        {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    /**
     * 当鼠标按钮被按下并移动时调用。
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     * @param clickedMouseButton 被点击的鼠标按钮
     * @param timeSinceLastClick 自上次点击以来的时间
     */
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    /**
     * 当按钮被点击时调用。
     * @param button 被点击的按钮
     */
    protected void actionPerformed(GuiButton button) {}

    /**
     * 设置屏幕的世界和分辨率。
     * @param mc Minecraft 实例
     * @param width 屏幕宽度
     * @param height 屏幕高度
     */
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.fontRendererObj = mc.fontRenderer;
        this.width = width;
        this.height = height;
        if (!MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Pre(this, this.buttonList)))
        {
            this.buttonList.clear();
            this.initGui();
        }
        MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Post(this, this.buttonList));
    }

    /**
     * 初始化 GUI，添加按钮和其他控件。
     */
    public void initGui() {}

    /**
     * 处理鼠标和键盘输入。
     */
    public void handleInput()
    {
        if (Mouse.isCreated())
        {
            while (Mouse.next())
            {
                this.handleMouseInput();
            }
        }

        if (Keyboard.isCreated())
        {
            while (Keyboard.next())
            {
                this.handleKeyboardInput();
            }
        }
    }

    /**
     * 处理鼠标输入。
     */
    public void handleMouseInput()
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int mouseButton = Mouse.getEventButton();

        if (Mouse.getEventButtonState())
        {
            if (this.mc.gameSettings.touchscreen && this.touchScreenCounter++ > 0)
            {
                return;
            }

            this.eventButton = mouseButton;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(mouseX, mouseY, this.eventButton);
        }
        else if (mouseButton != -1)
        {
            if (this.mc.gameSettings.touchscreen && --this.touchScreenCounter > 0)
            {
                return;
            }

            this.eventButton = -1;
            this.mouseMovedOrUp(mouseX, mouseY, mouseButton);
        }
        else if (this.eventButton != -1 && this.lastMouseEvent > 0L)
        {
            long timeSinceLastClick = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(mouseX, mouseY, this.eventButton, timeSinceLastClick);
        }
    }

    /**
     * 处理键盘输入。
     */
    public void handleKeyboardInput()
    {
        if (Keyboard.getEventKeyState())
        {
            this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }

        this.mc.func_152348_aa();
    }

    /**
     * 从主游戏循环调用以更新屏幕。
     */
    public void updateScreen() {}

    /**
     * 当屏幕卸载时调用。用于禁用键盘重复事件。
     */
    public void onGuiClosed() {}

    /**
     * 绘制背景渐变（当背景存在时）或在 background.png 上绘制平面渐变。
     */
    public void drawDefaultBackground()
    {
        this.drawWorldBackground(0);
    }

    /**
     * 绘制世界背景。
     * @param tint 背景色调
     */
    public void drawWorldBackground(int tint)
    {
        if (this.mc.theWorld != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else
        {
            this.drawBackground(tint);
        }
    }

    /**
     * 绘制背景。
     * @param tint 背景色调
     */
    public void drawBackground(int tint)
    {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;
        this.mc.getTextureManager().bindTexture(optionsBackground);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(4210752);
        tessellator.addVertexWithUV(0.0D, (double)this.height, 0.0D, 0.0D, (double)((float)this.height / f + (float)tint));
        tessellator.addVertexWithUV((double)this.width, (double)this.height, 0.0D, (double)((float)this.width / f), (double)((float)this.height / f + (float)tint));
        tessellator.addVertexWithUV((double)this.width, 0.0D, 0.0D, (double)((float)this.width / f), (double)tint);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, (double)tint);
        tessellator.draw();
    }

    /**
     * 返回此 GUI 是否应在单玩家模式下暂停游戏。
     * @return 如果应暂停游戏则返回 true，否则返回 false
     */
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    /**
     * 当确认对话框被点击时调用。
     * @param result 确认结果
     * @param id 确认 ID
     */
    public void confirmClicked(boolean result, int id) {}

    /**
     * 返回是否按下了 Ctrl 键（Windows 或 Mac）。
     * @return 如果按下了 Ctrl 键则返回 true，否则返回 false
     */
    public static boolean isCtrlKeyDown()
    {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    /**
     * 返回是否按下了 Shift 键。
     * @return 如果按下了 Shift 键则返回 true，否则返回 false
     */
    public static boolean isShiftKeyDown()
    {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }
}