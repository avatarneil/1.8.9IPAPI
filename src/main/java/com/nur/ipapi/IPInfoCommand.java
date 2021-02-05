package com.nur.ipapi;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nur.ipapi.ChatHandler.isJson;

public class IPInfoCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "ipinfo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "ipinfo <ip>";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Please provide an IP Address!"));
            return;
        }
        String zeroTo255 = "(25[0-5]|2[0-4][0-9]|[01]?[0-9]{1,2})";
        String IP_REGEXP = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
        Pattern IP_PATTERN = Pattern.compile(IP_REGEXP);
        final Matcher m = IP_PATTERN.matcher(args[0]);
        if (m.find()) {
            final String IP = m.group(0);
            final ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/alts " + IP));
            new Thread(new Runnable() {
                public void run() {
                    HttpURLConnection con = null;
                    try {
                        URL url = new URL("http://v2.api.iphub.info/ip/" + IP);
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
                        if (!isJson(response.toString())) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error while scanning " + IP).setChatStyle(style));
                            return;
                        }
                        JsonObject j = ChatHandler.parser.parse(response.toString()).getAsJsonObject();
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.WHITE + "IP Information: " + EnumChatFormatting.RED + IP).setChatStyle(style));

                        switch (j.get("block").getAsInt()) {
                            case 0:
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Type: " + EnumChatFormatting.GREEN + "Good").setChatStyle(style));
                                break;
                            case 1:
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Type: " + EnumChatFormatting.RED + "Bad").setChatStyle(style));
                                break;
                            case 2:
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Type: " + EnumChatFormatting.YELLOW + "Mixed").setChatStyle(style));
                                break;
                            default:
                                //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error while scanning " + m.group(0)).setChatStyle(style));
                        }

                        String countryName = j.get("countryName").getAsString();
                        if ("ZZ".equals(countryName)) countryName = "Planet Earth";

                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Country: " + EnumChatFormatting.RED + countryName).setChatStyle(style));
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "ISP: " + EnumChatFormatting.RED + j.get("isp").getAsString()).setChatStyle(style));
                    } catch (IOException e) {
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
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + args[0] + " is not a valid IP Address!"));
            return;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return new ArrayList<String>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
