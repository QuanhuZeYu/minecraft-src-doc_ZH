package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 纹理管理器类，实现周期性更新和资源重载监听
 */
@SideOnly(Side.CLIENT)
public class TextureManager implements ITickable, IResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger(); // 日志记录器
    private final Map<ResourceLocation, ITextureObject> mapTextureObjects = Maps.newHashMap(); // 资源位置→纹理对象映射
    private final Map<Integer, ResourceLocation> mapResourceLocations = Maps.newHashMap(); // 纹理ID→资源位置映射
    private final List<ITickableTextureObject> listTickables = Lists.newArrayList(); // 需周期性更新的纹理对象列表
    private final Map<String, Integer> mapTextureCounters = Maps.newHashMap(); // 动态纹理计数器
    private IResourceManager theResourceManager; // 资源管理器引用
    private static final String __OBFID = "CL_00001064"; // 反编译标识符

    // 构造函数
    public TextureManager(IResourceManager p_i1284_1_) {
        this.theResourceManager = p_i1284_1_;
    }

    // 绑定指定资源位置的纹理
    public void bindTexture(ResourceLocation resource) {
        ITextureObject texture = (ITextureObject) mapTextureObjects.get(resource);

        if (texture == null) { // 若未加载
            texture = new SimpleTexture(resource); // 创建简单纹理对象
            this.loadTexture(resource, (ITextureObject) texture); // 加载纹理
        }

        TextureUtil.bindTexture(((ITextureObject) texture).getGlTextureId()); // 绑定到OpenGL纹理单元
    }

    // 通过纹理ID获取资源位置
    public ResourceLocation getResourceLocation(int p_130087_1_) {
        return (ResourceLocation) mapResourceLocations.get(Integer.valueOf(p_130087_1_));
    }

    // 加载纹理图集
    public boolean loadTextureMap(ResourceLocation resl, TextureMap textureMap) {
        if (this.loadTickableTexture(resl, textureMap)) { // 加载可更新纹理
            mapResourceLocations.put(Integer.valueOf(textureMap.getTextureType()), resl); // 注册纹理ID
            return true;
        } else {
            return false;
        }
    }

    // 加载可更新纹理对象
    public boolean loadTickableTexture(ResourceLocation resl, ITickableTextureObject textureTickable) {
        if (this.loadTexture(resl, textureTickable)) { // 加载纹理
            listTickables.add(textureTickable); // 添加到周期性更新列表
            return true;
        } else {
            return false;
        }
    }

    // 加载指定资源位置的纹理对象
    public boolean loadTexture(ResourceLocation res, final ITextureObject texture) {
        boolean flag = true;
        ITextureObject texture1 = texture;

        try {
            ((ITextureObject) texture).loadTexture(theResourceManager); // 加载纹理数据
        } catch (IOException ioexception) { // IO异常处理
            logger.warn("Failed to load texture: " + res, ioexception); // 记录警告
            texture1 = TextureUtil.missingTexture; // 使用缺失纹理替代
            mapTextureObjects.put(res, texture1); // 注册缺失纹理
            flag = false;
        } catch (Throwable throwable) { // 严重异常处理
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Registering texture"); // 创建崩溃报告
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered"); // 添加分类
            crashreportcategory.addCrashSection("Resource location", res); // 记录资源位置
            crashreportcategory.addCrashSectionCallable("Texture object class", new Callable() { // 获取纹理类名
                private static final String __OBFID = "CL_00001065";

                public String call() {
                    return texture.getClass().getName();
                }
            });
            throw new ReportedException(crashreport); // 抛出异常报告
        }

        mapTextureObjects.put(res, texture1); // 注册纹理对象
        return flag;
    }

    // 获取指定资源位置的纹理对象
    public ITextureObject getTexture(ResourceLocation res) {
        return (ITextureObject) mapTextureObjects.get(res);
    }

    // 生成动态纹理位置
    public ResourceLocation getDynamicTextureLocation(String p_110578_1_, DynamicTexture p_110578_2_) {
        Integer integer = (Integer) mapTextureCounters.get(p_110578_1_);

        if (integer == null) { // 首次生成
            integer = Integer.valueOf(1);
        } else {
            integer = Integer.valueOf(integer.intValue() + 1); // 递增计数器
        }

        mapTextureCounters.put(p_110578_1_, integer); // 更新计数器
        ResourceLocation resourcelocation = new ResourceLocation(
                String.format("dynamic/%s_%d", new Object[] { p_110578_1_, integer })); // 生成动态资源位置
        this.loadTexture(resourcelocation, p_110578_2_); // 加载动态纹理
        return resourcelocation;
    }

    // 每帧更新所有周期性纹理对象
    public void tick() {
        Iterator<ITickableTextureObject> iterator = listTickables.iterator();

        while (iterator.hasNext()) {
            ITickable itickable = iterator.next();
            itickable.tick(); // 执行周期性更新逻辑
        }
    }

    // 删除指定资源位置的纹理
    public void deleteTexture(ResourceLocation p_147645_1_) {
        ITextureObject itextureobject = getTexture(p_147645_1_);

        if (itextureobject != null) { // 若存在则删除
            TextureUtil.deleteTexture(itextureobject.getGlTextureId()); // 释放GL资源
        }
    }

    // 资源重载时重新加载所有纹理
    @Override
    public void onResourceManagerReload(IResourceManager p_110549_1_) {
        cpw.mods.fml.common.ProgressManager.ProgressBar bar = cpw.mods.fml.common.ProgressManager.push(
                "Reloading Texture Manager",
                mapTextureObjects.keySet().size(), true); // 创建进度条
        Iterator<Entry<ResourceLocation, ITextureObject>> iterator = mapTextureObjects.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<ResourceLocation, ITextureObject> entry = iterator.next();
            bar.step(entry.getKey().toString()); // 更新进度条
            this.loadTexture(entry.getKey(), entry.getValue()); // 重新加载纹理
        }
        cpw.mods.fml.common.ProgressManager.pop(bar); // 完成进度条
    }
}