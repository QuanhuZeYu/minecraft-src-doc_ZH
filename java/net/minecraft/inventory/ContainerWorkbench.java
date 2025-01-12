package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

public class ContainerWorkbench extends Container
{
    /** 制作矩阵库存 (3x3)。 */
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
    public IInventory craftResult = new InventoryCraftResult();
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;
    private static final String __OBFID = "CL_00001744";

    public ContainerWorkbench(InventoryPlayer p_i1808_1_, World p_i1808_2_, int p_i1808_3_, int p_i1808_4_, int p_i1808_5_)
    {
        this.worldObj = p_i1808_2_;
        this.posX = p_i1808_3_;
        this.posY = p_i1808_4_;
        this.posZ = p_i1808_5_;
        this.addSlotToContainer(new SlotCrafting(p_i1808_1_.player, this.craftMatrix, this.craftResult, 0, 124, 35));
        int l;
        int i1;

        for (l = 0; l < 3; ++l)
        {
            for (i1 = 0; i1 < 3; ++i1)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, i1 + l * 3, 30 + i1 * 18, 17 + l * 18));
            }
        }

        for (l = 0; l < 3; ++l)
        {
            for (i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(p_i1808_1_, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }

        for (l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(p_i1808_1_, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory p_75130_1_)
    {
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer p_75134_1_)
    {
        super.onContainerClosed(p_75134_1_);

        if (!this.worldObj.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);

                if (itemstack != null)
                {
                    p_75134_1_.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    public boolean canInteractWith(EntityPlayer player)
    {
        return this.worldObj.getBlock(this.posX, this.posY, this.posZ) != Blocks.crafting_table ? false : player.getDistanceSq((double)this.posX + 0.5D, (double)this.posY + 0.5D, (double)this.posZ + 0.5D) <= 64.0D;
    }

    /**
     * 当玩家按住 Shift 键并单击插槽时调用。你必须覆盖这个，否则当有人这样做时你会崩溃。
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        // 获取要转移的物品堆
        ItemStack stackToTransfer = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        // 如果插槽存在且包含物品堆，则开始处理转移逻辑
        if (slot != null && slot.getHasStack())
        {
            ItemStack originalStack = slot.getStack();
            stackToTransfer = originalStack.copy();

            // 根据插槽索引处理物品堆的转移
            if (slotIndex == 0) // 主输出插槽
            {
                // 将物品堆从主输出插槽转移到玩家的主工具栏或背包中
                if (!this.mergeItemStack(originalStack, 10, 46, true))
                {
                    return null;
                }

                slot.onSlotChange(originalStack, stackToTransfer);
            }
            else if (slotIndex >= 10 && slotIndex < 37) // 玩家的主工具栏和背包
            {
                // 将物品堆从玩家的主工具栏或背包转移到主输出插槽中
                if (!this.mergeItemStack(originalStack, 37, 46, false))
                {
                    return null;
                }
            }
            else if (slotIndex >= 37 && slotIndex < 46) // 玩家的主工具栏
            {
                // 将物品堆从玩家的主工具栏转移到玩家的背包或主输出插槽中
                if (!this.mergeItemStack(originalStack, 10, 37, false))
                {
                    return null;
                }
            }
            else // 其他插槽
            {
                // 尝试将物品堆转移到玩家的主工具栏或背包中
                if (!this.mergeItemStack(originalStack, 10, 46, false))
                {
                    return null;
                }
            }

            // 如果插槽中的物品堆数量为0，则清空插槽
            if (originalStack.stackSize == 0)
            {
                slot.putStack((ItemStack) null);
            }
            else
            {
                slot.onSlotChanged();
            }

            // 如果物品堆数量没有变化，则返回null
            if (originalStack.stackSize == stackToTransfer.stackSize)
            {
                return null;
            }

            // 更新玩家拾取物品的逻辑
            slot.onPickupFromSlot(player, originalStack);
        }

        return stackToTransfer;
    }


    public boolean func_94530_a(ItemStack p_94530_1_, Slot p_94530_2_)
    {
        return p_94530_2_.inventory != this.craftResult && super.func_94530_a(p_94530_1_, p_94530_2_);
    }
}