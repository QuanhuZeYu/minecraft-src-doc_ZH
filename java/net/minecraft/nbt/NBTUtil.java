package net.minecraft.nbt;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.util.StringUtils;

public final class NBTUtil
{
    private static final String __OBFID = "CL_00001901";

    /**
     * 从NBTTagCompound对象中创建一个GameProfile对象
     * 此方法用于解析NBTTagCompound中的数据，如玩家名称和UUID，以及属性，并将这些信息封装到GameProfile对象中
     *
     * @param compound 包含玩家信息的NBTTagCompound对象，包括名称、UUID和属性等
     * @return 返回一个GameProfile对象，如果提供的NBTTagCompound中没有足够的信息（如名称和UUID都为空），则返回null
     */
    public static GameProfile func_152459_a(NBTTagCompound compound)
    {
        // 初始化玩家名称和UUID字符串为null
        String name = null;
        String uuidS = null;

        // 检查并获取NBTTagCompound中的玩家名称
        if (compound.hasKey("Name", 8))
        {
            name = compound.getString("Name");
        }

        // 检查并获取NBTTagCompound中的玩家UUID
        if (compound.hasKey("Id", 8))
        {
            uuidS = compound.getString("Id");
        }

        // 如果玩家名称和UUID都为空，则返回null
        if (StringUtils.isNullOrEmpty(name) && StringUtils.isNullOrEmpty(uuidS))
        {
            return null;
        }
        else
        {
            // 初始化UUID对象
            UUID uuid;

            try
            {
                // 尝试从UUID字符串中创建UUID对象
                uuid = UUID.fromString(uuidS);
            }
            catch (Throwable throwable)
            {
                // 如果创建失败，设置UUID为null
                uuid = null;
            }

            // 创建GameProfile对象
            GameProfile gameprofile = new GameProfile(uuid, name);

            // 检查并获取玩家属性
            if (compound.hasKey("Properties", 10))
            {
                NBTTagCompound nbttagcompound1 = compound.getCompoundTag("Properties");
                Iterator iterator = nbttagcompound1.getKeys().iterator();

                // 遍历玩家属性的键
                while (iterator.hasNext())
                {
                    String s2 = (String)iterator.next();
                    NBTTagList nbttaglist = nbttagcompound1.getTagList(s2, 10);

                    // 遍历每个属性的值
                    for (int i = 0; i < nbttaglist.tagCount(); ++i)
                    {
                        NBTTagCompound nbttagcompound2 = nbttaglist.getCompoundTagAt(i);
                        String s3 = nbttagcompound2.getString("Value");

                        // 根据是否存在签名，决定是否创建带有签名的Property对象
                        if (nbttagcompound2.hasKey("Signature", 8))
                        {
                            gameprofile.getProperties().put(s2, new Property(s2, s3, nbttagcompound2.getString("Signature")));
                        }
                        else
                        {
                            gameprofile.getProperties().put(s2, new Property(s2, s3));
                        }
                    }
                }
            }

            // 返回创建好的GameProfile对象
            return gameprofile;
        }
    }

    /**
     * func_152460_a<br>
     * 将游戏配置信息保存到NBT（Named Binary Tag）复合标签中
     * 此方法主要用于将游戏配置文件中的相关信息，如玩家名称、ID和属性，保存到NBT复合标签中，
     * 以便在游戏中使用或传输这些信息
     *
     * @param compound NBT复合标签，用于存储游戏配置信息
     * @param profile 游戏配置文件，包含玩家的名称、ID和属性等信息
     */
    public static void saveGameProfile(NBTTagCompound compound, GameProfile profile)
    {
        // 如果游戏配置文件中的玩家名称不为空，则将其保存到NBT复合标签中
        if (!StringUtils.isNullOrEmpty(profile.getName()))
        {
            compound.setString("Name", profile.getName());
        }

        // 如果游戏配置文件中的玩家ID不为空，则将其保存到NBT复合标签中
        if (profile.getId() != null)
        {
            compound.setString("Id", profile.getId().toString());
        }

        // 如果游戏配置文件中有玩家属性，则将这些属性保存到NBT复合标签中
        if (!profile.getProperties().isEmpty())
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            Iterator iterator = profile.getProperties().keySet().iterator();

            // 遍历所有玩家属性
            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                NBTTagList nbttaglist = new NBTTagList();
                NBTTagCompound nbttagcompound2;

                // 遍历每个属性的值，并将其保存到NBT列表标签中
                for (Iterator iterator1 = profile.getProperties().get(s).iterator(); iterator1.hasNext(); nbttaglist.appendTag(nbttagcompound2))
                {
                    Property property = (Property)iterator1.next();
                    nbttagcompound2 = new NBTTagCompound();
                    nbttagcompound2.setString("Value", property.getValue());

                    // 如果属性有签名信息，则将其一并保存
                    if (property.hasSignature())
                    {
                        nbttagcompound2.setString("Signature", property.getSignature());
                    }
                }

                // 将属性列表保存到NBT复合标签中
                nbttagcompound1.setTag(s, nbttaglist);
            }

            // 将包含所有属性的NBT复合标签保存到主NBT复合标签中
            compound.setTag("Properties", nbttagcompound1);
        }
    }
}