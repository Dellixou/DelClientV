package com.github.dellixou.delclientv3.commands;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.BlockUtils;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.*;
import com.github.dellixou.delclientv3.utils.remote.DiscordBotInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TestingCommand implements ICommand {

    public PathExecuter executer;
    private Minecraft mc = Minecraft.getMinecraft();

    public TestingCommand(PathExecuter executer) {
        this.executer = executer;
    }

    @Override
    public String getCommandName() {
        return "deltest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "TEST";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        DelClient.sendRemoteChat("" + DiscordBotInstance.getStatusRemote());
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
