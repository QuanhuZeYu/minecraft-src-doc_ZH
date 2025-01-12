package net.minecraft.util;

import org.apache.commons.lang3.Validate;

public class ResourceLocation
{
    private final String resourceDomain;
    private final String resourcePath;
    private static final String __OBFID = "CL_00001082";

    public ResourceLocation(String p_i1292_1_, String p_i1292_2_)
    {
        Validate.notNull(p_i1292_2_);

        if (p_i1292_1_ != null && p_i1292_1_.length() != 0)
        {
            this.resourceDomain = p_i1292_1_;
        }
        else
        {
            this.resourceDomain = "minecraft";
        }

        this.resourcePath = p_i1292_2_;
    }

    /**
     * 表示一个资源位置，通常用于游戏中的资源路径。
     * 资源位置由域（domain）和路径（path）两部分组成，格式为 "domain:path"。
     * 如果输入字符串中不包含分隔符 ":", 则默认域为 "minecraft"。
     *
     * @param resourceLocationString 资源位置字符串，格式为 "domain:path" 或 "path"
     */
    public ResourceLocation(String resourceLocationString) {
        // 默认资源域为 "minecraft"
        String domain = "minecraft";
        // 资源路径初始值为传入的字符串
        String path = resourceLocationString;
        // 查找分隔符 ":" 的索引位置
        int separatorIndex = resourceLocationString.indexOf(':');

        // 如果找到分隔符
        if (separatorIndex >= 0) {
            // 从分隔符之后的位置截取资源路径
            path = resourceLocationString.substring(separatorIndex + 1);

            // 如果分隔符不在字符串开头，则截取资源域
            if (separatorIndex > 1) {
                domain = resourceLocationString.substring(0, separatorIndex);
            }
        }

        // 将资源域转换为小写并赋值给成员变量
        this.resourceDomain = domain.toLowerCase();
        // 将资源路径赋值给成员变量
        this.resourcePath = path;
    }



    public String getResourcePath()
    {
        return this.resourcePath;
    }

    public String getResourceDomain()
    {
        return this.resourceDomain;
    }

    public String toString()
    {
        return this.resourceDomain + ":" + this.resourcePath;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof ResourceLocation))
        {
            return false;
        }
        else
        {
            ResourceLocation resourcelocation = (ResourceLocation)p_equals_1_;
            return this.resourceDomain.equals(resourcelocation.resourceDomain) && this.resourcePath.equals(resourcelocation.resourcePath);
        }
    }

    public int hashCode()
    {
        return 31 * this.resourceDomain.hashCode() + this.resourcePath.hashCode();
    }
}