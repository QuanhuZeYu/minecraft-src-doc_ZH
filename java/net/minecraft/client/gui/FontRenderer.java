package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class FontRenderer implements IResourceManagerReloadListener {
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    // 存储Unicode字符页的资源位置数组

    /**
     * 数组记录default.png中所有字符的宽度
     */
    protected int[] charWidth = new int[256];

    /** 默认文本的高度（像素） */
    public int FONT_HEIGHT = 9;

    public Random fontRandom = new Random();

    /**
     * 存储/font目录下每个字形的起始/结束列（高位/低位字节）
     */
    protected byte[] glyphWidth = new byte[65536];

    /**
     * 定义16种标准聊天颜色及其16种较暗版本（用于阴影效果）的RGB三元组数组
     */
    private int[] colorCode = new int[32];

    protected final ResourceLocation locationFontTexture;
    // 字体纹理资源位置

    /**
     * 用于加载和设置字形纹理的渲染引擎
     */
    private final TextureManager renderEngine;

    /** 当前待绘制字符的X坐标 */
    protected float posX;

    /** 当前待绘制字符的Y坐标 */
    protected float posY;

    /**
     * 是否使用Unicode字体而非默认字体
     */
    private boolean unicodeFlag;

    /**
     * 是否在渲染字符串前应用Unicode双向算法
     */
    private boolean bidiFlag;

    /** 当前颜色的红色分量 */
    private float red;

    /** 当前颜色的蓝色分量 */
    private float blue;

    /** 当前颜色的绿色分量 */
    private float green;

    /** 当前颜色的透明度分量 */
    private float alpha;

    /** 当前文本的颜色值 */
    private int textColor;

    /** 是否激活"k"样式（随机颜色） */
    private boolean randomStyle;

    /** 是否激活"l"样式（粗体） */
    private boolean boldStyle;

    /** 是否激活"o"样式（斜体） */
    private boolean italicStyle;

    /** 是否激活"n"样式（下划线） */
    private boolean underlineStyle;

    /**
     * 是否激活"m"样式（删除线）
     */
    private boolean strikethroughStyle;

    private static final String __OBFID = "CL_00000660";
    // Obfuscation处理标识符

    /**
     * 字体渲染器初始化类，负责加载字体资源、预计算颜色代码和字形尺寸。
     *
     * @param gameSettings   游戏设置对象，包含渲染相关配置（如防色盲模式）
     * @param fontLocation   字体纹理资源位置
     * @param textureManager 纹理管理器实例，用于加载和管理游戏资源
     * @param useUnicode     是否启用Unicode字符集支持
     */
    public FontRenderer(GameSettings gameSettings, ResourceLocation fontLocation, TextureManager textureManager,
            boolean useUnicode) {
        this.locationFontTexture = fontLocation; // 字体纹理资源位置
        this.renderEngine = textureManager; // 渲染引擎引用
        this.unicodeFlag = useUnicode; // Unicode字体启用标志

        bindTexture(this.locationFontTexture); // 绑定字体纹理

        // 初始化32种颜色代码（16标准色+16阴影色）
        for (int i = 0; i < 32; ++i) {
            int baseColor = (i >> 3 & 1) * 85; // 基础颜色值（第3位）
            int redComponent = (i >> 2 & 1) * 170 + baseColor; // 红色分量（第2位）
            int greenComponent = (i >> 1 & 1) * 170 + baseColor; // 绿色分量（第1位）
            int blueComponent = (i >> 0 & 1) * 170 + baseColor; // 蓝色分量（第0位）
            /*
              index R       G       B       颜色类型    描述
              0     0       0       0       标准色      纯黑
              1     0       0       170     标准色      蓝色
              2     0       170     0       标准色      绿色
              3     0       170     170     标准色      青色
              4     170     0       0       标准色      红色
              5     170     0       170     标准色      品红
              6     170     170     0       标准色      黄色
              7     170     170     170     标准色      亮灰
              8     85      85      85      阴影色      深灰
              9     85      85      255     阴影色      亮蓝（阴影版）
              10    85      255     85      阴影色      亮绿（阴影版）
              11    85      255     255     阴影色      亮青（阴影版）
              12    255     85      85      阴影色      亮红（阴影版）
              13    255     85      255     阴影色      亮品红（阴影版）
              14    255     255     85      阴影色      亮黄（阴影版）
              15    255     255     255     阴影色      纯白（阴影版）
             */
            // 特殊处理索引6的颜色（修正红绿平衡）
            if (i == 6) {
                redComponent += 85;
            }

            // 应用Anaglyph滤镜（彩色盲友好模式）
            if (gameSettings.anaglyph) {
                int filteredRed = (redComponent * 30 + greenComponent * 59 + blueComponent * 11) / 100;
                int filteredGreen = (redComponent * 30 + greenComponent * 70) / 100;
                int filteredBlue = (redComponent * 30 + blueComponent * 70) / 100;

                redComponent = filteredRed;
                greenComponent = filteredGreen;
                blueComponent = filteredBlue;
            }

            // 处理阴影颜色（后16种颜色需要降低亮度）
            if (i >= 16) {
                redComponent /= 4;
                greenComponent /= 4;
                blueComponent /= 4;
            }

            // 组合RGB分量生成最终颜色代码
            this.colorCode[i] = ((redComponent & 0xFF) << 16) |
                    ((greenComponent & 0xFF) << 8) |
                    (blueComponent & 0xFF);
        }

        this.readGlyphSizes(); // 读取字形尺寸数据
    }

    public void onResourceManagerReload(IResourceManager rm) {
        this.readFontTexture();
    }

    private void readFontTexture() {
        BufferedImage image; // 存储字体纹理的图像对象

        try {
            image = ImageIO.read(getResourceInputStream(this.locationFontTexture)); // 加载字体纹理图像
        } catch (IOException e) {
            throw new RuntimeException(e); // 加载失败时抛出运行时异常
        }

        int imageWidth = image.getWidth(); // 图像总宽度
        int imageHeight = image.getHeight(); // 图像总高度
        int[] rgbData = new int[imageWidth * imageHeight]; // 存储所有像素的RGB值数组
        image.getRGB(0, 0, imageWidth, imageHeight, rgbData, 0, imageWidth); // 提取图像像素数据
        int numColumnsInBlock = imageHeight / 16; // 每个字符块的列数（垂直方向分成16份）
        int numRowsInBlock = imageWidth / 16; // 每个字符块的行数（水平方向分成16份）
        byte extraPadding = 1; // 字符宽度的额外填充量
        float scaleFactor = 8.0F / (float) numRowsInBlock; // 缩放因子用于计算实际像素位置
        int charIndex = 0;

        while (charIndex < 256) {
            int blockRow = charIndex % 16;
            int blockColumn = charIndex / 16;

            // 处理特殊字符（索引32对应空格的特殊处理）
            if (charIndex == 32) {
                this.charWidth[charIndex] = 3 + extraPadding;
            }

            int currentCheckRow = numRowsInBlock - 1;

            while (true) {
                if (currentCheckRow >= 0) {
                    int j = blockRow * numRowsInBlock + currentCheckRow;
                    boolean isFullyOpaque = true;

                    for (int pixelRow = 0; pixelRow < numColumnsInBlock && isFullyOpaque; ++pixelRow) {
                        int globalPixelIndex = (blockColumn * numRowsInBlock + pixelRow) * imageWidth;

                        if ((rgbData[j + globalPixelIndex] >> 24 & 255) != 0) {
                            isFullyOpaque = false;
                        }
                    }

                    if (isFullyOpaque) {
                        --currentCheckRow;
                        continue;
                    }
                }

                ++currentCheckRow;
                this.charWidth[charIndex] = (int) (0.5D + (double) ((float) currentCheckRow * scaleFactor))
                        + extraPadding;
                ++charIndex;
                break;
            }
        }
    }

    private void readGlyphSizes() {
        // 读取字形尺寸数据文件
        try {
            // 创建字形尺寸数据文件的输入流
            InputStream glyphDataStream = getResourceInputStream(
                    new ResourceLocation("font/glyph_sizes.bin") // 数据文件路径
            );

            // 将二进制数据直接读取到字形宽度数组
            glyphDataStream.read(this.glyphWidth);
        } catch (IOException e) { // 捕获输入输出异常
            // 将IO异常转换为运行时异常抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 渲染单个字符并返回其占用的宽度（像素）
     * 
     * @param posX   当前渲染的X轴起始位置（通常用于调整字符间距）
     * @param c      需要渲染的字符
     * @param shadow 是否渲染为阴影效果
     * @return 字符渲染后的宽度（像素值）
     */
    private float renderCharAtPos(int posX, char c, boolean shadow) {
        // ------------------------- 逻辑分解 -------------------------
        // 1. 处理空格字符（ASCII 32）
        if (c == 32) { // 空格字符的特殊处理
            return 4.0F; // 固定返回4像素宽度
        }

        // 2. 定义特殊字符集合（原字符串解码后包含西欧语言特殊字符）
        final String specialChars = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207"
                +
                "\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
                +
                "\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

        // 3. 判断当前字符是否属于特殊字符集合
        boolean isSpecialChar = (specialChars.indexOf(c) != -1);

        // 4. 若字符属于特殊字符集合且未启用Unicode渲染模式
        if (isSpecialChar && !this.unicodeFlag) {
            // 使用默认字体渲染（可能为位图字体）
            return this.renderDefaultChar(posX, shadow);
        }
        // 5. 其他情况（非特殊字符 或 启用了Unicode模式）
        else {
            // 使用Unicode字体渲染（可能为矢量字体）
            return this.renderUnicodeChar(c, shadow);
        }
    }

    protected float renderDefaultChar(int charCode, boolean hasShadow) {
        // 计算字符在默认字体图像中的列偏移量（基于16x16字符块）
        float charColumnOffset = (float) (charCode % 16 * 8);
        // 计算字符在默认字体图像中的行偏移量（基于16x16字符块）
        float charRowOffset = (float) (charCode / 16 * 8);

        // 阴影垂直偏移量（阴影模式下为1.0，无阴影为0.0）
        float shadowVerticalOffset = hasShadow ? 1.0F : 0.0F;

        bindTexture(this.locationFontTexture); // 绑定字体纹理

        // 当前字符的实际显示宽度（右侧预留1像素防重叠）
        float adjustedWidth = (float) this.charWidth[charCode] - 0.01F;

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        // 设置左下角顶点（阴影部分）
        GL11.glTexCoord2f(charColumnOffset / 128.0F, charRowOffset / 128.0F);
        GL11.glVertex3f(this.posX + shadowVerticalOffset, this.posY, 0.0F);

        // 设置左上角顶点（阴影部分）
        GL11.glTexCoord2f(charColumnOffset / 128.0F, (charRowOffset + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - shadowVerticalOffset, this.posY + 7.99F, 0.0F);

        // 设置右下角顶点（主体部分）
        GL11.glTexCoord2f((charColumnOffset + adjustedWidth - 1.0F) / 128.0F, charRowOffset / 128.0F);
        GL11.glVertex3f(this.posX + adjustedWidth - 1.0F + shadowVerticalOffset, this.posY, 0.0F);

        // 设置右上角顶点（主体部分）
        GL11.glTexCoord2f((charColumnOffset + adjustedWidth - 1.0F) / 128.0F, (charRowOffset + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + adjustedWidth - 1.0F - shadowVerticalOffset, this.posY + 7.99F, 0.0F);

        GL11.glEnd();

        // 返回字符的实际显示宽度
        return (float) this.charWidth[charCode];
    }

    private ResourceLocation getUnicodePageLocation(int pageNumber) {
        // 检查对应页码的Unicode字符页资源是否已加载
        if (unicodePageLocations[pageNumber] == null) {
            // 格式化为两位十六进制的资源路径
            String pagePath = String.format("textures/font/unicode_page_%02x.png", pageNumber);
            unicodePageLocations[pageNumber] = new ResourceLocation(pagePath);
        }

        return unicodePageLocations[pageNumber];
    }

    private void loadGlyphTexture(int glyphPageNumber) {
        // 绑定当前glyph页的Unicode字符页纹理
        bindTexture(getUnicodePageLocation(glyphPageNumber));
    }

    protected float renderUnicodeChar(char unicodeChar, boolean hasShadow) {
        // 如果字符宽度为0则不渲染（如空字符或不可见字符）
        if (this.glyphWidth[unicodeChar] == 0) {
            return 0.0F;
        }
        // 计算字符所属的Unicode页码（每页256字符）
        int pageNumber = unicodeChar / 256;
        this.loadGlyphTexture(pageNumber); // 加载对应页码的字符页纹理
        // 解析字符在字符页内的列和行索引（高位和低位字节）
        int columnIndex = this.glyphWidth[unicodeChar] >>> 4; // 右移4位取高位
        int rowIndex = this.glyphWidth[unicodeChar] & 15; // 取低4位
        // 计算字符的基础列位置（左边缘）
        float baseColumn = (float) columnIndex;
        // 计算字符的总高度（包括1像素边距）
        float totalHeight = (float) (rowIndex + 1);
        // 计算字符在字符页中的像素坐标
        float pageColumnPosition = (float) (unicodeChar % 16 * 16);
        float pageRowPosition = (float) ((unicodeChar & 255) / 16 * 16);
        // 计算字符的实际显示宽度（右侧预留2像素边距）
        float adjustedWidth = totalHeight - baseColumn - 0.02F;
        // 阴影垂直偏移量
        float shadowVerticalOffset = hasShadow ? 1.0F : 0.0F;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        // 左下角顶点（阴影部分）
        GL11.glTexCoord2f(pageColumnPosition / 256.0F, pageRowPosition / 256.0F);
        GL11.glVertex3f(this.posX + shadowVerticalOffset, this.posY, 0.0F);
        // 左上角顶点（阴影部分）
        GL11.glTexCoord2f(pageColumnPosition / 256.0F, (pageRowPosition + 15.98F) / 256.0F);
        GL11.glVertex3f(this.posX - shadowVerticalOffset, this.posY + 7.99F, 0.0F);
        // 右下角顶点（主体部分）
        GL11.glTexCoord2f((pageColumnPosition + adjustedWidth) / 256.0F, pageRowPosition / 256.0F);
        GL11.glVertex3f(this.posX + adjustedWidth / 2.0F + shadowVerticalOffset, this.posY, 0.0F);
        // 右上角顶点（主体部分）
        GL11.glTexCoord2f((pageColumnPosition + adjustedWidth) / 256.0F, (pageRowPosition + 15.98F) / 256.0F);
        GL11.glVertex3f(this.posX + adjustedWidth / 2.0F - shadowVerticalOffset, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        // 返回实际显示宽度（包含1像素边距）
        return (totalHeight - baseColumn) / 2.0F + 1.0F;
    }

    /**
     * Draws the specified string with a shadow.
     */
    public int drawStringWithShadow(String text, int x, int y, int color) {
        return this.drawString(text, x, y, color, true);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String text, int x, int y, int color) {
        return this.drawString(text, x, y, color, false);
    }

    /**
     * 在指定坐标处绘制带阴影或不带阴影的字符串。
     * <p>
     * 此方法首先启用 alpha 通道透明处理，重置文本样式后：
     * - 若需要阴影，先在偏移位置（右下角）渲染阴影层
     * - 再在原始位置渲染主体文本
     * - 最终返回两者的最大宽度以确保完整覆盖区域
     *
     * @param text       要渲染的字符串内容
     * @param x          字符串左下角 X 坐标
     * @param y          字符串左下角 Y 坐标
     * @param color      文本颜色值（RGB 或 ARGB 格式）
     * @param dropShadow 是否启用阴影效果
     * @return 绘制完成的字符串区域的最大宽度
     * @see #enableAlpha()
     * @see #resetStyles()
     * @see #renderString(String, int, int, int, boolean)
     */
    public int drawString(String text, int x, int y, int color, boolean dropShadow) {
        enableAlpha(); // 启用透明度混合模式
        this.resetStyles(); // 重置所有文本样式标志

        int resultWidth = 0;

        if (dropShadow) {
            // 先渲染阴影层（右下角偏移1像素）
            int shadowWidth = this.renderString(text, x + 1, y + 1, color, true);
            // 再渲染主体内容（原始位置）
            int mainWidth = this.renderString(text, x, y, color, false);
            // 取两者的最大值作为最终宽度（防止阴影溢出）
            resultWidth = Math.max(shadowWidth, mainWidth);
        } else {
            // 无阴影时直接渲染主体内容
            resultWidth = this.renderString(text, x, y, color, false);
        }

        return resultWidth;
    }

    /**
     * Apply Unicode Bidirectional Algorithm to string and return a new possibly
     * reordered string for visual rendering.
     */
    private String bidiReorder(String p_147647_1_) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_147647_1_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException arabicshapingexception) {
            return p_147647_1_;
        }
    }

    private void resetStyles() {
        // 重置随机样式标志
        this.randomStyle = false;
        // 重置粗体样式标志
        this.boldStyle = false;
        // 重置斜体样式标志
        this.italicStyle = false;
        // 重置下划线样式标志
        this.underlineStyle = false;
        // 重置删除线样式标志
        this.strikethroughStyle = false;
    }

    /**
     * 在当前位置渲染单行字符串，并更新posX坐标（逐字符处理格式代码和渲染）
     */
    private void renderStringAtPos(String text, boolean isShadow) {
        for (int charIndex = 0; charIndex < text.length(); ++charIndex) {
            char currentChar = text.charAt(charIndex);
            int formatCodeIndex; // 格式代码标识符（如颜色/样式代码的索引）
            int mappedCharIndex; // 字符在字体映射表中的索引

            // 处理格式代码（以 § 开头，例如 §a 表示绿色）
            if (currentChar == 167 /* § 符号的ASCII值 */ && charIndex + 1 < text.length()) {
                // 获取格式代码的类型（小写字母或符号在预设字符串中的位置）
                formatCodeIndex = "0123456789abcdefklmnor".indexOf( // 总共21位 颜色代码16位 控制符代码5位
                        Character.toLowerCase(text.charAt(charIndex + 1)));

                // 处理颜色代码（0-9,a-f）
                if (formatCodeIndex < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    // 非法颜色代码默认重置为白色（索引15）
                    if (formatCodeIndex < 0 || formatCodeIndex > 15) {
                        formatCodeIndex = 15;
                    }
                    // 阴影模式下使用暗色版本（索引+16）
                    if (isShadow) {
                        formatCodeIndex += 16;
                    }

                    int colorValue = this.colorCode[formatCodeIndex];
                    this.textColor = colorValue;
                    setColor(
                            (float) (colorValue >> 16) / 255.0F, // R
                            (float) (colorValue >> 8 & 255) / 255.0F, // G
                            (float) (colorValue & 255) / 255.0F, // B
                            this.alpha);
                }
                // 处理样式代码（k=随机，l=粗体，m=删除线等）
                else if (formatCodeIndex == 16) {
                    this.randomStyle = true;
                } else if (formatCodeIndex == 17) {
                    this.boldStyle = true;
                } else if (formatCodeIndex == 18) {
                    this.strikethroughStyle = true;
                } else if (formatCodeIndex == 19) {
                    this.underlineStyle = true;
                } else if (formatCodeIndex == 20) {
                    this.italicStyle = true;
                }
                // 重置所有样式（r）
                else if (formatCodeIndex == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.blue, this.green, this.alpha);
                }
                ++charIndex; // 跳过已处理的格式代码字符（例如 § 后的字母）
            }
            // 处理普通字符渲染
            else {
                // 查找字符在字体映射表中的位置（支持特殊符号）
                mappedCharIndex = ("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5" +
                        "\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000" +
                        "\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]" +
                        "^_`abcdefghijklmnopqrstuvwxyz{|}~" +
                        "\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec" +
                        "\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3" +
                        "\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd" +
                        "\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551" +
                        "\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554" +
                        "\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a" +
                        "\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4" +
                        "\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7" +
                        "\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000")
                        .indexOf(currentChar);
                // 随机样式：选择相同宽度的随机字符
                if (this.randomStyle && mappedCharIndex != -1) {
                    int randomCharIndex;
                    do {
                        randomCharIndex = this.fontRandom.nextInt(this.charWidth.length);
                    } while (this.charWidth[mappedCharIndex] != this.charWidth[randomCharIndex]);
                    mappedCharIndex = randomCharIndex;
                }
                // 调整阴影位置（Unicode字体缩小一半）
                float scaleFactor = this.unicodeFlag ? 0.5F : 1.0F;
                boolean isUnsupportedChar = (currentChar == 0 || mappedCharIndex == -1 || this.unicodeFlag) && isShadow;
                if (isUnsupportedChar) {
                    this.posX -= scaleFactor;
                    this.posY -= scaleFactor;
                }
                // 渲染字符并获取宽度
                float charWidth = this.renderCharAtPos(mappedCharIndex, currentChar, this.italicStyle);

                // 还原阴影位置调整
                if (isUnsupportedChar) {
                    this.posX += scaleFactor;
                    this.posY += scaleFactor;
                }
                // 粗体样式：向右重复渲染一次
                if (this.boldStyle) {
                    this.posX += scaleFactor;

                    if (isUnsupportedChar) {
                        this.posX -= scaleFactor;
                        this.posY -= scaleFactor;
                    }
                    this.renderCharAtPos(mappedCharIndex, currentChar, this.italicStyle);
                    this.posX -= scaleFactor;

                    if (isUnsupportedChar) {
                        this.posX += scaleFactor;
                        this.posY += scaleFactor;
                    }
                    ++charWidth; // 粗体总宽度+1
                }
                doDraw(charWidth);
            }
        }
    }

    /**
     * 控制符追加绘制，例如: 删除线 下划线
     * @param charWidth 字符宽度
     */
    protected void doDraw(float charWidth) {
        {
            {
                Tessellator tessellator;

                if (this.strikethroughStyle) {
                    tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    tessellator.addVertex((double) this.posX, (double) (this.posY + (float) (this.FONT_HEIGHT / 2)),
                            0.0D);
                    tessellator.addVertex((double) (this.posX + charWidth),
                            (double) (this.posY + (float) (this.FONT_HEIGHT / 2)), 0.0D);
                    tessellator.addVertex((double) (this.posX + charWidth),
                            (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.addVertex((double) this.posX,
                            (double) (this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                if (this.underlineStyle) {
                    tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    int l = this.underlineStyle ? -1 : 0;
                    tessellator.addVertex((double) (this.posX + (float) l),
                            (double) (this.posY + (float) this.FONT_HEIGHT), 0.0D);
                    tessellator.addVertex((double) (this.posX + charWidth), (double) (this.posY + (float) this.FONT_HEIGHT),
                            0.0D);
                    tessellator.addVertex((double) (this.posX + charWidth),
                            (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.addVertex((double) (this.posX + (float) l),
                            (double) (this.posY + (float) this.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }

                this.posX += (float) ((int) charWidth);
            }
        }
    }

    /**
     *根据bidiflag渲染左右对齐的字符串
     */
    private int renderStringAligned(String string, int x, int y, int maxWidth, int color, boolean dropShadow) {
        if (this.bidiFlag) {
            int i1 = this.getStringWidth(this.bidiReorder(string));
            x = x + maxWidth - i1;
        }

        return this.renderString(string, x, y, color, dropShadow);
    }

    /**
     * 渲染单行字符串，设置OpenGL颜色、当前位置，并调用实际渲染方法
     * 
     * @param text      待渲染的字符串
     * @param startX    渲染起始X坐标
     * @param startY    渲染起始Y坐标
     * @param colorCode 颜色编码（ARGB格式）
     * @param isShadow  是否为阴影渲染（降低亮度）
     * @return 渲染后的X坐标终点位置
     */
    private int renderString(String text, int startX, int startY, int colorCode, boolean isShadow) {
        if (text == null) {
            return 0;
        }
        // 处理双向文本（如阿拉伯语右向左排列）
        if (this.bidiFlag) {
            text = this.bidiReorder(text);
        }
        // 确保颜色包含Alpha通道（若未设置，则默认不透明）
        if ((colorCode & 0xFF000000) == 0) {
            colorCode |= 0xFF000000; // 设置Alpha为255（完全不透明）
        }
        // 阴影处理：降低颜色亮度（通过右移2位减少RGB分量）
        if (isShadow) {
            colorCode = (colorCode & 0x00FCFCFC) >> 2 | (colorCode & 0xFF000000);
        }
        // 从ARGB颜色编码中提取各通道并归一化到[0,1]
        float alpha = (float) ((colorCode >> 24) & 0xFF) / 255.0F;
        float red = (float) ((colorCode >> 16) & 0xFF) / 255.0F;
        float green = (float) ((colorCode >> 8) & 0xFF) / 255.0F;
        float blue = (float) ((colorCode) & 0xFF) / 255.0F;
        // 设置OpenGL颜色和起始位置
        setColor(red, green, blue, alpha);
        this.posX = (float) startX;
        this.posY = (float) startY;
        // 执行实际渲染
        this.renderStringAtPos(text, isShadow);

        return (int) this.posX;
    }

    /**
     * 计算字符串在当前字体设置下的总显示宽度。
     * <p>
     * 此方法会遍历字符串中的每个字符，累加其显示宽度。特别处理控制字符（如字体格式控制符），
     * 并根据控制字符类型调整后续字符的宽度计算规则。
     *
     * @param inputText 要计算宽度的目标字符串，可能为 {@code null}
     * @return 字符串的总显示宽度（单位：像素），若输入为 {@code null} 则返回 0
     * @see #getCharWidth(char)
     */
    public int getStringWidth(String inputText) {
        // 如果输入字符串为空，直接返回0宽度
        if (inputText == null) {
            return 0;
        }

        int totalWidth = 0; // 累计总宽度
        boolean controlCharacterActive = false; // 控制字符生效标记

        for (int currentCharIndex = 0; currentCharIndex < inputText.length(); ++currentCharIndex) {
            char currentChar = inputText.charAt(currentCharIndex);
            int charWidth = this.getCharWidth(currentChar);

            // 处理控制字符的特殊逻辑
            if (charWidth < 0 && currentCharIndex < inputText.length() - 1) {
                ++currentCharIndex; // 跳过下一个字符（控制字符后的跟随字符）
                currentChar = inputText.charAt(currentCharIndex);

                // 判断控制字符类型
                if (currentChar != 'l' && currentChar != 'r') {
                    if (currentChar == 'C' || currentChar == 'R') {
                        controlCharacterActive = false;
                    }
                } else {
                    controlCharacterActive = true;
                }

                charWidth = 0; // 控制字符自身不计入宽度
            }

            totalWidth += charWidth;

            // 如果处于控制字符生效状态且当前字符有宽度，增加1像素间距
            if (controlCharacterActive && charWidth > 0) {
                totalWidth += 1;
            }
        }

        return totalWidth;
    }

    /**
     * 获取指定字符在当前渲染上下文中的实际显示宽度。
     *
     * @param character 要查询宽度的字符
     * @return 字符的显示宽度，特殊控制字符返回-1，空格返回4，其余字符根据字体定义返回实际像素宽度
     */
    public int getCharWidth(char character) {
        // 处理特殊控制字符（ASCII 167，对应^L/LF控制符）
        if (character == '\u0000') { // \u0000 对应 C0 控制字符中的 NULL（原文代码使用167可能是笔误？建议核实）
            return -1;
        } else if (character == ' ') { // 空格直接返回固定宽度
            return 4;
        } else {
            // 检查字符是否在默认字体支持范围内
            String defaultFontChars = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207"
                    + "\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
                    + "\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
            int defaultCharIndex = defaultFontChars.indexOf(character);

            if (character > 0 && defaultCharIndex != -1 && !this.unicodeFlag) {
                return this.charWidth[defaultCharIndex];
            } else if (this.glyphWidth[character] != 0) {
                // 解析glyphWidth中的高位和低位字节
                int highBits = this.glyphWidth[character] >>> 4; // 右移4位取高位
                int lowBits = this.glyphWidth[character] & 15; // 取低4位

                // 调整行索引不超过15
                if (lowBits > 7) {
                    lowBits = 15;
                    highBits = 0;
                }

                // 计算实际显示宽度
                return (lowBits - highBits) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String inputText, int maxWidth) {
        return this.trimStringToWidth(inputText, maxWidth, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String inputText, int maxWidth, boolean reverseOrder) {
        StringBuilder resultBuilder = new StringBuilder();
        int currentWidth = 0; // 累计当前行已使用的宽度
        int startIndex = reverseOrder ? inputText.length() - 1 : 0; // 起始索引（反转时从末尾开始）
        int stepDirection = reverseOrder ? -1 : 1; // 遍历步长（反转时逆序访问）
        boolean controlCharActive = false; // 控制字符生效标记
        boolean lineBreakControl = false; // 换行控制标记

        for (int currentIndex = startIndex; currentIndex >= 0 && currentIndex < inputText.length()
                && currentWidth < maxWidth; currentIndex += stepDirection) {
            char currentChar = inputText.charAt(currentIndex);
            int charWidth = this.getCharWidth(currentChar);

            // 处理控制字符逻辑
            if (controlCharActive) {
                controlCharActive = false;

                // 检查控制字符类型
                if (currentChar != 'l' && currentChar != 'r') {
                    if (currentChar == 'C' || currentChar == 'R') {
                        lineBreakControl = false;
                    }
                } else {
                    lineBreakControl = true;
                }
            } else if (charWidth < 0) {
                // 遇到无效宽度字符，激活控制字符处理
                controlCharActive = true;
            } else {
                currentWidth += charWidth;

                // 如果处于换行控制状态且当前字符有效，增加间隔
                if (lineBreakControl) {
                    currentWidth += 1;
                }
            }

            // 超过最大宽度时停止收集
            if (currentWidth > maxWidth) {
                break;
            }

            // 根据反转标志插入字符到合适位置
            if (reverseOrder) {
                resultBuilder.insert(0, currentChar);
            } else {
                resultBuilder.append(currentChar);
            }
        }

        return resultBuilder.toString();
    }

    /**
     * Remove all newline characters from the end of the string
     */
    private String trimStringNewline(String s) {
        while (s != null && s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    /**
     * Splits and draws a String with wordwrap (maximum length is parameter k)
     */
    public void drawSplitString(String inputText, int xPosition, int yPosition, int maxLineLength, int textColor) {
        // 重置所有文本样式标志
        this.resetStyles();

        // 设置当前文本颜色
        this.textColor = textColor;

        // 移除字符串首尾的多余换行符
        String trimmedText = this.trimStringNewline(inputText);

        // 执行实际的换行绘制操作
        this.renderSplitString(trimmedText, xPosition, yPosition, maxLineLength, false);
    }

    /**
     *执行使用WordWrap渲染多行字符串的实际工作以及
     *较深的滴影颜色如果是国旗
     * 放
     */
    private void renderSplitString(String str, int x, int y, int maxWidth, boolean addShadow) {
        List list = this.listFormattedStringToWidth(str, maxWidth);

        for (Iterator iterator = list.iterator(); iterator.hasNext(); y += this.FONT_HEIGHT) {
            String s1 = (String) iterator.next();
            this.renderStringAligned(s1, x, y, maxWidth, this.textColor, addShadow);
        }
    }

    /**
     * 计算换行后的总行数并乘以字体高度得到总尺寸
     */
    public int splitStringWidth(String inputText, int maxLineWidth) {
        // 计算换行后的总行数并乘以字体高度得到总尺寸
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(inputText, maxLineWidth).size();
    }

    /**
     * Set unicodeFlag controlling whether strings should be rendered with Unicode
     * fonts instead of the default.png
     * font.
     */
    public void setUnicodeFlag(boolean p_78264_1_) {
        this.unicodeFlag = p_78264_1_;
    }

    /**
     * Get unicodeFlag controlling whether strings should be rendered with Unicode
     * fonts instead of the default.png
     * font.
     */
    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    /**
     * Set bidiFlag to control if the Unicode Bidirectional Algorithm should be run
     * before rendering any string.
     */
    public void setBidiFlag(boolean p_78275_1_) {
        this.bidiFlag = p_78275_1_;
    }

    /**
     * 将格式化后的多行文本按换行符分割为列表
     */
    public List<String> listFormattedStringToWidth(String inputText, int wrapWidth) {
        // 将格式化后的多行文本按换行符分割为列表
        return Arrays.asList(this.wrapFormattedStringToWidth(inputText, wrapWidth).split("\n"));
    }

    /**
     * Inserts newline and formatting into a string to wrap it within the specified
     * width.
     */
    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int j = this.sizeStringToWidth(str, wrapWidth);

        if (str.length() <= j) {
            return str;
        } else {
            String s1 = str.substring(0, j);
            char c0 = str.charAt(j);
            boolean flag = c0 == 32 || c0 == 10;
            String s2 = getFormatFromString(s1) + str.substring(j + (flag ? 1 : 0));
            return s1 + "\n" + this.wrapFormattedStringToWidth(s2, wrapWidth);
        }
    }

    /**
     * Determines how many characters from the string will fit into the specified
     * width.
     */
    private int sizeStringToWidth(String str, int wrapWidth) {
        int j = str.length();
        int k = 0;
        int l = 0;
        int i1 = -1;

        for (boolean flag = false; l < j; ++l) {
            char c0 = str.charAt(l);

            switch (c0) {
                case 10:
                    --l;
                    break;
                case 167:
                    if (l < j - 1) {
                        ++l;
                        char c1 = str.charAt(l);

                        if (c1 != 108 && c1 != 76) {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }

                    break;
                case 32:
                    i1 = l;
                default:
                    k += this.getCharWidth(c0);

                    if (flag) {
                        ++k;
                    }
            }

            if (c0 == 10) {
                ++l;
                i1 = l;
                break;
            }

            if (k > wrapWidth) {
                break;
            }
        }

        return l != j && i1 != -1 && i1 < l ? i1 : l;
    }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
    private static boolean isFormatColor(char colorChar) {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102
                || colorChar >= 65 && colorChar <= 70;
    }

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
     */
    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114
                || formatChar == 82;
    }

    /**
     * Digests a string for nonprinting formatting characters then returns a string
     * containing only that formatting.
     */
    private static String getFormatFromString(String p_78282_0_) {
        String s1 = "";
        int i = -1;
        int j = p_78282_0_.length();

        while ((i = p_78282_0_.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = p_78282_0_.charAt(i + 1);

                if (isFormatColor(c0)) {
                    s1 = "\u00a7" + c0;
                } else if (isFormatSpecial(c0)) {
                    s1 = s1 + "\u00a7" + c0;
                }
            }
        }

        return s1;
    }

    /**
     * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should be
     * run before rendering any string
     */
    public boolean getBidiFlag() {
        return this.bidiFlag;
    }

    protected void setColor(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    protected void enableAlpha() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    protected void bindTexture(ResourceLocation location) {
        renderEngine.bindTexture(location);
    }

    protected InputStream getResourceInputStream(ResourceLocation location) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }
}