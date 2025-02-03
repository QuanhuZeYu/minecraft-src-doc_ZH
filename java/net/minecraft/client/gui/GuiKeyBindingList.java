package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.ArrayUtils;

@SideOnly(Side.CLIENT)
public class GuiKeyBindingList extends GuiListExtended
{
    private final GuiControls parentGuiControls; // 建议命名为：parentGuiControls，表示父控件界面
    private final Minecraft mc; // Minecraft实例
    private final GuiListExtended.IGuiListEntry[] keyBindingEntries; // 建议命名为：keyBindingEntries，表示键绑定条目列表
    private int maxKeyBindingTextWidth = 0; // 建议命名为：maxKeyBindingTextWidth，表示键绑定描述文本的最大宽度
    private static final String __OBFID = "CL_00000732";

    /**
     * 构造函数，初始化键绑定列表。
     * @param guiControls 父控件界面（GuiControls）
     * @param minecraft Minecraft实例
     */
    public GuiKeyBindingList(GuiControls guiControls, Minecraft minecraft)
    {
        super(minecraft, guiControls.width, guiControls.height, 63, guiControls.height - 32, 20);
        this.parentGuiControls = guiControls;
        this.mc = minecraft;
        KeyBinding[] akeybinding = (KeyBinding[])ArrayUtils.clone(minecraft.gameSettings.keyBindings); // 克隆键绑定数组
        this.keyBindingEntries = new GuiListExtended.IGuiListEntry[akeybinding.length + KeyBinding.getKeybinds().size()]; // 初始化条目数组
        Arrays.sort(akeybinding); // 对键绑定数组进行排序
        int i = 0;
        String s = null; // 当前分类名称
        KeyBinding[] akeybinding1 = akeybinding;
        int j = akeybinding.length;

        // 遍历键绑定数组，添加分类条目和键绑定条目
        for (int k = 0; k < j; ++k)
        {
            KeyBinding keybinding = akeybinding1[k];
            String s1 = keybinding.getKeyCategory(); // 获取键绑定分类
            if (!s1.equals(s)) // 如果分类发生变化
            {
                s = s1;
                this.keyBindingEntries[i++] = new GuiKeyBindingList.CategoryEntry(s1); // 添加分类条目
            }
            int l = minecraft.fontRenderer.getStringWidth(I18n.format(keybinding.getKeyDescription(), new Object[0])); // 计算键绑定描述的宽度
            if (l > this.maxKeyBindingTextWidth) // 更新最大宽度
            {
                this.maxKeyBindingTextWidth = l;
            }
            this.keyBindingEntries[i++] = new GuiKeyBindingList.KeyEntry(keybinding, null); // 添加键绑定条目
        }
    }

    /**
     * 获取列表中的条目数量。
     */
    protected int getSize()
    {
        return this.keyBindingEntries.length;
    }

    /**
     * 获取指定索引的列表条目。
     * @param index 条目索引
     */
    public GuiListExtended.IGuiListEntry getListEntry(int index)
    {
        return this.keyBindingEntries[index];
    }

