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

public class ClickWaypointCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "clickhere";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Place a click where you are looking. Only triggered when you are on the coords you placed the click.";
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
        double x;
        double y;
        double z;
        try{
            if(args[2].equalsIgnoreCase("center")){
                x = (int)player.posX;
                y = (int)player.posY;
                z = (int)player.posZ;
                if(x < 0){
                    x -= 0.5;
                }else{
                    x += 0.5;
                }
                if(z < 0){
                    z -= 0.5;
                }else{
                    z += 0.5;
                }
            }else{
                x = player.posX;
                y = player.posY;
                z = player.posZ;
            }
        }catch (Exception ignored){
            x = player.posX;
            y = player.posY;
            z = player.posZ;
        }
        if (userRoute.currentEditRoute == null){
            DelClient.sendChatToClient("&cError : &8Please select a route before editing!");
            return;
        }
        boolean independent = false;
        RouteItem routeItem = RouteItem.NOTHING;

        try{
            if(args[1].equalsIgnoreCase("tnt")){
                routeItem = RouteItem.TNT;
            }else if (args[1].equalsIgnoreCase("bonzo")){
                routeItem = RouteItem.BONZO;
            }
        }catch (Exception ignored){}
        try{
            independent = Boolean.parseBoolean(args[0]);
        }catch (Exception ignored){}

        userRoute.addWaypoints(userRoute.currentEditRoute, x, y, z, false, false, false, true, 0, 0, independent, routeItem, false, false, false, 0);
        DelClient.sendChatToClient("&7Click placed : &8" + userRoute.currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9Click placed in route : " + userRoute.currentEditRoute.getName() + " -> X: " + x + " Y: " + y + " Z: " + z);

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
