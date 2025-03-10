package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.io.Charsets;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiRepair extends GuiContainer implements ICrafting {
    /**
     * 铁砧GUI的纹理路径（建议命名：ANVIL_GUI_TEXTURE）
     */
    private static final ResourceLocation ANVIL_GUI_TEXTURE = new ResourceLocation("textures/gui/container/anvil.png");

    /**
     * 铁砧容器实例（建议命名：repairContainer）
     */
    private ContainerRepair repairContainer;

    /**
     * 物品重命名输入框（建议命名：renameTextField）
     */
    private GuiTextField renameTextField;

    /**
     * 玩家物品栏引用（建议命名：playerInventory）
     */
    private InventoryPlayer playerInventory;

    private static final String __OBFID = "CL_00000738";

    /**
     * 构造方法
     * @param playerInv 玩家物品栏
     * @param world 世界对象
     * @param x/y/z 坐标（可能是界面打开位置）
     */
    public GuiRepair(InventoryPlayer playerInv, World world, int x, int y, int z) {
        super(new ContainerRepair(playerInv, world, x, y, z, Minecraft.getMinecraft().thePlayer));
        this.playerInventory = playerInv;            // playerInventory
        this.repairContainer = (ContainerRepair)this.inventorySlots;  // repairContainer
    }

    /**
     * 初始化GUI组件（创建文本框等）
     */
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);  // 启用长按键盘事件

        // 计算界面居中位置
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // 创建重命名输入框（位置：第三栏左侧）
        this.renameTextField = new GuiTextField(
                this.fontRendererObj,
                guiLeft + 62,  // X位置
                guiTop + 24,   // Y位置
                103,           // 宽度
                12             // 高度
        );
        this.renameTextField.setTextColor(-1);          // 白色文字
        this.renameTextField.setDisabledTextColour(-1); // 禁用时白色
        this.renameTextField.setEnableBackgroundDrawing(false); // 无背景框
        this.renameTextField.setMaxStringLength(40);    // 最大40字符

        // 注册容器监听到crafters列表
        this.inventorySlots.removeCraftingFromCrafters(this);  // 先移除旧监听
        this.inventorySlots.addCraftingToCrafters(this);       // 添加新监听
    }

    /**
     * GUI关闭时清理操作
     */
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);  // 关闭键盘重复事件
        this.inventorySlots.removeCraftingFromCrafters(this); // 移除容器监听
    }

    /**
     * 绘制前景层（文字和动态元素）
     */
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        // 绘制标题"Repair"
        this.fontRendererObj.drawString(
                I18n.format("container.repair"),
                60, 6, 0x404040  // 颜色值4210752的十六进制表示
        );

        // 显示修理费用逻辑
        if (this.repairContainer.maximumCost > 0) {
            int textColor = 0x80FF20;            // 正常颜色（绿色）
            boolean showCost = true;             // 是否显示花费
            String costText = I18n.format("container.repair.cost", this.repairContainer.maximumCost);

            // 昂贵修理（超过40级且非创造模式）
            if (this.repairContainer.maximumCost >= 40 && !this.mc.thePlayer.capabilities.isCreativeMode) {
                costText = I18n.format("container.repair.expensive");
                textColor = 0xFF6060;           // 红色警告
            }
            // 输出槽无物品时隐藏费用
            else if (!this.repairContainer.getSlot(2).getHasStack()) {
                showCost = false;
            }
            // 无法取出修复物品时变红（如经验不足）
            else if (!this.repairContainer.getSlot(2).canTakeStack(this.playerInventory.player)) {
                textColor = 0xFF6060;
            }

            // 绘制费用文本（带阴影效果）
            if (showCost) {
                int shadowColor = (0xFF000000 | (textColor & 0xFCFCFC) >> 2);  // 计算阴影颜色
                int textX = this.xSize - 8 - this.fontRendererObj.getStringWidth(costText);
                int textY = 67;  // Y位置固定

                // Unicode字体时绘制背景框
                if (this.fontRendererObj.getUnicodeFlag()) {
                    drawRect(textX - 3, textY - 2, this.xSize - 7, textY + 10, 0xFF000000);
                    drawRect(textX - 2, textY - 1, this.xSize - 8, textY + 9, 0x3F3F3F3F);
                }
                // 普通字体三次绘制实现阴影
                else {
                    this.fontRendererObj.drawString(costText, textX, textY + 1, shadowColor); // 阴影右下
                    this.fontRendererObj.drawString(costText, textX + 1, textY, shadowColor); // 阴影右上
                }
                this.fontRendererObj.drawString(costText, textX, textY, textColor);  // 主文本
            }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
    }

    /**
     * 处理键盘输入（更新重命名）
     */
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.renameTextField.textboxKeyTyped(typedChar, keyCode)) {  // 输入到文本框时
            this.updateItemName();  // 原方法名为func_147090_g()
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * 向服务端同步物品名称（建议方法名：updateItemName）
     */
    private void updateItemName() {
        String newName = this.renameTextField.getText();
        Slot inputSlot = this.repairContainer.getSlot(0);  // 输入槽位

        // 清除默认名称时设为空字符串
        if (inputSlot.getHasStack() &&
                !inputSlot.getStack().hasDisplayName() &&
                newName.equals(inputSlot.getStack().getDisplayName())
        ) {
            newName = "";
        }

        // 更新容器并发送网络包
        this.repairContainer.updateItemName(newName);
        this.mc.thePlayer.sendQueue.addToSendQueue(
                new C17PacketCustomPayload(
                        "MC|ItemName",
                        newName.getBytes(Charsets.UTF_8)  // UTF8编码
                )
        );
    }

    /**
     * 处理鼠标点击事件（聚焦文本框）
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.renameTextField.mouseClicked(mouseX, mouseY, mouseButton);  // 文本框点击检测
    }

    /**
     * 绘制界面（覆盖父类，在绘制后渲染文本框）
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        this.renameTextField.drawTextBox();  // 渲染输入框文本
    }

    /**
     * 绘制背景层和动态按钮元素
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);  // 重置颜色
        this.mc.getTextureManager().bindTexture(ANVIL_GUI_TEXTURE);

        // 计算基准绘制位置
        int baseX = (this.width - this.xSize) / 2;
        int baseY = (this.height - this.ySize) / 2;

        // 绘制主背景
        this.drawTexturedModalRect(baseX, baseY, 0, 0, this.xSize, this.ySize);

        // 输入槽位状态指示（当输入槽为空时灰色覆盖层）
        this.drawTexturedModalRect(
                baseX + 59,                               // X位置：第二个输入槽左侧
                baseY + 20,                               // Y位置
                0,                                        // 纹理左坐标
                this.ySize + (this.repairContainer.getSlot(0).getHasStack() ? 0 : 16), // 根据是否存在物品切换纹理
                110,                                      // 纹理宽度
                16                                        // 纹理高度
        );

        // 合成按钮（当输入槽有物品但输出槽无物品时高亮）
        if ((this.repairContainer.getSlot(0).getHasStack() ||
                this.repairContainer.getSlot(1).getHasStack()) &&
                !this.repairContainer.getSlot(2).getHasStack()
        ) {
            this.drawTexturedModalRect(
                    baseX + 99,  // X位置：输出槽右侧
                    baseY + 45,  // Y位置：合成按钮区域
                    this.xSize,  // 从纹理右边开始
                    0,           // 垂直位置
                    28,          // 按钮宽度
                    21           // 按钮高度
            );
        }
    }

    /**
     * ICrafting接口实现 - 同步容器内容（此处仅同步第一槽）
     */
    public void sendContainerAndContentsToPlayer(Container container, List<ItemStack> items) {
        this.sendSlotContents(container, 0, container.getSlot(0).getStack());
    }

    /**
     * 当特定槽位更新时触发（更新输入框状态）
     */
    public void sendSlotContents(Container container, int slotId, ItemStack stack) {
        if (slotId == 0) {  // 只关注输入槽
            this.renameTextField.setText(stack == null ? "" : stack.getDisplayName());
            this.renameTextField.setEnabled(stack != null);  // 无物品时禁用输入
            if (stack != null) {
                this.updateItemName();  // 强制更新名称
            }
        }
    }

    // ICrafting接口的进度更新方法（本界面无需处理）
    public void sendProgressBarUpdate(Container container, int var1, int var2) {}
}

// ######################## 核心逻辑说明 ########################
/*
1. 界面结构：
   - 继承GuiContainer实现容器类界面
   - 包含一个重命名输入框（id=0槽位对应）
   - 左侧两个输入槽（工具和材料），右侧输出槽

2. 核心功能：
   - 物品名称修改实时同步服务端（通过MC|ItemName包）
   - 动态显示修复成本（绿色/红色）
   - 根据输入槽状态显示提示按钮

3. 关键方法：
   - updateItemName(): 处理名称修改逻辑
   - 费用显示流程：在drawGuiContainerForegroundLayer中计算并绘制

4. 混淆字段转换建议：
   - field_147092_v → repairContainer
   - field_147091_w → renameTextField
   - field_147093_u → ANVIL_GUI_TEXTURE
   - func_147090_g() → updateItemName()

5. 注意事项：
   - 文字国际化都通过I18n.format处理
   - 合成按钮高亮逻辑需要两个输入槽任一非空且输出槽为空
*/