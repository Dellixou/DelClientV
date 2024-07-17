package com.github.dellixou.delclientv3.commands;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoForaging;
import com.github.dellixou.delclientv3.modules.macro.AutoPowder;
import com.github.dellixou.delclientv3.modules.macro.AutoPowderV2;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;

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
        RotationUtils.smoothLook(RotationUtils.getRotationToBlock(new BlockPos(0, 0, 0)), 6, null);
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
