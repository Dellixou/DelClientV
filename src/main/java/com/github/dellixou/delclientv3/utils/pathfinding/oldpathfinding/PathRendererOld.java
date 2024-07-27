package com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding;

import com.github.dellixou.delclientv3.utils.RenderUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PathRendererOld {

    private List<BlockPos> path;
    public List<BlockPos> detected = new ArrayList<>();

    public void setPath(List<BlockPos> newPath) {
        this.path = newPath;
    }

    /*
     * Show detected woods.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(detected.size() > 0){
            for(BlockPos pos : detected){
                RenderUtils.highlightBlockOld(pos, Color.GREEN, 0.3f, event.partialTicks);
            }
        }
    }

    /*
     * Show path.
     */
    @SubscribeEvent
    public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event) {
        if (path == null || path.isEmpty()) return;

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

        for (int i = 0; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            //BlockPos next = path.get(i + 1);

            Vec3 posStart = new Vec3(current.getX() + 0.5, current.getY() + 0.1, current.getZ() + 0.5);
            Vec3 posEnd = new Vec3(current.getX() + 0.5, current.getY() + 0.9, current.getZ() + 0.5);
            //Vec3 posEnd = new Vec3(next.getX() + 0.5, next.getY() + 0.1, next.getZ() + 0.5);

            RenderUtils.drawLineWithGL(posStart, posEnd, false, 0.5f, 0.3f, 1f);
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}