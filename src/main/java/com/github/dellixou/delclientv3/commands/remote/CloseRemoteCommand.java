package com.github.dellixou.delclientv3.commands.remote;

import com.github.dellixou.delclientv3.DelClient;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CloseRemoteCommand implements ICommand {

    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "closeremote";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Close the remote control for Discord.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if(DiscordBotInstance.getStatusWebSocket() == StatusWebSocket.OPENED){
                DelClient.sendRemoteChat("Closing the web socket. &8This can maybe freeze your game for some seconds.");

                JDA jda = DiscordBotInstance.getJda();
                if (jda != null) {
                    jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                    jda.shutdown();

                    if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                        jda.shutdownNow();
                        jda.awaitShutdown();
                    }

                    DiscordBotInstance.setJda(null);
                }

                UngrabUtils.regrabMouse();

                DiscordBotInstance.setStatusWebSocket(StatusWebSocket.CLOSED);
                DelClient.sendRemoteChat("&aSuccessful!&7 You closed the remote control web socket.");
            }else{
                DelClient.sendRemoteChat("&cError!&7 Web socket is already closed.");
            }
        } catch (InterruptedException e) {
            DelClient.sendRemoteChat("&cError!&7 Web socket is already closed.");
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
