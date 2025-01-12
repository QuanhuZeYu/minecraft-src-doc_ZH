package net.minecraft.inventory;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class SlotCrafting extends Slot
{
    /** 链接到此结果槽的工艺矩阵库存。 */
    private final IInventory craftMatrix;
    /** The player that is using the GUI where this slot resides. */
    private EntityPlayer thePlayer;
    /** The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset. */
    private int amountCrafted;
    private static final String __OBFID = "CL_00001761";

    public SlotCrafting(EntityPlayer player, IInventory craftMatrixInventory, IInventory craftResultInventory, int slotIndex, int xPosition, int yPosition)
    {
        super(craftResultInventory, slotIndex, xPosition, yPosition);
        // 当前玩家
        this.thePlayer = player;
        // 工作台中的物品矩阵
        this.craftMatrix = craftMatrixInventory;
    }


    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    public ItemStack decrStackSize(int p_75209_1_)
    {
        if (this.getHasStack())
        {
            this.amountCrafted += Math.min(p_75209_1_, this.getStack().stackSize);
        }

        return super.decrStackSize(p_75209_1_);
    }

    /**
     * 传入的 itemStack 是输出 - 即铁锭和镐，而不是矿石和木材。通常会增加一个
     * 内部计数然后调用 onCrafting(item)。
     */
    protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_)
    {
        this.amountCrafted += p_75210_2_;
        this.onCrafting(p_75210_1_);
    }

    /**
     * 传入的 itemStack 是输出 - 即铁锭和镐，而不是矿石和木材。
     */
    protected void onCrafting(ItemStack craftedItem)
    {
        // 调用物品的onCrafting方法
        craftedItem.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted);
        this.amountCrafted = 0;

        // 检查并添加制作工作台的成就
        if (craftedItem.getItem() == Item.getItemFromBlock(Blocks.crafting_table))
        {
            this.thePlayer.addStat(AchievementList.buildWorkBench, 1);
        }

        // 检查并添加制作镐的成就
        if (craftedItem.getItem() instanceof ItemPickaxe)
        {
            this.thePlayer.addStat(AchievementList.buildPickaxe, 1);
        }

        // 检查并添加制作熔炉的成就
        if (craftedItem.getItem() == Item.getItemFromBlock(Blocks.furnace))
        {
            this.thePlayer.addStat(AchievementList.buildFurnace, 1);
        }

        // 检查并添加制作镰刀的成就
        if (craftedItem.getItem() instanceof ItemHoe)
        {
            this.thePlayer.addStat(AchievementList.buildHoe, 1);
        }

        // 检查并添加制作面包的成就
        if (craftedItem.getItem() == Items.bread)
        {
            this.thePlayer.addStat(AchievementList.makeBread, 1);
        }

        // 检查并添加制作蛋糕的成就
        if (craftedItem.getItem() == Items.cake)
        {
            this.thePlayer.addStat(AchievementList.bakeCake, 1);
        }

        // 检查并添加制作更好的镐的成就（非木制镐）
        if (craftedItem.getItem() instanceof ItemPickaxe && ((ItemPickaxe)craftedItem.getItem()).func_150913_i() != Item.ToolMaterial.WOOD)
        {
            this.thePlayer.addStat(AchievementList.buildBetterPickaxe, 1);
        }

        // 检查并添加制作剑的成就
        if (craftedItem.getItem() instanceof ItemSword)
        {
            this.thePlayer.addStat(AchievementList.buildSword, 1);
        }

        // 检查并添加制作附魔台的成就
        if (craftedItem.getItem() == Item.getItemFromBlock(Blocks.enchanting_table))
        {
            this.thePlayer.addStat(AchievementList.enchantments, 1);
        }

        // 检查并添加制作书架的成就
        if (craftedItem.getItem() == Item.getItemFromBlock(Blocks.bookshelf))
        {
            this.thePlayer.addStat(AchievementList.bookcase, 1);
        }
    }


    public void onPickupFromSlot(EntityPlayer player, ItemStack itemStackPickedUp)
    {
        // 触发玩家 crafting 事件
        FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStackPickedUp, craftMatrix);
        // 调用 onCrafting 方法处理 crafting 逻辑
        this.onCrafting(itemStackPickedUp);

        // 遍历 crafting 矩阵中的所有槽位
        for (int slotIndex = 0; slotIndex < this.craftMatrix.getSizeInventory(); ++slotIndex)
        {
            ItemStack itemStackInSlot = this.craftMatrix.getStackInSlot(slotIndex);

            // 如果槽位中有物品，则减少该物品的数量
            if (itemStackInSlot != null)
            {
                this.craftMatrix.decrStackSize(slotIndex, 1);

                // 检查该物品是否有容器物品
                if (itemStackInSlot.getItem().hasContainerItem(itemStackInSlot))
                {
                    ItemStack containerItemStack = itemStackInSlot.getItem().getContainerItem(itemStackInSlot);

                    // 如果容器物品损坏，则触发销毁物品事件并继续下一轮循环
                    if (containerItemStack != null && containerItemStack.isItemStackDamageable() && containerItemStack.getItemDamage() > containerItemStack.getMaxDamage())
                    {
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, containerItemStack));
                        continue;
                    }

                    // 如果容器物品不应留在 crafting 网格中，或者无法添加到玩家的 inventory 中，则进行相应处理
                    if (!itemStackInSlot.getItem().doesContainerItemLeaveCraftingGrid(itemStackInSlot) || !player.inventory.addItemStackToInventory(containerItemStack))
                    {
                        // 如果槽位为空，则将容器物品放回该槽位
                        if (this.craftMatrix.getStackInSlot(slotIndex) == null)
                        {
                            this.craftMatrix.setInventorySlotContents(slotIndex, containerItemStack);
                        }
                        // 否则，将容器物品丢弃到玩家所在的世界中
                        else
                        {
                            player.dropPlayerItemWithRandomChoice(containerItemStack, false);
                        }
                    }
                }
            }
        }
    }

}