    /**
     * 获取滚动条的X坐标。
     */
    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 15;
    }

    /**
     * 获取列表的宽度。
     */
    public int getListWidth()
    {
        return super.getListWidth() + 32;
    }

    /**
     * 分类条目类，用于显示键绑定分类标题。
     */
    @SideOnly(Side.CLIENT)
    public class CategoryEntry implements GuiListExtended.IGuiListEntry
    {
        private final String categoryName; // 建议命名为：categoryName，表示分类名称
        private final int categoryNameWidth; // 建议命名为：categoryNameWidth，表示分类名称的宽度
        private static final String __OBFID = "CL_00000734";

        /**
         * 构造函数，初始化分类条目。
         * @param name 分类名称
         */
        public CategoryEntry(String name)
        {
            this.categoryName = I18n.format(name, new Object[0]); // 本地化分类名称
            this.categoryNameWidth = GuiKeyBindingList.this.mc.fontRenderer.getStringWidth(this.categoryName); // 计算分类名称的宽度
        }

        /**
         * 绘制分类条目。
         */
        public void drawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_, Tessellator tessellator, int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {
            GuiKeyBindingList.this.mc.fontRenderer.drawString(this.categoryName, GuiKeyBindingList.this.mc.currentScreen.width / 2 - this.categoryNameWidth / 2, p_148279_3_ + p_148279_5_ - GuiKeyBindingList.this.mc.fontRenderer.FONT_HEIGHT - 1, 16777215); // 居中绘制分类名称
        }

        /**
         * 处理鼠标按下事件。
         */
        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            return false; // 分类条目不可点击
        }

        /**
         * 处理鼠标释放事件。
         */
        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_, int p_148277_6_) {}
    }

    /**
     * 键绑定条目类，用于显示和编辑键绑定。
     */
    @SideOnly(Side.CLIENT)
    public class KeyEntry implements GuiListExtended.IGuiListEntry
    {
        private final KeyBinding field_148282_b; // 建议命名为：keyBinding，表示键绑定实例
        private final String field_148283_c; // 建议命名为：keyBindingDescription，表示键绑定描述
        private final GuiButton btnChangeKeyBinding; // 修改键绑定按钮
        private final GuiButton btnReset; // 重置键绑定按钮
        private static final String __OBFID = "CL_00000735";

        /**
         * 构造函数，初始化键绑定条目。
         * @param keyBinding 键绑定实例
         */
        private KeyEntry(KeyBinding keyBinding)
        {
            this.field_148282_b = keyBinding;
            this.field_148283_c = I18n.format(keyBinding.getKeyDescription(), new Object[0]); // 本地化键绑定描述
            this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 18, I18n.format(keyBinding.getKeyDescription(), new Object[0])); // 初始化“修改键绑定”按钮
            this.btnReset = new GuiButton(0, 0, 0, 50, 18, I18n.format("controls.reset", new Object[0])); // 初始化“重置”按钮
        }

        /**
         * 绘制键绑定条目。
         */
        public void drawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_, Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_)
        {
            boolean flag1 = GuiKeyBindingList.this.parentGuiControls.selectedKeyBinding == this.field_148282_b; // 判断当前键绑定是否被选中
            GuiKeyBindingList.this.mc.fontRenderer.drawString(this.field_148283_c, p_148279_2_ + 90 - GuiKeyBindingList.this.maxKeyBindingTextWidth, p_148279_3_ + p_148279_5_ / 2 - GuiKeyBindingList.this.mc.fontRenderer.FONT_HEIGHT / 2, 16777215); // 绘制键绑定描述
            this.btnReset.xPosition = p_148279_2_ + 190; // 设置“重置”按钮的X坐标
            this.btnReset.yPosition = p_148279_3_; // 设置“重置”按钮的Y坐标
            this.btnReset.enabled = this.field_148282_b.getKeyCode() != this.field_148282_b.getKeyCodeDefault(); // 设置“重置”按钮的状态
            this.btnReset.drawButton(GuiKeyBindingList.this.mc, p_148279_7_, p_148279_8_); // 绘制“重置”按钮
            this.btnChangeKeyBinding.xPosition = p_148279_2_ + 105; // 设置“修改键绑定”按钮的X坐标
            this.btnChangeKeyBinding.yPosition = p_148279_3_; // 设置“修改键绑定”按钮的Y坐标
            this.btnChangeKeyBinding.displayString = GameSettings.getKeyDisplayString(this.field_148282_b.getKeyCode()); // 设置“修改键绑定”按钮的显示文本
            boolean flag2 = false; // 判断键绑定是否冲突
            if (this.field_148282_b.getKeyCode() != 0)
            {
                KeyBinding[] akeybinding = GuiKeyBindingList.this.mc.gameSettings.keyBindings; // 获取所有键绑定
                int l1 = akeybinding.length;
                for (int i2 = 0; i2 < l1; ++i2)
                {
                    KeyBinding keybinding = akeybinding[i2];
                    if (keybinding != this.field_148282_b && keybinding.getKeyCode() == this.field_148282_b.getKeyCode()) // 检查键绑定冲突
                    {
                        flag2 = true;
                        break;
                    }
                }
            }
            if (flag1) // 如果键绑定被选中，添加高亮效果
            {
                this.btnChangeKeyBinding.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + this.btnChangeKeyBinding.displayString + EnumChatFormatting.WHITE + " <";
            }
            else if (flag2) // 如果键绑定冲突，显示红色文本
            {
                this.btnChangeKeyBinding.displayString = EnumChatFormatting.RED + this.btnChangeKeyBinding.displayString;
            }
            this.btnChangeKeyBinding.drawButton(GuiKeyBindingList.this.mc, p_148279_7_, p_148279_8_); // 绘制“修改键绑定”按钮
        }

        /**
         * 处理鼠标按下事件。
         */
        public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
        {
            if (this.btnChangeKeyBinding.mousePressed(GuiKeyBindingList.this.mc, p_148278_2_, p_148278_3_)) // 点击“修改键绑定”按钮
            {
                GuiKeyBindingList.this.parentGuiControls.selectedKeyBinding = this.field_148282_b; // 设置当前选中的键绑定
                return true;
            }
            else if (this.btnReset.mousePressed(GuiKeyBindingList.this.mc, p_148278_2_, p_148278_3_)) // 点击“重置”按钮
            {
                GuiKeyBindingList.this.mc.gameSettings.setOptionKeyBinding(this.field_148282_b, this.field_148282_b.getKeyCodeDefault()); // 重置键绑定
                KeyBinding.resetKeyBindingArrayAndHash(); // 更新键绑定数组
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * 处理鼠标释放事件。
         */
        public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_, int p_148277_6_)
        {
            this.btnChangeKeyBinding.mouseReleased(p_148277_2_, p_148277_3_); // 释放“修改键绑定”按钮
            this.btnReset.mouseReleased(p_148277_2_, p_148277_3_); // 释放“重置”按钮
        }

        KeyEntry(KeyBinding p_i45030_2_, Object p_i45030_3_)
        {
            this(p_i45030_2_);
        }
    }
}