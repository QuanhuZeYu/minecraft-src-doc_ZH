package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class FallbackResourceManager implements IResourceManager {
    protected final List<net.minecraft.client.resources.IResourcePack> resourcePacks = new ArrayList();
    private final IMetadataSerializer frmMetadataSerializer;
    private static final String __OBFID = "CL_00001074";

    public FallbackResourceManager(IMetadataSerializer p_i1289_1_) {
        this.frmMetadataSerializer = p_i1289_1_;
    }

    public void addResourcePack(IResourcePack p_110538_1_) {
        this.resourcePacks.add(p_110538_1_);
    }

    public Set<String> getResourceDomains() {
        return null;
    }

    public IResource getResource(ResourceLocation resl) throws IOException {
        IResourcePack iresourcepack = null;
        ResourceLocation resourcelocation1 = getLocationMcmeta(resl);

        for (int i = this.resourcePacks.size() - 1; i >= 0; --i) {
            IResourcePack iresourcepack1 = (IResourcePack) this.resourcePacks.get(i);

            if (iresourcepack == null && iresourcepack1.resourceExists(resourcelocation1)) {
                iresourcepack = iresourcepack1;
            }

            if (iresourcepack1.resourceExists(resl)) {
                InputStream inputstream = null;

                if (iresourcepack != null) {
                    inputstream = iresourcepack.getInputStream(resourcelocation1);
                }

                return new SimpleResource(resl, iresourcepack1.getInputStream(resl), inputstream,
                        this.frmMetadataSerializer);
            }
        }

        throw new FileNotFoundException(resl.toString());
    }

    public List<net.minecraft.client.resources.IResource> getAllResources(ResourceLocation p_135056_1_)
            throws IOException {
        ArrayList arraylist = Lists.newArrayList();
        ResourceLocation resourcelocation1 = getLocationMcmeta(p_135056_1_);
        Iterator iterator = this.resourcePacks.iterator();

        while (iterator.hasNext()) {
            IResourcePack iresourcepack = (IResourcePack) iterator.next();

            if (iresourcepack.resourceExists(p_135056_1_)) {
                InputStream inputstream = iresourcepack.resourceExists(resourcelocation1)
                        ? iresourcepack.getInputStream(resourcelocation1)
                        : null;
                arraylist.add(new SimpleResource(p_135056_1_, iresourcepack.getInputStream(p_135056_1_), inputstream,
                        this.frmMetadataSerializer));
            }
        }

        if (arraylist.isEmpty()) {
            throw new FileNotFoundException(p_135056_1_.toString());
        } else {
            return arraylist;
        }
    }

    static ResourceLocation getLocationMcmeta(ResourceLocation p_110537_0_) {
        return new ResourceLocation(p_110537_0_.getResourceDomain(), p_110537_0_.getResourcePath() + ".mcmeta");
    }
}