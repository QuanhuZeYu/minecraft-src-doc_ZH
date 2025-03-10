package net.minecraft.client.main;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

@SideOnly(Side.CLIENT)
public class Main {
    // 定义一个静态常量，表示复杂泛型类型 Map<String, Collection<String>>
    private static final java.lang.reflect.Type field_152370_a = new ParameterizedType() {

        // 反编译混淆标识符（通常可以忽略）
        private static final String __OBFID = "CL_00000828";

        @Override
        public java.lang.reflect.Type[] getActualTypeArguments() {
            // 返回当前泛型的具体类型参数：第一个是String，第二个是嵌套的ParameterizedType
            return new java.lang.reflect.Type[] {
                    String.class,
                    new ParameterizedType() { // 表示 Collection<String>
                        private static final String __OBFID = "CL_00001836";

                        @Override
                        public java.lang.reflect.Type[] getActualTypeArguments() {
                            // Collection的类型参数是 String
                            return new java.lang.reflect.Type[] { String.class };
                        }

                        @Override
                        public java.lang.reflect.Type getRawType() {
                            // 原始类型是 Collection 接口
                            return Collection.class;
                        }

                        @Override
                        public java.lang.reflect.Type getOwnerType() {
                            // 不属于任何外围类（顶层类型）
                            return null;
                        }
                    }
            };
        }

        @Override
        public java.lang.reflect.Type getRawType() {
            // 当前泛型的原始类型是 Map 接口
            return Map.class;
        }

        @Override
        public java.lang.reflect.Type getOwnerType() {
            // 不属于任何外围类（顶层类型）
            return null;
        }
    };
    private static final String __OBFID = "CL_00001461";

