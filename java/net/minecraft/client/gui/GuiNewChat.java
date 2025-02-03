package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiNewChat extends Gui
{
    private static final Logger logger = LogManager.getLogger(); // 日志记录器
    private final Minecraft mc; // Minecraft实例
    /** 之前通过聊天GUI发送的消息列表 */
    private final List sentMessages = new ArrayList();
    /** 聊天框中显示的聊天行 */
    private final List chatLines = new ArrayList();
    /** 当前显示的聊天行列表 */
    private final List messageLines = new ArrayList();
    private int currentLine; // 当前滚动的位置
    private boolean isScoroll; // 是否正在滚动
    private static final String __OBFID = "CL_00000669"; // 混淆ID

    /**
     * 构造函数
     *
     * @param mc Minecraft实例
     */
    public GuiNewChat(Minecraft mc)
    {
        this.mc = mc;
    }

    /**
     * 绘制聊天窗口
     *
     * @param timeStamp 当前的时间戳
     */
    public void drawChat(int timeStamp)
    {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int j = this.func_146232_i(); // 获取最大可见行数
            boolean flag = false; // 是否正在滚动
            int k = 0; // 计数器
            int l = this.messageLines.size(); // 当前显示的聊天行数量
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F; // 聊天透明度

            if (l > 0)
            {
                if (this.getChatOpen())
                {
                    flag = true; // 如果聊天窗口打开，则标记为正在滚动
                }

                float f1 = this.func_146244_h(); // 获取聊天缩放比例
                int i1 = MathHelper.ceiling_float_int((float)this.func_146228_f() / f1); // 计算每行的最大宽度

                GL11.glPushMatrix(); // 保存当前矩阵
                GL11.glTranslatef(2.0F, 20.0F, 0.0F); // 平移坐标系
                GL11.glScalef(f1, f1, 1.0F); // 缩放坐标系

                int j1;
                int deltaTime;
                int messageAlpha;

                // 遍历当前显示的聊天行
                for (j1 = 0; j1 + this.currentLine < this.messageLines.size() && j1 < j; ++j1)
                {
                    ChatLine chatline = (ChatLine)this.messageLines.get(j1 + this.currentLine);
                    if (chatline != null)
                    {
                        deltaTime = timeStamp - chatline.getUpdatedCounter(); // 计算消息的时间差
                        if (deltaTime < 200 || flag) // 如果消息未超时或正在滚动
                        {
                            double d0 = (double)deltaTime / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;
                            if (d0 < 0.0D)
                            {
                                d0 = 0.0D;
                            }
                            if (d0 > 1.0D)
                            {
                                d0 = 1.0D;
                            }
                            d0 *= d0;
                            messageAlpha = (int)(255.0D * d0); // 计算消息的透明度
                            if (flag)
                            {
                                messageAlpha = 255; // 如果正在滚动，则完全显示
                            }
                            messageAlpha = (int)((float)messageAlpha * f);
                            ++k;
                            if (messageAlpha > 3) // 如果透明度大于3
                            {
                                byte b0 = 0;
                                int j2 = -j1 * 9;
                                drawRect(b0, j2 - 9, b0 + i1 + 4, j2, messageAlpha / 2 << 24); // 绘制背景矩形
                                GL11.glEnable(GL11.GL_BLEND); // 启用混合
                                String s = chatline.func_151461_a().getFormattedText(); // 获取格式化后的文本
                                this.mc.fontRenderer.drawStringWithShadow(s, b0, j2 - 8, 16777215 + (messageAlpha << 24)); // 绘制文本
                                GL11.glDisable(GL11.GL_ALPHA_TEST); // 禁用Alpha测试
                            }
                        }
                    }
                }

                if (flag) // 如果正在滚动
                {
                    j1 = this.mc.fontRenderer.FONT_HEIGHT;
                    GL11.glTranslatef(-3.0F, 0.0F, 0.0F);
                    int k2 = l * j1 + l;
                    deltaTime = k * j1 + k;
                    int l2 = this.currentLine * deltaTime / l;
                    int l1 = deltaTime * deltaTime / k2;
                    if (k2 != deltaTime)
                    {
                        messageAlpha = l2 > 0 ? 170 : 96;
                        int i3 = this.isScoroll ? 13382451 : 3355562;
                        drawRect(0, -l2, 2, -l2 - l1, i3 + (messageAlpha << 24)); // 绘制滚动条
                        drawRect(2, -l2, 1, -l2 - l1, 13421772 + (messageAlpha << 24)); // 绘制滚动条背景
                    }
                }
                GL11.glPopMatrix(); // 恢复矩阵
            }
        }
    }

    /**
     * 清除聊天窗口中的所有消息
     */
    public void clearChatMessages()
    {
        this.messageLines.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    /**
     * 打印聊天消息
     *
     * @param iChatComponent 聊天消息组件
     */
    public void printChatMessage(IChatComponent iChatComponent)
    {
        this.printChatMessageWithOptionalDeletion(iChatComponent, 0);
    }

    /**
     * 打印聊天消息，并可以选择删除指定ID的聊天行
     *
     * @param iChatComponent 聊天消息组件
     * @param deleteID 要删除的聊天行ID
     */
    public void printChatMessageWithOptionalDeletion(IChatComponent iChatComponent, int deleteID)
    {
        this.func_146237_a(iChatComponent, deleteID, this.mc.ingameGUI.getUpdateCounter(), false);
        logger.info("[CHAT] " + iChatComponent.getUnformattedText());
    }

    /**
     * 根据设置格式化字符串
     *
     * @param originStr 原始字符串
     * @return 格式化后的字符串
     */
    private String func_146235_b(String originStr)
    {
        return Minecraft.getMinecraft().gameSettings.chatColours ? originStr : EnumChatFormatting.getTextWithoutFormattingCodes(originStr);
    }

    /**
     * 添加聊天消息到聊天窗口
     *
     * @param iChatComponent 聊天消息组件
     * @param lineID 聊天行ID
     * @param timeStamp 更新时间戳
     * @param onlyAddHistory 是否仅添加到历史记录
     */
    private void func_146237_a(IChatComponent iChatComponent, int lineID, int timeStamp, boolean onlyAddHistory)
    {
        if (lineID != 0)
        {
            this.deleteChatLine(lineID); // 删除指定ID的聊天行
        }

        int k = MathHelper.floor_float((float)this.func_146228_f() / this.func_146244_h()); // 计算每行的最大宽度
        int curWidth = 0; // 当前行宽
        ChatComponentText chatcomponenttext = new ChatComponentText(""); // 创建空文本组件
        ArrayList arraylist = Lists.newArrayList(); // 存储分割后的聊天行
        ArrayList arraylist1 = Lists.newArrayList(iChatComponent); // 存储聊天消息组件

        for (int i1 = 0; i1 < arraylist1.size(); ++i1)
        {
            IChatComponent ichatcomponent1 = (IChatComponent)arraylist1.get(i1);
            String s = this.func_146235_b(ichatcomponent1.getChatStyle().getFormattingCode() + ichatcomponent1.getUnformattedTextForChat()); // 格式化字符串
            int j1 = this.mc.fontRenderer.getStringWidth(s); // 计算字符串宽度
            ChatComponentText chatcomponenttext1 = new ChatComponentText(s); // 创建文本组件
            chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy()); // 复制样式
            boolean flag1 = false; // 标记是否需要分割字符串

            if (curWidth + j1 > k) // 如果当前行宽超过最大宽度
            {
                String s1 = this.mc.fontRenderer.trimStringToWidth(s, k - curWidth, false); // 截取字符串
                String s2 = s1.length() < s.length() ? s.substring(s1.length()) : null; // 获取剩余部分
                if (s2 != null && s2.length() > 0)
                {
                    int k1 = s1.lastIndexOf(" "); // 查找最后一个空格
                    if (k1 >= 0 && this.mc.fontRenderer.getStringWidth(s.substring(0, k1)) > 0)
                    {
                        s1 = s.substring(0, k1); // 截取到空格处
                        s2 = s.substring(k1); // 获取剩余部分
                    }
                    ChatComponentText chatcomponenttext2 = new ChatComponentText(s2); // 创建新的文本组件
                    chatcomponenttext2.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy()); // 复制样式
                    arraylist1.add(i1 + 1, chatcomponenttext2); // 将剩余部分添加到列表
                }
                j1 = this.mc.fontRenderer.getStringWidth(s1); // 计算截取后的字符串宽度
                chatcomponenttext1 = new ChatComponentText(s1); // 创建新的文本组件
                chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy()); // 复制样式
                flag1 = true; // 标记需要分割
            }

            if (curWidth + j1 <= k) // 如果当前行宽未超过最大宽度
            {
                curWidth += j1; // 增加当前行宽
                chatcomponenttext.appendSibling(chatcomponenttext1); // 追加文本组件
            }
            else
            {
                flag1 = true; // 标记需要分割
            }

            if (flag1) // 如果需要分割
            {
                arraylist.add(chatcomponenttext); // 将当前行添加到列表
                curWidth = 0; // 重置当前行宽
                chatcomponenttext = new ChatComponentText(""); // 创建新的空文本组件
            }
        }

        arraylist.add(chatcomponenttext); // 将最后一行添加到列表
        boolean flag2 = this.getChatOpen(); // 检查聊天窗口是否打开

        IChatComponent ichatcomponent2;
        for (Iterator iterator = arraylist.iterator(); iterator.hasNext(); this.messageLines.add(0, new ChatLine(timeStamp, ichatcomponent2, lineID))) // 遍历分割后的聊天行
        {
            ichatcomponent2 = (IChatComponent)iterator.next();
            if (flag2 && this.currentLine > 0) // 如果聊天窗口打开且正在滚动
            {
                this.isScoroll = true; // 标记正在滚动
                this.scroll(1); // 滚动一行
            }
        }

        while (this.messageLines.size() > 100) // 如果聊天行数量超过100
        {
            this.messageLines.remove(this.messageLines.size() - 1); // 删除最早的一行
        }

        if (!onlyAddHistory) // 如果不是仅添加到历史记录
        {
            this.chatLines.add(0, new ChatLine(timeStamp, iChatComponent, lineID)); // 添加到聊天历史记录
            while (this.chatLines.size() > 100) // 如果历史记录超过100行
            {
                this.chatLines.remove(this.chatLines.size() - 1); // 删除最早的一行
            }
        }
    }

    /**
     * 刷新聊天窗口
     */
    public void refreshChat()
    {
        this.messageLines.clear(); // 清空当前显示的聊天行
        this.resetScroll(); // 重置滚动位置
        for (int i = this.chatLines.size() - 1; i >= 0; --i) // 遍历聊天历史记录
        {
            ChatLine chatline = (ChatLine)this.chatLines.get(i);
            this.func_146237_a(chatline.func_151461_a(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true); // 重新添加聊天行
        }
    }

    /**
     * 获取之前发送的消息列表
     *
     * @return 之前发送的消息列表
     */
    public List<String> getSentMessages()
    {
        return this.sentMessages;
    }

    /**
     * 将消息添加到已发送消息列表中
     *
     * @param p_146239_1_ 消息内容
     */
    public void addToSentMessages(String p_146239_1_)
    {
        if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(p_146239_1_))
        {
            this.sentMessages.add(p_146239_1_); // 添加消息到列表
        }
    }

    /**
     * 重置聊天窗口的滚动位置
     */
    public void resetScroll()
    {
        this.currentLine = 0; // 重置滚动位置
        this.isScoroll = false; // 标记未滚动
    }

    /**
     * 滚动聊天窗口
     *
     * @param p_146229_1_ 滚动的行数
     */
    public void scroll(int p_146229_1_)
    {
        this.currentLine += p_146229_1_; // 增加滚动位置
        int j = this.messageLines.size(); // 获取当前显示的聊天行数量
        if (this.currentLine > j - this.func_146232_i()) // 如果滚动位置超过最大范围
        {
            this.currentLine = j - this.func_146232_i(); // 重置滚动位置
        }
        if (this.currentLine <= 0) // 如果滚动位置小于0
        {
            this.currentLine = 0; // 重置滚动位置
            this.isScoroll = false; // 标记未滚动
        }
    }

    /**
     * 根据鼠标点击位置获取聊天组件
     *
     * @param p_146236_1_ 鼠标X坐标
     * @param p_146236_2_ 鼠标Y坐标
     * @return 点击的聊天组件
     */
    public IChatComponent func_146236_a(int p_146236_1_, int p_146236_2_)
    {
        if (!this.getChatOpen()) // 如果聊天窗口未打开
        {
            return null; // 返回null
        }
        else
        {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight); // 获取屏幕分辨率
            int k = scaledresolution.getScaleFactor(); // 获取缩放因子
            float f = this.func_146244_h(); // 获取聊天缩放比例
            int l = p_146236_1_ / k - 3; // 计算聊天窗口的X坐标
            int i1 = p_146236_2_ / k - 27; // 计算聊天窗口的Y坐标
            l = MathHelper.floor_float((float)l / f); // 计算点击的X位置
            i1 = MathHelper.floor_float((float)i1 / f); // 计算点击的Y位置

            if (l >= 0 && i1 >= 0) // 如果点击位置在聊天窗口内
            {
                int j1 = Math.min(this.func_146232_i(), this.messageLines.size()); // 获取最大可见行数
                if (l <= MathHelper.floor_float((float)this.func_146228_f() / this.func_146244_h()) && i1 < this.mc.fontRenderer.FONT_HEIGHT * j1 + j1) // 如果点击位置在有效范围内
                {
                    int k1 = i1 / this.mc.fontRenderer.FONT_HEIGHT + this.currentLine; // 计算点击的聊天行
                    if (k1 >= 0 && k1 < this.messageLines.size()) // 如果点击的行有效
                    {
                        ChatLine chatline = (ChatLine)this.messageLines.get(k1); // 获取点击的聊天行
                        int l1 = 0; // 当前行宽
                        Iterator iterator = chatline.func_151461_a().iterator(); // 遍历聊天行的组件
                        while (iterator.hasNext())
                        {
                            IChatComponent ichatcomponent = (IChatComponent)iterator.next();
                            if (ichatcomponent instanceof ChatComponentText) // 如果是文本组件
                            {
                                l1 += this.mc.fontRenderer.getStringWidth(this.func_146235_b(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue())); // 增加当前行宽
                                if (l1 > l) // 如果当前行宽超过点击位置
                                {
                                    return ichatcomponent; // 返回点击的组件
                                }
                            }
                        }
                    }
                    return null; // 返回null
                }
                else
                {
                    return null; // 返回null
                }
            }
            else
            {
                return null; // 返回null
            }
        }
    }

    /**
     * 检查聊天窗口是否打开
     *
     * @return 如果聊天窗口打开则返回true
     */
    public boolean getChatOpen()
    {
        return this.mc.currentScreen instanceof GuiChat; // 检查当前屏幕是否是聊天窗口
    }

    /**
     * 根据ID删除聊天行
     *
     * @param p_146242_1_ 聊天行ID
     */
    public void deleteChatLine(int p_146242_1_)
    {
        Iterator iterator = this.messageLines.iterator(); // 遍历当前显示的聊天行
        ChatLine chatline;
        while (iterator.hasNext())
        {
            chatline = (ChatLine)iterator.next();
            if (chatline.getChatLineID() == p_146242_1_) // 如果ID匹配
            {
                iterator.remove(); // 删除聊天行
            }
        }

        iterator = this.chatLines.iterator(); // 遍历聊天历史记录
        while (iterator.hasNext())
        {
            chatline = (ChatLine)iterator.next();
            if (chatline.getChatLineID() == p_146242_1_) // 如果ID匹配
            {
                iterator.remove(); // 删除聊天行
                break;
            }
        }
    }

    /**
     * 获取聊天窗口的宽度
     *
     * @return 聊天窗口的宽度
     */
    public int func_146228_f()
    {
        return func_146233_a(this.mc.gameSettings.chatWidth); // 根据设置计算宽度
    }

    /**
     * 获取聊天窗口的高度
     *
     * @return 聊天窗口的高度
     */
    public int func_146246_g()
    {
        return func_146243_b(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused); // 根据设置计算高度
    }

    /**
     * 获取聊天窗口的缩放比例
     *
     * @return 聊天窗口的缩放比例
     */
    public float func_146244_h()
    {
        return this.mc.gameSettings.chatScale; // 返回缩放比例
    }

    /**
     * 根据设置计算聊天窗口的宽度
     *
     * @param p_146233_0_ 设置值
     * @return 计算后的宽度
     */
    public static int func_146233_a(float p_146233_0_)
    {
        short short1 = 320;
        byte b0 = 40;
        return MathHelper.floor_float(p_146233_0_ * (float)(short1 - b0) + (float)b0); // 计算宽度
    }

    /**
     * 根据设置计算聊天窗口的高度
     *
     * @param p_146243_0_ 设置值
     * @return 计算后的高度
     */
    public static int func_146243_b(float p_146243_0_)
    {
        short short1 = 180;
        byte b0 = 20;
        return MathHelper.floor_float(p_146243_0_ * (float)(short1 - b0) + (float)b0); // 计算高度
    }

    /**
     * 获取最大可见行数
     *
     * @return 最大可见行数
     */
    public int func_146232_i()
    {
        return this.func_146246_g() / 9; // 计算行数
    }
}