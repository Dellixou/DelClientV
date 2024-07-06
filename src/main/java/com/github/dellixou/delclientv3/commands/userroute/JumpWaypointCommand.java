package com.github.dellixou.delclientv3.commands.userroute;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.utils.misc.RouteItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class JumpWaypointCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "jumphere";
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
        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;
        if (userRoute.currentEditRoute == null){
            DelClient.sendChatToClient("&cError : &8Please select a route before editing!");
            return;
        }
        boolean edge = false;
        try{
            if(args[0].equalsIgnoreCase("edge")){
                edge = true;
            }else{
                edge = false;
            }
        }catch (Exception e){
            edge = false;
        }

        userRoute.addWaypoints(userRoute.currentEditRoute, x, y, z, false, true, false, false, player.rotationYaw, player.rotationPitch, true, RouteItem.NOTHING, edge, false, false, 0);
        DelClient.sendChatToClient("&7Jump placed : &8" + userRoute.currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9Jump placed in route : " + userRoute.currentEditRoute.getName() +  " -> X: " + x + " Y: " + y + " Z: " + z);

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
