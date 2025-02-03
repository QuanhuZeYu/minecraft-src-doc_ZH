package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiCreateFlatWorld extends GuiScreen
{
    /**
     * 用于渲染物品的RenderItem实例。
     */
    private static RenderItem itemRenderer = new RenderItem();

    /**
     * 创建世界的GUI实例。
     */
    private final GuiCreateWorld createWorldGui;

    /**
     * 当前平坦世界的生成信息。
     */
    private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();

    /**
     * 界面标题的本地化字符串。
     */
    private String titleText;

    /**
     * 方块类型的本地化字符串。
     */
    private String blockTypeText;

    /**
     * 高度的本地化字符串。
     */
    private String heightText;

    /**
     * 平坦世界生成信息的详细列表GUI。
     */
    private GuiCreateFlatWorld.Details createFlatWorldListSlotGui;

    /**
     * 添加图层的按钮。
     */
    private GuiButton addLayerButton;

    /**
     * 编辑图层的按钮。
     */
    private GuiButton editLayerButton;

    /**
     * 删除图层的按钮。
     */
    private GuiButton removeLayerButton;

    private static final String __OBFID = "CL_00000687";

    /**
     * 构造函数，初始化创建平坦世界的GUI。
     * @param p_i1029_1_ 创建世界的GUI实例。
     * @param p_i1029_2_ 平坦世界的生成信息字符串。
     */
    public GuiCreateFlatWorld(GuiCreateWorld p_i1029_1_, String p_i1029_2_)
    {
        this.createWorldGui = p_i1029_1_;
        this.setGenFlatWorldStr(p_i1029_2_);
    }

    /**
     * 获取当前平坦世界的生成信息字符串。
     * @return 平坦世界的生成信息字符串。
     */
    public String getFlatWorldInfo()
    {
        return this.theFlatGeneratorInfo.toString();
    }

    /**
     * 根据给定的字符串设置平坦世界的生成信息。
     * @param p_146383_1_ 平坦世界的生成信息字符串。
     */
    public void setGenFlatWorldStr(String p_146383_1_)
    {
        this.theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(p_146383_1_);
    }

    /**
     * 初始化GUI，添加按钮和其他控件。
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.titleText = I18n.format("createWorld.customize.flat.title", new Object[0]);
        this.blockTypeText = I18n.format("createWorld.customize.flat.tile", new Object[0]);
        this.heightText = I18n.format("createWorld.customize.flat.height", new Object[0]);
        this.createFlatWorldListSlotGui = new GuiCreateFlatWorld.Details();
        this.buttonList.add(this.addLayerButton = new GuiButton(2, this.width / 2 - 154, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer", new Object[0]) + " (NYI)"));
        this.buttonList.add(this.editLayerButton = new GuiButton(3, this.width / 2 - 50, this.height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer", new Object[0]) + " (NYI)"));
        this.buttonList.add(this.removeLayerButton = new GuiButton(4, this.width / 2 - 155, this.height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 + 5, this.height - 52, 150, 20, I18n.format("createWorld.customize.presets", new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.addLayerButton.visible = this.editLayerButton.visible = false;
        this.theFlatGeneratorInfo.func_82645_d();
        this.updateButtonState();
    }

    /**
     * 处理按钮点击事件。
     * @param button 被点击的按钮。
     */
    protected void actionPerformed(GuiButton button)
    {
        int i = this.theFlatGeneratorInfo.getFlatLayers().size() - this.createFlatWorldListSlotGui.field_148228_k - 1;

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(this.createWorldGui);
        }
        else if (button.id == 0)
        {
            this.createWorldGui.generatorOptions = this.getFlatWorldInfo();
            this.mc.displayGuiScreen(this.createWorldGui);
        }
        else if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiFlatPresets(this));
        }
        else if (button.id == 4 && this.isSelectLayer())
        {
            this.theFlatGeneratorInfo.getFlatLayers().remove(i);
            this.createFlatWorldListSlotGui.field_148228_k = Math.min(this.createFlatWorldListSlotGui.field_148228_k, this.theFlatGeneratorInfo.getFlatLayers().size() - 1);
        }

        this.theFlatGeneratorInfo.func_82645_d();
        this.updateButtonState();
    }

    /**
     * 更新按钮状态。
     */
    public void updateButtonState()
    {
        boolean flag = this.isSelectLayer();
        this.removeLayerButton.enabled = flag;
        this.editLayerButton.enabled = flag;
        this.editLayerButton.enabled = false;
        this.addLayerButton.enabled = false;
    }

    /**
     * 检查是否有选中的图层。
     * @return 如果有选中的图层返回true，否则返回false。
     */
    private boolean isSelectLayer()
    {
        return this.createFlatWorldListSlotGui.field_148228_k > -1 && this.createFlatWorldListSlotGui.field_148228_k < this.theFlatGeneratorInfo.getFlatLayers().size();
    }

    /**
     * 绘制屏幕及其所有组件。
     * @param mouseX 鼠标的X坐标。
     * @param mouseY 鼠标的Y坐标。
     * @param partialTicks 部分刻数。
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.titleText, this.width / 2, 8, 16777215);
        int k = this.width / 2 - 92 - 16;
        this.drawString(this.fontRendererObj, this.blockTypeText, k, 32, 16777215);
        this.drawString(this.fontRendererObj, this.heightText, k + 2 + 213 - this.fontRendererObj.getStringWidth(this.heightText), 32, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    class Details extends GuiSlot
    {
        /**
         * 当前选中的图层索引。
         */
        public int field_148228_k = -1;

        private static final String __OBFID = "CL_00000688";

        /**
         * 构造函数，初始化详细列表GUI。
         */
        public Details()
        {
            super(GuiCreateFlatWorld.this.mc, GuiCreateFlatWorld.this.width, GuiCreateFlatWorld.this.height, 43, GuiCreateFlatWorld.this.height - 60, 24);
        }

        /**
         * 绘制物品图标。
         * @param p_148225_1_ X坐标。
         * @param p_148225_2_ Y坐标。
         * @param p_148225_3_ 物品堆栈。
         */
        private void drawIcon(int p_148225_1_, int p_148225_2_, ItemStack p_148225_3_)
        {
            this.drawBackground(p_148225_1_ + 1, p_148225_2_ + 1);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            if (p_148225_3_ != null)
            {
                RenderHelper.enableGUIStandardItemLighting();
                GuiCreateFlatWorld.itemRenderer.renderItemIntoGUI(GuiCreateFlatWorld.this.fontRendererObj, GuiCreateFlatWorld.this.mc.getTextureManager(), p_148225_3_, p_148225_1_ + 2, p_148225_2_ + 2);
                RenderHelper.disableStandardItemLighting();
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        /**
         * 绘制背景框。
         * @param p_148226_1_ X坐标。
         * @param p_148226_2_ Y坐标。
         */
        private void drawBackground(int p_148226_1_, int p_148226_2_)
        {
            this.drawIcon(p_148226_1_, p_148226_2_, 0, 0);
        }

        /**
         * 绘制图标。
         * @param p_148224_1_ X坐标。
         * @param p_148224_2_ Y坐标。
         * @param p_148224_3_ 图标X偏移。
         * @param p_148224_4_ 图标Y偏移。
         */
        private void drawIcon(int p_148224_1_, int p_148224_2_, int p_148224_3_, int p_148224_4_)
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiCreateFlatWorld.this.mc.getTextureManager().bindTexture(Gui.statIcons);
            float f = 0.0078125F;
            float f1 = 0.0078125F;
            boolean flag = true;
            boolean flag1 = true;
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((double)(p_148224_1_ + 0), (double)(p_148224_2_ + 18), (double)GuiCreateFlatWorld.this.zLevel, (double)((float)(p_148224_3_ + 0) * 0.0078125F), (double)((float)(p_148224_4_ + 18) * 0.0078125F));
            tessellator.addVertexWithUV((double)(p_148224_1_ + 18), (double)(p_148224_2_ + 18), (double)GuiCreateFlatWorld.this.zLevel, (double)((float)(p_148224_3_ + 18) * 0.0078125F), (double)((float)(p_148224_4_ + 18) * 0.0078125F));
            tessellator.addVertexWithUV((double)(p_148224_1_ + 18), (double)(p_148224_2_ + 0), (double)GuiCreateFlatWorld.this.zLevel, (double)((float)(p_148224_3_ + 18) * 0.0078125F), (double)((float)(p_148224_4_ + 0) * 0.0078125F));
            tessellator.addVertexWithUV((double)(p_148224_1_ + 0), (double)(p_148224_2_ + 0), (double)GuiCreateFlatWorld.this.zLevel, (double)((float)(p_148224_3_ + 0) * 0.0078125F), (double)((float)(p_148224_4_ + 0) * 0.0078125F));
            tessellator.draw();
        }

        /**
         * 获取列表项的数量。
         * @return 列表项的数量。
         */
        protected int getSize()
        {
            return GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size();
        }

        /**
         * 处理列表项点击事件。
         * @param p_148144_1_ 被点击的列表项索引。
         * @param p_148144_2_ 是否双击。
         * @param p_148144_3_ 鼠标X坐标。
         * @param p_148144_4_ 鼠标Y坐标。
         */
        protected void elementClicked(int p_148144_1_, boolean p_148144_2_, int p_148144_3_, int p_148144_4_)
        {
            this.field_148228_k = p_148144_1_;
            GuiCreateFlatWorld.this.updateButtonState();
        }

        /**
         * 检查指定索引的列表项是否被选中。
         * @param p_148131_1_ 列表项索引。
         * @return 如果被选中返回true，否则返回false。
         */
        protected boolean isSelected(int p_148131_1_)
        {
            return p_148131_1_ == this.field_148228_k;
        }

        /**
         * 绘制列表背景。
         */
        protected void drawBackground() {}

        /**
         * 绘制列表项。
         * @param p_148126_1_ 列表项索引。
         * @param p_148126_2_ X坐标。
         * @param p_148126_3_ Y坐标。
         * @param p_148126_4_ 列表项高度。
         * @param p_148126_5_ Tessellator实例。
         * @param p_148126_6_ 鼠标X坐标。
         * @param p_148126_7_ 鼠标Y坐标。
         */
        protected void drawSlot(int p_148126_1_, int p_148126_2_, int p_148126_3_, int p_148126_4_, Tessellator p_148126_5_, int p_148126_6_, int p_148126_7_)
        {
            FlatLayerInfo flatlayerinfo = (FlatLayerInfo)GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().get(GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - p_148126_1_ - 1);
            Item item = Item.getItemFromBlock(flatlayerinfo.func_151536_b());
            ItemStack itemstack = flatlayerinfo.func_151536_b() == Blocks.air ? null : new ItemStack(item, 1, flatlayerinfo.getFillBlockMeta());
            String s = itemstack != null && item != null ? item.getItemStackDisplayName(itemstack) : "Air";
            this.drawIcon(p_148126_2_, p_148126_3_, itemstack);
            GuiCreateFlatWorld.this.fontRendererObj.drawString(s, p_148126_2_ + 18 + 5, p_148126_3_ + 3, 16777215);
            String s1;

            if (p_148126_1_ == 0)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.top", new Object[] {Integer.valueOf(flatlayerinfo.getLayerCount())});
            }
            else if (p_148126_1_ == GuiCreateFlatWorld.this.theFlatGeneratorInfo.getFlatLayers().size() - 1)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.bottom", new Object[] {Integer.valueOf(flatlayerinfo.getLayerCount())});
            }
            else
            {
                s1 = I18n.format("createWorld.customize.flat.layer", new Object[] {Integer.valueOf(flatlayerinfo.getLayerCount())});
            }

            GuiCreateFlatWorld.this.fontRendererObj.drawString(s1, p_148126_2_ + 2 + 213 - GuiCreateFlatWorld.this.fontRendererObj.getStringWidth(s1), p_148126_3_ + 3, 16777215);
        }

        /**
         * 获取滚动条的X坐标。
         * @return 滚动条的X坐标。
         */
        protected int getScrollBarX()
        {
            return this.width - 70;
        }
    }
}