package com.github.dellixou.delclientv3.commands.userroute;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SaveRoutesCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "saveroutes";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Save all routes!";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        DelClient.fileManager.saveRoutes();
        DelClient.sendChatToClient("&aSaved all routes!");
        //DelClient.sendChatToClient("&eDelClient >> &9User Route -> &aSaved all!");
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
