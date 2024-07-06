package com.github.dellixou.delclientv3.events.draw;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class DrawBlockHighlightEvent {

    @SubscribeEvent
    public void onDrawBlockHighlightEvent(net.minecraftforge.client.event.DrawBlockHighlightEvent event) {
            //if(!DelClient.instance.currentPlayerLocation.equalsIgnoreCase("dungeon")) return;
            UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            double renderDistance = DelClient.settingsManager.getSettingById("user_route_render_distance").getValDouble();
            boolean renderWall = DelClient.settingsManager.getSettingById("user_route_render_wall").getValBoolean();

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

            double d0 = event.player.prevPosX + (event.player.posX - event.player.prevPosX) * (double)event.partialTicks;
            double d1 = event.player.prevPosY + (event.player.posY - event.player.prevPosY) * (double)event.partialTicks;
            double d2 = event.player.prevPosZ + (event.player.posZ - event.player.prevPosZ) * (double)event.partialTicks;

            Vec3 pos = new Vec3(d0, d1, d2);

            GL11.glTranslated(-pos.xCoord, -pos.yCoord, -pos.zCoord);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            if(renderWall){
                GL11.glDisable(GL11.GL_DEPTH_TEST);	// Draw the line on top of the geometry
            }

            for(int j = 0; j < userRoute.routes.size(); j++){
                for(int i = 0; i < userRoute.routes.get(j).getNonIndeWaypoints().size(); i++){

                    // Square of start
                    if(i == 0){
                        double x = userRoute.routes.get(j).getNonIndeWaypoints().get(i).getX();
                        double z = userRoute.routes.get(j).getNonIndeWaypoints().get(i).getZ();
                        x -= 0.5;
                        z -= 0.5;
                        Vec3 posA = new Vec3 (x,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.2,z);
                        Vec3 posB = new Vec3 (x+1,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.2,z);
                        Vec3 posC = new Vec3 (x+1,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.2,z+1);
                        Vec3 posD = new Vec3 (x,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.2,z+1);

                        // Render Distance
                        double distance = Math.sqrt((player.posZ - posA.zCoord) * (player.posZ - posA.zCoord) + (player.posX - posA.xCoord) * (player.posX - posA.xCoord));
                        if(distance < renderDistance){
                            drawLineWithGL(posC, posD, true, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            drawLineWithGL(posD, posA, true, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            drawLineWithGL(posB, posC, true, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            drawLineWithGL(posA, posB, true, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                        }
                    }

                    try{
                        Vec3 posA = new Vec3 (userRoute.routes.get(j).getNonIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.1,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getZ());
                        boolean whiteLine = userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getLookOnly();
                        boolean square = userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getClick();
                        if(square){
                            double x = userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getX();
                            double z = userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getZ();
                            x -= 0.5;
                            z -= 0.5;
                            Vec3 pos1 = new Vec3 (x,userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getY()+0.2,z);
                            Vec3 pos2 = new Vec3 (x+1,userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getY()+0.2,z);
                            Vec3 pos3 = new Vec3 (x+1,userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getY()+0.2,z+1);
                            Vec3 pos4 = new Vec3 (x,userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getY()+0.2,z+1);

                            double distance = Math.sqrt((player.posZ - pos1.zCoord) * (player.posZ - pos1.zCoord) + (player.posX - pos1.xCoord) * (player.posX - pos1.xCoord));

                            if(distance < renderDistance){
                                drawLineWithGL(pos3, pos4, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos4, pos1, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos2, pos3, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos1, pos2, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            }
                        }
                        double distance = Math.sqrt((player.posZ - posA.zCoord) * (player.posZ - posA.zCoord) + (player.posX - posA.xCoord) * (player.posX - posA.xCoord));
                        if(distance < renderDistance){
                            Vec3 posJ = new Vec3 (userRoute.routes.get(j).getNonIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getNonIndeWaypoints().get(i).getY()+0.1,userRoute.routes.get(j).getNonIndeWaypoints().get(i).getZ());
                            Vec3 posI = new Vec3 (userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getX(),userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getY()+0.1,userRoute.routes.get(j).getNonIndeWaypoints().get(i+1).getZ());
                            drawLineWithGL(posJ, posI, whiteLine, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                        }
                    }catch (Exception e){
                        // No neighboors
                    }
                }

                for(int i = 0; i < userRoute.routes.get(j).getIndeWaypoints().size(); i++){

                    try{
                        Vec3 posA = new Vec3 (userRoute.routes.get(j).getIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.1,userRoute.routes.get(j).getIndeWaypoints().get(i).getZ());
                        boolean whiteLine = userRoute.routes.get(j).getIndeWaypoints().get(i).getLookOnly();
                        boolean independent = userRoute.routes.get(j).getIndeWaypoints().get(i).getIndependent();
                        boolean square = userRoute.routes.get(j).getIndeWaypoints().get(i).getClick() || userRoute.routes.get(j).getIndeWaypoints().get(i).getLookOnly() || userRoute.routes.get(j).getIndeWaypoints().get(i).getWait() || userRoute.routes.get(j).getIndeWaypoints().get(i).getUseJump() || userRoute.routes.get(j).getIndeWaypoints().get(i).getBonzo();
                        if(square){

                            double x = userRoute.routes.get(j).getIndeWaypoints().get(i).getX();
                            double y = userRoute.routes.get(j).getIndeWaypoints().get(i).getY();
                            double z = userRoute.routes.get(j).getIndeWaypoints().get(i).getZ();
                            x -= 0.5;
                            z -= 0.5;
                            Vec3 pos1 = new Vec3 (x,userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.2,z);
                            Vec3 pos2 = new Vec3 (x+1,userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.2,z);
                            Vec3 pos3 = new Vec3 (x+1,userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.2,z+1);
                            Vec3 pos4 = new Vec3 (x,userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.2,z+1);

                            double distance = Math.sqrt((player.posZ - pos1.zCoord) * (player.posZ - pos1.zCoord) + (player.posX - pos1.xCoord) * (player.posX - pos1.xCoord));

                            if(distance < renderDistance){
                                drawLineWithGL(pos3, pos4, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos4, pos1, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos2, pos3, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                                drawLineWithGL(pos1, pos2, false, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            }
                        }
                        else if(independent){
                            double distance = Math.sqrt((player.posZ - posA.zCoord) * (player.posZ - posA.zCoord) + (player.posX - posA.xCoord) * (player.posX - posA.xCoord));
                            if(distance < renderDistance){
                                Vec3 posE = new Vec3 (userRoute.routes.get(j).getIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.5,userRoute.routes.get(j).getIndeWaypoints().get(i).getZ());
                                Vec3 posT = new Vec3 (userRoute.routes.get(j).getIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getIndeWaypoints().get(i).getY()-0.5,userRoute.routes.get(j).getIndeWaypoints().get(i).getZ());
                                drawLineWithGL(posE, posT, whiteLine, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            }
                        }else{
                            double distance = Math.sqrt((player.posZ - posA.zCoord) * (player.posZ - posA.zCoord) + (player.posX - posA.xCoord) * (player.posX - posA.xCoord));
                            if(distance < renderDistance){
                                Vec3 posJ = new Vec3 (userRoute.routes.get(j).getIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.1,userRoute.routes.get(j).getIndeWaypoints().get(i).getZ());
                                Vec3 posI = new Vec3 (userRoute.routes.get(j).getIndeWaypoints().get(i).getX(),userRoute.routes.get(j).getIndeWaypoints().get(i).getY()+0.1,userRoute.routes.get(j).getIndeWaypoints().get(i).getZ());
                                drawLineWithGL(posJ, posI, whiteLine, userRoute.routes.get(j).red, userRoute.routes.get(j).green, userRoute.routes.get(j).blue);
                            }
                        }
                    }catch (Exception e){
                        // No neighboors
                    }
                }
            }


            GL11.glPopAttrib();
            GL11.glPopMatrix();
    }

    private void drawLineWithGL(Vec3 blockA, Vec3 blockB, boolean whiteLine, float red, float green, float blue) {

        double width = DelClient.settingsManager.getSettingById("user_route_width").getValDouble() * 0.1;

        if(whiteLine){
            GL11.glColor4f(255, 255, 255, 0F);
            GL11.glLineWidth(3);
        }else{
            GL11.glLineWidth((float) width);
            GL11.glColor4f(red, green, blue, 0F);
        }

        GL11.glBegin(GL11.GL_LINE_STRIP);

        GL11.glVertex3d(blockA.xCoord, blockA.yCoord, blockA.zCoord);
        GL11.glVertex3d(blockB.xCoord, blockB.yCoord, blockB.zCoord);

        GL11.glEnd();
    }

}
