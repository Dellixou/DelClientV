package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.pathfinding.ForagingPathFinding;
import com.github.dellixou.delclientv3.utils.pathfinding.PlayerPathFinder;
import com.github.dellixou.delclientv3.utils.renderer.BlockOutlineRenderer;
import com.github.dellixou.delclientv3.utils.renderer.RenderUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AutoForaging extends Module {

    /**
     * Constructor to initialize the AutoForaging module.
     */
    public AutoForaging() {
        super("Auto Foraging", Keyboard.KEY_0, Category.MACRO, true, "auto_fora");
    }

    // Fields for managing auto fish.
    public BlockPos targetWood = null;
    public ForagingPathFinding pathFinder = new ForagingPathFinding();
    private int currentDelay = 0;

    /**
     * Setup.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Wood Range", this, 6, 3, 30, true, "auto_fora_wood_range", "global"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 10, 1, 20, true, "auto_fora_delay", "global"));
        DelClient.settingsManager.rSetting(new Setting("Look Speed : Near", this, 5, 1, 30, true, "auto_fora_look_speed", "look"));
        DelClient.settingsManager.rSetting(new Setting("Look Speed : Far", this, 5, 1, 30, true, "auto_fora_look_speed_far", "look"));
        DelClient.settingsManager.rSetting(new Setting("Show Debug", this, false, "auto_fora_debug", "misc"));
        DelClient.settingsManager.rSetting(new Setting("Show PathFinding", this, false, "auto_fora_pathfinding", "misc"));
    }

    /**
     * On enable.
     */
    public void onEnable(){
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(pathFinder);
        pathFinder.renderer = new BlockOutlineRenderer();
        MinecraftForge.EVENT_BUS.register(pathFinder.renderer);
        super.onEnable();
    }

    /**
     * On disable.
     */
    public void onDisable(){
        MinecraftForge.EVENT_BUS.unregister(this);
        MinecraftForge.EVENT_BUS.unregister(pathFinder);
        pathFinder.resetPath();
        targetWood = null;
        super.onDisable();
    }

    /**
     * On update.
     */
    public void onUpdate(){
        if(this.isToggled()){
            if(targetWood == null){
                if(currentDelay >= DelClient.settingsManager.getSettingById("auto_fora_delay").getValDouble()){
                    scanWoods();
                    currentDelay = 0;
                }else{
                    currentDelay += 1;
                }
            }else{
                if(mc.theWorld.getBlockState(targetWood).getBlock() instanceof BlockAir){
                    pathFinder.resetPath();
                    targetWood = null;
                    pathFinder.resetWoodTarget();
                }
            }
        }
    }

    /**
     * Scan map for woods.
     */
    private void scanWoods() {
        EntityPlayer player = mc.thePlayer;
        double range = DelClient.settingsManager.getSettingById("auto_fora_wood_range").getValDouble();
        double minDistance = range * range; // Initial distance set to maximum range
        BlockPos closestWood = null; // Variable to hold the closest wood position

        BlockPos playerPos = player.getPosition();
        int radius = (int) Math.ceil(range);

        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos woodPos = playerPos.add(x, y, z);
                    double distanceSq = playerPos.distanceSq(woodPos);
                    if (distanceSq <= radius * radius) {
                        Block block = mc.theWorld.getBlockState(woodPos).getBlock();
                        if (block instanceof BlockLog) {
                            IBlockState state = mc.theWorld.getBlockState(woodPos);
                            BlockPlanks.EnumType woodType = state.getValue(BlockPlanks.VARIANT);
                            if (woodType == BlockPlanks.EnumType.ACACIA) {
                                if (distanceSq < minDistance) {
                                    if(hasAdjacentAcacia(woodPos)){
                                        minDistance = distanceSq;
                                        closestWood = woodPos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (closestWood != null) {
            targetWood = closestWood;
            tryingToGoWood(targetWood);
            //DelClient.sendChatToClient("&aDetected a wood block: Coords --> X: " + closestWood.getX() + ", Y: " + closestWood.getY() + ", Z: " + closestWood.getZ());
            //Minecraft.getMinecraft().thePlayer.playSound("random.orb", 0.1f, 1);
        }
    }

    /**
     * Adjacent acacia?
     */
    private boolean hasAdjacentAcacia(BlockPos pos) {
        BlockPos[] adjacentPositions = {
                pos.north(), pos.south(), pos.east(), pos.west(), pos.up(), pos.down()
        };
        int acaciaCount = 0;
        for (BlockPos adjacentPos : adjacentPositions) {
            Block block = mc.theWorld.getBlockState(adjacentPos).getBlock();
            if (block instanceof BlockLog) {
                IBlockState state = mc.theWorld.getBlockState(adjacentPos);
                BlockPlanks.EnumType woodType = state.getValue(BlockPlanks.VARIANT);
                if (woodType == BlockPlanks.EnumType.ACACIA) {
                    acaciaCount++;
                    if (acaciaCount >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Path finding to go to the wood.
     */
    public void tryingToGoWood(BlockPos targetWood){
        if(mc.thePlayer.getPosition().distanceSq(targetWood) <= 9){
            pathFinder.updateWoodTarget(targetWood);
            pathFinder.isNearTarget = true;
        }else{
            pathFinder.createPath(mc.theWorld, mc.thePlayer.getPosition(), targetWood);
        }
    }

    /**
     * Event to render wood chosen.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        float partialTicks = event.partialTicks;
        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0f);

        //      for (BlockPos pos : detectedWoodBlocks.keySet()) {
        //            drawOutlinedBoundingBox(pos, 0.0f, 1.0f, 0.0f, 0.4f);
        //        }
        if(pathFinder.randomizedWoodLoc != null){
            RenderUtils.drawOutlinedBoundingBox(pathFinder.randomizedWoodLoc, 0.0f, 1.0f, 0.0f, 1.0f);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * Event to render wood chosen.
     */
    @SubscribeEvent
    public void onHighlight(DrawBlockHighlightEvent event){
        if(pathFinder.randomizedWoodLoc != null){
            RenderUtils.highlightBlock(pathFinder.randomizedWoodLoc, new Color(0, 147, 7), 0.3f, event.partialTicks);
        }
    }

}
