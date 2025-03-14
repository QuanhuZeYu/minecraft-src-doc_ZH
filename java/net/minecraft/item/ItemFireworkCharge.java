package net.minecraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class ItemFireworkCharge extends Item
{
    @SideOnly(Side.CLIENT)
    private IIcon field_150904_a;
    private static final String __OBFID = "CL_00000030";

    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_)
    {
        return p_77618_2_ > 0 ? this.field_150904_a : super.getIconFromDamageForRenderPass(p_77618_1_, p_77618_2_);
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack p_82790_1_, int p_82790_2_)
    {
        if (p_82790_2_ != 1)
        {
            return super.getColorFromItemStack(p_82790_1_, p_82790_2_);
        }
        else
        {
            NBTBase nbtbase = func_150903_a(p_82790_1_, "Colors");

            if (nbtbase != null && nbtbase instanceof NBTTagIntArray)
            {
                NBTTagIntArray nbttagintarray = (NBTTagIntArray)nbtbase;
                int[] aint = nbttagintarray.func_150302_c();

                if (aint.length == 1)
                {
                    return aint[0];
                }
                else
                {
                    int j = 0;
                    int k = 0;
                    int l = 0;
                    int[] aint1 = aint;
                    int i1 = aint.length;

                    for (int j1 = 0; j1 < i1; ++j1)
                    {
                        int k1 = aint1[j1];
                        j += (k1 & 16711680) >> 16;
                        k += (k1 & 65280) >> 8;
                        l += (k1 & 255) >> 0;
                    }

                    j /= aint.length;
                    k /= aint.length;
                    l /= aint.length;
                    return j << 16 | k << 8 | l;
                }
            }
            else
            {
                return 9079434;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static NBTBase func_150903_a(ItemStack p_150903_0_, String p_150903_1_)
    {
        if (p_150903_0_.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = p_150903_0_.getTagCompound().getCompoundTag("Explosion");

            if (nbttagcompound != null)
            {
                return nbttagcompound.getTag(p_150903_1_);
            }
        }

        return null;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_, boolean p_77624_4_)
    {
        if (p_77624_1_.hasTagCompound())
        {
            NBTTagCompound nbttagcompound = p_77624_1_.getTagCompound().getCompoundTag("Explosion");

            if (nbttagcompound != null)
            {
                func_150902_a(nbttagcompound, p_77624_3_);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void func_150902_a(NBTTagCompound p_150902_0_, List<String> p_150902_1_)
    {
        byte b0 = p_150902_0_.getByte("Type");

        if (b0 >= 0 && b0 <= 4)
        {
            p_150902_1_.add(StatCollector.translateToLocal("item.fireworksCharge.type." + b0).trim());
        }
        else
        {
            p_150902_1_.add(StatCollector.translateToLocal("item.fireworksCharge.type").trim());
        }

        int[] aint = p_150902_0_.getIntArray("Colors");
        int j;
        int k;

        if (aint.length > 0)
        {
            boolean flag = true;
            String s = "";
            int[] aint1 = aint;
            int i = aint.length;

            for (j = 0; j < i; ++j)
            {
                k = aint1[j];

                if (!flag)
                {
                    s = s + ", ";
                }

                flag = false;
                boolean flag1 = false;

                for (int l = 0; l < 16; ++l)
                {
                    if (k == ItemDye.field_150922_c[l])
                    {
                        flag1 = true;
                        s = s + StatCollector.translateToLocal("item.fireworksCharge." + ItemDye.field_150923_a[l]);
                        break;
                    }
                }

                if (!flag1)
                {
                    s = s + StatCollector.translateToLocal("item.fireworksCharge.customColor");
                }
            }

            p_150902_1_.add(s);
        }

        int[] aint2 = p_150902_0_.getIntArray("FadeColors");
        boolean flag2;

        if (aint2.length > 0)
        {
            flag2 = true;
            String s1 = StatCollector.translateToLocal("item.fireworksCharge.fadeTo") + " ";
            int[] aint3 = aint2;
            j = aint2.length;

            for (k = 0; k < j; ++k)
            {
                int j1 = aint3[k];

                if (!flag2)
                {
                    s1 = s1 + ", ";
                }

                flag2 = false;
                boolean flag4 = false;

                for (int i1 = 0; i1 < 16; ++i1)
                {
                    if (j1 == ItemDye.field_150922_c[i1])
                    {
                        flag4 = true;
                        s1 = s1 + StatCollector.translateToLocal("item.fireworksCharge." + ItemDye.field_150923_a[i1]);
                        break;
                    }
                }

                if (!flag4)
                {
                    s1 = s1 + StatCollector.translateToLocal("item.fireworksCharge.customColor");
                }
            }

            p_150902_1_.add(s1);
        }

        flag2 = p_150902_0_.getBoolean("Trail");

        if (flag2)
        {
            p_150902_1_.add(StatCollector.translateToLocal("item.fireworksCharge.trail"));
        }

        boolean flag3 = p_150902_0_.getBoolean("Flicker");

        if (flag3)
        {
            p_150902_1_.add(StatCollector.translateToLocal("item.fireworksCharge.flicker"));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
        super.registerIcons(register);
        this.field_150904_a = register.registerIcon(this.getIconString() + "_overlay");
    }
}