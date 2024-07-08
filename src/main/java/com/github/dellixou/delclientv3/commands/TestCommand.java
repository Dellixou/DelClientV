package com.github.dellixou.delclientv3.commands;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoPowder;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TestCommand implements ICommand {
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
        AutoPowder autoPowder = (AutoPowder) ModuleManager.getModuleById("auto_powder");
        autoPowder.detectedChests.clear();
        autoPowder.pathFinder.realChestsPosition.clear();
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
