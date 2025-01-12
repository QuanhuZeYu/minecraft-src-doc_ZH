package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class ItemBucket extends Item
{
    /** field for checking if the bucket has been filled. */
    private Block isFull;
    private static final String __OBFID = "CL_00000000";

    public ItemBucket(Block p_i45331_1_)
    {
        this.maxStackSize = 1;
        this.isFull = p_i45331_1_;
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player)
    {
        boolean flag = this.isFull == Blocks.air;
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(worldIn, player, flag);

        if (movingobjectposition == null)
        {
            return itemStackIn;
        }
        else
        {
            FillBucketEvent event = new FillBucketEvent(player, itemStackIn, worldIn, movingobjectposition);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                return itemStackIn;
            }

            if (event.getResult() == Event.Result.ALLOW)
            {
                if (player.capabilities.isCreativeMode)
                {
                    return itemStackIn;
                }

                if (--itemStackIn.stackSize <= 0)
                {
                    return event.result;
                }

                if (!player.inventory.addItemStackToInventory(event.result))
                {
                    player.dropPlayerItemWithRandomChoice(event.result, false);
                }

                return itemStackIn;
            }
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                int i = movingobjectposition.blockX;
                int j = movingobjectposition.blockY;
                int k = movingobjectposition.blockZ;

                if (!worldIn.canMineBlock(player, i, j, k))
                {
                    return itemStackIn;
                }

                if (flag)
                {
                    if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStackIn))
                    {
                        return itemStackIn;
                    }

                    Material material = worldIn.getBlock(i, j, k).getMaterial();
                    int l = worldIn.getBlockMetadata(i, j, k);

                    if (material == Material.water && l == 0)
                    {
                        worldIn.setBlockToAir(i, j, k);
                        return this.handleItemUsage$$(itemStackIn, player, Items.water_bucket);
                    }

                    if (material == Material.lava && l == 0)
                    {
                        worldIn.setBlockToAir(i, j, k);
                        return this.handleItemUsage$$(itemStackIn, player, Items.lava_bucket);
                    }
                }
                else
                {
                    if (this.isFull == Blocks.air)
                    {
                        return new ItemStack(Items.bucket);
                    }

                    if (movingobjectposition.sideHit == 0)
                    {
                        --j;
                    }

                    if (movingobjectposition.sideHit == 1)
                    {
                        ++j;
                    }

                    if (movingobjectposition.sideHit == 2)
                    {
                        --k;
                    }

                    if (movingobjectposition.sideHit == 3)
                    {
                        ++k;
                    }

                    if (movingobjectposition.sideHit == 4)
                    {
                        --i;
                    }

                    if (movingobjectposition.sideHit == 5)
                    {
                        ++i;
                    }

                    if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStackIn))
                    {
                        return itemStackIn;
                    }

                    if (this.tryPlaceContainedLiquid(worldIn, i, j, k) && !player.capabilities.isCreativeMode)
                    {
                        return new ItemStack(Items.bucket);
                    }
                }
            }

            return itemStackIn;
        }
    }

    /**
     * 处理物品栈的使用逻辑。
     * 如果玩家处于创造模式，则直接返回物品栈。
     * 如果玩家不是处于创造模式，且物品栈数量减少后为零，则返回一个新物品栈。
     * 否则，将新物品添加到玩家的物品栏中，如果物品栏已满，则将其丢弃在玩家脚下。
     *
     * @param originalStack 原始物品栈
     * @param player 使用物品的玩家
     * @param newItem 要添加的新物品类型
     * @return 使用后剩余的物品栈
     */
    private ItemStack handleItemUsage$$(ItemStack originalStack, EntityPlayer player, Item newItem)
    {
        // 如果玩家处于创造模式，则不消耗物品
        if (player.capabilities.isCreativeMode)
        {
            return originalStack;
        }
        else
        {
            // 减少物品栈的数量
            originalStack.stackSize--;

            // 如果物品栈数量变为零，则返回一个新的物品栈
            if (originalStack.stackSize <= 0)
            {
                return new ItemStack(newItem);
            }
            else
            {
                // 创建新物品栈
                ItemStack newStack = new ItemStack(newItem);

                // 尝试将新物品添加到玩家的物品栏中
                if (!player.inventory.addItemStackToInventory(newStack))
                {
                    // 如果物品栏已满，则将新物品丢弃在玩家脚下
                    player.dropPlayerItemWithRandomChoice(newStack, false);
                }

                // 返回使用后剩余的物品栈
                return originalStack;
            }
        }
    }


    /**
     * Attempts to place the liquid contained inside the bucket.
     */
    public boolean tryPlaceContainedLiquid(World p_77875_1_, int p_77875_2_, int p_77875_3_, int p_77875_4_)
    {
        if (this.isFull == Blocks.air)
        {
            return false;
        }
        else
        {
            Material material = p_77875_1_.getBlock(p_77875_2_, p_77875_3_, p_77875_4_).getMaterial();
            boolean flag = !material.isSolid();

            if (!p_77875_1_.isAirBlock(p_77875_2_, p_77875_3_, p_77875_4_) && !flag)
            {
                return false;
            }
            else
            {
                if (p_77875_1_.provider.isHellWorld && this.isFull == Blocks.flowing_water)
                {
                    p_77875_1_.playSoundEffect((double)((float)p_77875_2_ + 0.5F), (double)((float)p_77875_3_ + 0.5F), (double)((float)p_77875_4_ + 0.5F), "random.fizz", 0.5F, 2.6F + (p_77875_1_.rand.nextFloat() - p_77875_1_.rand.nextFloat()) * 0.8F);

                    for (int l = 0; l < 8; ++l)
                    {
                        p_77875_1_.spawnParticle("largesmoke", (double)p_77875_2_ + Math.random(), (double)p_77875_3_ + Math.random(), (double)p_77875_4_ + Math.random(), 0.0D, 0.0D, 0.0D);
                    }
                }
                else
                {
                    if (!p_77875_1_.isRemote && flag && !material.isLiquid())
                    {
                        p_77875_1_.func_147480_a(p_77875_2_, p_77875_3_, p_77875_4_, true);
                    }

                    p_77875_1_.setBlock(p_77875_2_, p_77875_3_, p_77875_4_, this.isFull, 0, 3);
                }

                return true;
            }
        }
    }
}