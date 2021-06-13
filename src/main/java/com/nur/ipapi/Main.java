package com.nur.ipapi;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Main.MODID, name = Main.NAME, version = Main.VERSION, acceptedMinecraftVersions = "[1.8.8,1.8.9]")
public class Main {
    public static final String MODID = "ipapi";
    public static final String NAME = "IP API";
    public static final String VERSION = "1.1";

    public static String ipHubApiKey = "";
    public static String ipQsApiKey = "";
    public static boolean apiKeyNotSetWarningSent = false;

    public static void setAPIKey(String arg) {
        ipHubApiKey = arg;
        ConfigHandler.setString("api.ipHub", "key", arg);
    }

    public static void setIPQSApiKey(String arg) {
        ipQsApiKey = arg;
        ConfigHandler.setString("api.ipQs", "key", arg);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChatHandler());
        
        ClientCommandHandler.instance.registerCommand(new IPInfoCommand());
        ClientCommandHandler.instance.registerCommand(new ToggleIPCheckCommand());
        ClientCommandHandler.instance.registerCommand(new SetIPHubKeyCommand());
        ClientCommandHandler.instance.registerCommand(new SetIPQSKeyCommand());

        ipHubApiKey = ConfigHandler.getString("api.ipHub", "key", "");
        ipQsApiKey = ConfigHandler.getString("api.ipQs", "key", "");
    }
}
