package net.minecraft.client.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tv.twitch.chat.ChatUserInfo;

@SideOnly(Side.CLIENT)
public class GuiChat extends GuiScreen implements GuiYesNoCallback
{
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet(new String[] {"http", "https"});
    private static final Logger logger = LogManager.getLogger();
    private String lastInputText = "";
    /**
     * 记录当前选择的聊天消息历史位置，按下上键时会选择上一条消息（不会因为连续发送相同消息而增加）
     */
    private int sentHistoryCursor = -1;
    private boolean isAutoCompleting;
    private boolean isTabCompleteRequested;
    private int autoCompleteIndex;
    private List<String> autoCompleteOptions = new ArrayList();
    /** 用于传递URI到各种对话框和主机操作系统 */
    private URI clickedURI;
    /** 聊天输入框 */
    protected GuiTextField inputField;
    /** 按下聊天键时输入框中显示的默认文本 */
    private String defaultInputFieldText = "";
    private static final String __OBFID = "CL_00000682";

    public GuiChat() {}

    public GuiChat(String defaultText)
    {
        this.defaultInputFieldText = defaultText;
    }

    /**
     * 初始化GUI，添加按钮和其他控件。
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        this.inputField = new GuiTextField(this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
        this.inputField.setMaxStringLength(100);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText(this.defaultInputFieldText);
        this.inputField.setCanLoseFocus(false);
    }

    /**
     * 当屏幕卸载时调用，用于禁用键盘重复事件。
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        this.mc.ingameGUI.getChatGUI().resetScroll();
    }

    /**
     * 从主游戏循环中调用以更新屏幕。
     */
    public void updateScreen()
    {
        this.inputField.updateCursorCounter();
    }

