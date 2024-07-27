package com.github.dellixou.delclientv3.commands.userroute;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.utils.misc.Route;
import com.github.dellixou.delclientv3.utils.enums.RouteItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartWaypointCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "starthere";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Start Waypoint";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> commandAliases = new ArrayList<>();
        return commandAliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        if(args.length <= 0){
            DelClient.sendChatToClient("&cError : &8Please name the route before editing!");
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
        double x = (int)player.posX;
        double y = (int)player.posY;
        double z = (int)player.posZ;
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
        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        Route currentRoute = userRoute.createNewRoute(r, g, b, args[0]);
        userRoute.addWaypoints(currentRoute, x, y, z, true, true, false, false, 0, 0, false, RouteItem.NOTHING, false, false, false, 0);
        DelClient.sendChatToClient("&7Start placed : &8" + userRoute.currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9Start Waypoint placed in route  : " + userRoute.currentEditRoute.getName() +  " -> X: " + x + " Y: " + y + " Z: " + z);

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
