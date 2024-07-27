package com.github.dellixou.delclientv3.commands.misc;

import com.github.dellixou.delclientv3.events.gui.OpenClickGUIEvent;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class OpenGUICommand implements ICommand {
    @Override
    public String getCommandName() {
        return "delclient";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Open GUI";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        commandAliases.add("dc");
        commandAliases.add("delzzz");
        commandAliases.add("delcheat");
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftForge.EVENT_BUS.register(new OpenClickGUIEvent());
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
