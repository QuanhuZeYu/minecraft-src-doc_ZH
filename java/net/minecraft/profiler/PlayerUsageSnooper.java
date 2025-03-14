package net.minecraft.profiler;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.util.HttpUtil;

public class PlayerUsageSnooper
{
    /** 用于存储临时数据的 Map*/
    private final Map field_152773_a = Maps.newHashMap();
    /** 用于存储持久化数据的 Map*/
    private final Map field_152774_b = Maps.newHashMap();
    /** 唯一的标识符，用于标识此次数据收集*/
    private final String uniqueID = UUID.randomUUID().toString();
    /**​ 数据上报的服务器 URL */
    private final URL serverUrl;
    /** 玩家统计数据收集器的实例*/
    private final IPlayerUsage playerStatsCollector;
    /**​ 定时器，用于每隔 15 分钟触发一次数据上报 */
    private final Timer threadTrigger = new Timer("Snooper Timer", true);
    /** 用于同步操作的锁对象*/
    private final Object syncLock = new Object();
    /** Minecraft 启动时的时间戳（以毫秒为单位）*/
    private final long minecraftStartTimeMilis;
    /** 标识是否正在运行*/
    private boolean isRunning;
    /**​ 每次调用 getSelfCounterFor 时自增的计数器 */
    private int selfCounter;
    private static final String __OBFID = "CL_00001515";

    public PlayerUsageSnooper(String p_i1563_1_, IPlayerUsage p_i1563_2_, long p_i1563_3_)
    {
        try
        {
            // 构造数据上报的 URL
            this.serverUrl = new URL("http://snoop.minecraft.net/" + p_i1563_1_ + "?version=" + 2);
        }
        catch (MalformedURLException malformedurlexception)
        {
            throw new IllegalArgumentException();
        }

        this.playerStatsCollector = p_i1563_2_;
        this.minecraftStartTimeMilis = p_i1563_3_;
    }

    /**
     * 启动数据收集器。多次调用不会报错。
     */
    public void startSnooper()
    {
        if (!this.isRunning)
        {
            this.isRunning = true;
            // 初始化数据收集
            this.func_152766_h();
            // 设置定时任务，每隔 15 分钟上报一次数据
            this.threadTrigger.schedule(new TimerTask()
            {
                private static final String __OBFID = "CL_00001516";
                public void run()
                {
                    if (PlayerUsageSnooper.this.playerStatsCollector.isSnooperEnabled())
                    {
                        HashMap hashmap;

                        synchronized (PlayerUsageSnooper.this.syncLock)
                        {
                            // 复制持久化数据
                            hashmap = new HashMap(PlayerUsageSnooper.this.field_152774_b);

                            // 如果是第一次上报，则添加临时数据
                            if (PlayerUsageSnooper.this.selfCounter == 0)
                            {
                                hashmap.putAll(PlayerUsageSnooper.this.field_152773_a);
                            }

                            // 添加计数器和唯一标识符
                            hashmap.put("snooper_count", Integer.valueOf(PlayerUsageSnooper.access$308(PlayerUsageSnooper.this)));
                            hashmap.put("snooper_token", PlayerUsageSnooper.this.uniqueID);
                        }

                        // 上报数据到服务器
                        HttpUtil.func_151226_a(PlayerUsageSnooper.this.serverUrl, hashmap, true);
                    }
                }
            }, 0L, 900000L);
        }
    }

    /**
     * 初始化数据收集，添加 JVM 参数、系统信息和版本信息
     */
    private void func_152766_h()
    {
        this.addJvmArgsToSnooper();
        this.func_152768_a("snooper_token", this.uniqueID);
        this.func_152767_b("snooper_token", this.uniqueID);
        this.func_152767_b("os_name", System.getProperty("os.name"));
        this.func_152767_b("os_version", System.getProperty("os.version"));
        this.func_152767_b("os_architecture", System.getProperty("os.arch"));
        this.func_152767_b("java_version", System.getProperty("java.version"));
        this.func_152767_b("version", "1.7.10");
        this.playerStatsCollector.addServerTypeToSnooper(this);
    }

    /**
     * 添加 JVM 参数到数据收集器
     */
    private void addJvmArgsToSnooper()
    {
        RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
        List list = runtimemxbean.getInputArguments();
        int i = 0;
        Iterator iterator = list.iterator();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();

            if (s.startsWith("-X"))
            {
                this.func_152768_a("jvm_arg[" + i++ + "]", s);
            }
        }

        this.func_152768_a("jvm_args", Integer.valueOf(i));
    }

    /**
     * 添加内存和 CPU 信息到数据收集器
     */
    public void addMemoryStatsToSnooper()
    {
        this.func_152767_b("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
        this.func_152767_b("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
        this.func_152767_b("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
        this.func_152767_b("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
        this.playerStatsCollector.addServerStatsToSnooper(this);
    }

    /**
     * 添加临时数据到数据收集器
     */
    public void func_152768_a(String p_152768_1_, Object p_152768_2_)
    {
        synchronized (this.syncLock)
        {
            this.field_152774_b.put(p_152768_1_, p_152768_2_);
        }
    }

    /**
     * 添加持久化数据到数据收集器
     */
    public void func_152767_b(String p_152767_1_, Object p_152767_2_)
    {
        synchronized (this.syncLock)
        {
            this.field_152773_a.put(p_152767_1_, p_152767_2_);
        }
    }

    @SideOnly(Side.CLIENT)
    /**
     * 获取当前收集到的所有统计数据
     */
    public Map<String, String> getCurrentStats()
    {
        LinkedHashMap linkedhashmap = new LinkedHashMap();

        synchronized (this.syncLock)
        {
            this.addMemoryStatsToSnooper();
            Iterator iterator = this.field_152773_a.entrySet().iterator();
            Entry entry;

            while (iterator.hasNext())
            {
                entry = (Entry)iterator.next();
                linkedhashmap.put(entry.getKey(), entry.getValue().toString());
            }

            iterator = this.field_152774_b.entrySet().iterator();

            while (iterator.hasNext())
            {
                entry = (Entry)iterator.next();
                linkedhashmap.put(entry.getKey(), entry.getValue().toString());
            }

            return linkedhashmap;
        }
    }

    /**
     * 检查数据收集器是否正在运行
     */
    public boolean isSnooperRunning()
    {
        return this.isRunning;
    }

    /**
     * 停止数据收集器
     */
    public void stopSnooper()
    {
        this.threadTrigger.cancel();
    }

    @SideOnly(Side.CLIENT)
    /**
     * 获取唯一标识符
     */
    public String getUniqueID()
    {
        return this.uniqueID;
    }

    /**
     * 返回游戏启动时的时间戳（以毫秒为单位）
     */
    public long getMinecraftStartTimeMillis()
    {
        return this.minecraftStartTimeMilis;
    }

    /**
     * 内部方法，用于自增计数器
     */
    static int access$308(PlayerUsageSnooper p_access$308_0_)
    {
        return p_access$308_0_.selfCounter++;
    }
}