    public static void main(String[] p_main_0_) {
        // 设置系统属性优先使用IPv4协议栈
        System.setProperty("java.net.preferIPv4Stack", "true");

        // 创建命令行参数解析器
        OptionParser optionparser = new OptionParser();
        optionparser.allowsUnrecognizedOptions(); // 允许未知参数存在
        optionparser.accepts("demo");
        optionparser.accepts("fullscreen");
        // 允许的参数：server、port、gameDir、assetsDir、resourcePackDir、proxyHost、proxyPort、proxyUser、proxyPass、username、uuid、accessToken、version、width、height、userProperties、assetIndex、userType
        // 其中，server、port、gameDir、assetsDir、proxyHost、proxyPort、proxyUser、proxyPass、username、uuid、version、width、height、userType是必须的参数
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecServer = optionparser.accepts("server")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecPort = optionparser.accepts("port").withRequiredArg()
                .ofType(Integer.class).defaultsTo(Integer.valueOf(25565), new Integer[0]);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecGameDir = optionparser.accepts("gameDir")
                .withRequiredArg()
                .ofType(File.class).defaultsTo(new File("."), new File[0]);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecAssetsDir = optionparser.accepts("assetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecResourcePackDir = optionparser.accepts("resourcePackDir")
                .withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecProxyHost = optionparser.accepts("proxyHost")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecProxyprot = optionparser.accepts("proxyPort")
                .withRequiredArg()
                .defaultsTo("8080", new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecProxyUser = optionparser.accepts("proxyUser")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecProxyPass = optionparser.accepts("proxyPass")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecUserName = optionparser.accepts("username")
                .withRequiredArg()
                .defaultsTo("Player" + Minecraft.getSystemTime() % 1000L, new String[0]);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecUUID = optionparser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecAccessToken = optionparser.accepts("accessToken")
                .withRequiredArg().required();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecVersion = optionparser.accepts("version")
                .withRequiredArg()
                .required();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecWidth = optionparser.accepts("width").withRequiredArg()
                .ofType(Integer.class).defaultsTo(Integer.valueOf(854), new Integer[0]);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecHeight = optionparser.accepts("height").withRequiredArg()
                .ofType(Integer.class).defaultsTo(Integer.valueOf(480), new Integer[0]);
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecUserProperties = optionparser.accepts("userProperties")
                .withRequiredArg().required();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecAssetIndex = optionparser.accepts("assetIndex")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec argumentacceptingoptionspecUserType = optionparser.accepts("userType")
                .withRequiredArg()
                .defaultsTo("legacy", new String[0]);
        NonOptionArgumentSpec nonoptionargumentspec = optionparser.nonOptions();
        OptionSet optionset = optionparser.parse(p_main_0_);
        List list = optionset.valuesOf(nonoptionargumentspec);
        String proxyHost = (String) optionset.valueOf(argumentacceptingoptionspecProxyHost);
        Proxy proxy = Proxy.NO_PROXY;

        if (proxyHost != null) {
            try {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(proxyHost,
                        ((Integer) optionset.valueOf(argumentacceptingoptionspecProxyprot)).intValue()));
            } catch (Exception exception) {
                ;
            }
        }

        final String proxyUser = (String) optionset.valueOf(argumentacceptingoptionspecProxyUser);
        final String proxyPass = (String) optionset.valueOf(argumentacceptingoptionspecProxyPass);

        if (!proxy.equals(Proxy.NO_PROXY) && func_110121_a(proxyUser) && func_110121_a(proxyPass)) {
            Authenticator.setDefault(new Authenticator() {
                private static final String __OBFID = "CL_00000829";

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                }
            });
        }

        int width = ((Integer) optionset.valueOf(argumentacceptingoptionspecWidth)).intValue();
        int height = ((Integer) optionset.valueOf(argumentacceptingoptionspecHeight)).intValue();
        boolean isFullscreen = optionset.has("fullscreen");
        boolean isDemo = optionset.has("demo");
        String version = (String) optionset.valueOf(argumentacceptingoptionspecVersion);
        HashMultimap userPropertiesMap = HashMultimap.create();
        Iterator iteratorUserProperties = ((Map) (new Gson()).fromJson(
                (String) optionset.valueOf(argumentacceptingoptionspecUserProperties),
                field_152370_a)).entrySet().iterator();

        while (iteratorUserProperties.hasNext()) {
            Entry entry = (Entry) iteratorUserProperties.next();
            userPropertiesMap.putAll(entry.getKey(), (Iterable) entry.getValue());
        }

        File gameDir = (File) optionset.valueOf(argumentacceptingoptionspecGameDir);
        File AssetsDir = optionset.has(argumentacceptingoptionspecAssetsDir)
                ? (File) optionset.valueOf(argumentacceptingoptionspecAssetsDir)
                : new File(gameDir, "assets/");
        File resourcePackDir = optionset.has(argumentacceptingoptionspecResourcePackDir)
                ? (File) optionset.valueOf(argumentacceptingoptionspecResourcePackDir)
                : new File(gameDir, "resourcepacks/");
        String UUID = optionset.has(argumentacceptingoptionspecUUID)
                ? (String) argumentacceptingoptionspecUUID.value(optionset)
                : (String) argumentacceptingoptionspecUserName.value(optionset);
        String assetIndex = optionset.has(argumentacceptingoptionspecAssetIndex)
                ? (String) argumentacceptingoptionspecAssetIndex.value(optionset)
                : null;
        Session session = new Session((String) argumentacceptingoptionspecUserName.value(optionset), UUID,
                (String) argumentacceptingoptionspecAccessToken.value(optionset),
                (String) argumentacceptingoptionspecUserType.value(optionset));
        // 创建Minecraft实例
        Minecraft minecraft = new Minecraft(session, width, height, isFullscreen, isDemo, gameDir, AssetsDir,
                resourcePackDir,
                proxy,
                version,
                userPropertiesMap,
                assetIndex);
        String server = (String) optionset.valueOf(argumentacceptingoptionspecServer);

        if (server != null) {
            minecraft.setServer(server, ((Integer) optionset.valueOf(argumentacceptingoptionspecPort)).intValue());
        }

        // 添加JVM关闭钩子（优雅终止机制）
        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            private static final String __OBFID = "CL_00001835"; // 反编译混淆标识符

            @Override
            public void run() {
                // 在JVM关闭时执行：停止集成服务器
                Minecraft.stopIntegratedServer();
            }
        });

        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }

        Thread.currentThread().setName("Client thread");
        minecraft.run();
    }

    private static boolean func_110121_a(String p_110121_0_) {
        return p_110121_0_ != null && !p_110121_0_.isEmpty();
    }
}