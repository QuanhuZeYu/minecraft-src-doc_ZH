package net.minecraft.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class RecipesMapExtending extends ShapedRecipes
{
    private static final String __OBFID = "CL_00000088";

    public RecipesMapExtending()
    {
        super(3, 3, new ItemStack[] {new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.filled_map, 0, 32767), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper), new ItemStack(Items.paper)}, new ItemStack(Items.map, 0, 0));
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(InventoryCrafting p_77569_1_, World p_77569_2_)
    {
        if (!super.matches(p_77569_1_, p_77569_2_))
        {
            return false;
        }
        else
        {
            ItemStack itemstack = null;

            for (int i = 0; i < p_77569_1_.getSizeInventory() && itemstack == null; ++i)
            {
                ItemStack itemstack1 = p_77569_1_.getStackInSlot(i);

                if (itemstack1 != null && itemstack1.getItem() == Items.filled_map)
                {
                    itemstack = itemstack1;
                }
            }

            if (itemstack == null)
            {
                return false;
            }
            else
            {
                MapData mapdata = Items.filled_map.getMapData(itemstack, p_77569_2_);
                return mapdata == null ? false : mapdata.scale < 4;
            }
        }
    }

    /**
     * 返回一个项目，该项目是该配方的结果。
     * 具体来说，这个方法从给定的 crafting inventory 中找到第一个 filled map，然后对其进行复制，并设置其大小为1。
     * 最后，它会在 map 的 NBTTagCompound 中添加一个键值对，表示该地图正在进行缩放。
     */
    public ItemStack getCraftingResult(InventoryCrafting craftingInventory)
    {
        // 初始化 itemStack 为 null，用于存储找到的 filled map
        ItemStack resultingMap = null;

        // 遍历 crafting inventory 中的每一个槽位
        for (int slotIndex = 0; slotIndex < craftingInventory.getSizeInventory() && resultingMap == null; ++slotIndex)
        {
            // 获取当前槽位中的物品
            ItemStack currentStack = craftingInventory.getStackInSlot(slotIndex);

            // 检查当前槽位中的物品是否为 filled map
            if (currentStack != null && currentStack.getItem() == Items.filled_map)
            {
                // 如果是 filled map，则将其赋值给 resultingMap
                resultingMap = currentStack;
            }
        }

        // 复制找到的 filled map
        resultingMap = resultingMap.copy();

        // 设置复制的地图的数量为1
        resultingMap.stackSize = 1;

        // 如果地图没有 NBTTagCompound，则创建一个新的
        if (resultingMap.getTagCompound() == null)
        {
            resultingMap.setTagCompound(new NBTTagCompound());
        }

        // 在 NBTTagCompound 中设置一个键值对，表示该地图正在进行缩放
        resultingMap.getTagCompound().setBoolean("map_is_scaling", true);

        // 返回处理后的地图
        return resultingMap;
    }

}