package com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding;

import com.github.dellixou.delclientv3.utils.TickUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class PathRenderer {

    public static void renderPath(List<Vec3> path, List<BlockPos> pathBlocks) {
        if (path == null || path.isEmpty()) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * TickUtils.getPartialTicks();
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * TickUtils.getPartialTicks();
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * TickUtils.getPartialTicks();

        disableGlShit();

        GlStateManager.translate(-playerX, -playerY, -playerZ);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (Vec3 point : path) {
            GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
        }
        GL11.glEnd();

        GlStateManager.translate(playerX, playerY, playerZ);

        enableGlShit();
    }

    private static void disableGlShit() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(3.0f);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        GlStateManager.pushMatrix();
    }

    private static void enableGlShit() {
        GlStateManager.popMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }
}