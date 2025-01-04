package net.minecraft.world;

public class ChunkCoordIntPair
{
    /** 该块坐标对的 X 位置 */
    public final int chunkXPos;
    /** 该块坐标对的 Z 位置 */
    public final int chunkZPos;
    private static final String __OBFID = "CL_00000133";

    public ChunkCoordIntPair(int x, int z)
    {
        this.chunkXPos = x;
        this.chunkZPos = z;
    }

    /**
     * 将块坐标对转换为整数（适合散列）
     */
    public static long chunkXZ2Int(int x, int z)
    {
        /*
          将 x 和 z 分别转换为无符号的32位整数。
          将 z 左移32位，确保它占据高32位。
          将 x 和左移后的 z 进行按位或运算，组合成一个64位整数。
         */
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }

    public int hashCode()
    {
        int i = 1664525 * this.chunkXPos + 1013904223;
        int j = 1664525 * (this.chunkZPos ^ -559038737) + 1013904223;
        return i ^ j;
    }

    public boolean equals(Object chunkCoordIntPair)
    {
        if (this == chunkCoordIntPair)
        {
            return true;
        }
        else if (!(chunkCoordIntPair instanceof ChunkCoordIntPair))
        {
            return false;
        }
        else
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)chunkCoordIntPair;
            return this.chunkXPos == chunkcoordintpair.chunkXPos && this.chunkZPos == chunkcoordintpair.chunkZPos;
        }
    }

    public int getCenterXPos()
    {
        return (this.chunkXPos << 4) + 8;
    }

    public int getCenterZPosition()
    {
        return (this.chunkZPos << 4) + 8;
    }

    public ChunkPosition func_151349_a(int pairInt)
    {
        return new ChunkPosition(this.getCenterXPos(), pairInt, this.getCenterZPosition());
    }

    public String toString()
    {
        return "[" + this.chunkXPos + ", " + this.chunkZPos + "]";
    }
}