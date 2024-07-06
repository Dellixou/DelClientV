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

public class RemoveLastWaypointCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "removelw";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Remove Last Waypoint";
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
            DelClient.sendChatToClient("&cError : &8Please select a route before editing!");
            return;
        }
        DelClient.sendChatToClient("&7Last point removed : &8" + userRoute.currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9User Route in route : " + userRoute.currentEditRoute.getName() +  " -> Removed Last Waypoint");
        userRoute.currentEditRoute.removeLastWaypoint();
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
