package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
/**
 * GUI标签组件，用于在屏幕上绘制带有可选背景框的文本标签。
 * 支持多行文本显示、居中对齐和边框样式配置。
 */
public class GuiLabel extends Gui {
    // 推荐命名: width
    /** 标签内容区域宽度（不包含内边距） */
    protected int width;

    // 推荐命名: height
    /** 标签内容区域高度（不包含内边距） */
    protected int height;

    // 推荐命名: xPosition
    /** 标签左上角X坐标 */
    public int xPosition;

    // 推荐命名: yPosition
    /** 标签左上角Y坐标 */
    public int yPosition;

    // 推荐命名: textLines
    /** 存储标签文本行的列表 */
    private ArrayList textLines;

    // 推荐命名: centered
    /** 是否居中显示文本 */
    private boolean centered;

    // 推荐命名: visible
    /** 标签是否可见 */
    public boolean visible;

    // 推荐命名: drawBackground
    /** 是否绘制背景和边框 */
    private boolean drawBackground;

    // 推荐命名: textColor
    /** 文本颜色（RGB格式） */
    private int textColor;

    // 推荐命名: backgroundColor
    /** 背景色颜色（ARGB格式） */
    private int backgroundColor;

    // 推荐命名: borderColorTopLeft
    /** 左上边框颜色（RGB格式） */
    private int borderColorTopLeft;

    // 推荐命名: borderColorBottomRight
    /** 右下边框颜色（RGB格式） */
    private int borderColorBottomRight;

    // 推荐命名: fontRenderer
    /** 字体渲染器实例 */
    private FontRenderer fontRenderer;

    // 推荐命名: padding
    /** 背景框内边距大小 */
    private int padding;

    /** 混淆标识符，用于反混淆处理 */
    private static final String __OBFID = "CL_00000671";

    /**
     * 绘制标签及其文本内容
     * @param mc Minecraft实例
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    public void drawLabel(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // 启用OpenGL混合功能实现透明度效果
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // 绘制背景框
            this.drawBackground(mc, mouseX, mouseY);

            // 计算文本垂直居中位置
            int k = this.yPosition + this.height / 2 + this.padding / 2;
            int l = k - this.textLines.size() * 10 / 2;

            // 逐行绘制文本
            for (int i1 = 0; i1 < this.textLines.size(); ++i1) {
                String line = (String) this.textLines.get(i1);
                if (this.centered) {
                    drawCenteredString(
                            this.fontRenderer,
                            line,
                            this.xPosition + this.width / 2,
                            l + i1 * 10,
                            this.textColor
                    );
                } else {
                    drawString(
                            this.fontRenderer,
                            line,
                            this.xPosition,
                            l + i1 * 10,
                            this.textColor
                    );
                }
            }
        }
    }

    /**
     * 绘制背景和边框（内部方法）
     * @param minecraft Minecraft实例
     * @param mouseX 鼠标X坐标（未使用）
     * @param mouseY 鼠标Y坐标（未使用）
     */
    protected void drawBackground(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.drawBackground) {
            // 计算带内边距的总尺寸
            int totalWidth = this.width + this.padding * 2;
            int totalHeight = this.height + this.padding * 2;

            // 计算实际绘制位置
            int drawX = this.xPosition - this.padding;
            int drawY = this.yPosition - this.padding;

            // 绘制背景矩形
            drawRect(drawX, drawY, drawX + totalWidth, drawY + totalHeight, this.backgroundColor);

            // 绘制边框线（顶部和底部）
            this.drawHorizontalLine(drawX, drawX + totalWidth, drawY, this.borderColorTopLeft);
            this.drawHorizontalLine(drawX, drawX + totalWidth, drawY + totalHeight, this.borderColorBottomRight);

            // 绘制边框线（左右两侧）
            this.drawVerticalLine(drawX, drawY, drawY + totalHeight, this.borderColorTopLeft);
            this.drawVerticalLine(drawX + totalWidth, drawY, drawY + totalHeight, this.borderColorBottomRight);
        }
    }
}