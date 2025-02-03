package net.minecraft.client.gui;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class GuiIngame extends Gui
{
    // 纹理路径常量
    protected static final ResourceLocation vignetteTexPath = new ResourceLocation("textures/misc/vignette.png"); // 渐晕效果纹理路径
    protected static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png"); // 小部件纹理路径
    protected static final ResourceLocation pumpkinBlurTexPath = new ResourceLocation("textures/misc/pumpkinblur.png"); // 南瓜模糊效果纹理路径
    protected static final RenderItem itemRenderer = new RenderItem(); // 物品渲染器实例

    protected final Random rand = new Random(); // 随机数生成器
    protected final Minecraft mc; // Minecraft 主类实例
    /** 保留所有先前聊天数据的 ChatGUI 实例 */
    protected final GuiNewChat persistentChatGUI; // 持久化聊天 GUI
    protected final GuiStreamIndicator streamIndicator; // 流媒体指示器 GUI
    protected int updateCounter; // 更新计数器，用于动画和计时
    /** 当前播放的唱片音乐名称 */
    protected String currentRecordName = ""; // 当前播放的唱片名称
    /** 唱片播放消息显示的剩余时间（以 tick 为单位） */
    protected int recordDisplayTime; // 唱片播放消息的显示时间
    protected boolean isRecordPlaying; // 是否正在播放唱片
    /** 前一帧的渐晕亮度（每帧缓慢变化 1%） */
    public float previousVignetteBrightness = 1.0F; // 前一帧的渐晕亮度
    /** 物品高亮显示的剩余时间 */
    protected int highlightDuration; // 物品高亮的剩余时间
    /** 当前正在高亮显示的物品 */
    protected ItemStack highlightedItemStack; // 高亮显示的物品堆栈
    private static final String __OBFID = "CL_00000661"; // 混淆 ID，用于反混淆

    /**
     * 创建游戏内界面（GUI）的实例，用于渲染HUD、聊天、状态指示等元素。
     * <p>
     * 该构造函数初始化游戏内界面所需的核心组件，包括聊天界面和流媒体指示器。
     * 它依赖于Minecraft主类实例来访问游戏状态和资源。
     *
     * @param minecraft Minecraft主类实例，提供游戏核心功能及上下文环境。
     *                  - 用于访问游戏窗口、玩家实体、资源管理器等关键组件
     *                  - 作为GUI子系统与游戏引擎的桥梁
     *                  - 必须为非空有效实例
     *
     * @implNote 初始化流程包括：
     * 1. 建立Minecraft实例引用
     * 2. 创建持久化聊天界面组件
     * 3. 初始化流媒体状态指示器
     *
     * @see GuiNewChat 用于实现可持久化的聊天消息系统
     * @see GuiStreamIndicator 用于处理Twitch等流媒体平台的状态显示
     */
    public GuiIngame(Minecraft minecraft) {
        this.mc = minecraft;
        this.persistentChatGUI = new GuiNewChat(minecraft);
        this.streamIndicator = new GuiStreamIndicator(this.mc);
    }

    /**
     * 渲染游戏内的HUD界面，包括快捷栏、状态栏、调试信息等。
     *
     * @param partialTicks 用于插值渲染的时间片段，通常用于平滑动画效果。
     * @param hasScreenFocus 当前是否有屏幕焦点，影响某些UI元素的显示。
     * @param mouseX 当前鼠标的X坐标。
     * @param mouseY 当前鼠标的Y坐标。
     */
    public void renderGameOverlay(float partialTicks, boolean hasScreenFocus, int mouseX, int mouseY)
    {
        // 获取当前窗口的缩放分辨率
        ScaledResolution scaledResolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        // 获取字体渲染器
        FontRenderer fontRenderer = this.mc.fontRenderer;

        // 设置HUD的渲染状态
        this.mc.entityRenderer.setupOverlayRendering();
        GL11.glEnable(GL11.GL_BLEND);

        // 根据图形设置渲染不同的效果
        if (Minecraft.isFancyGraphicsEnabled())
        {
            this.renderVignette(this.mc.thePlayer.getBrightness(partialTicks), screenWidth, screenHeight);
        }
        else
        {
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }

        // 如果玩家戴着南瓜头，渲染南瓜模糊效果
        ItemStack headItem = this.mc.thePlayer.inventory.armorItemInSlot(3);
        if (this.mc.gameSettings.thirdPersonView == 0 && headItem != null && headItem.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
        {
            this.renderPumpkinBlur(screenWidth, screenHeight);
        }

        // 如果玩家处于传送门效果中，渲染传送门效果
        if (!this.mc.thePlayer.isPotionActive(Potion.confusion))
        {
            float portalEffect = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;
            if (portalEffect > 0.0F)
            {
                this.func_130015_b(portalEffect, screenWidth, screenHeight);
            }
        }

        // 渲染玩家的HUD元素，包括快捷栏、十字准心、BOSS血条等
        if (!this.mc.playerController.enableEverythingIsScrewedUpMode())
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(widgetsTexPath);
            InventoryPlayer playerInventory = this.mc.thePlayer.inventory;
            this.zLevel = -90.0F;

            // 绘制快捷栏背景
            this.drawTexturedModalRect(screenWidth / 2 - 91, screenHeight - 22, 0, 0, 182, 22);
            this.drawTexturedModalRect(screenWidth / 2 - 91 - 1 + playerInventory.currentItem * 20, screenHeight - 22 - 1, 0, 22, 24, 22);

            // 绘制十字准心
            this.mc.getTextureManager().bindTexture(icons);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(775, 769, 1, 0);
            this.drawTexturedModalRect(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);

            // 渲染BOSS血条
            this.mc.mcProfiler.startSection("bossHealth");
            this.renderBossHealth();
            this.mc.mcProfiler.endSection();

            // 如果玩家需要显示HUD，渲染其他HUD元素
            if (this.mc.playerController.shouldDrawHUD())
            {
                this.renderStatusBars(screenWidth, screenHeight);
            }

            // 渲染快捷栏中的物品
            this.mc.mcProfiler.startSection("actionBar");
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < 9; ++i)
            {
                int slotX = screenWidth / 2 - 90 + i * 20 + 2;
                int slotY = screenHeight - 16 - 3;
                this.renderInventorySlot(i, slotX, slotY, partialTicks);
            }
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            this.mc.mcProfiler.endSection();
            GL11.glDisable(GL11.GL_BLEND);
        }

        // 如果玩家正在睡觉，渲染睡眠效果
        if (this.mc.thePlayer.getSleepTimer() > 0)
        {
            this.mc.mcProfiler.startSection("sleep");
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            int sleepTimer = this.mc.thePlayer.getSleepTimer();
            float sleepAlpha = (float)sleepTimer / 100.0F;
            if (sleepAlpha > 1.0F)
            {
                sleepAlpha = 1.0F - (float)(sleepTimer - 100) / 10.0F;
            }
            int sleepColor = (int)(220.0F * sleepAlpha) << 24 | 1052704;
            drawRect(0, 0, screenWidth, screenHeight, sleepColor);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            this.mc.mcProfiler.endSection();
        }

        // 如果玩家正在骑马，渲染跳跃条
        if (this.mc.thePlayer.isRidingHorse())
        {
            this.mc.mcProfiler.startSection("jumpBar");
            this.mc.getTextureManager().bindTexture(Gui.icons);
            float jumpPower = this.mc.thePlayer.getHorseJumpPower();
            int jumpBarWidth = (int)(jumpPower * 183.0F);
            int jumpBarY = screenHeight - 32 + 3;
            this.drawTexturedModalRect(screenWidth / 2 - 91, jumpBarY, 0, 84, 182, 5);
            if (jumpBarWidth > 0)
            {
                this.drawTexturedModalRect(screenWidth / 2 - 91, jumpBarY, 0, 89, jumpBarWidth, 5);
            }
            this.mc.mcProfiler.endSection();
        }
        // 否则，渲染经验条和等级
        else if (this.mc.playerController.gameIsSurvivalOrAdventure())
        {
            this.mc.mcProfiler.startSection("expBar");
            this.mc.getTextureManager().bindTexture(Gui.icons);
            int expBarCap = this.mc.thePlayer.xpBarCap();
            if (expBarCap > 0)
            {
                int expBarWidth = (int)(this.mc.thePlayer.experience * 183.0F);
                int expBarY = screenHeight - 32 + 3;
                this.drawTexturedModalRect(screenWidth / 2 - 91, expBarY, 0, 64, 182, 5);
                if (expBarWidth > 0)
                {
                    this.drawTexturedModalRect(screenWidth / 2 - 91, expBarY, 0, 69, expBarWidth, 5);
                }
            }
            this.mc.mcProfiler.endSection();

            // 渲染玩家等级
            if (this.mc.thePlayer.experienceLevel > 0)
            {
                this.mc.mcProfiler.startSection("expLevel");
                int levelColor = 8453920;
                String levelStr = "" + this.mc.thePlayer.experienceLevel;
                int levelX = (screenWidth - fontRenderer.getStringWidth(levelStr)) / 2;
                int levelY = screenHeight - 31 - 4;
                fontRenderer.drawString(levelStr, levelX + 1, levelY, 0);
                fontRenderer.drawString(levelStr, levelX - 1, levelY, 0);
                fontRenderer.drawString(levelStr, levelX, levelY + 1, 0);
                fontRenderer.drawString(levelStr, levelX, levelY - 1, 0);
                fontRenderer.drawString(levelStr, levelX, levelY, levelColor);
                this.mc.mcProfiler.endSection();
            }
        }

        // 如果启用了物品高亮显示，渲染高亮提示
        if (this.mc.gameSettings.heldItemTooltips)
        {
            this.mc.mcProfiler.startSection("toolHighlight");
            if (this.highlightDuration > 0 && this.highlightedItemStack != null)
            {
                String itemName = this.highlightedItemStack.getDisplayName();
                int nameX = (screenWidth - fontRenderer.getStringWidth(itemName)) / 2;
                int nameY = screenHeight - 59;
                if (!this.mc.playerController.shouldDrawHUD())
                {
                    nameY += 14;
                }
                int highlightAlpha = (int)((float)this.highlightDuration * 256.0F / 10.0F);
                if (highlightAlpha > 255)
                {
                    highlightAlpha = 255;
                }
                if (highlightAlpha > 0)
                {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    fontRenderer.drawStringWithShadow(itemName, nameX, nameY, 16777215 + (highlightAlpha << 24));
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }
            this.mc.mcProfiler.endSection();
        }

        // 如果处于演示模式，渲染演示模式的相关信息
        if (this.mc.isDemo())
        {
            this.mc.mcProfiler.startSection("demo");
            String demoText = "";
            if (this.mc.theWorld.getTotalWorldTime() >= 120500L)
            {
                demoText = I18n.format("demo.demoExpired", new Object[0]);
            }
            else
            {
                demoText = I18n.format("demo.remainingTime", new Object[] {StringUtils.ticksToElapsedTime((int)(120500L - this.mc.theWorld.getTotalWorldTime()))});
            }
            int demoTextWidth = fontRenderer.getStringWidth(demoText);
            fontRenderer.drawStringWithShadow(demoText, screenWidth - demoTextWidth - 10, 5, 16777215);
            this.mc.mcProfiler.endSection();
        }

        // 如果启用了调试信息，渲染调试信息
        if (this.mc.gameSettings.showDebugInfo)
        {
            this.mc.mcProfiler.startSection("debug");
            GL11.glPushMatrix();
            fontRenderer.drawStringWithShadow("Minecraft 1.7.10 (" + this.mc.debug + ")", 2, 2, 16777215);
            fontRenderer.drawStringWithShadow(this.mc.debugInfoRenders(), 2, 12, 16777215);
            fontRenderer.drawStringWithShadow(this.mc.getEntityDebug(), 2, 22, 16777215);
            fontRenderer.drawStringWithShadow(this.mc.debugInfoEntities(), 2, 32, 16777215);
            fontRenderer.drawStringWithShadow(this.mc.getWorldProviderName(), 2, 42, 16777215);
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            String memoryUsage = "Used memory: " + usedMemory * 100L / maxMemory + "% (" + usedMemory / 1024L / 1024L + "MB) of " + maxMemory / 1024L / 1024L + "MB";
            int debugColor = 14737632;
            this.drawString(fontRenderer, memoryUsage, screenWidth - fontRenderer.getStringWidth(memoryUsage) - 2, 2, debugColor);
            memoryUsage = "Allocated memory: " + totalMemory * 100L / maxMemory + "% (" + totalMemory / 1024L / 1024L + "MB)";
            this.drawString(fontRenderer, memoryUsage, screenWidth - fontRenderer.getStringWidth(memoryUsage) - 2, 12, debugColor);
            int offset = 22;
            for (String branding : FMLCommonHandler.instance().getBrandings(false))
            {
                this.drawString(fontRenderer, branding, screenWidth - fontRenderer.getStringWidth(branding) - 2, offset += 10, debugColor);
            }
            int playerX = MathHelper.floor_double(this.mc.thePlayer.posX);
            int playerY = MathHelper.floor_double(this.mc.thePlayer.posY);
            int playerZ = MathHelper.floor_double(this.mc.thePlayer.posZ);
            this.drawString(fontRenderer, String.format("x: %.5f (%d) // c: %d (%d)", new Object[] {Double.valueOf(this.mc.thePlayer.posX), Integer.valueOf(playerX), Integer.valueOf(playerX >> 4), Integer.valueOf(playerX & 15)}), 2, 64, debugColor);
            this.drawString(fontRenderer, String.format("y: %.3f (feet pos, %.3f eyes pos)", new Object[] {Double.valueOf(this.mc.thePlayer.boundingBox.minY), Double.valueOf(this.mc.thePlayer.posY)}), 2, 72, debugColor);
            this.drawString(fontRenderer, String.format("z: %.5f (%d) // c: %d (%d)", new Object[] {Double.valueOf(this.mc.thePlayer.posZ), Integer.valueOf(playerZ), Integer.valueOf(playerZ >> 4), Integer.valueOf(playerZ & 15)}), 2, 80, debugColor);
            int playerFacing = MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            this.drawString(fontRenderer, "f: " + playerFacing + " (" + Direction.directions[playerFacing] + ") / " + MathHelper.wrapAngleTo180_float(this.mc.thePlayer.rotationYaw), 2, 88, debugColor);
            if (this.mc.theWorld != null && this.mc.theWorld.blockExists(playerX, playerY, playerZ))
            {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(playerX, playerZ);
                this.drawString(fontRenderer, "lc: " + (chunk.getTopFilledSegment() + 15) + " b: " + chunk.getBiomeGenForWorldCoords(playerX & 15, playerZ & 15, this.mc.theWorld.getWorldChunkManager()).biomeName + " bl: " + chunk.getSavedLightValue(EnumSkyBlock.Block, playerX & 15, playerY, playerZ & 15) + " sl: " + chunk.getSavedLightValue(EnumSkyBlock.Sky, playerX & 15, playerY, playerZ & 15) + " rl: " + chunk.getBlockLightValue(playerX & 15, playerY, playerZ & 15, 0), 2, 96, debugColor);
            }
            this.drawString(fontRenderer, String.format("ws: %.3f, fs: %.3f, g: %b, fl: %d", new Object[] {Float.valueOf(this.mc.thePlayer.capabilities.getWalkSpeed()), Float.valueOf(this.mc.thePlayer.capabilities.getFlySpeed()), Boolean.valueOf(this.mc.thePlayer.onGround), Integer.valueOf(this.mc.theWorld.getHeightValue(playerX, playerZ))}), 2, 104, debugColor);
            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive())
            {
                this.drawString(fontRenderer, String.format("shader: %s", new Object[] {this.mc.entityRenderer.getShaderGroup().getShaderGroupName()}), 2, 112, debugColor);
            }
            GL11.glPopMatrix();
            this.mc.mcProfiler.endSection();
        }

        // 如果正在播放唱片，渲染唱片播放提示
        if (this.recordDisplayTime > 0)
        {
            this.mc.mcProfiler.startSection("overlayMessage");
            float recordAlpha = (float)this.recordDisplayTime - partialTicks;
            int recordAlphaInt = (int)(recordAlpha * 255.0F / 20.0F);
            if (recordAlphaInt > 255)
            {
                recordAlphaInt = 255;
            }
            if (recordAlphaInt > 8)
            {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(screenWidth / 2), (float)(screenHeight - 68), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                int recordColor = 16777215;
                if (this.isRecordPlaying)
                {
                    recordColor = Color.HSBtoRGB(recordAlpha / 50.0F, 0.7F, 0.6F) & 16777215;
                }
                fontRenderer.drawString(this.currentRecordName, -fontRenderer.getStringWidth(this.currentRecordName) / 2, -4, recordColor + (recordAlphaInt << 24 & -16777216));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
            this.mc.mcProfiler.endSection();
        }

        // 渲染计分板
        ScoreObjective scoreObjective = this.mc.theWorld.getScoreboard().func_96539_a(1);
        if (scoreObjective != null)
        {
            this.renderScoreboard(scoreObjective, screenHeight, screenWidth, fontRenderer);
        }

        // 渲染聊天窗口
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, (float)(screenHeight - 48), 0.0F);
        this.mc.mcProfiler.startSection("chat");
        this.persistentChatGUI.drawChat(this.updateCounter);
        this.mc.mcProfiler.endSection();
        GL11.glPopMatrix();

        // 如果按下玩家列表快捷键，渲染玩家列表
        scoreObjective = this.mc.theWorld.getScoreboard().func_96539_a(0);
        if (this.mc.gameSettings.keyBindPlayerList.getIsKeyPressed() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.playerInfoList.size() > 1 || scoreObjective != null))
        {
            this.mc.mcProfiler.startSection("playerList");
            NetHandlerPlayClient netHandler = this.mc.thePlayer.sendQueue;
            List playerList = netHandler.playerInfoList;
            int maxPlayers = netHandler.currentServerMaxPlayers;
            int playerRows = maxPlayers;
            int playerCols = 1;
            while (playerRows > 20)
            {
                playerRows = (maxPlayers + playerCols - 1) / playerCols;
                ++playerCols;
            }
            int playerCellWidth = 300 / playerCols;
            if (playerCellWidth > 150)
            {
                playerCellWidth = 150;
            }
            int playerListX = (screenWidth - playerCols * playerCellWidth) / 2;
            int playerListY = 10;
            drawRect(playerListX - 1, playerListY - 1, playerListX + playerCols * playerCellWidth, playerListY + 9 * playerRows, Integer.MIN_VALUE);
            for (int i = 0; i < maxPlayers; ++i)
            {
                int col = playerListX + i % playerCols * playerCellWidth;
                int row = playerListY + i / playerCols * 9;
                drawRect(col, row, col + playerCellWidth - 1, row + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                if (i < playerList.size())
                {
                    GuiPlayerInfo playerInfo = (GuiPlayerInfo)playerList.get(i);
                    ScorePlayerTeam playerTeam = this.mc.theWorld.getScoreboard().getPlayersTeam(playerInfo.name);
                    String playerName = ScorePlayerTeam.formatPlayerName(playerTeam, playerInfo.name);
                    fontRenderer.drawStringWithShadow(playerName, col, row, 16777215);
                    if (scoreObjective != null)
                    {
                        int scoreX = col + fontRenderer.getStringWidth(playerName) + 5;
                        int scoreY = col + playerCellWidth - 12 - 5;
                        if (scoreY - scoreX > 5)
                        {
                            Score playerScore = scoreObjective.getScoreboard().func_96529_a(playerInfo.name, scoreObjective);
                            String scoreStr = EnumChatFormatting.YELLOW + "" + playerScore.getScorePoints();
                            fontRenderer.drawStringWithShadow(scoreStr, scoreY - fontRenderer.getStringWidth(scoreStr), row, 16777215);
                        }
                    }
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(icons);
                    int pingLevel = 0;
                    if (playerInfo.responseTime < 0)
                    {
                        pingLevel = 5;
                    }
                    else if (playerInfo.responseTime < 150)
                    {
                        pingLevel = 0;
                    }
                    else if (playerInfo.responseTime < 300)
                    {
                        pingLevel = 1;
                    }
                    else if (playerInfo.responseTime < 600)
                    {
                        pingLevel = 2;
                    }
                    else if (playerInfo.responseTime < 1000)
                    {
                        pingLevel = 3;
                    }
                    else
                    {
                        pingLevel = 4;
                    }
                    this.zLevel += 100.0F;
                    this.drawTexturedModalRect(col + playerCellWidth - 12, row, 0 + pingLevel * 10, 176 + pingLevel * 8, 10, 8);
                    this.zLevel -= 100.0F;
                }
            }
        }

        // 最后，重置OpenGL状态
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    /**
     * 设置流媒体指示器的水平位置（该方法疑似用于调整界面元素位置）
     *
     * @param rawXPosition 原始X轴位置参数（单位：像素）
     * @param unusedYPosition Y轴位置参数（当前版本未使用，可能为预留参数或功能未实现）
     *
     * 方法逻辑：
     * 1. 对X轴位置进行偏移计算：原始位置向左偏移10像素
     * 2. 将计算结果强制转换为整型（可能因为底层接口需要整数坐标）
     * 3. 设置固定垂直位置为10像素（Y轴参数未被使用）
     * 4. 调用流媒体指示器的位置更新方法
     *
     * 注意：由于参数命名被混淆，实际功能需结合上下文判断
     */
    public void func_152126_a(float rawXPosition, float unusedYPosition)
    {
        // 计算调整后的X轴位置：原始值减10像素后取整
        int adjustedX = (int)(rawXPosition - 10.0F);

        // 调用流媒体指示器的位置设置方法
        // 参数说明：
        // - 第一个参数：调整后的X轴坐标
        // - 第二个参数：固定Y轴坐标为10像素（硬编码值，未使用传入的Y参数）
        this.streamIndicator.func_152437_a(adjustedX, 10);
    }

    /**
     * 渲染计分板目标及其关联的分数条目（用于游戏内计分板显示）
     *
     * @param objective   需要渲染的计分板目标对象
     * @param baseX       渲染基准X坐标（通常为屏幕右侧起始位置）
     * @param baseY       渲染基准Y坐标（通常为屏幕顶部起始位置）
     * @param fontRenderer 字体渲染器实例（用于文本测量和绘制）
     *
     * 方法逻辑：
     * 1. 获取计分板目标对应的所有分数条目集合
     * 2. 当条目数量不超过15时执行渲染（防止界面溢出）
     * 3. 计算计分板标题和所有分数条目的最大显示宽度
     * 4. 计算计分板整体高度和垂直居中位置
     * 5. 遍历所有分数条目进行逐条渲染：
     *    - 绘制半透明背景条
     *    - 渲染玩家名称（带队伍颜色格式）
     *    - 渲染红色分数值（右对齐）
     * 6. 在顶部渲染计分板标题栏（带特殊背景效果）
     *
     * 注意：颜色值使用ARGB格式，坐标计算包含硬编码的间距调整
     */
    protected void renderScoreboard(ScoreObjective objective, int baseX, int baseY, FontRenderer fontRenderer) {
        Scoreboard scoreboard = objective.getScoreboard();
        // 获取排序后的分数条目集合（按分数从高到低排序）
        Collection<Score> scores = scoreboard.func_96534_i(objective);

        if (scores.size() <= 15) {
            // 计算计分板标题宽度
            int maxWidth = fontRenderer.getStringWidth(objective.getDisplayName());
            String formattedName;

            // 遍历所有分数条目计算最大文本宽度
            for (Score score : scores) {
                ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                formattedName = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName())
                        + ": " + EnumChatFormatting.RED + score.getScorePoints();
                maxWidth = Math.max(maxWidth, fontRenderer.getStringWidth(formattedName));
            }

            // 计算整体布局参数
            int totalHeight = scores.size() * fontRenderer.FONT_HEIGHT; // 总高度 = 条目数 × 行高
            int verticalCenter = baseY / 2 + totalHeight / 3;          // 垂直居中位置
            final int margin = 3;                                       // 右侧边距
            int startX = baseX - maxWidth - margin;                     // 起始X坐标（右侧布局）
            int entryIndex = 0;                                         // 当前渲染条目索引

            // 逐条渲染分数条目
            for (Score score : scores) {
                entryIndex++;
                ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
                String scoreValue = EnumChatFormatting.RED.toString() + score.getScorePoints();

                // 计算当前条目Y坐标（从下往上排列）
                int entryY = verticalCenter - entryIndex * fontRenderer.FONT_HEIGHT;
                int rightEdge = baseX - margin + 2; // 右侧边界（留出2像素偏移）

                // 绘制条目背景（半透明黑色矩形）
                drawRect(startX - 2, entryY, rightEdge, entryY + fontRenderer.FONT_HEIGHT, 0x50000000);

                // 绘制玩家名称（左对齐，半透明白色）
                fontRenderer.drawString(playerName, startX, entryY, 0x20FFFFFF);

                // 绘制分数值（右对齐）
                fontRenderer.drawString(scoreValue,
                        rightEdge - fontRenderer.getStringWidth(scoreValue),
                        entryY,
                        0x20FFFFFF);

                // 渲染标题栏（当处理最后一个条目时）
                if (entryIndex == scores.size()) {
                    String title = objective.getDisplayName();
                    // 标题栏背景（较暗的半透明层）
                    drawRect(startX - 2,
                            entryY - fontRenderer.FONT_HEIGHT - 1,
                            rightEdge,
                            entryY - 1,
                            0x60000000);
                    // 标题栏底部边框
                    drawRect(startX - 2, entryY - 1, rightEdge, entryY, 0x50000000);
                    // 居中显示标题文本
                    fontRenderer.drawString(title,
                            startX + maxWidth / 2 - fontRenderer.getStringWidth(title) / 2,
                            entryY - fontRenderer.FONT_HEIGHT,
                            0x20FFFFFF);
                }
            }
        }
    }

    /**
     * 渲染游戏状态条（生命值、护甲、食物、氧气等）
     *
     * @param screenWidth  屏幕宽度（用于定位横向坐标）
     * @param screenHeight 屏幕高度（用于定位纵向坐标）
     *<p>
     * 方法逻辑：
     * 1. 计算玩家受伤闪烁效果
     * 2. 生命值相关渲染：
     *    - 计算生命值与吸收值总量
     *    - 处理中毒/凋零状态的特殊显示
     *    - 分页渲染心形图标（每页10个，最多3行）
     * 3. 护甲值渲染：
     *    - 根据当前护甲值显示完整/半缺/空护甲图标
     * 4. 食物条渲染：
     *    - 处理饥饿状态特效
     *    - 显示食物饱和度晃动效果
     * 5. 坐骑血量显示（当骑乘生物时）：
     *    - 分段显示生物血量（最多30点，分3行）
     * 6. 氧气条显示（水下时）：
     *    - 根据剩余氧气量显示气泡图标
     *
     * 注意：使用纹理图集进行绘制，坐标参数对应gui.png中的元素位置
     */
    protected void renderStatusBars(int screenWidth, int screenHeight) {
        // ================== 初始化部分 ==================
        // 计算受伤闪烁效果（受伤保护时间大于10时每3 ticks闪烁一次）
        boolean isFlashing = this.mc.thePlayer.hurtResistantTime / 3 % 2 == 1;
        if (this.mc.thePlayer.hurtResistantTime < 10) isFlashing = false;

        // 获取生命值相关数据
        int currentHealth = MathHelper.ceiling_float_int(this.mc.thePlayer.getHealth());
        int previousHealth = MathHelper.ceiling_float_int(this.mc.thePlayer.prevHealth);
        this.rand.setSeed(this.updateCounter * 312871L); // 为抖动效果准备随机种子

        // 获取食物相关数据
        FoodStats foodStats = this.mc.thePlayer.getFoodStats();
        int foodLevel = foodStats.getFoodLevel();
        int prevFoodLevel = foodStats.getPrevFoodLevel();

        // ================== 布局计算 ==================
        // 基础坐标计算（右侧居中布局）
        int rightAnchor = screenWidth / 2 + 91;   // 右侧锚点坐标
        int leftAnchor = screenWidth / 2 - 91;    // 左侧锚点坐标
        int baseY = screenHeight - 39;            // 基础Y坐标（屏幕底部向上39像素）

        // 生命值容器计算（考虑吸收护盾）
        float maxHealth = (float)this.mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue();
        float absorption = this.mc.thePlayer.getAbsorptionAmount();
        int heartRows = MathHelper.ceiling_float_int((maxHealth + absorption) / 2.0F / 10.0F); // 计算需要显示的行数
        int rowSpacing = Math.max(10 - (heartRows - 2), 3); // 行间距（最少3像素）
        int healthStartY = baseY - (heartRows - 1) * rowSpacing - 10; // 生命值起始Y坐标

        // ================== 护甲渲染 ==================
        this.mc.mcProfiler.startSection("armor");
        int armorValue = this.mc.thePlayer.getTotalArmorValue();
        for (int armorSlot = 0; armorSlot < 10; ++armorSlot) {
            if (armorValue > 0) {
                int xPos = leftAnchor + armorSlot * 8;
                // 根据护甲剩余量选择图标：完整护甲（34,9）、半缺护甲（25,9）、空护甲（16,9）
                if (armorSlot * 2 + 1 < armorValue) {
                    this.drawTexturedModalRect(xPos, healthStartY, 34, 9, 9, 9);
                } else if (armorSlot * 2 + 1 == armorValue) {
                    this.drawTexturedModalRect(xPos, healthStartY, 25, 9, 9, 9);
                } else {
                    this.drawTexturedModalRect(xPos, healthStartY, 16, 9, 9, 9);
                }
            }
        }
        this.mc.mcProfiler.endStartSection("health");

        // ================== 生命值渲染 ==================
        float absorptionCopy = absorption;
        // 从最高血量开始向下渲染
        for (int heartIndex = MathHelper.ceiling_float_int((maxHealth + absorption) / 2.0F) - 1; heartIndex >= 0; --heartIndex) {
            // 确定纹理偏移（正常：16，中毒：52，凋零：88）
            int textureX = 16;
            if (this.mc.thePlayer.isPotionActive(Potion.poison)) {
                textureX += 36;
            } else if (this.mc.thePlayer.isPotionActive(Potion.wither)) {
                textureX += 72;
            }

            // 计算绘制位置
            int row = MathHelper.ceiling_float_int((heartIndex + 1) / 10.0F) - 1;
            int xPos = leftAnchor + heartIndex % 10 * 8;
            int yPos = baseY - row * rowSpacing;

            // 添加抖动效果（当生命值极低时）
            if (currentHealth <= 4) yPos += this.rand.nextInt(2);
            // 处理再生药水特效
            if (heartIndex == (this.updateCounter % MathHelper.ceiling_float_int(maxHealth + 5.0F))) {
                yPos -= 2;
            }

            // 绘制背景心形容器
            int textureY = this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 45 : 0; // 极限模式使用不同颜色
            this.drawTexturedModalRect(xPos, yPos, isFlashing ? 25 : 16, textureY, 9, 9);

            // 绘制吸收护盾（金色边框）
            if (absorptionCopy > 0) {
                this.drawTexturedModalRect(xPos, yPos,
                        (absorptionCopy == absorption && absorption % 2 == 1) ? 169 : 160,
                        textureY, 9, 9);
                absorptionCopy -= 2.0F;
            }
            // 绘制实际生命值（红色填充）
            else {
                if (heartIndex * 2 + 1 < currentHealth) {
                    this.drawTexturedModalRect(xPos, yPos, textureX + 36, textureY, 9, 9);
                } else if (heartIndex * 2 + 1 == currentHealth) {
                    this.drawTexturedModalRect(xPos, yPos, textureX + 45, textureY, 9, 9);
                }
            }
        }

        // ================== 食物条渲染 ==================
        if (this.mc.thePlayer.ridingEntity == null) {
            this.mc.mcProfiler.endStartSection("food");
            for (int foodSlot = 0; foodSlot < 10; ++foodSlot) {
                int yPos = baseY;
                int textureX = 16;
                int textureY = 27; // 食物条专用纹理行

                // 处理饥饿状态显示
                if (this.mc.thePlayer.isPotionActive(Potion.hunger)) {
                    textureX += 36;
                    yPos += this.rand.nextInt(3) - 1; // 添加抖动效果
                }

                // 绘制食物槽背景
                int xPos = rightAnchor - foodSlot * 8 - 9;
                this.drawTexturedModalRect(xPos, yPos, 16 + (isFlashing ? 9 : 0), textureY, 9, 9);

                // 填充食物值（肉排图标）
                if (foodSlot * 2 + 1 < foodLevel) {
                    this.drawTexturedModalRect(xPos, yPos, textureX + 36, textureY, 9, 9);
                } else if (foodSlot * 2 + 1 == foodLevel) {
                    this.drawTexturedModalRect(xPos, yPos, textureX + 45, textureY, 9, 9);
                }
            }
        }

        // ================== 坐骑血量渲染 ==================
        else if (this.mc.thePlayer.ridingEntity instanceof EntityLivingBase) {
            this.mc.mcProfiler.endStartSection("mountHealth");
            EntityLivingBase mount = (EntityLivingBase) this.mc.thePlayer.ridingEntity;
            int mountHealth = (int) Math.ceil(mount.getHealth());
            float mountMaxHealth = mount.getMaxHealth();
            int heartCount = Math.min((int)(mountMaxHealth + 0.5F) / 2, 30); // 最多显示30点血量

            int yPos = baseY;
            // 分页渲染（每页10个心形）
            for (int rendered = 0; heartCount > 0; rendered += 10) {
                int heartsThisRow = Math.min(heartCount, 10);
                for (int i = 0; i < heartsThisRow; ++i) {
                    int xPos = rightAnchor - i * 8 - 9;
                    this.drawTexturedModalRect(xPos, yPos, 52, 9, 9, 9); // 空容器
                    // 填充血量（红色心形）
                    if (i * 2 + 1 + rendered < mountHealth) {
                        this.drawTexturedModalRect(xPos, yPos, 88, 9, 9, 9);
                    } else if (i * 2 + 1 + rendered == mountHealth) {
                        this.drawTexturedModalRect(xPos, yPos, 97, 9, 9, 9);
                    }
                }
                yPos -= 10; // 换行
                heartCount -= heartsThisRow;
            }
        }

        // ================== 氧气条渲染 ==================
        this.mc.mcProfiler.endStartSection("air");
        if (this.mc.thePlayer.isInsideOfMaterial(Material.water)) {
            int air = this.mc.thePlayer.getAir();
            int fullBubbles = (int) ((air - 2) * 10.0 / 300.0);
            int partialBubbles = (int) (air * 10.0 / 300.0) - fullBubbles;

            for (int i = 0; i < fullBubbles + partialBubbles; ++i) {
                int xPos = rightAnchor - i * 8 - 9;
                // 完整气泡（16,18）或半透明气泡（25,18）
                this.drawTexturedModalRect(xPos, healthStartY,
                        i < fullBubbles ? 16 : 25,
                        18, 9, 9);
            }
        }
        this.mc.mcProfiler.endSection();
    }

    /**
     * 渲染Boss血条（用于末影龙、凋灵等Boss战场景）
     *<p>
     * 实现逻辑：
     * 1. 有效性检查：当Boss名称存在且血条显示时间未过期时执行渲染
     * 2. 血条位置计算：始终位于屏幕顶部中央
     * 3. 血条组成：
     *    - 背景条（灰色底条）
     *    - 动态血量条（根据当前血量比例显示）
     *    - 顶部Boss名称（带文字阴影效果）
     * 4. 视觉效果：
     *    - 自动渐隐计时（statusBarTime控制显示持续时间）
     *    - 精确到像素的血量比例显示
     *
     * 纹理坐标说明：参考gui/icons.png纹理图集
     * 血条坐标：0,74（背景）和 0,79（填充条）
     */
    protected void renderBossHealth() {
        // 检查是否需要渲染（存在激活的Boss且血条显示时间未结束）
        if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
            // 更新显示计时（每帧减少1，当减到0时停止显示）
            BossStatus.statusBarTime--;

            // 获取字体渲染器和屏幕尺寸
            FontRenderer fontRenderer = this.mc.fontRenderer;
            ScaledResolution resolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            int screenWidth = resolution.getScaledWidth();

            // 血条尺寸参数
            final int BOSS_BAR_WIDTH = 182;    // 血条总宽度（像素）
            final int HEALTH_BAR_Y = 12;       // 距离屏幕顶部的固定偏移量

            // 计算血条位置（水平居中）
            int healthBarStartX = screenWidth / 2 - BOSS_BAR_WIDTH / 2;

            // 计算当前血量显示长度（healthScale范围：0.0-1.0）
            int currentHealthWidth = (int)(BossStatus.healthScale * (BOSS_BAR_WIDTH + 1));

            // 绘制血条背景（灰色底条，绘制两次加深颜色）
            this.drawTexturedModalRect(healthBarStartX, HEALTH_BAR_Y, 0, 74, BOSS_BAR_WIDTH, 5);
            this.drawTexturedModalRect(healthBarStartX, HEALTH_BAR_Y, 0, 74, BOSS_BAR_WIDTH, 5);

            // 绘制动态血量条（红色进度条）
            if (currentHealthWidth > 0) {
                this.drawTexturedModalRect(healthBarStartX, HEALTH_BAR_Y, 0, 79, currentHealthWidth, 5);
            }

            // 渲染Boss名称（白色文字带阴影，位于血条上方10像素）
            String bossName = BossStatus.bossName;
            int textX = screenWidth / 2 - fontRenderer.getStringWidth(bossName) / 2;
            int textY = HEALTH_BAR_Y - 10;
            fontRenderer.drawStringWithShadow(bossName, textX, textY, 0xFFFFFF); // 0xFFFFFF = 白色

            // 恢复OpenGL状态（防止颜色污染后续渲染）
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(icons); // 重新绑定默认HUD纹理
        }
    }

    /**
     * 渲染南瓜头遮挡效果（当玩家佩戴南瓜头盔时出现的屏幕模糊效果）
     *
     * @param screenWidth  当前屏幕宽度（像素）
     * @param screenHeight 当前屏幕高度（像素）
     *
     * 实现原理：
     * 1. 修改OpenGL渲染状态准备2D渲染：
     *    - 禁用深度测试（避免遮挡其他元素）
     *    - 关闭深度写入（防止影响深度缓冲）
     *    - 设置透明混合模式（使用标准透明度混合）
     *    - 禁用alpha测试（确保完整显示半透明纹理）
     * 2. 绑定南瓜模糊纹理（assets/minecraft/textures/misc/pumpkinblur.png）
     * 3. 使用全屏四边形绘制模糊效果：
     *    - 顶点坐标覆盖整个屏幕空间
     *    - UV纹理坐标完整映射纹理（注意Y轴翻转）
     * 4. 恢复原始OpenGL渲染状态
     *
     * 技术细节：
     * - 使用-90的Z坐标确保渲染在大多数HUD元素之上（但低于GUI界面）
     * - 通过两次glColor4f调用保证颜色状态正确性
     * - 使用立即模式渲染（Tessellator）适合简单全屏效果
     */
    protected void renderPumpkinBlur(int screenWidth, int screenHeight) {
        // ================== 准备渲染状态 ==================
        GL11.glDisable(GL11.GL_DEPTH_TEST);     // 禁用深度测试（不需要3D排序）
        GL11.glDepthMask(false);                // 关闭深度缓冲写入
        OpenGlHelper.glBlendFunc(
                770, 771, 1, 0);                   // 设置混合模式：SRC_ALPHA, ONE_MINUS_SRC_ALPHA
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // 设置纯白不透明颜色
        GL11.glDisable(GL11.GL_ALPHA_TEST);     // 禁用alpha测试（显示所有像素）

        // ================== 纹理绑定 ==================
        this.mc.getTextureManager().bindTexture(pumpkinBlurTexPath); // 绑定南瓜模糊纹理

        // ================== 几何体绘制 ==================
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();        // 开始绘制四边形
        // 定义四个顶点（顺时针顺序，Z=-90确保在HUD层之上）
        tessellator.addVertexWithUV(0.0D, screenHeight, -90.0D, 0.0D, 1.0D);      // 左下：纹理(0,1)
        tessellator.addVertexWithUV(screenWidth, screenHeight, -90.0D, 1.0D, 1.0D); // 右下：纹理(1,1)
        tessellator.addVertexWithUV(screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);         // 右上：纹理(1,0)
        tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);              // 左上：纹理(0,0)
        tessellator.draw(); // 提交绘制

        // ================== 恢复渲染状态 ==================
        GL11.glDepthMask(true);                 // 重新启用深度写入
        GL11.glEnable(GL11.GL_DEPTH_TEST);      // 恢复深度测试
        GL11.glEnable(GL11.GL_ALPHA_TEST);      // 恢复alpha测试
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // 重置颜色状态（防止影响后续渲染）
    }

    /**
     * 渲染渐晕效果（Vignette Effect），用于模拟视野边缘变暗的效果
     *
     * @param vignetteBrightness 渐晕亮度值（范围：0.0-1.0，值越小越暗）
     * @param screenWidth        屏幕宽度（像素）
     * @param screenHeight       屏幕高度（像素）
     *
     * 实现原理：
     * 1. 亮度值处理：
     *    - 反转亮度值（1.0 - brightness），使输入值与视觉效果一致
     *    - 限制亮度值在有效范围内（0.0-1.0）
     *    - 使用平滑插值避免亮度突变（基于前一帧亮度值）
     * 2. 修改OpenGL渲染状态：
     *    - 禁用深度测试（不需要3D排序）
     *    - 关闭深度写入（防止影响深度缓冲）
     *    - 设置自定义混合模式（GL_ZERO, GL_ONE_MINUS_SRC_COLOR）
     * 3. 绑定渐晕纹理（assets/minecraft/textures/misc/vignette.png）
     * 4. 使用全屏四边形绘制渐晕效果：
     *    - 顶点坐标覆盖整个屏幕空间
     *    - UV纹理坐标完整映射纹理（注意Y轴翻转）
     * 5. 恢复原始OpenGL渲染状态
     *
     * 技术细节：
     * - 使用-90的Z坐标确保渲染在大多数HUD元素之上（但低于GUI界面）
     * - 通过平滑插值实现渐晕效果的平滑过渡
     * - 使用自定义混合模式实现边缘变暗效果
     */
    protected void renderVignette(float vignetteBrightness, int screenWidth, int screenHeight) {
        // ================== 亮度值处理 ==================
        // 反转亮度值（使输入值与视觉效果一致）
        vignetteBrightness = 1.0F - vignetteBrightness;

        // 限制亮度值在有效范围内
        if (vignetteBrightness < 0.0F) vignetteBrightness = 0.0F;
        if (vignetteBrightness > 1.0F) vignetteBrightness = 1.0F;

        // 使用平滑插值避免亮度突变（基于前一帧亮度值）
        this.previousVignetteBrightness = (float)(
                (double)this.previousVignetteBrightness +
                        (double)(vignetteBrightness - this.previousVignetteBrightness) * 0.01D
        );

        // ================== 准备渲染状态 ==================
        GL11.glDisable(GL11.GL_DEPTH_TEST);     // 禁用深度测试（不需要3D排序）
        GL11.glDepthMask(false);                // 关闭深度缓冲写入
        OpenGlHelper.glBlendFunc(0, 769, 1, 0); // 设置混合模式：GL_ZERO, GL_ONE_MINUS_SRC_COLOR
        GL11.glColor4f(
                this.previousVignetteBrightness,
                this.previousVignetteBrightness,
                this.previousVignetteBrightness,
                1.0F
        ); // 设置颜色（灰度值由亮度决定）

        // ================== 纹理绑定 ==================
        this.mc.getTextureManager().bindTexture(vignetteTexPath); // 绑定渐晕纹理

        // ================== 几何体绘制 ==================
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();        // 开始绘制四边形
        // 定义四个顶点（顺时针顺序，Z=-90确保在HUD层之上）
        tessellator.addVertexWithUV(0.0D, screenHeight, -90.0D, 0.0D, 1.0D);      // 左下：纹理(0,1)
        tessellator.addVertexWithUV(screenWidth, screenHeight, -90.0D, 1.0D, 1.0D); // 右下：纹理(1,1)
        tessellator.addVertexWithUV(screenWidth, 0.0D, -90.0D, 1.0D, 0.0D);         // 右上：纹理(1,0)
        tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);              // 左上：纹理(0,0)
        tessellator.draw(); // 提交绘制

        // ================== 恢复渲染状态 ==================
        GL11.glDepthMask(true);                 // 重新启用深度写入
        GL11.glEnable(GL11.GL_DEPTH_TEST);      // 恢复深度测试
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // 重置颜色状态（防止影响后续渲染）
        OpenGlHelper.glBlendFunc(770, 771, 1, 0); // 恢复标准混合模式
    }

    /**
     * 渲染传送门的全屏覆盖特效
     * @param alphaParam 透明度参数，范围通常为0.0-1.0，用于控制效果淡入淡出
     * @param width 屏幕/视口的宽度（单位：像素）
     * @param height 屏幕/视口的高度（单位：像素）
     */
    protected void func_130015_b(float alphaParam, int width, int height)
    {
        // region 透明度计算
        // 当透明度未饱和时进行平滑过渡计算
        if (alphaParam < 1.0F)
        {
            // 使用四次方曲线创建渐变动画（0.8为强度系数，0.2为最小基础值）
            alphaParam *= alphaParam;      // 线性→二次曲线
            alphaParam *= alphaParam;      // 二次→四次曲线（加速度更快）
            alphaParam = alphaParam * 0.8F + 0.2F;  // 压缩亮度范围并提升基线
        }
        // endregion

        // region OpenGL状态配置
        GL11.glDisable(GL11.GL_ALPHA_TEST);   // 禁用透明度测试（需要混合透明像素）
        GL11.glDisable(GL11.GL_DEPTH_TEST);   // 禁用深度测试（避免被场景物体遮挡）
        GL11.glDepthMask(false);              // 禁用深度缓冲写入（不影响现有深度缓冲）
        OpenGlHelper.glBlendFunc(
                770, 771,    // 源因子：GL_SRC_ALPHA（770），目标因子：GL_ONE_MINUS_SRC_ALPHA（771）
                1, 0         // 未使用的参数（保留默认值）
        );
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alphaParam); // 设置白色叠加色及计算后的透明度
        // endregion

        // region 纹理准备
        IIcon portalIcon = Blocks.portal.getBlockTextureFromSide(1); // 获取传送门方块侧面纹理
        // 绑定方块纹理图集到当前渲染状态
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        // 获取纹理坐标信息（UV范围：0.0-1.0）
        float minU = portalIcon.getMinU();
        float minV = portalIcon.getMinV();
        float maxU = portalIcon.getMaxU();
        float maxV = portalIcon.getMaxV();
        // endregion

        // region 几何体绘制
        Tessellator tessellator = Tessellator.instance; // 获取Tessellator实例
        tessellator.startDrawingQuads();                // 开始绘制四边形

        // 定义屏幕对齐四边形（Z轴固定为-90，位于近平面前方）
        // 左上顶点（左下纹理坐标）
        tessellator.addVertexWithUV(0.0D, (double)height, -90.0D, (double)minU, (double)maxV);
        // 右上顶点（右下纹理坐标）
        tessellator.addVertexWithUV((double)width, (double)height, -90.0D, (double)maxU, (double)maxV);
        // 右下顶点（右上纹理坐标）
        tessellator.addVertexWithUV((double)width, 0.0D, -90.0D, (double)maxU, (double)minV);
        // 左下顶点（左上纹理坐标）
        tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, (double)minU, (double)minV);

        tessellator.draw(); // 提交绘制数据到GPU
        // endregion

        // region 恢复OpenGL状态
        GL11.glDepthMask(true);           // 重新启用深度缓冲写入
        GL11.glEnable(GL11.GL_DEPTH_TEST);// 恢复深度测试
        GL11.glEnable(GL11.GL_ALPHA_TEST);// 恢复透明度测试
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // 重置颜色状态为不透明白色
        // endregion
    }

    /**
     * 渲染指定物品栏槽位的物品及其状态（用于物品栏/容器界面）
     *
     * @param slotIndex    槽位索引（0-8对应快捷栏，9-35对应主物品栏）
     * @param xPos         渲染起始X坐标（屏幕坐标系）
     * @param yPos         渲染起始Y坐标（屏幕坐标系）
     * @param partialTicks 部分刻时间（用于动画插值，范围0.0-1.0）
     *
     * 方法逻辑：
     * 1. 获取指定槽位的物品堆栈
     * 2. 处理物品拾取动画：
     *    - 当物品被新拾取时播放缩放动画
     *    - 动画持续时间基于animationsToGo属性
     * 3. 渲染物品主体：
     *    - 包含基础图标、附魔光效等
     * 4. 渲染覆盖层信息：
     *    - 物品数量、耐久度条、特殊状态标识
     */
    protected void renderInventorySlot(int slotIndex, int xPos, int yPos, float partialTicks) {
        // 从玩家主物品栏获取物品
        ItemStack itemStack = this.mc.thePlayer.inventory.mainInventory[slotIndex];

        if (itemStack != null) {
            // 计算剩余动画时间（animationsToGo表示剩余动画帧数）
            float remainingAnimation = (float)itemStack.animationsToGo - partialTicks;

            // 物品拾取动画处理（弹跳效果）
            if (remainingAnimation > 0.0F) {
                GL11.glPushMatrix(); // 保存当前矩阵状态

                // 计算动画缩放参数（随时间衰减）
                float scaleFactor = 1.0F + remainingAnimation / 5.0F;

                // 变换坐标系到物品中心点
                GL11.glTranslatef(xPos + 8.0F, yPos + 12.0F, 0.0F); // 8,12为16x16图标的中心偏移
                GL11.glScalef(1.0F / scaleFactor, (scaleFactor + 1.0F) / 2.0F, 1.0F); // X轴缩小，Y轴拉伸

                // 将坐标系还原回原始绘制位置
                GL11.glTranslatef(-(xPos + 8.0F), -(yPos + 12.0F), 0.0F);
            }

            // 渲染物品主体（图标+附魔光效）
            itemRenderer.renderItemAndEffectIntoGUI(
                    this.mc.fontRenderer,
                    this.mc.getTextureManager(),
                    itemStack,
                    xPos,
                    yPos
            );

            // 恢复矩阵状态（如果执行过动画变换）
            if (remainingAnimation > 0.0F) {
                GL11.glPopMatrix();
            }

            // 渲染物品覆盖层（数量/耐久度等）
            itemRenderer.renderItemOverlayIntoGUI(
                    this.mc.fontRenderer,
                    this.mc.getTextureManager(),
                    itemStack,
                    xPos,
                    yPos
            );
        }
    }

    /**
     * 游戏内界面定时更新逻辑（每游戏刻调用一次）
     *
     * 主要功能：
     * 1. 管理唱片播放提示的显示时长
     * 2. 更新全局界面计数器
     * 3. 维护直播状态指示器
     * 4. 处理手持物品的高亮效果逻辑
     */
    public void updateTick() {
        // ============== 唱片播放提示管理 ==============
        // 递减唱片提示显示计时（当值>0时每刻减少1）
        if (this.recordDisplayTime > 0) {
            this.recordDisplayTime--;
        }

        // ============== 全局计数器更新 ==============
        this.updateCounter++; // 用于动画计时等需要连续递增值的场景

        // ============== 直播指示器更新 ==============
        this.streamIndicator.func_152439_a(); // 更新直播状态（数据获取/网络连接检测等）

        // ============== 手持物品高亮逻辑 ==============
        if (this.mc.thePlayer != null) {
            ItemStack currentItem = this.mc.thePlayer.inventory.getCurrentItem();

            // 空手状态处理
            if (currentItem == null) {
                this.highlightDuration = 0; // 立即取消高亮
            }
            // 手持物品未发生变化时
            else if (this.highlightedItemStack != null
                    && currentItem.getItem() == this.highlightedItemStack.getItem() // 同类型物品
                    && ItemStack.areItemStackTagsEqual(currentItem, this.highlightedItemStack) // NBT数据一致
                    && (currentItem.isItemStackDamageable() || // 可损坏物品或
                    currentItem.getItemDamage() == this.highlightedItemStack.getItemDamage())) { // 相同元数据
                // 递减高亮持续时间（当剩余时间>0时）
                if (this.highlightDuration > 0) {
                    this.highlightDuration--;
                }
            }
            // 手持物品发生变化时
            else {
                this.highlightDuration = 40; // 重置高亮持续时间（约2秒，20tick/秒）
            }

            // 更新当前高亮物品引用
            this.highlightedItemStack = currentItem;
        }
    }



    /**
     * 设置当前播放的唱片界面提示信息
     * <p>
     * 主要功能：
     * 1. 生成带唱片名称的本地化提示文本
     * 2. 将生成文本提交至界面显示系统
     */
    public void setRecordPlayingMessage(String p_73833_1_) {
        // ============== 本地化消息构建 ==============
        // 根据语言文件中的"record.nowPlaying"键生成提示文本
        // 使用传入参数填充占位符（通常为唱片名称/ID）
        String formattedMsg = I18n.format("record.nowPlaying", new Object[] {p_73833_1_});

        // ============== 界面信息更新 ==============
        // 调用内部渲染方法显示提示（true参数表示强制立即更新显示）
        this.showRecordMessage(formattedMsg, true);
    }

    /**
     * 内部方法：提交唱片播放信息至显示系统
     * <p>
     * 主要功能：
     * 1. 设置当前播放的唱片名称/信息
     * 2. 初始化唱片提示显示时长
     * 3. 同步播放状态同步
     *
     * @param p_110326_1_ 显示文本（已本地化处理的完整信息）
     * @param p_110326_2_ 当前是否正在播放的状态标识
     */
    public void showRecordMessage(String p_110326_1_, boolean p_110326_2_) {
        // ============== 唱片信息设置 ==============
        // 更新当前需要显示的完整唱片信息（包含曲目名称等）
        this.currentRecordName = p_110326_1_;

        // ============== 显示时间初始化 ==============
        // 设置固定显示时长（60游戏刻 = 60/20 = 3秒）
        this.recordDisplayTime = 60;

        // ============== 状态同步 ==============
        // 更新播放状态标志（用于控制界面元素显隐/动画等）
        this.isRecordPlaying = p_110326_2_;
    }

    /**
     * 获取持久聊天窗口的指针，该窗口包含所有历史聊天记录等
     *
     * @return 持久聊天窗口的实例（通常用于显示和管理聊天记录）
     */
    public GuiNewChat getChatGUI() {
        return this.persistentChatGUI;
    }

    /**
     * 获取当前更新的计数器值
     * <p>
     * 主要用途：
     * 1. 用于动画计时、周期性事件等需要连续递增值的场景
     *
     * @return 当前的全局更新计数器值
     */
    public int getUpdateCounter() {
        return this.updateCounter;
    }
}