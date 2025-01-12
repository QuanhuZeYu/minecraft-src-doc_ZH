package net.minecraft.item.crafting;

import java.util.ArrayList;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class RecipeFireworks implements IRecipe
{
    private ItemStack craftedFireworkItem;
    private static final String __OBFID = "CL_00000083";


    public boolean matches(InventoryCrafting craftingInventory, World world)
    {
        // 初始化结果物品为 null
        this.craftedFireworkItem = null;
        // 计数器，用于统计每种物品的数量
        int gunpowderCount = 0;
        int fireworkChargeCount = 0;
        int dyeCount = 0;
        int paperCount = 0;
        int effectsCount = 0;
        int additionalEffectCount = 0;

        // 遍历 craftingInventory 中的所有槽位
        for (int slot = 0; slot < craftingInventory.getSizeInventory(); ++slot)
        {
            ItemStack itemStack = craftingInventory.getStackInSlot(slot);

            // 如果槽位非空，则检查物品类型并增加相应的计数器
            if (itemStack != null)
            {
                if (itemStack.getItem() == Items.gunpowder)
                {
                    ++gunpowderCount;
                }
                else if (itemStack.getItem() == Items.firework_charge)
                {
                    ++fireworkChargeCount;
                }
                else if (itemStack.getItem() == Items.dye)
                {
                    ++dyeCount;
                }
                else if (itemStack.getItem() == Items.paper)
                {
                    ++paperCount;
                }
                else if (itemStack.getItem() == Items.glowstone_dust)
                {
                    ++effectsCount;
                }
                else if (itemStack.getItem() == Items.diamond)
                {
                    ++effectsCount;
                }
                else if (itemStack.getItem() == Items.fire_charge)
                {
                    ++additionalEffectCount;
                }
                else if (itemStack.getItem() == Items.feather)
                {
                    ++additionalEffectCount;
                }
                else if (itemStack.getItem() == Items.gold_nugget)
                {
                    ++additionalEffectCount;
                }
                else
                {
                    // 如果物品不是 skull，则返回 false，因为这些物品不符合任何配方
                    if (itemStack.getItem() != Items.skull)
                    {
                        return false;
                    }
                    ++additionalEffectCount;
                }
            }
        }

        // 将 dyeCount 和 additionalEffectCount 合并到 effectsCount 中
        effectsCount += dyeCount + additionalEffectCount;

        // 检查烟花配方条件
        if (gunpowderCount <= 3 && paperCount <= 1)
        {
            // 检查是否匹配单个烟花的配方
            if (gunpowderCount >= 1 && paperCount == 1 && effectsCount == 0)
            {
                // 创建一个新的烟花物品
                this.craftedFireworkItem = new ItemStack(Items.fireworks);
                NBTTagCompound fireworksTag = new NBTTagCompound();

                // 如果存在爆炸药水，则处理爆炸效果
                if (fireworkChargeCount > 0)
                {
                    NBTTagCompound explosionTag = new NBTTagCompound();
                    NBTTagList explosionsList = new NBTTagList();

                    // 遍历 craftingInventory 中的所有槽位
                    for (int slot = 0; slot < craftingInventory.getSizeInventory(); ++slot)
                    {
                        ItemStack itemStack = craftingInventory.getStackInSlot(slot);

                        // 检查是否为带有爆炸效果的烟花药水
                        if (itemStack != null && itemStack.getItem() == Items.firework_charge && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Explosion", 10))
                        {
                            explosionsList.appendTag(itemStack.getTagCompound().getCompoundTag("Explosion"));
                        }
                    }

                    // 将爆炸效果列表和飞行时间设置到标签中
                    explosionTag.setTag("Explosions", explosionsList);
                    explosionTag.setByte("Flight", (byte)gunpowderCount);
                    fireworksTag.setTag("Fireworks", explosionTag);
                }
                // 设置标签以防止 NPE
                this.craftedFireworkItem.setTagCompound(fireworksTag);
                return true;
            }
            // 检查是否匹配烟花药水的配方
            else if (gunpowderCount == 1 && paperCount == 0 && fireworkChargeCount == 0 && dyeCount > 0 && additionalEffectCount <= 1)
            {
                // 创建一个新的烟花药水物品
                this.craftedFireworkItem = new ItemStack(Items.firework_charge);
                NBTTagCompound fireworksChargeTag = new NBTTagCompound();
                NBTTagCompound explosionTag = new NBTTagCompound();
                byte type = 0;
                ArrayList<Integer> colorsList = new ArrayList<>();

                // 遍历 craftingInventory 中的所有槽位
                for (int slot = 0; slot < craftingInventory.getSizeInventory(); ++slot)
                {
                    ItemStack itemStack = craftingInventory.getStackInSlot(slot);

                    // 检查物品类型并设置相应的爆炸效果
                    if (itemStack != null)
                    {
                        if (itemStack.getItem() == Items.dye)
                        {
                            colorsList.add(ItemDye.field_150922_c[itemStack.getItemDamage()]);
                        }
                        else if (itemStack.getItem() == Items.glowstone_dust)
                        {
                            explosionTag.setBoolean("Flicker", true);
                        }
                        else if (itemStack.getItem() == Items.diamond)
                        {
                            explosionTag.setBoolean("Trail", true);
                        }
                        else if (itemStack.getItem() == Items.fire_charge)
                        {
                            type = 1;
                        }
                        else if (itemStack.getItem() == Items.feather)
                        {
                            type = 4;
                        }
                        else if (itemStack.getItem() == Items.gold_nugget)
                        {
                            type = 2;
                        }
                        else if (itemStack.getItem() == Items.skull)
                        {
                            type = 3;
                        }
                    }
                }

                // 将颜色列表转换为数组
                int[] colors = new int[colorsList.size()];
                for (int index = 0; index < colors.length; ++index)
                {
                    colors[index] = colorsList.get(index);
                }

                // 设置爆炸效果的颜色和类型
                explosionTag.setIntArray("Colors", colors);
                explosionTag.setByte("Type", type);
                fireworksChargeTag.setTag("Explosion", explosionTag);
                this.craftedFireworkItem.setTagCompound(fireworksChargeTag);
                return true;
            }
            // 检查是否匹配添加渐变颜色到烟花药水的配方
            else if (gunpowderCount == 0 && paperCount == 0 && fireworkChargeCount == 1 && dyeCount > 0 && dyeCount == effectsCount)
            {
                ArrayList<Integer> fadeColorsList = new ArrayList<>();

                // 遍历 craftingInventory 中的所有槽位
                for (int slot = 0; slot < craftingInventory.getSizeInventory(); ++slot)
                {
                    ItemStack itemStack = craftingInventory.getStackInSlot(slot);

                    // 检查物品类型并设置相应的渐变颜色
                    if (itemStack != null)
                    {
                        if (itemStack.getItem() == Items.dye)
                        {
                            fadeColorsList.add(ItemDye.field_150922_c[itemStack.getItemDamage()]);
                        }
                        else if (itemStack.getItem() == Items.firework_charge)
                        {
                            this.craftedFireworkItem = itemStack.copy();
                            this.craftedFireworkItem.stackSize = 1;
                        }
                    }
                }

                // 将渐变颜色列表转换为数组
                int[] fadeColors = new int[fadeColorsList.size()];
                for (int index = 0; index < fadeColors.length; ++index)
                {
                    fadeColors[index] = fadeColorsList.get(index);
                }

                // 检查是否已设置标签
                if (this.craftedFireworkItem != null && this.craftedFireworkItem.hasTagCompound())
                {
                    NBTTagCompound explosionTag = this.craftedFireworkItem.getTagCompound().getCompoundTag("Explosion");
                    // 如果存在爆炸效果标签，则设置渐变颜色
                    if (explosionTag != null)
                    {
                        explosionTag.setIntArray("FadeColors", fadeColors);
                        return true;
                    }
                }
            }
        }
        // 如果不匹配任何配方，则返回 false
        return false;
    }



    /**
     * 返回一个项目，该项目是该配方的结果
     */
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
    {
        return this.craftedFireworkItem.copy();
    }

    /**
     * Returns the size of the recipe area
     */
    public int getRecipeSize()
    {
        return 10;
    }

    public ItemStack getRecipeOutput()
    {
        return this.craftedFireworkItem;
    }
}