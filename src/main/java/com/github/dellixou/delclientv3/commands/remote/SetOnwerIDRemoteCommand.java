package com.github.dellixou.delclientv3.commands.remote;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.remote.DiscordBotInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SetOnwerIDRemoteCommand implements ICommand {

    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "setownerid";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Set the owner ID for the remote control.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if(args.length > 0){
            DelClient.fileManager.setRemoteOwnerID(args[0]);
            DiscordBotInstance.ownerID = DelClient.fileManager.getRemoteOwnerID();
            DelClient.sendRemoteChat("&aSuccessful! You changed the owner ID!");
        }else{
            DelClient.sendWarning("Please provide a valid ID, you can read the guide in the Discord to know how to setup the bot.");
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
