package com.github.dellixou.delclientv3.events.draw;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.utils.misc.Route;
import com.github.dellixou.delclientv3.utils.misc.RouteItem;
import com.github.dellixou.delclientv3.utils.misc.Waypoint;
import com.github.dellixou.delclientv3.utils.renderer.WorldTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderWorldLastEvent {

    private final Minecraft mc = Minecraft.getMinecraft();
    private UserRoute userRoute;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event){
        userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
    }

    @SubscribeEvent
    public void onRenderWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent event) {
            //if(!DelClient.instance.currentPlayerLocation.equalsIgnoreCase("dungeon")) return;
            for(Route route : userRoute.routes){
                if(route.getWaypoints().size() <= 0){ continue; }
                WorldTextRenderer.renderTextInWorld(Minecraft.getMinecraft(), route.getName(), route.getWaypoints().get(0).getX(), route.getWaypoints().get(0).getY() + 3, route.getWaypoints().get(0).getZ(), event.partialTicks, 0.01F, true);//0.031F)
                if(route.getIndeWaypoints().size() <= 0){ continue; }
                for(Waypoint waypoint : route.getIndeWaypoints()){
                    String text = "";
                    if(waypoint.getClick()){
                        if(waypoint.getRouteItem().equals(RouteItem.TNT)){
                            text = "Click | TNT";
                        }else if(waypoint.getRouteItem().equals(RouteItem.BONZO)){
                            text = "Click | Bonzo";
                        }else{
                            text = "Click | Unknown item";
                        }
                    }
                    if(waypoint.getLookOnly()){
                        text = "Look | yaw : " + (int)waypoint.getYaw() + " pitch : " + (int)waypoint.getPitch();
                    }
                    if(waypoint.getWait()){
                        text = "Wait | " + waypoint.getTime() + "s";
                    }
                    if(waypoint.getUseJump()){
                        text = "Jump | Edge : " + waypoint.getEdgeJump();
                    }
                    if(waypoint.getBonzo()){
                        text = "Bonzo staff";
                    }
                    double x = waypoint.getX();
                    double y = waypoint.getY();
                    double z = waypoint.getZ();
                    WorldTextRenderer.renderTextInWorld(Minecraft.getMinecraft(), text, x, y + 2, z, event.partialTicks, 0.01F, false);
                }
            }
    }


}
