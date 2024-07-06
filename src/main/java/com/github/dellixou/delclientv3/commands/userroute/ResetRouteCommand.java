package com.github.dellixou.delclientv3.commands.userroute;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ResetRouteCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "resetroute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Reset the current selected edited route.";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
        if (userRoute.currentEditRoute == null){
            DelClient.sendChatToClient("&cError : &8Please name the route before reset!");
            return;
        }
        DelClient.sendChatToClient("&aRoute : &7" + userRoute.currentEditRoute.getName() + " &areset !");
        userRoute.currentEditRoute.resetWaypoints();
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
