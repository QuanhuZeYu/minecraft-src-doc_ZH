package net.minecraft.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public class Timer
{
    /** 每秒的计时器 tick 数，表示游戏运行的速率（例如，标准 Minecraft 游戏速率为 20 ticks/秒） */
    float ticksPerSecond;

    /** 上一次调用 updateTimer() 时，高精度时钟报告的时间（以秒为单位） */
    private double lastHRTime;

    /** 自上一次调用 updateTimer() 以来，经过的完整 tick 数，最大值为 10，以防止卡顿 */
    public int elapsedTicks;

    /**
     * 自上一次 tick 以来经过的时间（以 tick 为单位），用于显示渲染逻辑（范围：0.0 - 1.0）。
     * 如果游戏暂停，此值会冻结以避免画面抖动。
     */
    public float renderPartialTicks;

    /**
     * 计时器速度的乘数，用于控制游戏运行的快慢。例如，0.5 表示游戏以半速运行。
     */
    public float timerSpeed = 1.0F;

    /** 自上一次 tick 以来经过的时间（以 tick 为单位，范围：0.0 - 1.0） */
    public float elapsedPartialTicks;

    /** 上一次同步时，系统时钟报告的时间（以毫秒为单位） */
    private long lastSyncSysClock;

    /** 上一次同步时，高精度时钟报告的时间（以毫秒为单位） */
    private long lastSyncHRClock;

    /** 用于计算时间同步调整的临时变量 */
    private long field_74285_i;

    /** 用于将高精度时钟与系统时钟同步的比率，每秒更新一次 */
    private double timeSyncAdjustment = 1.0D;

    private static final String __OBFID = "CL_00000658";

    public Timer(float p_i1018_1_)
    {
        this.ticksPerSecond = p_i1018_1_;
        this.lastSyncSysClock = Minecraft.getSystemTime();
        this.lastSyncHRClock = System.nanoTime() / 1000000L;
    }

    /**
     * 使用当前时间更新计时器的所有字段
     */
    public void updateTimer()
    {
        long i = Minecraft.getSystemTime();
        long j = i - this.lastSyncSysClock;
        long k = System.nanoTime() / 1000000L;
        double d0 = (double)k / 1000.0D;

        if (j <= 1000L && j >= 0L)
        {
            this.field_74285_i += j;

            if (this.field_74285_i > 1000L)
            {
                long l = k - this.lastSyncHRClock;
                double d1 = (double)this.field_74285_i / (double)l;
                this.timeSyncAdjustment += (d1 - this.timeSyncAdjustment) * 0.20000000298023224D;
                this.lastSyncHRClock = k;
                this.field_74285_i = 0L;
            }

            if (this.field_74285_i < 0L)
            {
                this.lastSyncHRClock = k;
            }
        }
        else
        {
            this.lastHRTime = d0;
        }

        this.lastSyncSysClock = i;
        double d2 = (d0 - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = d0;

        if (d2 < 0.0D)
        {
            d2 = 0.0D;
        }

        if (d2 > 1.0D)
        {
            d2 = 1.0D;
        }

        this.elapsedPartialTicks = (float)((double)this.elapsedPartialTicks + d2 * (double)this.timerSpeed * (double)this.ticksPerSecond);
        this.elapsedTicks = (int)this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float)this.elapsedTicks;

        if (this.elapsedTicks > 10)
        {
            this.elapsedTicks = 10;
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
    }
}