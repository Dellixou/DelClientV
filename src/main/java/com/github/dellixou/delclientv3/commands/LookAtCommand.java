package com.github.dellixou.delclientv3.commands;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.movements.PlayerLookSmooth;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LookAtCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "lookatc";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Make player look at a point smoothly.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        float x = 0;
        float y = 0;
        float z= 0;
        float duration = 1000;
        if(args.length == 4){
            x = Float.parseFloat(args[0]);
            y = Float.parseFloat(args[1]);
            z = Float.parseFloat(args[2]);
            duration = Float.parseFloat(args[3]);
        }else{
            DelClient.sendChatToClient("&cLook command error : &7Usage /lookatc <x> <y> <z> <duration>");
            return;
        }
        //PlayerLookSmooth.lookAtBlock(x, y, z, duration);
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
