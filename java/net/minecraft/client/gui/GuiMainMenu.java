package net.minecraft.client.gui;

import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {

    private static final Logger logger = LogManager.getLogger();
    /** 用于主菜单屏幕的随机数生成器。 */
    private static final Random rand = new Random();
    /** 屏幕更新的计数器。 */
    private float updateCounter;
    /** 主菜单的欢迎语。 */
    private String splashText;
    private GuiButton buttonResetDemo;
    /** 用于旋转全景背景的计时器，每秒增加。 */
    private int panoramaTimer;
    /** 为主菜单的全景背景分配的纹理。 */
    private DynamicTexture viewportTexture;
    private final Object synchronizationObject = new Object();
    private String warningText;
    private String warningLinkText;
    private String warningLinkUrl;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
    /** 所有全景图片路径的数组。 */
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[] {
            new ResourceLocation("textures/gui/title/background/panorama_0.png"),
            new ResourceLocation("textures/gui/title/background/panorama_1.png"),
            new ResourceLocation("textures/gui/title/background/panorama_2.png"),
            new ResourceLocation("textures/gui/title/background/panorama_3.png"),
            new ResourceLocation("textures/gui/title/background/panorama_4.png"),
            new ResourceLocation("textures/gui/title/background/panorama_5.png")
    };
    public static final String MORE_INFO_TEXT = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int warningTextWidth;
    private int warningLinkWidth;
    private int warningTextX;
    private int warningTextY;
    private int warningLinkX;
    private int warningLinkY;
    private ResourceLocation panoramaTexture;
    private static final String __OBFID = "CL_00001154";

    public GuiMainMenu() {
        this.warningLinkText = MORE_INFO_TEXT;
        this.splashText = "missingno";
        BufferedReader bufferedreader = null;
        try {
            ArrayList<String> splashList = new ArrayList<>();
            bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));
            String s;
            while ((s = bufferedreader.readLine()) != null) {
                s = s.trim();
                if (!s.isEmpty()) {
                    splashList.add(s);
                }
            }
            if (!splashList.isEmpty()) {
                do {
                    this.splashText = splashList.get(rand.nextInt(splashList.size()));
                } while (this.splashText.hashCode() == 125780783);
            }
        } catch (IOException ioexception1) {
            // 忽略异常
        } finally {
            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                } catch (IOException ioexception) {
                    // 忽略异常
                }
            }
        }
        this.updateCounter = rand.nextFloat();
        this.warningText = "";
        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.func_153193_b()) {
            this.warningText = I18n.format("title.oldgl1", new Object[0]);
            this.warningLinkText = I18n.format("title.oldgl2", new Object[0]);
            this.warningLinkUrl = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    /**
     * 从主游戏循环中调用以更新屏幕。
     */
    public void updateScreen() {
        ++this.panoramaTimer;
    }

    /**
     * 返回当这个GUI在单机模式下显示时是否暂停游戏。
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * 当按键被按下时触发。这相当于KeyListener.keyTyped(KeyEvent e)。
     */
    protected void keyTyped(char typedChar, int keyCode) {}

    /**
     * 向屏幕添加按钮和其他控件。
     */
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.panoramaTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 11 && calendar.get(5) == 9) {
            this.splashText = "Happy birthday, ez!";
        } else if (calendar.get(2) + 1 == 6 && calendar.get(5) == 1) {
            this.splashText = "Happy birthday, Notch!";
        } else if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            this.splashText = "Merry X-mas!";
        } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            this.splashText = "Happy new year!";
        } else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }
        boolean flag = true;
        int topButtonY = this.height / 4 + 48;
        if (this.mc.isDemo()) {
            this.addDemoButtons(topButtonY, 24);
        } else {
            this.addSingleplayerMultiplayerButtons(topButtonY, 24);
        }
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, topButtonY + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, topButtonY + 72 + 12, 98, 20, I18n.format("menu.quit", new Object[0])));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, topButtonY + 72 + 12));
        synchronized (this.synchronizationObject) {
            this.warningTextWidth = this.fontRendererObj.getStringWidth(this.warningText);
            this.warningLinkWidth = this.fontRendererObj.getStringWidth(this.warningLinkText);
            int maxWidth = Math.max(this.warningTextWidth, this.warningLinkWidth);
            this.warningTextX = (this.width - maxWidth) / 2;
            this.warningTextY = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
            this.warningLinkX = this.warningTextX + maxWidth;
            this.warningLinkY = this.warningTextY + 24;
        }
    }

    /**
     * 为购买了游戏的玩家在主菜单中添加单机模式和多人模式按钮。
     */
    private void addSingleplayerMultiplayerButtons(int topButtonY, int buttonSpacing) {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, topButtonY, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, topButtonY + buttonSpacing * 1, I18n.format("menu.multiplayer", new Object[0])));
        GuiButton realmsButton = new GuiButton(14, this.width / 2 - 100, topButtonY + buttonSpacing * 2, I18n.format("menu.online", new Object[0]));
        GuiButton fmlModButton = new GuiButton(6, this.width / 2 - 100, topButtonY + buttonSpacing * 2, "Mods");
        fmlModButton.xPosition = this.width / 2 + 2;
        realmsButton.width = 98;
        fmlModButton.width = 98;
        this.buttonList.add(realmsButton);
        this.buttonList.add(fmlModButton);
    }

    /**
     * 为正在试玩的玩家在主菜单中添加试玩模式按钮。
     */
    private void addDemoButtons(int topButtonY, int buttonSpacing) {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, topButtonY, I18n.format("menu.playdemo", new Object[0])));
        this.buttonList.add(this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, topButtonY + buttonSpacing * 1, I18n.format("menu.resetdemo", new Object[0])));
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");
        if (worldinfo == null) {
            this.buttonResetDemo.enabled = false;
        }
    }

    /**
     * 当按钮被点击时触发。
     */
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }
        if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }
        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (button.id == 14) {
            this.switchToRealms();
        }
        if (button.id == 4) {
            this.mc.shutdown();
        }
        if (button.id == 6) {
            this.mc.displayGuiScreen(new GuiModList(this));
        }
        if (button.id == 11) {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }
        if (button.id == 12) {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");
            if (worldinfo != null) {
                GuiYesNo guiyesno = GuiSelectWorld.func_152129_a(this, worldinfo.getWorldName(), 12);
                this.mc.displayGuiScreen(guiyesno);
            }
        }
    }

    /**
     * 切换到 Realms 页面。
     */
    private void switchToRealms() {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    /**
     * 当确认对话框被点击时触发。
     */
    public void confirmClicked(boolean result, int id) {
        if (result && id == 12) {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        } else if (id == 13) {
            if (result) {
                try {
                    Class oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI(this.warningLinkUrl)});
                } catch (Throwable throwable) {
                    logger.error("Couldn\'t open link", throwable);
                }
            }
            this.mc.displayGuiScreen(this);
        }
    }

    /**
     * 绘制主菜单的全景背景。
     */
    private void drawPanorama(int mouseX, int mouseY, float partialTicks) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        byte b0 = 8;
        for (int k = 0; k < b0 * b0; ++k) {
            GL11.glPushMatrix();
            float f1 = ((float)(k % b0) / (float)b0 - 0.5F) / 64.0F;
            float f2 = ((float)(k / b0) / (float)b0 - 0.5F) / 64.0F;
            float f3 = 0.0F;
            GL11.glTranslatef(f1, f2, f3);
            GL11.glRotatef(MathHelper.sin(((float)this.panoramaTimer + partialTicks) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-((float)this.panoramaTimer + partialTicks) * 0.1F, 0.0F, 1.0F, 0.0F);
            for (int l = 0; l < 6; ++l) {
                GL11.glPushMatrix();
                if (l == 1) {
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                }
                if (l == 2) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                if (l == 3) {
                    GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                }
                if (l == 4) {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }
                if (l == 5) {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }
                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[l]);
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_I(16777215, 255 / (k + 1));
                float f4 = 0.0F;
                tessellator.addVertexWithUV(-1.0D, -1.0D, 1.0D, (double)(0.0F + f4), (double)(0.0F + f4));
                tessellator.addVertexWithUV(1.0D, -1.0D, 1.0D, (double)(1.0F - f4), (double)(0.0F + f4));
                tessellator.addVertexWithUV(1.0D, 1.0D, 1.0D, (double)(1.0F - f4), (double)(1.0F - f4));
                tessellator.addVertexWithUV(-1.0D, 1.0D, 1.0D, (double)(0.0F + f4), (double)(1.0F - f4));
                tessellator.draw();
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glColorMask(true, true, true, false);
        }
        tessellator.setTranslation(0.0D, 0.0D, 0.0D);
        GL11.glColorMask(true, true, true, true);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * 旋转并模糊主菜单的天空盒。
     */
    private void rotateAndBlurSkybox(float partialTicks) {
        this.mc.getTextureManager().bindTexture(this.panoramaTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        byte b0 = 3;
        for (int i = 0; i < b0; ++i) {
            tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F / (float)(i + 1));
            int j = this.width;
            int k = this.height;
            float f1 = (float)(i - b0 / 2) / 256.0F;
            tessellator.addVertexWithUV((double)j, (double)k, (double)this.zLevel, (double)(0.0F + f1), 1.0D);
            tessellator.addVertexWithUV((double)j, 0.0D, (double)this.zLevel, (double)(1.0F + f1), 1.0D);
            tessellator.addVertexWithUV(0.0D, 0.0D, (double)this.zLevel, (double)(1.0F + f1), 0.0D);
            tessellator.addVertexWithUV(0.0D, (double)k, (double)this.zLevel, (double)(0.0F + f1), 0.0D);
        }
        tessellator.draw();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColorMask(true, true, true, true);
    }

    /**
     * 渲染主菜单的天空盒。
     */
    private void renderSkybox(int mouseX, int mouseY, float partialTicks) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GL11.glViewport(0, 0, 256, 256);
        this.drawPanorama(mouseX, mouseY, partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.rotateAndBlurSkybox(partialTicks);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        float f1 = this.width > this.height ? 120.0F / (float)this.width : 120.0F / (float)this.height;
        float f2 = (float)this.height * f1 / 256.0F;
        float f3 = (float)this.width * f1 / 256.0F;
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.width;
        int l = this.height;
        tessellator.addVertexWithUV(0.0D, (double)l, (double)this.zLevel, (double)(0.5F - f2), (double)(0.5F + f3));
        tessellator.addVertexWithUV((double)k, (double)l, (double)this.zLevel, (double)(0.5F - f2), (double)(0.5F - f3));
        tessellator.addVertexWithUV((double)k, 0.0D, (double)this.zLevel, (double)(0.5F + f2), (double)(0.5F - f3));
        tessellator.addVertexWithUV(0.0D, 0.0D, (double)this.zLevel, (double)(0.5F + f2), (double)(0.5F + f3));
        tessellator.draw();
    }

    /**
     * 绘制屏幕及其所有组件。
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        Tessellator tessellator = Tessellator.instance;
        short logoWidth = 274;
        int logoX = this.width / 2 - logoWidth / 2;
        byte logoY = 30;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        this.mc.getTextureManager().bindTexture(minecraftTitleTextures);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if ((double)this.updateCounter < 1.0E-4D) {
            this.drawTexturedModalRect(logoX + 0, logoY + 0, 0, 0, 99, 44);
            this.drawTexturedModalRect(logoX + 99, logoY + 0, 129, 0, 27, 44);
            this.drawTexturedModalRect(logoX + 99 + 26, logoY + 0, 126, 0, 3, 44);
            this.drawTexturedModalRect(logoX + 99 + 26 + 3, logoY + 0, 99, 0, 26, 44);
            this.drawTexturedModalRect(logoX + 155, logoY + 0, 0, 45, 155, 44);
        } else {
            this.drawTexturedModalRect(logoX + 0, logoY + 0, 0, 0, 155, 44);
            this.drawTexturedModalRect(logoX + 155, logoY + 0, 0, 45, 155, 44);
        }
        tessellator.setColorOpaque_I(-1);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float f1 = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
        f1 = f1 * 100.0F / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
        GL11.glScalef(f1, f1, f1);
        this.drawCenteredString(this.fontRendererObj, this.splashText, 0, -8, -256);
        GL11.glPopMatrix();
        String s = "Minecraft 1.7.10";
        if (this.mc.isDemo()) {
            s = s + " Demo";
        }
        List<String> brandings = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));
        for (int i = 0; i < brandings.size(); i++) {
            String brd = brandings.get(i);
            if (!Strings.isNullOrEmpty(brd)) {
                this.drawString(this.fontRendererObj, brd, 2, this.height - (10 + i * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215);
            }
        }
        ForgeHooksClient.renderMainMenu(this, fontRendererObj, width, height);
        String s1 = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRendererObj, s1, this.width - this.fontRendererObj.getStringWidth(s1) - 2, this.height - 10, -1);
        if (this.warningText != null && this.warningText.length() > 0) {
            drawRect(this.warningTextX - 2, this.warningTextY - 2, this.warningLinkX + 2, this.warningLinkY - 1, 1428160512);
            this.drawString(this.fontRendererObj, this.warningText, this.warningTextX, this.warningTextY, -1);
            this.drawString(this.fontRendererObj, this.warningLinkText, (this.width - this.warningLinkWidth) / 2, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 当鼠标点击时触发。
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        synchronized (this.synchronizationObject) {
            if (this.warningText.length() > 0 && mouseX >= this.warningTextX && mouseX <= this.warningLinkX && mouseY >= this.warningTextY && mouseY <= this.warningLinkY) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.warningLinkUrl, 13, true);
                guiconfirmopenlink.hideWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
    }
}