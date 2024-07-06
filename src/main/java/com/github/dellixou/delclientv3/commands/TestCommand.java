package com.github.dellixou.delclientv3.commands;

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
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // Obtenir les noms des familles de polices disponibles
        String[] fontFamilies = ge.getAvailableFontFamilyNames();

        // Afficher les noms des familles de polices
        for (String fontName : fontFamilies) {
            System.out.println(fontName);
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
