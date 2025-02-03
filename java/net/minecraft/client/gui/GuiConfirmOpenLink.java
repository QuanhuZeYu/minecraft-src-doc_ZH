package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

/**
 * 该GUI用于在玩家尝试打开外部链接前显示确认对话框，并提供复制链接的选项。
 */
@SideOnly(Side.CLIENT)
public class GuiConfirmOpenLink extends GuiYesNo {
    /** 打开不安全链接时的警告文本 */
    private final String openLinkWarning;
    /** 复制到剪贴板按钮的标签文本 */
    private final String copyLinkButtonText;
    /** 需要处理的链接URL */
    private final String linkUrl;
    /** 控制是否显示警告文本的标志 */
    private boolean showWarning = true;
    // 混淆标识符，用于反混淆处理
    private static final String __OBFID = "CL_00000683";

    /**
     * 构造一个链接确认对话框。
     *
     * @param parentScreen 父级GUI回调接口
     * @param linkUrl      需要确认的链接URL
     * @param id           对话框的唯一标识ID
     * @param isTrusted    标识链接是否来自受信任来源
     */
    public GuiConfirmOpenLink(GuiYesNoCallback parentScreen, String linkUrl, int id, boolean isTrusted) {
        super(parentScreen,
                I18n.format(isTrusted ? "chat.link.confirmTrusted" : "chat.link.confirm"),
                linkUrl,
                id);
        // 根据信任状态设置按钮文本
        this.confirmButtonText = I18n.format(isTrusted ? "chat.link.open" : "gui.yes");
        this.cancelButtonText = I18n.format(isTrusted ? "gui.cancel" : "gui.no");
        this.copyLinkButtonText = I18n.format("chat.copy");
        this.openLinkWarning = I18n.format("chat.link.warning");
        this.linkUrl = linkUrl;
    }

    /**
     * 初始化GUI组件，添加三个操作按钮。
     */
    @Override
    public void initGui() {
        int buttonWidth = 100;
        int buttonSpacing = 105;
        int baseX = this.width / 3 - 83;
        int yPos = this.height / 6 + 96;

        // 添加确认按钮（左侧）
        this.buttonList.add(new GuiButton(0, baseX, yPos, buttonWidth, 20, this.confirmButtonText));
        // 添加复制按钮（中间）
        this.buttonList.add(new GuiButton(2, baseX + buttonSpacing, yPos, buttonWidth, 20, this.copyLinkButtonText));
        // 添加取消按钮（右侧）
        this.buttonList.add(new GuiButton(1, baseX + buttonSpacing * 2, yPos, buttonWidth, 20, this.cancelButtonText));
    }

    /**
     * 处理按钮点击事件。
     *
     * @param button 被点击的按钮对象
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            copyLinkToClipboard();
        }
        // 通知父级GUI处理结果
        this.parentScreen.confirmClicked(button.id == 0, this.parentButtonId);
    }

    /**
     * 将链接URL复制到系统剪贴板。
     */
    public void copyLinkToClipboard() {
        setClipboardString(this.linkUrl);
    }

    /**
     * 绘制GUI界面元素。
     *
     * @param mouseX       当前鼠标X坐标
     * @param mouseY       当前鼠标Y坐标
     * @param partialTicks 部分渲染刻数（用于动画）
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.showWarning) {
            // 在指定位置绘制居中警告文本（橙色）
            this.drawCenteredString(
                    this.fontRendererObj,
                    this.openLinkWarning,
                    this.width / 2,
                    110,
                    0xFFCC6600  // 16764108的十六进制表示
            );
        }
    }

    public void hideWarning()
    {
        this.showWarning = false;
    }
}