package net.minecraft.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockWorkbench extends Block
{
    @SideOnly(Side.CLIENT)
    private IIcon iIconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iIconFront;
    private static final String __OBFID = "CL_00000221";

    protected BlockWorkbench()
    {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return side == 1 ? this.iIconTop : (side == 0 ? Blocks.planks.getBlockTextureFromSide(side) : (side != 2 && side != 4 ? this.blockIcon : this.iIconFront));
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        this.blockIcon = reg.registerIcon(this.getTextureName() + "_side");
        this.iIconTop = reg.registerIcon(this.getTextureName() + "_top");
        this.iIconFront = reg.registerIcon(this.getTextureName() + "_front");
    }

    /**
     * 在块激活时调用（右键单击块。）
     */
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ)
    {
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            player.displayGUIWorkbench(x, y, z);
            return true;
        }
    }
}