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

	@SubscribeEvent
    public void onOtherChat(ClientChatReceivedEvent event)
    {
    	if(!toggled)return;
		String msg = event.message.getUnformattedText();
		if(msg.contains("IP Address")||msg.contains("Scanning")||(msg.contains("*")&&msg.contains(":")&&msg.contains("[")&&msg.contains("]"))||msg.contains("Last used IP:")) {
			String zeroTo255 = "(25[0-5]|2[0-4][0-9]|[01]?[0-9]{1,2})";
			String IP_REGEXP = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
			Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);
			Matcher m = IP_PATTERN.matcher(msg);
			if(m.find()) {
				if("".equals(Main.apiKey)) {
					if(!Main.apiKeyNotSetWarningSent) {
						event.message.appendSibling(new ChatComponentText(EnumChatFormatting.DARK_RED+" (API Key not set)")).setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/setiphubkey ")));
						Main.apiKeyNotSetWarningSent = true;
					}
					return;
				}
				final String IP = m.group(0);
				//ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://iphub.info/?ip="+IP));
				final ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/alts "+IP));
				if (IPHandler.isCached(IP)) {
					IPHubResult cachedResult = IPHandler.getCached(IP);
					if(cachedResult!=null) {
						event.message.appendSibling(new ChatComponentText(" "+cachedResult.getFormattedStringWithParentheses()));
						event.message.setChatStyle(style);
						return;
					}
				}
				HttpURLConnection con = null;
				try {
					URL url = new URL("http://v2.api.iphub.info/ip/"+IP);
					con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("GET");
					con.setRequestProperty("User-Agent", "Mozilla/5.0");
					con.addRequestProperty("X-Key", Main.apiKey);
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					if(!isJson(response.toString())) {
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""+EnumChatFormatting.RED+EnumChatFormatting.BOLD+"(!) "+EnumChatFormatting.RED+"Error while scanning "+IP).setChatStyle(style));
						event.message.setChatStyle(style);
						return;
					}
					JsonObject j = parser.parse(response.toString()).getAsJsonObject();

					int blockInt = j.get("block").getAsInt();
					IPHubResult result = IPHubResult.fromBlock(blockInt);
					if(result == null)Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""+EnumChatFormatting.RED+EnumChatFormatting.BOLD+"(!) "+EnumChatFormatting.RED+"Error while scanning "+IP).setChatStyle(style));
					else {
						event.message.appendSibling(new ChatComponentText(" "+result.getFormattedStringWithParentheses()));
						event.message.setChatStyle(style);
						IPHandler.cache(IP, result);
					}
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
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""+EnumChatFormatting.RED+EnumChatFormatting.BOLD+"(!) "+EnumChatFormatting.RED+"Error while scanning "+IP+": "+EnumChatFormatting.UNDERLINE+j).setChatStyle(style));
					} catch (Exception exception) {
						Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""+EnumChatFormatting.RED+EnumChatFormatting.BOLD+"(!) "+EnumChatFormatting.RED+"Error while scanning "+IP).setChatStyle(style));
						exception.printStackTrace();
					}
					ex.printStackTrace();
				}
			}
		}
    }
}
