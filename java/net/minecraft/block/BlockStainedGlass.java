package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockStainedGlass extends BlockBreakable
{
    private static final IIcon[] field_149998_a = new IIcon[16];
    private static final String __OBFID = "CL_00000312";

    public BlockStainedGlass(Material p_i45427_1_)
    {
        super("glass", p_i45427_1_, false);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return field_149998_a[meta % field_149998_a.length];
    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and wood.
     */
    public int damageDropped(int meta)
    {
        return meta;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public static int func_149997_b(int p_149997_0_)
    {
        return ~p_149997_0_ & 15;
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<net.minecraft.item.ItemStack> list)
    {
        for (int i = 0; i < field_149998_a.length; ++i)
        {
            list.add(new ItemStack(itemIn, 1, i));
        }
    }

    /**
     * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
     */
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass()
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        for (int i = 0; i < field_149998_a.length; ++i)
        {
            field_149998_a[i] = reg.registerIcon(this.getTextureName() + "_" + ItemDye.field_150921_b[func_149997_b(i)]);
        }
    }

    /**
     * Return true if a player with Silk Touch can harvest this block directly, and not its normal drops.
     */
    protected boolean canSilkHarvest()
    {
        return true;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock()
    {
        return false;
    }
}