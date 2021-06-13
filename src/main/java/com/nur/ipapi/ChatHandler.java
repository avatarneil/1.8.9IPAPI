package com.nur.ipapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatHandler {
    static boolean toggled = true;
    Gson gson = new Gson();
    final static JsonParser parser = new JsonParser();

    public static boolean isJson(String json) {
        try {
            parser.parse(json).getAsJsonObject();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param apiResponse
     * @return 0=good, 1=warn, 2=bad
     */
    private Integer determineRiskThreshold(JsonObject apiResponse) {
        Integer fraudScore = apiResponse.get("fraud_score").getAsInt();
        Boolean proxy = apiResponse.get("proxy").getAsBoolean();
        Boolean vpn = apiResponse.get("vpn").getAsBoolean();
        Boolean tor = apiResponse.get("tor").getAsBoolean();
        Boolean activeVpn = apiResponse.get("active_vpn").getAsBoolean();
        Boolean activeTor = apiResponse.get("active_tor").getAsBoolean();

        // See fraud_score lookup table in https://www.ipqualityscore.com/documentation/proxy-detection/overview
        if (fraudScore >= 85) {
            return 1;
        }
        if (fraudScore >= 70) {
            return 2;
        }
        if (proxy || vpn || tor || activeVpn || activeTor) {
            return 1;
        }

        return 0;
    }

    @SubscribeEvent
    public void onOtherChat(ClientChatReceivedEvent event) {
        if (!toggled) return;
        String msg = event.message.getUnformattedText();
        if (msg.contains("IP Address") || msg.contains("Scanning") || (msg.contains("*") && msg.contains(":") && msg.contains("[") && msg.contains("]")) || msg.contains("Last used IP:")) {
            String zeroTo255 = "(25[0-5]|2[0-4][0-9]|[01]?[0-9]{1,2})";
            String IP_REGEXP = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
            Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);
            Matcher m = IP_PATTERN.matcher(msg);
            if (m.find()) {
                if ("".equals(Main.apiKey)) {
                    if (!Main.apiKeyNotSetWarningSent) {
                        event.message.appendSibling(new ChatComponentText(EnumChatFormatting.DARK_RED + " (API Key not set)")).setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/setiphubkey ")));
                        Main.apiKeyNotSetWarningSent = true;
                    }
                    return;
                }
                final String IP = m.group(0);
                //ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://iphub.info/?ip="+IP));
                final ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/alts " + IP));
                if (IPHandler.isCached(IP)) {
                    IPInfo cachedResult = IPHandler.getCached(IP);
                    if (cachedResult != null) {
                        event.message.appendSibling(cachedResult.prepareRiskForDisplay());
                        event.message.setChatStyle(style);
                        return;
                    }
                }
                HttpURLConnection con = null;
                try {
                    URL url = new URL("https://ipqualityscore.com/api/json/ip" + "/" + Main.apiKey + "/" + IP);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    if (!isJson(response.toString())) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error while scanning " + IP).setChatStyle(style));
                        event.message.setChatStyle(style);
                        return;
                    }
                    JsonObject j = parser.parse(response.toString()).getAsJsonObject();

                    IPInfo parsedInfo = new IPInfo(j);
                    event.message.appendSibling(parsedInfo.prepareRiskForDisplay());
                    event.message.setChatStyle(style);
                    IPHandler.cache(IP, parsedInfo);

                } catch (Exception ex) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                        String inputLine;
                        StringBuilder errorMessage = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            errorMessage.append(inputLine);
                        }
                        in.close();
                        String j = ChatHandler.parser.parse(errorMessage.toString()).getAsJsonObject().get("error").getAsString();
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error while scanning " + IP + ": " + EnumChatFormatting.UNDERLINE + j).setChatStyle(style));
                    } catch (Exception exception) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error while scanning " + IP).setChatStyle(style));
                        exception.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
        }
    }
}
