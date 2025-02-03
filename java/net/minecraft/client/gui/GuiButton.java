package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButton extends Gui {
    // 按钮纹理资源的位置
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    // 按钮宽度（像素）
    public int width;
    // 按钮高度（像素）
    public int height;
    // 按钮的x坐标位置
    public int xPosition;
    // 按钮的y坐标位置
    public int yPosition;
    // 显示在按钮上的文本
    public String displayString;
    // 按钮的唯一标识符
    public int id;
    // 是否启用该按钮（true为启用，false为禁用）
    public boolean enabled;
    // 是否显示该按钮（false则隐藏）
    public boolean visible;
    // 内部状态标志（可能用于鼠标悬停检测）
    protected boolean field_146123_n;
    // 包内标识符（防止混淆，不影响功能）
    private static final String __OBFID = "CL_00000668";
    // 前景色打包值（包含颜色和透明度信息）
    public int packedFGColour;

    /**
     * 构造函数：创建一个按钮
     * @param buttonId 按钮的唯一标识符
     * @param x x坐标位置
     * @param y y坐标位置
     * @param buttonText 显示在按钮上的文本
     */
    public GuiButton(int buttonId, int x, int y, String buttonText) {
        // 调用另一个构造函数，并设置默认宽度为200，高度为20
        this(buttonId, x, y, 200, 20, buttonText);
    }

    /**
     * 构造函数：创建一个按钮
     * @param id 按钮状态名称（可能用于内部处理）
     * @param xPos 按钮的唯一标识符
     * @param yPos y坐标位置
     * @param width 按钮宽度
     * @param height 按钮高度
     * @param displayStr 显示在按钮上的文本
     */
    public GuiButton(int id, int xPos, int yPos, int width, int height, String displayStr) {
        // 初始化按钮宽度和高度（初始值为200和20）
        this.width = 200;
        this.height = 20;
        // 启用状态设为true
        this.enabled = true;
        // 可见性设为true
        this.visible = true;
        // 设置按钮标识符
        this.id = id;
        // 设置x坐标位置（这里可能参数顺序有问题，id被赋值给xPosition）
        this.xPosition = xPos;
        // 设置y坐标位置
        this.yPosition = yPos;
        // 设置宽度
        this.width = width;
        // 设置高度
        this.height = height;
        // 设置显示文本
        this.displayString = displayStr;
    }

    /**
     * 获取按钮的悬停状态
     * @param mouseOver 鼠标是否悬停在按钮上
     * @return 0（禁用）、1（正常）或2（悬停）
     */
    public int getHoverState(boolean mouseOver) {
        byte b0 = 1;

        if (!this.enabled) {
            // 如果按钮被禁用，返回0
            b0 = 0;
        } else if (mouseOver) {
            // 如果鼠标悬停在按钮上，返回2
            b0 = 2;
        }

        return b0;
    }

    /**
     * 将按钮绘制到屏幕上
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // 获取字体渲染器
            FontRenderer fontrenderer = mc.fontRenderer;
            // 绑定按钮纹理资源
            mc.getTextureManager().bindTexture(buttonTextures);
            // 设置颜色为白色（不透明）
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            // 检测鼠标是否悬停在按钮上
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            // 获取悬停状态
            int k = this.getHoverState(this.field_146123_n);
            // 启用混合模式
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // 绘制按钮的左侧部分
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
            // 绘制按钮的右侧部分
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            // 处理鼠标拖动事件
            this.mouseDragged(mc, mouseX, mouseY);
            // 初始化前景颜色为默认值（14737632对应#D9D9D9）
            int l = 14737632;

            if (packedFGColour != 0) {
                // 如果有自定义前景颜色，则使用该颜色
                l = packedFGColour;
            } else if (!this.enabled) {
                // 如果按钮被禁用，前景色设为灰色（10526880对应#A9A9A9）
                l = 10526880;
            } else if (this.field_146123_n) {
                // 如果鼠标悬停在按钮上，前景色设为红色（16777120对应#FF0000）
                l = 16777120;
            }

            // 在按钮中心绘制文本
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        }
    }

    /**
     * 处理鼠标拖动事件（默认为空实现）
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {}

    /**
     * 处理鼠标释放事件（默认为空实现）
     */
    public void mouseReleased(int mouseX, int mouseY) {}

    /**
     * 检测鼠标是否点击了按钮
     * @param mc Minecraft实例
     * @param mouseX 鼠标x坐标
     * @param mouseY 鼠标y坐标
     * @return 如果鼠标点击在按钮上则返回true，否则返回false
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    /**
     * 检测鼠标是否悬停在按钮上（内部方法）
     * @return 如果鼠标悬停则返回true
     */
    public boolean func_146115_a() {
        return this.field_146123_n;
    }

    /**
     * 处理鼠标移动事件（默认为空实现）
     */
    public void func_146111_b(int mouseX, int mouseY) {}

    /**
     * 播放按钮点击音效
     */
    public void playClickSound(SoundHandler soundHandlerIn) {
        // 播放按钮点击声
        soundHandlerIn.playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }

    /**
     * 获取按钮宽度
     * @return 按钮的宽度
     */
    public int getButtonWidth() {
        return this.width;
    }

    /**
     * 获取按钮高度（内部方法）
     * @return 按钮的高度
     */
    public int func_154310_c() {
        return this.height;
    }
}
