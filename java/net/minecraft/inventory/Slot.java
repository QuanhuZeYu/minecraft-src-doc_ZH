package net.minecraft.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class Slot
{
    /** 该插槽在物品栏中的索引。 */
    private final int slotIndex;
    /** 我们想要从中提取插槽的物品栏。 */
    public final IInventory inventory;
    /** 插槽的ID（也是物品栏数组列表中的索引）。 */
    public int slotNumber;
    /** 物品栏插槽在屏幕上的显示位置x轴坐标。 */
    public int xDisplayPosition;
    /** 物品栏插槽在屏幕上的显示位置y轴坐标。 */
    public int yDisplayPosition;

    private static final String __OBFID = "CL_00001762";

    /** Position within background texture file, normally -1 which causes no background to be drawn. */
    protected IIcon backgroundIcon = null;

    /** Background texture file assigned to this slot, if any. Vanilla "/gui/items.png" is used if this is null. */
    @SideOnly(Side.CLIENT)
    protected ResourceLocation texture;

    public Slot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_)
    {
        this.inventory = p_i1824_1_;
        this.slotIndex = p_i1824_2_;
        this.xDisplayPosition = p_i1824_3_;
        this.yDisplayPosition = p_i1824_4_;
    }

    /**
     * 如果 par2 的项目数多于 par1，则调用 onCrafting(item,countIncrease)
     */
    public void onSlotChange(ItemStack oldStack, ItemStack newStack) {
        // 检查旧堆栈和新堆栈是否都非空
        if (oldStack != null && newStack != null) {
            // 如果新旧堆栈中物品相同
            if (oldStack.getItem() == newStack.getItem()) {
                // 计算物品数量的增加
                int itemCountDifference = newStack.stackSize - oldStack.stackSize;

                // 如果数量增加了
                if (itemCountDifference > 0) {
                    // 调用onCrafting方法，传入旧堆栈和数量差
                    this.onCrafting(oldStack, itemCountDifference);
                }
            }
        }
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {}

    /**
     * 传入的 itemStack 是输出 - 即铁锭和镐，而不是矿石和木材。
     */
    protected void onCrafting(ItemStack p_75208_1_) {}

    public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack p_82870_2_)
    {
        this.onSlotChanged();
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack stack)
    {
        return true;
    }

    /**
     * Helper fnct to get the stack in the slot.
     */
    public ItemStack getStack()
    {
        return this.inventory.getStackInSlot(this.slotIndex);
    }

    /**
     * Returns if this slot contains a stack.
     */
    public boolean getHasStack()
    {
        return this.getStack() != null;
    }

    /**
     * Helper method to put a stack in the slot.
     */
    public void putStack(ItemStack p_75215_1_)
    {
        this.inventory.setInventorySlotContents(this.slotIndex, p_75215_1_);
        this.onSlotChanged();
    }

    /**
     * Called when the stack in a Slot changes
     */
    public void onSlotChanged()
    {
        this.inventory.markDirty();
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    public int getSlotStackLimit()
    {
        return this.inventory.getInventoryStackLimit();
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    public ItemStack decrStackSize(int p_75209_1_)
    {
        return this.inventory.decrStackSize(this.slotIndex, p_75209_1_);
    }

    /**
     * returns true if this slot is in par2 of par1
     */
    public boolean isSlotInInventory(IInventory p_75217_1_, int p_75217_2_)
    {
        return p_75217_1_ == this.inventory && p_75217_2_ == this.slotIndex;
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    public boolean canTakeStack(EntityPlayer p_82869_1_)
    {
        return true;
    }

    /**
     * Returns the icon index on items.png that is used as background image of the slot.
     */
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex()
    {
        return backgroundIcon;
    }

    @SideOnly(Side.CLIENT)
    public boolean func_111238_b()
    {
        return true;
    }

    /*========================================= FORGE START =====================================*/
    /**
     * Gets the path of the texture file to use for the background image of this slot when drawing the GUI.
     * @return String: The texture file that will be used in GuiContainer.drawSlotInventory for the slot background.
     */
    @SideOnly(Side.CLIENT)
    public ResourceLocation getBackgroundIconTexture()
    {
        return (texture == null ? TextureMap.locationItemsTexture : texture);
    }

    /**
     * Sets which icon index to use as the background image of the slot when it's empty.
     * @param icon The icon to use, null for none
     */
    public void setBackgroundIcon(IIcon icon)
    {
        backgroundIcon = icon;
    }

    /**
     * Sets the texture file to use for the background image of the slot when it's empty.
     * @param textureFilename String: Path of texture file to use, or null to use "/gui/items.png"
     */
    @SideOnly(Side.CLIENT)
    public void setBackgroundIconTexture(ResourceLocation texture)
    {
        this.texture = texture;
    }

    /**
     * Retrieves the index in the inventory for this slot, this value should typically not
     * be used, but can be useful for some occasions.
     *
     * @return Index in associated inventory for this slot.
     */
    public int getSlotIndex()
    {
        return slotIndex;
    }
    /*========================================= FORGE END =====================================*/
}