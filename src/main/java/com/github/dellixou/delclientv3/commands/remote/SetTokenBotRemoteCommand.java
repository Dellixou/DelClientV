package com.github.dellixou.delclientv3.commands.remote;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.FileUtils;
import com.github.dellixou.delclientv3.utils.UngrabUtils;
import com.github.dellixou.delclientv3.utils.remote.DiscordBotInstance;
import com.github.dellixou.delclientv3.utils.remote.enums.StatusWebSocket;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SetTokenBotRemoteCommand implements ICommand {

    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "settokenbot";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Set the token bot for the remote control.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length > 0){
            DelClient.fileManager.setRemoteToken(args[0]);
            DelClient.sendRemoteChat("&aSuccessful! You changed your token bot!");
        }else{
            DelClient.sendWarning("Please provide a valid token, you can read the guide in the Discord to know how to setup the bot.");
        }
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
    public int compareTo(ICommand iCommand) {
        return 0;
    }
}
