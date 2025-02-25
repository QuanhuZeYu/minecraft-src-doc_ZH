package net.minecraft.client.shader;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.BufferUtils;

@SideOnly(Side.CLIENT)
public class ShaderLoader
{
    private final ShaderLoader.ShaderType field_148061_a;
    private final String field_148059_b;
    private int field_148060_c;
    private int field_148058_d = 0;
    private static final String __OBFID = "CL_00001043";

    private ShaderLoader(ShaderLoader.ShaderType type, int shaderId, String filename)
    {
        this.field_148061_a = type;
        this.field_148060_c = shaderId;
        this.field_148059_b = filename;
    }

    public void func_148056_a(ShaderManager manager)
    {
        ++this.field_148058_d;
        OpenGlHelper.func_153178_b(manager.func_147986_h(), this.field_148060_c);
    }

    public void func_148054_b(ShaderManager manager)
    {
        --this.field_148058_d;

        if (this.field_148058_d <= 0)
        {
            OpenGlHelper.func_153180_a(this.field_148060_c);
            this.field_148061_a.func_148064_d().remove(this.field_148059_b);
        }
    }

    public String func_148055_a()
    {
        return this.field_148059_b;
    }

    public static ShaderLoader func_148057_a(IResourceManager resourceManager, ShaderLoader.ShaderType type, String filename) throws IOException
    {
        ShaderLoader shaderloader = (ShaderLoader)type.func_148064_d().get(filename);

        if (shaderloader == null)
        {
            ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + filename + type.func_148063_b());
            BufferedInputStream bufferedinputstream = new BufferedInputStream(resourceManager.getResource(resourcelocation).getInputStream());
            byte[] abyte = IOUtils.toByteArray(bufferedinputstream);
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(abyte.length);
            bytebuffer.put(abyte);
            bytebuffer.position(0);
            int i = OpenGlHelper.func_153195_b(type.func_148065_c());
            OpenGlHelper.func_153169_a(i, bytebuffer);
            OpenGlHelper.func_153170_c(i);

            if (OpenGlHelper.func_153157_c(i, OpenGlHelper.field_153208_p) == 0)
            {
                String s1 = StringUtils.trim(OpenGlHelper.func_153158_d(i, 32768));
                JsonException jsonexception = new JsonException("Couldn\'t compile " + type.func_148062_a() + " program: " + s1);
                jsonexception.func_151381_b(resourcelocation.getResourcePath());
                throw jsonexception;
            }

            shaderloader = new ShaderLoader(type, i, filename);
            type.func_148064_d().put(filename, shaderloader);
        }

        return shaderloader;
    }

    @SideOnly(Side.CLIENT)
    public static enum ShaderType
    {
        VERTEX("vertex", ".vsh", OpenGlHelper.field_153209_q),
        FRAGMENT("fragment", ".fsh", OpenGlHelper.field_153210_r);
        private final String field_148072_c;
        private final String field_148069_d;
        private final int field_148070_e;
        private final Map field_148067_f = Maps.newHashMap();

        private static final String __OBFID = "CL_00001044";

        private ShaderType(String p_i45090_3_, String p_i45090_4_, int p_i45090_5_)
        {
            this.field_148072_c = p_i45090_3_;
            this.field_148069_d = p_i45090_4_;
            this.field_148070_e = p_i45090_5_;
        }

        public String func_148062_a()
        {
            return this.field_148072_c;
        }

        protected String func_148063_b()
        {
            return this.field_148069_d;
        }

        protected int func_148065_c()
        {
            return this.field_148070_e;
        }

        protected Map<String, net.minecraft.client.shader.ShaderLoader> func_148064_d()
        {
            return this.field_148067_f;
        }
    }
}