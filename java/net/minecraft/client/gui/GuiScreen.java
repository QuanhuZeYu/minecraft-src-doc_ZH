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

@SideOnly(Side.CLIENT)
public class GuiScreen extends Gui
{
    /** Holds a instance of RenderItem, used to draw the achievement icons on screen (is based on ItemStack) */
    protected static RenderItem itemRender = new RenderItem();
    /** Reference to the Minecraft object. */
    public Minecraft mc;
    /** The width of the screen object. */
    public int width;
    /** The height of the screen object. */
    public int height;
    /** A list of all the buttons in this container. */
    protected List<net.minecraft.client.gui.GuiButton> buttonList = new ArrayList();
    /** A list of all the labels in this container. */
    protected List<net.minecraft.client.gui.GuiLabel> labelList = new ArrayList();
    public boolean allowUserInput;
    /** The FontRenderer used by GuiScreen */
    protected FontRenderer fontRendererObj;
    /** The button that was just pressed. */
    private GuiButton selectedButton;
    private int eventButton;
    private long lastMouseEvent;
    private int field_146298_h;
    private static final String __OBFID = "CL_00000710";

    /**
     * Draws the screen and all the components in it.
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
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, mouseX, mouseY);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
    }

    /**
     * Returns a string stored in the system clipboard.
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
     * Stores the given string in the system clipboard
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
     * Draws the text when mouse is over creative inventory tab. Params: current creative tab to be checked, current
     * mouse x position, current mouse y position.
     */
    protected void drawCreativeTabHoveringText(String tabName, int mouseX, int mouseY)
    {
        this.func_146283_a(Arrays.asList(new String[] {tabName}), mouseX, mouseY);
    }

    protected void func_146283_a(List<String> textLines, int x, int y)
    {
        drawHoveringText(textLines, x, y, fontRendererObj);   
    }

    protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font)
    {
        if (!textLines.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = textLines.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int l = font.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int j2 = x + 12;
            int k2 = y - 12;
            int i1 = 8;

            if (textLines.size() > 1)
            {
                i1 += 2 + (textLines.size() - 1) * 10;
            }

            if (j2 + k > this.width)
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > this.height)
            {
                k2 = this.height - i1 - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int j1 = -267386864;
            this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            for (int i2 = 0; i2 < textLines.size(); ++i2)
            {
                String s1 = (String)textLines.get(i2);
                font.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0)
                {
                    k2 += 2;
                }

                k2 += 10;
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
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
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
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
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
     * Called when a mouse button is pressed and the mouse is moved around. Parameters are : mouseX, mouseY,
     * lastButtonClicked & timeSinceMouseClick.
     */
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    protected void actionPerformed(GuiButton button) {}

    /**
     * Causes the screen to lay out its subcomponents again. This is the equivalent of the Java call
     * Container.validate()
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
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {}

    /**
     * Delegates mouse and keyboard input.
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
     * Handles mouse input.
     */
    public void handleMouseInput()
    {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int k = Mouse.getEventButton();

        if (Mouse.getEventButtonState())
        {
            if (this.mc.gameSettings.touchscreen && this.field_146298_h++ > 0)
            {
                return;
            }

            this.eventButton = k;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(i, j, this.eventButton);
        }
        else if (k != -1)
        {
            if (this.mc.gameSettings.touchscreen && --this.field_146298_h > 0)
            {
                return;
            }

            this.eventButton = -1;
            this.mouseMovedOrUp(i, j, k);
        }
        else if (this.eventButton != -1 && this.lastMouseEvent > 0L)
        {
            long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
        }
    }

    /**
     * Handles keyboard input.
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
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {}

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {}

    /**
     * Draws either a gradient over the background screen (when it exists) or a flat gradient over background.png
     */
    public void drawDefaultBackground()
    {
        this.drawWorldBackground(0);
    }

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
     * Draws the background (i is always 0 as of 1.2.2)
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
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    public void confirmClicked(boolean result, int id) {}

    /**
     * Returns true if either windows ctrl key is down or if either mac meta key is down
     */
    public static boolean isCtrlKeyDown()
    {
        return Minecraft.isRunningOnMac ? Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) : Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157);
    }

    /**
     * Returns true if either shift key is down
     */
    public static boolean isShiftKeyDown()
    {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
    }
}