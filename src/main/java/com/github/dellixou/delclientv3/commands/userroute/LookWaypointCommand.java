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

public class LookWaypointCommand implements ICommand {
    @Override
    public String getCommandName() {
        return "lookhere";
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
        // args[0] = independent/no
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
        if (userRoute.currentEditRoute == null){
            DelClient.sendChatToClient("&cError : &8Please select a route before editing!");
            return;
        }
        boolean independent;
        if(args.length <= 0){
            independent = false;
        }else{
            independent = true;
        }
        float yaw = player.rotationYaw;
        yaw = normalizeYaw(yaw);

        userRoute.addWaypoints(userRoute.currentEditRoute, x, y + 0.5, z, false, false, true, false, yaw, player.rotationPitch, independent, RouteItem.NOTHING, false, false, false, 0);
        DelClient.sendChatToClient("&7Look placed : &8" + userRoute.currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9Look placed in route : " + userRoute.currentEditRoute.getName() +  " -> X: " + x + " Y: " + y + " Z: " + z);

    }

    public float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw > 180) {
            yaw -= 360;
        } else if (yaw < -180) {
            yaw += 360;
        }
        return yaw;
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