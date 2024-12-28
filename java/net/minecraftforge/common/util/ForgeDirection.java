package net.minecraftforge.common.util;

public enum ForgeDirection
{
    /** -Y 0*/
    DOWN(0, -1, 0),

    /** +Y 1*/
    UP(0, 1, 0),

    /** -Z 2*/
    NORTH(0, 0, -1),

    /** +Z 3*/
    SOUTH(0, 0, 1),

    /** -X 4*/
    WEST(-1, 0, 0),

    /** +X 5*/
    EAST(1, 0, 0),

    /**
     * 仅由 getOrientation 用于无效输入 6
     */
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};
    // Left hand rule rotation matrix for all possible axes of rotation
    public static final int[][] ROTATION_MATRIX = {
        {0, 1, 4, 5, 3, 2, 6}, // -y y -x x z -z
        {0, 1, 5, 4, 2, 3, 6}, // y -y x -x -z z
    	{5, 4, 2, 3, 0, 1, 6}, // x -x -z z -y y
    	{4, 5, 2, 3, 1, 0, 6}, // -x x z -z y -y
    	{2, 3, 1, 0, 4, 5, 6}, // -z z y -y -x x
    	{3, 2, 0, 1, 4, 5, 6}, // z -z -y y -x x
    	{0, 1, 2, 3, 4, 5, 6}, // -y y -z z -x x
    };

    private ForgeDirection(int x, int y, int z)
    {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        flag = 1 << ordinal();
    }

    public static ForgeDirection getOrientation(int id)
    {
        if (id >= 0 && id < VALID_DIRECTIONS.length)
        {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public ForgeDirection getOpposite()
    {
        return getOrientation(OPPOSITES[ordinal()]);
    }

    public ForgeDirection getRotation(ForgeDirection axis)
    {
    	return getOrientation(ROTATION_MATRIX[axis.ordinal()][ordinal()]);
    }
}