package com.github.dellixou.delclientv3.commands.remote;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.PathExecuter;
import com.github.dellixou.delclientv3.utils.remote.DiscordBotInstance;
import com.github.dellixou.delclientv3.utils.remote.enums.StatusWebSocket;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class OpenRemoteCommand implements ICommand {

    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "openremote";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Open the remote control for Discord to control your player anywhere.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if(DiscordBotInstance.getStatusWebSocket() == StatusWebSocket.CLOSED){
                DelClient.sendRemoteChat("Opening the web socket. &8This can maybe freeze your game for some seconds.");
                DiscordBotInstance.initBot("MTI2MzUwMTA1Nzk0Mjc1MzMwMA.G45J0J.gboJVEdTUJiI7k8cmShHDVOny9tsTbfV7JEFNI");
                DiscordBotInstance.setStatusWebSocket(StatusWebSocket.OPENED);
                DelClient.sendRemoteChat("&aSuccessful!&7 You opened the remote control web socket.");
            }else{
                DelClient.sendRemoteChat("&cError!&7 You already opened the remote control web socket.");
            }
        } catch (InterruptedException e) {
            DelClient.sendRemoteChat("&cError!&7 Impossible to start the remote! &7Verify your bot token and settings. If need you can ask in supports channel.");
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