    /**
     * 当按键被键入时调用，相当于KeyListener.keyTyped(KeyEvent e)。
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        this.isTabCompleteRequested = false;

        if (keyCode == 15) // Tab键
        {
            this.performAutoComplete();
        }
        else
        {
            this.isAutoCompleting = false;
        }

        if (keyCode == 1) // Esc键
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (keyCode != 28 && keyCode != 156) // Enter键
        {
            if (keyCode == 200) // 上键
            {
                this.getSentHistory(-1);
            }
            else if (keyCode == 208) // 下键
            {
                this.getSentHistory(1);
            }
            else if (keyCode == 201) // Page Up键
            {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().func_146232_i() - 1);
            }
            else if (keyCode == 209) // Page Down键
            {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().func_146232_i() + 1);
            }
            else
            {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
        {
            String inputText = this.inputField.getText().trim();

            if (inputText.length() > 0)
            {
                this.sendChatMessage(inputText);
            }

            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    /**
     * 发送聊天消息。
     * @param message 要发送的消息
     */
    public void sendChatMessage(String message)
    {
        this.mc.ingameGUI.getChatGUI().addToSentMessages(message);
        if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, message) != 0) return;
        this.mc.thePlayer.sendChatMessage(message);
    }

    /**
     * 处理鼠标输入。
     */
    public void handleMouseInput()
    {
        super.handleMouseInput();
        int scrollDelta = Mouse.getEventDWheel();

        if (scrollDelta != 0)
        {
            if (scrollDelta > 1)
            {
                scrollDelta = 1;
            }

            if (scrollDelta < -1)
            {
                scrollDelta = -1;
            }

            if (!isShiftKeyDown())
            {
                scrollDelta *= 7;
            }

            this.mc.ingameGUI.getChatGUI().scroll(scrollDelta);
        }
    }

    /**
     * 当鼠标点击时调用。
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0 && this.mc.gameSettings.chatLinks)
        {
            IChatComponent chatComponent = this.mc.ingameGUI.getChatGUI().func_146236_a(Mouse.getX(), Mouse.getY());

            if (chatComponent != null)
            {
                ClickEvent clickEvent = chatComponent.getChatStyle().getChatClickEvent();

                if (clickEvent != null)
                {
                    if (isShiftKeyDown())
                    {
                        this.inputField.writeText(chatComponent.getUnformattedTextForChat());
                    }
                    else
                    {
                        URI uri;

                        if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL)
                        {
                            try
                            {
                                uri = new URI(clickEvent.getValue());

                                if (!ALLOWED_PROTOCOLS.contains(uri.getScheme().toLowerCase()))
                                {
                                    throw new URISyntaxException(clickEvent.getValue(), "Unsupported protocol: " + uri.getScheme().toLowerCase());
                                }

                                if (this.mc.gameSettings.chatLinksPrompt)
                                {
                                    this.clickedURI = uri;
                                    this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickEvent.getValue(), 0, false));
                                }
                                else
                                {
                                    this.openURI(uri);
                                }
                            }
                            catch (URISyntaxException urisyntaxexception)
                            {
                                logger.error("Can\'t open url for " + clickEvent, urisyntaxexception);
                            }
                        }
                        else if (clickEvent.getAction() == ClickEvent.Action.OPEN_FILE)
                        {
                            uri = (new File(clickEvent.getValue())).toURI();
                            this.openURI(uri);
                        }
                        else if (clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
                        {
                            this.inputField.setText(clickEvent.getValue());
                        }
                        else if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND)
                        {
                            this.sendChatMessage(clickEvent.getValue());
                        }
                        else if (clickEvent.getAction() == ClickEvent.Action.TWITCH_USER_INFO)
                        {
                            ChatUserInfo chatUserInfo = this.mc.func_152346_Z().func_152926_a(clickEvent.getValue());

                            if (chatUserInfo != null)
                            {
                                this.mc.displayGuiScreen(new GuiTwitchUserMode(this.mc.func_152346_Z(), chatUserInfo));
                            }
                            else
                            {
                                logger.error("Tried to handle twitch user but couldn\'t find them!");
                            }
                        }
                        else
                        {
                            logger.error("Don\'t know how to handle " + clickEvent);
                        }
                    }

                    return;
                }
            }
        }

        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * 当确认对话框点击时调用。
     */
    public void confirmClicked(boolean result, int id)
    {
        if (id == 0)
        {
            if (result)
            {
                this.openURI(this.clickedURI);
            }

            this.clickedURI = null;
            this.mc.displayGuiScreen(this);
        }
    }

    /**
     * 尝试使用系统默认浏览器打开指定URI（如网页链接、文件路径等）
     * 该方法通过反射调用Java AWT Desktop类的方法以实现跨环境兼容
     *
     * @param uri 需要打开的统一资源标识符对象
     */
    private void openURI(URI uri) {
        try {
            // 通过反射获取Desktop类（避免编译时直接依赖）
            Class<?> desktopClass = Class.forName("java.awt.Desktop");

            // 获取Desktop类的静态方法getDesktop（无参数）
            java.lang.reflect.Method getDesktopMethod = desktopClass.getMethod("getDesktop", new Class[0]);

            // 调用getDesktop静态方法获取Desktop实例（单例模式）
            Object desktopInstance = getDesktopMethod.invoke(null, new Object[0]);

            // 获取Desktop的browse方法，参数类型为URI.class
            java.lang.reflect.Method browseMethod = desktopClass.getMethod("browse", new Class[] { URI.class });

            // 调用browse方法，传入Desktop实例和URI参数
            browseMethod.invoke(desktopInstance, new Object[] { uri });

        /* 等效的非反射调用代码：
           Desktop.getDesktop().browse(uri);
           使用反射是为了：
           1. 避免在无图形界面环境（如服务器）中直接加载AWT类导致异常
           2. 增强对旧版Java的兼容性（编译时不依赖特定类）*/

        } catch (Throwable throwable) {
            // 捕获所有异常和错误（包括链接打开失败、无桌面环境、安全权限问题等）
            logger.error("Couldn't open link", throwable);

        /* 错误日志示例：
           - "java.awt.HeadlessException"：无显示设备、键盘或鼠标的环境
           - "java.lang.UnsupportedOperationException"：平台不支持browse操作
           - "java.lang.NoSuchMethodError"：旧版JDK不存在Desktop类（JDK1.6+）
           - "java.security.AccessControlException"：无权限操作 */
        }
    }

    /**
     * 执行自动补全逻辑，根据输入框内容提供建议并更新界面
     * 包含两种模式：
     * 1. 初次触发：收集候选词并展示第一个补全项
     * 2. 连续触发：循环切换备选补全项
     */
    public void performAutoComplete() {
        String autoCompleteText; // 存储当前补全文本的临时变量

        // 模式判断：是否处于自动补全状态
        if (this.isAutoCompleting) {
        /* 连续触发模式：
           删除之前补全的内容（通过计算光标位移）
           func_146197_a(-1, cursorPos, false) 推测为获取单词起始位置的方法
           - 参数1：-1 表示向左查找单词边界
           - 参数2：当前光标位置
           - 参数3：是否考虑颜色代码等特殊字符 */
            int wordStart = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.inputField.deleteFromCursor(wordStart - this.inputField.getCursorPosition());

            // 循环索引（当超过选项数量时重置为0）
            if (this.autoCompleteIndex >= this.autoCompleteOptions.size()) {
                this.autoCompleteIndex = 0;
            }
        } else {
        /* 初次触发模式：
           准备自动补全环境 */
            int cursorPos = this.inputField.getCursorPosition();
            int wordStart = this.inputField.func_146197_a(-1, cursorPos, false); // 获取当前单词起始位置

            // 重置自动补全状态
            this.autoCompleteOptions.clear();
            this.autoCompleteIndex = 0;

            // 提取部分文本（光标前的完整内容 + 光标后的部分词）
            String fullTextBeforeCursor = this.inputField.getText().substring(0, cursorPos);
            String partialWord = this.inputField.getText().substring(wordStart, cursorPos).toLowerCase();

            // 请求自动补全建议（具体实现未展示）
            this.requestAutoComplete(fullTextBeforeCursor, partialWord);

            // 无建议时直接返回
            if (this.autoCompleteOptions.isEmpty()) {
                return;
            }

            // 进入自动补全状态
            this.isAutoCompleting = true;
            // 删除当前正在输入的部分单词（准备替换为补全项）
            this.inputField.deleteFromCursor(wordStart - cursorPos);
        }

        // 当存在多个候选项时显示提示信息
        if (this.autoCompleteOptions.size() > 1) {
            StringBuilder suggestions = new StringBuilder();

            // 构建候选列表字符串（存在问题：见下方说明）
            Iterator<String> it = this.autoCompleteOptions.iterator();
            while (it.hasNext()) {
                String option = it.next();
                if (suggestions.length() > 0) {
                    suggestions.append(", ");
                }
                suggestions.append(option); // 原始代码此处有逻辑错误（见分析）
            }

            // 在聊天栏显示提示（ID=1 的消息会被后续提示覆盖）
            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                    new ChatComponentText(suggestions.toString()),
                    1
            );
        }

        /* 插入补全内容：
        1. 获取当前索引对应的候选项
        2. 移除颜色格式代码（防止颜色代码干扰输入）
        3. 自增索引为下次切换准备 */
        String selectedOption = this.autoCompleteOptions.get(this.autoCompleteIndex);
        this.inputField.writeText(EnumChatFormatting.getTextWithoutFormattingCodes(selectedOption));
        this.autoCompleteIndex++;
    }

    /**
     * 请求自动补全。
     * @param prefix 前缀
     * @param partial 部分文本
     */
    private void requestAutoComplete(String prefix, String partial)
    {
        if (prefix.length() >= 1)
        {
            net.minecraftforge.client.ClientCommandHandler.instance.autoComplete(prefix, partial);
            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(prefix));
            this.isTabCompleteRequested = true;
        }
    }

    /**
     * 获取聊天消息历史记录。
     * @param direction 方向，-1为上一个消息，1为下一个消息
     */
    public void getSentHistory(int direction)
    {
        int newCursorPosition = this.sentHistoryCursor + direction;
        int sentMessagesSize = this.mc.ingameGUI.getChatGUI().getSentMessages().size();

        if (newCursorPosition < 0)
        {
            newCursorPosition = 0;
        }

        if (newCursorPosition > sentMessagesSize)
        {
            newCursorPosition = sentMessagesSize;
        }

        if (newCursorPosition != this.sentHistoryCursor)
        {
            if (newCursorPosition == sentMessagesSize)
            {
                this.sentHistoryCursor = sentMessagesSize;
                this.inputField.setText(this.lastInputText);
            }
            else
            {
                if (this.sentHistoryCursor == sentMessagesSize)
                {
                    this.lastInputText = this.inputField.getText();
                }

                this.inputField.setText((String)this.mc.ingameGUI.getChatGUI().getSentMessages().get(newCursorPosition));
                this.sentHistoryCursor = newCursorPosition;
            }
        }
    }

    /**
     * 绘制屏幕及其所有组件。
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        IChatComponent chatComponent = this.mc.ingameGUI.getChatGUI().func_146236_a(Mouse.getX(), Mouse.getY());

        if (chatComponent != null && chatComponent.getChatStyle().getChatHoverEvent() != null)
        {
            HoverEvent hoverEvent = chatComponent.getChatStyle().getChatHoverEvent();

            if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM)
            {
                ItemStack itemStack = null;

                try
                {
                    NBTBase nbtBase = JsonToNBT.func_150315_a(hoverEvent.getValue().getUnformattedText());

                    if (nbtBase != null && nbtBase instanceof NBTTagCompound)
                    {
                        itemStack = ItemStack.loadItemStackFromNBT((NBTTagCompound)nbtBase);
                    }
                }
                catch (NBTException nbtexception)
                {
                    ;
                }

                if (itemStack != null)
                {
                    this.renderToolTip(itemStack, mouseX, mouseY);
                }
                else
                {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Item!", mouseX, mouseY);
                }
            }
            else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT)
            {
                this.func_146283_a(Splitter.on("\n").splitToList(hoverEvent.getValue().getFormattedText()), mouseX, mouseY);
            }
            else if (hoverEvent.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT)
            {
                StatBase statBase = StatList.func_151177_a(hoverEvent.getValue().getUnformattedText());

                if (statBase != null)
                {
                    IChatComponent statComponent = statBase.func_150951_e();
                    ChatComponentTranslation statTranslation = new ChatComponentTranslation("stats.tooltip.type." + (statBase.isAchievement() ? "achievement" : "statistic"), new Object[0]);
                    statTranslation.getChatStyle().setItalic(Boolean.valueOf(true));
                    String description = statBase instanceof Achievement ? ((Achievement)statBase).getDescription() : null;
                    ArrayList<String> tooltipLines = Lists.newArrayList(new String[] {statComponent.getFormattedText(), statTranslation.getFormattedText()});

                    if (description != null)
                    {
                        tooltipLines.addAll(this.fontRendererObj.listFormattedStringToWidth(description, 150));
                    }

                    this.func_146283_a(tooltipLines, mouseX, mouseY);
                }
                else
                {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid statistic/achievement!", mouseX, mouseY);
                }
            }

            GL11.glDisable(GL11.GL_LIGHTING);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 处理自动补全结果。
     * @param autoCompleteResults 自动补全结果
     */
    public void handleAutoCompleteResults(String[] autoCompleteResults)
    {
        if (this.isTabCompleteRequested)
        {
            this.isAutoCompleting = false;
            this.autoCompleteOptions.clear();
            String[] results = autoCompleteResults;
            int resultCount = autoCompleteResults.length;

            String[] complete = net.minecraftforge.client.ClientCommandHandler.instance.latestAutoComplete;
            if (complete != null)
            {
                results = com.google.common.collect.ObjectArrays.concat(complete, results, String.class);
                resultCount = results.length;
            }

            for (int i = 0; i < resultCount; ++i)
            {
                String result = results[i];

                if (result.length() > 0)
                {
                    this.autoCompleteOptions.add(result);
                }
            }

            String partialText = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String commonPrefix = StringUtils.getCommonPrefix(autoCompleteResults);

            if (commonPrefix.length() > 0 && !partialText.equalsIgnoreCase(commonPrefix))
            {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(commonPrefix);
            }
            else if (this.autoCompleteOptions.size() > 0)
            {
                this.isAutoCompleting = true;
                this.performAutoComplete();
            }
        }
    }

    /**
     * 返回此GUI是否应在单机模式下暂停游戏。
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}