package com.github.dellixou.delclientv3.commands.userroute;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.utils.misc.Route;
import com.github.dellixou.delclientv3.utils.misc.RouteItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SwitchRouteCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "switchedit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Place a waypoint for auto route!";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
        try{
            Route croute = userRoute.findRoute(args[0]);
            userRoute.currentEditRoute = croute;
            //DelClient.sendChatToClient("&eDelClient >> &9User Route -> Switched route to route : " + croute.getName());
            DelClient.sendChatToClient("&7Edit switched to : &8" + userRoute.currentEditRoute.getName());
        }catch (Exception e){
            DelClient.sendChatToClient("&cError : &8This route doesn't exist!");
            //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &cThis route doesn't exist!");
            userRoute.currentEditRoute = null;
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
