package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockFlower extends BlockBush
{
    private static final String[][] field_149860_M = new String[][] {{"flower_dandelion"}, {"flower_rose", "flower_blue_orchid", "flower_allium", "flower_houstonia", "flower_tulip_red", "flower_tulip_orange", "flower_tulip_white", "flower_tulip_pink", "flower_oxeye_daisy"}};
    public static final String[] field_149859_a = new String[] {"poppy", "blueOrchid", "allium", "houstonia", "tulipRed", "tulipOrange", "tulipWhite", "tulipPink", "oxeyeDaisy"};
    public static final String[] field_149858_b = new String[] {"dandelion"};
    @SideOnly(Side.CLIENT)
    private IIcon[] field_149861_N;
    private int field_149862_O;
    private static final String __OBFID = "CL_00000246";

    protected BlockFlower(int p_i2173_1_)
    {
        super(Material.plants);
        this.field_149862_O = p_i2173_1_;
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (meta >= this.field_149861_N.length)
        {
            meta = 0;
        }

        return this.field_149861_N[meta];
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        this.field_149861_N = new IIcon[field_149860_M[this.field_149862_O].length];

        for (int i = 0; i < this.field_149861_N.length; ++i)
        {
            this.field_149861_N[i] = reg.registerIcon(field_149860_M[this.field_149862_O][i]);
        }
    }

    /**
     * Determines the damage on the item the block drops. Used in cloth and wood.
     */
    public int damageDropped(int meta)
    {
        return meta;
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<net.minecraft.item.ItemStack> list)
    {
        for (int i = 0; i < this.field_149861_N.length; ++i)
        {
            list.add(new ItemStack(itemIn, 1, i));
        }
    }

    public static BlockFlower func_149857_e(String p_149857_0_)
    {
        String[] astring = field_149858_b;
        int i = astring.length;
        int j;
        String s1;

        for (j = 0; j < i; ++j)
        {
            s1 = astring[j];

            if (s1.equals(p_149857_0_))
            {
                return Blocks.yellow_flower;
            }
        }

        astring = field_149859_a;
        i = astring.length;

        for (j = 0; j < i; ++j)
        {
            s1 = astring[j];

            if (s1.equals(p_149857_0_))
            {
                return Blocks.red_flower;
            }
        }

        return null;
    }

    public static int func_149856_f(String p_149856_0_)
    {
        int i;

        for (i = 0; i < field_149858_b.length; ++i)
        {
            if (field_149858_b[i].equals(p_149856_0_))
            {
                return i;
            }
        }

        for (i = 0; i < field_149859_a.length; ++i)
        {
            if (field_149859_a[i].equals(p_149856_0_))
            {
                return i;
            }
        }

        return 0;
    }
}