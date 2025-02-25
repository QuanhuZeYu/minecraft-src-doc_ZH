package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class LayeredTexture extends AbstractTexture
{
    private static final Logger logger = LogManager.getLogger();
    public final List<String> layeredTextureNames;
    private static final String __OBFID = "CL_00001051";

    public LayeredTexture(String ... p_i1274_1_)
    {
        this.layeredTextureNames = Lists.newArrayList(p_i1274_1_);
    }

    public void loadTexture(IResourceManager p_110551_1_) throws IOException
    {
        this.deleteGlTexture();
        BufferedImage bufferedimage = null;

        try
        {
            Iterator iterator = this.layeredTextureNames.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();

                if (s != null)
                {
                    InputStream inputstream = p_110551_1_.getResource(new ResourceLocation(s)).getInputStream();
                    BufferedImage bufferedimage1 = ImageIO.read(inputstream);

                    if (bufferedimage == null)
                    {
                        bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), 2);
                    }

                    bufferedimage.getGraphics().drawImage(bufferedimage1, 0, 0, (ImageObserver)null);
                }
            }
        }
        catch (IOException ioexception)
        {
            logger.error("Couldn\'t load layered image", ioexception);
            return;
        }

        TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
    }
}