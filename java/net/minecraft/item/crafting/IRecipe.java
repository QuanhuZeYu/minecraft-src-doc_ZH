package net.minecraft.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRecipe
{
    /**
     * 用于检查配方是否与当前的制作库存匹配
     */
    boolean matches(InventoryCrafting p_77569_1_, World p_77569_2_);

    /**
     * 返回一个项目，该项目是该配方的结果
     */
    ItemStack getCraftingResult(InventoryCrafting p_77572_1_);

    /**
     * 返回配方区域的大小
     */
    int getRecipeSize();

    ItemStack getRecipeOutput();
}