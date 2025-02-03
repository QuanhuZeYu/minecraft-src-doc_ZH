package net.minecraft.client.gui.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public abstract class GuiContainer extends GuiScreen
{
    protected static final ResourceLocation inventoryBackgroundTexture = new ResourceLocation("textures/gui/container/inventory.png");
    /** 物品栏窗口的X轴大小（以像素为单位） */
    protected int xSize = 176;
    /** 物品栏窗口的Y轴大小（以像素为单位） */
    protected int ySize = 166;
    /** 玩家物品栏槽位的列表 */
    public Container inventorySlots;
    /** GUI的起始X位置。用于GUI背景的不一致使用。 */
    protected int guiLeft;
    /** GUI的起始Y位置。用于GUI背景的不一致使用。 */
    protected int guiTop;
    private Slot theSlot;
    /** 当启用触摸屏时使用。 */
    private Slot clickedSlot;
    /** 当启用触摸屏时使用。 */
    private boolean isRightMouseClick;
    /** 当启用触摸屏时使用 */
    private ItemStack draggedStack;
    private int draggedStackOffsetX;
    private int draggedStackOffsetY;
    private Slot returningStackDestSlot;
    private long returningStackTime;
    /** 当启用触摸屏时使用 */
    private ItemStack returningStack;
    private Slot lastDragSlot;
    private long lastDragTime;
    protected final Set<net.minecraft.inventory.Slot> dragSlots = new HashSet();
    protected boolean isDragging;
    private int dragMode;
    private int dragButton;
    private boolean isQuickCrafting;
    private int quickCraftingAmount;
    private long lastClickTime;
    private Slot lastClickSlot;
    private int lastClickButton;
    private boolean isDoubleClick;
    private ItemStack lastClickStack;
    private static final String __OBFID = "CL_00000737";

    /**
     * 构造函数，初始化容器。
     * @param p_i1072_1_ 容器对象
     */
    public GuiContainer(Container p_i1072_1_)
    {
        this.inventorySlots = p_i1072_1_;
        this.isQuickCrafting = true;
    }

    /**
     * 初始化GUI，添加按钮和其他控件。
     */
    public void initGui()
    {
        super.initGui();
        this.mc.thePlayer.openContainer = this.inventorySlots;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    /**
     * 绘制屏幕及其所有组件。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分刻
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)k, (float)l, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.theSlot = null;
        short short1 = 240;
        short short2 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)short1 / 1.0F, (float)short2 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k1;

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
            this.drawSlot(slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.func_111238_b())
            {
                this.theSlot = slot;
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                int j1 = slot.xDisplayPosition;
                k1 = slot.yDisplayPosition;
                GL11.glColorMask(true, true, true, false);
                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GL11.glColorMask(true, true, true, true);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        //Forge: 强制禁用光照，因为存在一些问题，光照可能会根据物品栏中的物品错误地应用。
        GL11.glDisable(GL11.GL_LIGHTING);
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GL11.glEnable(GL11.GL_LIGHTING);
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = this.draggedStack == null ? inventoryplayer.getItemStack() : this.draggedStack;

        if (itemstack != null)
        {
            byte b0 = 8;
            k1 = this.draggedStack == null ? 8 : 16;
            String s = null;

            if (this.draggedStack != null && this.isRightMouseClick)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_float_int((float)itemstack.stackSize / 2.0F);
            }
            else if (this.isDragging && this.dragSlots.size() > 1)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = this.quickCraftingAmount;

                if (itemstack.stackSize == 0)
                {
                    s = "" + EnumChatFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - k - b0, mouseY - l - k1, s);
        }

        if (this.returningStack != null)
        {
            float f1 = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;

            if (f1 >= 1.0F)
            {
                f1 = 1.0F;
                this.returningStack = null;
            }

            k1 = this.returningStackDestSlot.xDisplayPosition - this.draggedStackOffsetX;
            int j2 = this.returningStackDestSlot.yDisplayPosition - this.draggedStackOffsetY;
            int l1 = this.draggedStackOffsetX + (int)((float)k1 * f1);
            int i2 = this.draggedStackOffsetY + (int)((float)j2 * f1);
            this.drawItemStack(this.returningStack, l1, i2, (String)null);
        }

        GL11.glPopMatrix();

        if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack())
        {
            ItemStack itemstack1 = this.theSlot.getStack();
            this.renderToolTip(itemstack1, mouseX, mouseY);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    }

    /**
     * 绘制物品堆栈。
     * @param stack 物品堆栈
     * @param x X坐标
     * @param y Y坐标
     * @param altText 替代文本
     */
    private void drawItemStack(ItemStack stack, int x, int y, String altText)
    {
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (stack != null) font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = fontRendererObj;
        itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), stack, x, y - (this.draggedStack == null ? 0 : 8), altText);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    /**
     * 绘制GuiContainer的前景层（物品前面的所有内容）。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

    /**
     * 绘制GuiContainer的背景层。
     * @param partialTicks 部分刻
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    protected abstract void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);

    /**
     * 绘制槽位。
     * @param slotIn 槽位
     */
    private void drawSlot(Slot slotIn)
    {
        int i = slotIn.xDisplayPosition;
        int j = slotIn.yDisplayPosition;
        ItemStack itemstack = slotIn.getStack();
        boolean flag = false;
        boolean flag1 = slotIn == this.clickedSlot && this.draggedStack != null && !this.isRightMouseClick;
        ItemStack itemstack1 = this.mc.thePlayer.inventory.getItemStack();
        String s = null;

        if (slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick && itemstack != null)
        {
            itemstack = itemstack.copy();
            itemstack.stackSize /= 2;
        }
        else if (this.isDragging && this.dragSlots.contains(slotIn) && itemstack1 != null)
        {
            if (this.dragSlots.size() == 1)
            {
                return;
            }

            if (Container.func_94527_a(slotIn, itemstack1, true) && this.inventorySlots.canDragIntoSlot(slotIn))
            {
                itemstack = itemstack1.copy();
                flag = true;
                Container.func_94525_a(this.dragSlots, this.dragMode, itemstack, slotIn.getStack() == null ? 0 : slotIn.getStack().stackSize);

                if (itemstack.stackSize > itemstack.getMaxStackSize())
                {
                    s = EnumChatFormatting.YELLOW + "" + itemstack.getMaxStackSize();
                    itemstack.stackSize = itemstack.getMaxStackSize();
                }

                if (itemstack.stackSize > slotIn.getSlotStackLimit())
                {
                    s = EnumChatFormatting.YELLOW + "" + slotIn.getSlotStackLimit();
                    itemstack.stackSize = slotIn.getSlotStackLimit();
                }
            }
            else
            {
                this.dragSlots.remove(slotIn);
                this.updateDragItem();
            }
        }

        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        if (itemstack == null)
        {
            IIcon iicon = slotIn.getBackgroundIconIndex();

            if (iicon != null)
            {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND); // Forge: 需要启用混合
                this.mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                this.drawTexturedModelRectFromIcon(i, j, iicon, 16, 16);
                GL11.glDisable(GL11.GL_BLEND); // Forge: 清理
                GL11.glEnable(GL11.GL_LIGHTING);
                flag1 = true;
            }
        }

        if (!flag1)
        {
            if (flag)
            {
                drawRect(i, j, i + 16, j + 16, -2130706433);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j, s);
        }

        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    /**
     * 更新拖拽物品的数量。
     */
    private void updateDragItem()
    {
        ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

        if (itemstack != null && this.isDragging)
        {
            this.quickCraftingAmount = itemstack.stackSize;
            ItemStack itemstack1;
            int i;

            for (Iterator iterator = this.dragSlots.iterator(); iterator.hasNext(); this.quickCraftingAmount -= itemstack1.stackSize - i)
            {
                Slot slot = (Slot)iterator.next();
                itemstack1 = itemstack.copy();
                i = slot.getStack() == null ? 0 : slot.getStack().stackSize;
                Container.func_94525_a(this.dragSlots, this.dragMode, itemstack1, i);

                if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }

                if (itemstack1.stackSize > slot.getSlotStackLimit())
                {
                    itemstack1.stackSize = slot.getSlotStackLimit();
                }
            }
        }
    }

    /**
     * 返回给定坐标处的槽位，如果没有则返回null。
     * @param x X坐标
     * @param y Y坐标
     * @return 槽位
     */
    private Slot getSlotAtPosition(int x, int y)
    {
        for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k)
        {
            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(k);

            if (this.isMouseOverSlot(slot, x, y))
            {
                return slot;
            }
        }

        return null;
    }

    /**
     * 当鼠标点击时调用。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param mouseButton 鼠标按钮
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean flag = mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;
        Slot slot = this.getSlotAtPosition(mouseX, mouseY);
        long l = Minecraft.getSystemTime();
        this.isDoubleClick = this.lastClickSlot == slot && l - this.lastClickTime < 250L && this.lastClickButton == mouseButton;
        this.isQuickCrafting = false;

        if (mouseButton == 0 || mouseButton == 1 || flag)
        {
            int i1 = this.guiLeft;
            int j1 = this.guiTop;
            boolean flag1 = mouseX < i1 || mouseY < j1 || mouseX >= i1 + this.xSize || mouseY >= j1 + this.ySize;
            int k1 = -1;

            if (slot != null)
            {
                k1 = slot.slotNumber;
            }

            if (flag1)
            {
                k1 = -999;
            }

            if (this.mc.gameSettings.touchscreen && flag1 && this.mc.thePlayer.inventory.getItemStack() == null)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
                return;
            }

            if (k1 != -1)
            {
                if (this.mc.gameSettings.touchscreen)
                {
                    if (slot != null && slot.getHasStack())
                    {
                        this.clickedSlot = slot;
                        this.draggedStack = null;
                        this.isRightMouseClick = mouseButton == 1;
                    }
                    else
                    {
                        this.clickedSlot = null;
                    }
                }
                else if (!this.isDragging)
                {
                    if (this.mc.thePlayer.inventory.getItemStack() == null)
                    {
                        if (mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100)
                        {
                            this.handleMouseClick(slot, k1, mouseButton, 3);
                        }
                        else
                        {
                            boolean flag2 = k1 != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                            byte b0 = 0;

                            if (flag2)
                            {
                                this.lastClickStack = slot != null && slot.getHasStack() ? slot.getStack() : null;
                                b0 = 1;
                            }
                            else if (k1 == -999)
                            {
                                b0 = 4;
                            }

                            this.handleMouseClick(slot, k1, mouseButton, b0);
                        }

                        this.isQuickCrafting = true;
                    }
                    else
                    {
                        this.isDragging = true;
                        this.dragButton = mouseButton;
                        this.dragSlots.clear();

                        if (mouseButton == 0)
                        {
                            this.dragMode = 0;
                        }
                        else if (mouseButton == 1)
                        {
                            this.dragMode = 1;
                        }
                    }
                }
            }
        }

        this.lastClickSlot = slot;
        this.lastClickTime = l;
        this.lastClickButton = mouseButton;
    }

    /**
     * 当鼠标按钮按下并移动时调用。参数为：mouseX, mouseY, lastButtonClicked & timeSinceMouseClick。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param clickedMouseButton 点击的鼠标按钮
     * @param timeSinceLastClick 自上次点击以来的时间
     */
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        Slot slot = this.getSlotAtPosition(mouseX, mouseY);
        ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

        if (this.clickedSlot != null && this.mc.gameSettings.touchscreen)
        {
            if (clickedMouseButton == 0 || clickedMouseButton == 1)
            {
                if (this.draggedStack == null)
                {
                    if (slot != this.clickedSlot)
                    {
                        this.draggedStack = this.clickedSlot.getStack().copy();
                    }
                }
                else if (this.draggedStack.stackSize > 1 && slot != null && Container.func_94527_a(slot, this.draggedStack, false))
                {
                    long i1 = Minecraft.getSystemTime();

                    if (this.lastDragSlot == slot)
                    {
                        if (i1 - this.lastDragTime > 500L)
                        {
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
                            this.handleMouseClick(slot, slot.slotNumber, 1, 0);
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
                            this.lastDragTime = i1 + 750L;
                            --this.draggedStack.stackSize;
                        }
                    }
                    else
                    {
                        this.lastDragSlot = slot;
                        this.lastDragTime = i1;
                    }
                }
            }
        }
        else if (this.isDragging && slot != null && itemstack != null && itemstack.stackSize > this.dragSlots.size() && Container.func_94527_a(slot, itemstack, true) && slot.isItemValid(itemstack) && this.inventorySlots.canDragIntoSlot(slot))
        {
            this.dragSlots.add(slot);
            this.updateDragItem();
        }
    }

    /**
     * 当鼠标移动或鼠标按钮释放时调用。签名：(mouseX, mouseY, which) which==-1 是鼠标移动，which==0 或 which==1 是鼠标释放。
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param state 状态
     */
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state)
    {
        super.mouseMovedOrUp(mouseX, mouseY, state); //Forge, 调用父类以释放按钮
        Slot slot = this.getSlotAtPosition(mouseX, mouseY);
        int l = this.guiLeft;
        int i1 = this.guiTop;
        boolean flag = mouseX < l || mouseY < i1 || mouseX >= l + this.xSize || mouseY >= i1 + this.ySize;
        int j1 = -1;

        if (slot != null)
        {
            j1 = slot.slotNumber;
        }

        if (flag)
        {
            j1 = -999;
        }

        Slot slot1;
        Iterator iterator;

        if (this.isDoubleClick && slot != null && state == 0 && this.inventorySlots.func_94530_a((ItemStack)null, slot))
        {
            if (isShiftKeyDown())
            {
                if (slot != null && slot.inventory != null && this.lastClickStack != null)
                {
                    iterator = this.inventorySlots.inventorySlots.iterator();

                    while (iterator.hasNext())
                    {
                        slot1 = (Slot)iterator.next();

                        if (slot1 != null && slot1.canTakeStack(this.mc.thePlayer) && slot1.getHasStack() && slot1.inventory == slot.inventory && Container.func_94527_a(slot1, this.lastClickStack, true))
                        {
                            this.handleMouseClick(slot1, slot1.slotNumber, state, 1);
                        }
                    }
                }
            }
            else
            {
                this.handleMouseClick(slot, j1, state, 6);
            }

            this.isDoubleClick = false;
            this.lastClickTime = 0L;
        }
        else
        {
            if (this.isDragging && this.dragButton != state)
            {
                this.isDragging = false;
                this.dragSlots.clear();
                this.isQuickCrafting = true;
                return;
            }

            if (this.isQuickCrafting)
            {
                this.isQuickCrafting = false;
                return;
            }

            boolean flag1;

            if (this.clickedSlot != null && this.mc.gameSettings.touchscreen)
            {
                if (state == 0 || state == 1)
                {
                    if (this.draggedStack == null && slot != this.clickedSlot)
                    {
                        this.draggedStack = this.clickedSlot.getStack();
                    }

                    flag1 = Container.func_94527_a(slot, this.draggedStack, false);

                    if (j1 != -1 && this.draggedStack != null && flag1)
                    {
                        this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, 0);
                        this.handleMouseClick(slot, j1, 0, 0);

                        if (this.mc.thePlayer.inventory.getItemStack() != null)
                        {
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, 0);
                            this.draggedStackOffsetX = mouseX - l;
                            this.draggedStackOffsetY = mouseY - i1;
                            this.returningStackDestSlot = this.clickedSlot;
                            this.returningStack = this.draggedStack;
                            this.returningStackTime = Minecraft.getSystemTime();
                        }
                        else
                        {
                            this.returningStack = null;
                        }
                    }
                    else if (this.draggedStack != null)
                    {
                        this.draggedStackOffsetX = mouseX - l;
                        this.draggedStackOffsetY = mouseY - i1;
                        this.returningStackDestSlot = this.clickedSlot;
                        this.returningStack = this.draggedStack;
                        this.returningStackTime = Minecraft.getSystemTime();
                    }

                    this.draggedStack = null;
                    this.clickedSlot = null;
                }
            }
            else if (this.isDragging && !this.dragSlots.isEmpty())
            {
                this.handleMouseClick((Slot)null, -999, Container.func_94534_d(0, this.dragMode), 5);
                iterator = this.dragSlots.iterator();

                while (iterator.hasNext())
                {
                    slot1 = (Slot)iterator.next();
                    this.handleMouseClick(slot1, slot1.slotNumber, Container.func_94534_d(1, this.dragMode), 5);
                }

                this.handleMouseClick((Slot)null, -999, Container.func_94534_d(2, this.dragMode), 5);
            }
            else if (this.mc.thePlayer.inventory.getItemStack() != null)
            {
                if (state == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100)
                {
                    this.handleMouseClick(slot, j1, state, 3);
                }
                else
                {
                    flag1 = j1 != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));

                    if (flag1)
                    {
                        this.lastClickStack = slot != null && slot.getHasStack() ? slot.getStack() : null;
                    }

                    this.handleMouseClick(slot, j1, state, flag1 ? 1 : 0);
                }
            }
        }

        if (this.mc.thePlayer.inventory.getItemStack() == null)
        {
            this.lastClickTime = 0L;
        }

        this.isDragging = false;
    }

    /**
     * 返回鼠标位置是否在指定槽位上。
     * @param slotIn 槽位
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 是否在槽位上
     */
    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY)
    {
        return this.isInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    /**
     * 检查点是否在矩形区域内。
     * @param left 左边界
     * @param top 上边界
     * @param right 右边界
     * @param bottom 下边界
     * @param pointX 点的X坐标
     * @param pointY 点的Y坐标
     * @return 是否在区域内
     */
    protected boolean isInRegion(int left, int top, int right, int bottom, int pointX, int pointY)
    {
        int k1 = this.guiLeft;
        int l1 = this.guiTop;
        pointX -= k1;
        pointY -= l1;
        return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
    }

    /**
     * 处理鼠标点击槽位。
     * @param slotIn 槽位
     * @param slotId 槽位ID
     * @param clickedButton 点击的按钮
     * @param clickType 点击类型
     */
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType)
    {
        if (slotIn != null)
        {
            slotId = slotIn.slotNumber;
        }

        this.mc.playerController.windowClick(this.inventorySlots.windowId, slotId, clickedButton, clickType, this.mc.thePlayer);
    }

    /**
     * 当按键被键入时调用。相当于KeyListener.keyTyped(KeyEvent e)。
     * @param typedChar 键入的字符
     * @param keyCode 键码
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.thePlayer.closeScreen();
        }

        this.checkHotbarKeys(keyCode);

        if (this.theSlot != null && this.theSlot.getHasStack())
        {
            if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, 3);
            }
            else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode())
            {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }
    }

    /**
     * 检查热键是否被按下。
     * @param keyCode 键码
     * @return 是否按下热键
     */
    protected boolean checkHotbarKeys(int keyCode)
    {
        if (this.mc.thePlayer.inventory.getItemStack() == null && this.theSlot != null)
        {
            for (int j = 0; j < 9; ++j)
            {
                if (keyCode == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode())
                {
                    this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, j, 2);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 当GUI关闭时调用。用于禁用键盘重复事件。
     */
    public void onGuiClosed()
    {
        if (this.mc.thePlayer != null)
        {
            this.inventorySlots.onContainerClosed(this.mc.thePlayer);
        }
    }

    /**
     * 返回此GUI在单机模式下显示时是否暂停游戏。
     * @return 是否暂停游戏
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * 从主游戏循环调用以更新屏幕。
     */
    public void updateScreen()
    {
        super.updateScreen();

        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead)
        {
            this.mc.thePlayer.closeScreen();
        }
    }
}