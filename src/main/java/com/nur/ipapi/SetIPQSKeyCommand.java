package com.nur.ipapi;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class SetIPQSKeyCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "setipqskey";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "setipqskey <api key>";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "/setipqskey <api key>"));
            return;
        }
        try {
            Main.setIPQSApiKey(args[0]);
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.RED + "Error occured (likely an invalid API key)!"));
            e.printStackTrace();
            return;
        }
        sender.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "(!) " + EnumChatFormatting.GREEN + "Set API Key!"));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
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
