package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Map;
import net.minecraft.util.RegistrySimple;

@SideOnly(Side.CLIENT)
public class SoundRegistry extends RegistrySimple
{
    private Map field_148764_a;
    private static final String __OBFID = "CL_00001151";

    /**
     * Creates the Map we will use to map keys to their registered values.
     */
    protected Map<Object, Object> createUnderlyingMap()
    {
        this.field_148764_a = Maps.newHashMap();
        return this.field_148764_a;
    }

    public void registerSound(SoundEventAccessorComposite p_148762_1_)
    {
        this.putObject(p_148762_1_.getSoundEventLocation(), p_148762_1_);
    }

    public void func_148763_c()
    {
        this.field_148764_a.clear();
    }
}