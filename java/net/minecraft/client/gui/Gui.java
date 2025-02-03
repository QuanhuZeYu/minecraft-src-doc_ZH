package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Gui 类是 Minecraft 客户端图形用户界面的基础绘制工具类。
 * 它封装了常见的 2D 渲染操作，包括几何图形绘制、文字渲染和纹理贴图等功能。
 * 所有方法均基于 OpenGL 实现，并通过 Tessellator 进行顶点批量处理以提高性能。
 * 注意：此类仅在客户端生效（@SideOnly(Side.CLIENT)）。
 */
@SideOnly(Side.CLIENT)
public class Gui {
    // 预定义的常用纹理资源路径
    public static final ResourceLocation optionsBackground = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation statIcons = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation icons = new ResourceLocation("textures/gui/icons.png");

    // 当前绘制操作的 Z 轴层级，用于控制渲染深度
    protected float zLevel;

    // 混淆标识符，用于反混淆工具，开发者无需关心
    private static final String __OBFID = "CL_00000662";

    /**
     * 绘制水平线段
     * @param startX 起始 X 坐标
     * @param endX   结束 X 坐标（自动处理反向）
     * @param y      固定的 Y 坐标
     * @param color  颜色值（ARGB 格式）
     */
    protected void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX) {
            int i1 = startX;
            startX = endX;
            endX = i1;
        }
        drawRect(startX, y, endX + 1, y + 1, color);
    }

    /**
     * 绘制垂直线段
     * @param x       固定的 X 坐标
     * @param startY  起始 Y 坐标
     * @param endY    结束 Y 坐标（自动处理反向）
     * @param color   颜色值（ARGB 格式）
     */
    protected void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY) {
            int i1 = startY;
            startY = endY;
            endY = i1;
        }
        drawRect(x, startY + 1, x + 1, endY, color);
    }

    /**
     * 绘制实色矩形（自动处理坐标反向）
     * @param left   左侧 X 坐标
     * @param top    顶部 Y 坐标
     * @param right  右侧 X 坐标
     * @param bottom 底部 Y 坐标
     * @param color  颜色值（ARGB 格式）
     */
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        // 确保坐标顺序正确
        if (left < right) {
            int temp = left;
            left = right;
            right = temp;
        }
        if (top < bottom) {
            int temp = top;
            top = bottom;
            bottom = temp;
        }

        // 解析 ARGB 颜色分量
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);          // 启用混合
        GL11.glDisable(GL11.GL_TEXTURE_2D);    // 禁用纹理
        OpenGlHelper.glBlendFunc(770, 771, 1, 0); // 设置混合模式（SRC_ALPHA, ONE_MINUS_SRC_ALPHA）
        GL11.glColor4f(red, green, blue, alpha);

        // 通过 Tessellator 绘制四边形
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) left, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) top, 0.0D);
        tessellator.addVertex((double) left, (double) top, 0.0D);
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);     // 恢复纹理
        GL11.glDisable(GL11.GL_BLEND);         // 关闭混合
    }

    /**
     * 绘制垂直渐变矩形
     * @param left       左侧 X 坐标
     * @param top        顶部 Y 坐标
     * @param right      右侧 X 坐标
     * @param bottom     底部 Y 坐标
     * @param startColor 起始颜色（顶部，ARGB 格式）
     * @param endColor   结束颜色（底部，ARGB 格式）
     */
    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        // 解析起始颜色分量
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        // 解析结束颜色分量
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);          // 启用混合
        GL11.glDisable(GL11.GL_ALPHA_TEST);    // 禁用 Alpha 测试
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);     // 启用平滑着色

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(startRed, startGreen, startBlue, startAlpha);
        tessellator.addVertex((double) right, (double) top, (double) this.zLevel);
        tessellator.addVertex((double) left, (double) top, (double) this.zLevel);
        tessellator.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
        tessellator.addVertex((double) left, (double) bottom, (double) this.zLevel);
        tessellator.addVertex((double) right, (double) bottom, (double) this.zLevel);
        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);       // 恢复默认着色模型
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * 绘制居中文字
     * @param fontRendererIn 字体渲染器
     * @param text           文本内容
     * @param x              中心 X 坐标
     * @param y              文字基线 Y 坐标
     * @param color          颜色值（ARGB 格式）
     */
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
    }

    /**
     * 绘制左对齐文字
     * @param fontRendererIn 字体渲染器
     * @param text           文本内容
     * @param x              起始 X 坐标
     * @param y              文字基线 Y 坐标
     * @param color          颜色值（ARGB 格式）
     */
    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawStringWithShadow(text, x, y, color);
    }

    /**
     * 绘制纹理矩形（基于纹理坐标系）
     * @param x         屏幕 X 坐标
     * @param y         屏幕 Y 坐标
     * @param textureX  纹理 X 坐标（像素）
     * @param textureY  纹理 Y 坐标（像素）
     * @param width     绘制宽度
     * @param height    绘制高度
     */
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        float texelSize = 0.00390625F; // 1/256，假设纹理大小为 256x256
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, textureX * texelSize, (textureY + height) * texelSize);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, (textureX + width) * texelSize, (textureY + height) * texelSize);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, (textureX + width) * texelSize, textureY * texelSize);
        tessellator.addVertexWithUV(x, y, this.zLevel, textureX * texelSize, textureY * texelSize);
        tessellator.draw();
    }

    /**
     * 通过 IIcon 绘制纹理矩形（支持任意纹理尺寸）
     * @param x      屏幕 X 坐标
     * @param y      屏幕 Y 坐标
     * @param icon   纹理图标对象
     * @param width  绘制宽度
     * @param height 绘制高度
     */
    public void drawTexturedModelRectFromIcon(int x, int y, IIcon icon, int width, int height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + width, y, this.zLevel, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(x, y, this.zLevel, icon.getMinU(), icon.getMinV());
        tessellator.draw();
    }

    /**
     * 绘制自定义尺寸的纹理矩形（支持非 256x256 纹理）
     * @param x           屏幕 X 坐标
     * @param y           屏幕 Y 坐标
     * @param u           纹理 U 坐标（像素）
     * @param v           纹理 V 坐标（像素）
     * @param width       绘制宽度
     * @param height      绘制高度
     * @param textureWidth  纹理总宽度（用于 UV 计算）
     * @param textureHeight 纹理总高度（用于 UV 计算）
     */
    public static void func_146110_a(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
        float uScale = 1.0F / textureWidth;
        float vScale = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, u * uScale, (v + height) * vScale);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, (u + width) * uScale, (v + height) * vScale);
        tessellator.addVertexWithUV(x + width, y, 0.0D, (u + width) * uScale, v * vScale);
        tessellator.addVertexWithUV(x, y, 0.0D, u * uScale, v * vScale);
        tessellator.draw();
    }

    /**
     * 绘制可平铺的纹理矩形（支持部分纹理重复）
     * @param x           屏幕 X 坐标
     * @param y           屏幕 Y 坐标
     * @param u           纹理起始 U 坐标（像素）
     * @param v           纹理起始 V 坐标（像素）
     * @param uWidth      使用的纹理宽度（像素）
     * @param vHeight     使用的纹理高度（像素）
     * @param width       绘制宽度
     * @param height      绘制高度
     * @param tileWidth   纹理平铺总宽度（用于 UV 缩放）
     * @param tileHeight  纹理平铺总高度（用于 UV 缩放）
     */
    public static void func_152125_a(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float uScale = 1.0F / tileWidth;
        float vScale = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, u * uScale, (v + vHeight) * vScale);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, (u + uWidth) * uScale, (v + vHeight) * vScale);
        tessellator.addVertexWithUV(x + width, y, 0.0D, (u + uWidth) * uScale, v * vScale);
        tessellator.addVertexWithUV(x, y, 0.0D, u * uScale, v * vScale);
        tessellator.draw();
    }
}