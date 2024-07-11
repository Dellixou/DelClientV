package com.github.dellixou.delclientv3.utils.pathfinding;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoPowder;
import com.github.dellixou.delclientv3.utils.movements.PlayerLookSmooth;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PlayerPathFinder {

    private static List<BlockPos> path;
    final int[] counter = {1, 2};
    private static int pathIndex = 1;
    private static Minecraft mc = Minecraft.getMinecraft();
    private BlockPos chestLoc;
    private final AutoPowder autoPowder = (AutoPowder) ModuleManager.getModuleById("auto_powder");
    BlockPos aa = null;

    public Map<BlockPos, BlockPos> realChestsPosition = new HashMap<>();

    /**
     * Update player movement along the path.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
    }

    /**
     * Find the closest point to the player.
     */
    private int findClosestForwardPoint(EntityPlayerSP player) {
        if (pathIndex >= path.size() - 1) return -1;

        int closestIndex = pathIndex;
        double closestDistance = Double.MAX_VALUE;

        for (int i = pathIndex; i < path.size(); i++) {
            BlockPos point = path.get(i);
            double distance = player.getDistanceSq(point.getX() + 0.5, point.getY(), point.getZ() + 0.5);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Perform a raycast to detect blocks in front of the player.
     */
    private BlockPos raycastForBlock(EntityPlayerSP player, float yaw) {
        // Convert yaw angle to radians
        double yawRad = Math.toRadians(yaw);

        // Calculate direction vector based on yaw (horizontal direction only)
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        // Start raycast position just in front of the player
        double startX = player.posX + dirX;
        double startY = player.posY + player.getEyeHeight(); // Start at eye level
        double startZ = player.posZ + dirZ;

        // Maximum distance for raycast (adjust as needed)
        double maxDistance = 2.0;

        // Perform raycast to detect blocks
        for (double distance = 0; distance < maxDistance; distance += 0.5) {
            int blockX = MathHelper.floor_double(startX + dirX * distance);
            int blockY = MathHelper.floor_double(startY);
            int blockZ = MathHelper.floor_double(startZ + dirZ * distance);

            BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);

            // Check if the block position is solid (wall)
            if (mc.theWorld.getBlockState(blockPos).getBlock().isNormalCube()) {
                return blockPos;
            }
        }

        return null; // No block found
    }

    /**
     * Create the path.
     */
    public void createPath(World world, BlockPos start, BlockPos end) {
        new Thread(() -> {
            int attempts = 5; // Number of attempts to find a path
            PathEntity pathEntity = null;
            for (int i = 0; i < attempts; i++) {
                pathEntity = findPath(world, start, end);
                if (pathEntity != null) {
                    break;
                }
                try {
                    Thread.sleep(100); // Wait before retrying
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (pathEntity != null) {
                BlockPos chestPos = realChestsPosition.get(end);
                List<BlockPos> rawPath = convertPathToBlockPosList(pathEntity);
                path = optimizePath(rawPath);
                pathIndex = 1; // Reset path index when a new path is created
                this.chestLoc = chestPos;
            } else {
                AutoPowder autoPowder1 = (AutoPowder) ModuleManager.getModuleById("auto_powder");
                autoPowder1.currentChestDone();
            }
        }).start();
    }

    /**
     * Find a path.
     */
    private static PathEntity findPath(World world, BlockPos start, BlockPos end) {
        EntityLiving tempEntity = new EntityLiving(world) {
            @Override
            public void moveEntityWithHeading(float strafe, float forward) {}
        };
        tempEntity.setPosition(start.getX(), start.getY(), start.getZ());

        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        PathFinder pathFinder = new PathFinder(nodeProcessor);

        return pathFinder.createEntityPathTo(world, tempEntity, end, 32.0F);
    }

    /**
     * Convert the path to block pos.
     */
    private static List<BlockPos> convertPathToBlockPosList(PathEntity pathEntity) {
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < pathEntity.getCurrentPathLength(); i++) {
            blockPosList.add(new BlockPos(pathEntity.getPathPointFromIndex(i).xCoord,
                    pathEntity.getPathPointFromIndex(i).yCoord,
                    pathEntity.getPathPointFromIndex(i).zCoord));
        }
        return blockPosList;
    }

    /*
     * It's in line?
     */
    private boolean isInLine(BlockPos a, BlockPos b, BlockPos c) {
        return (b.getX() - a.getX()) * (c.getZ() - a.getZ()) == (c.getX() - a.getX()) * (b.getZ() - a.getZ())
                && (b.getX() - a.getX()) * (c.getY() - a.getY()) == (c.getX() - a.getX()) * (b.getY() - a.getY());
    }

    /*
     * Try to optimize the path !
     */
    private static List<BlockPos> optimizePath(List<BlockPos> originalPath) {
        List<BlockPos> optimizedPath = new ArrayList<>();

        if (originalPath.size() < 3) {
            return originalPath;
        }

        optimizedPath.add(originalPath.get(0));

        for (int i = 1; i < originalPath.size() - 1; i++) {
            BlockPos prev = originalPath.get(i - 1);
            BlockPos current = originalPath.get(i);
            BlockPos next = originalPath.get(i + 1);

            if (!isDiagonalMove(prev, next)) {
                optimizedPath.add(current);
            }
        }

        optimizedPath.add(originalPath.get(originalPath.size() - 1));

        return optimizedPath;
    }

    /*
     * Check if the path contains diagonal movement.
     */
    private static boolean isDiagonalMove(BlockPos start, BlockPos end) {
        return Math.abs(start.getX() - end.getX()) == 1
                && Math.abs(start.getZ() - end.getZ()) == 1
                && start.getY() == end.getY();
    }

    /*
     * Reset current path.
     */
    public static void resetPath() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        if (path != null) {
            path.clear();
        }
    }

    /**
     * Smoothly update player rotation.
     */
    private static float updateRotation(float currentYaw, float targetYaw, float maxChange) {
        float f = targetYaw - currentYaw;

        while (f < -180.0F) {
            f += 360.0F;
        }
        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return currentYaw + Math.min(maxChange, Math.max(-maxChange, f));
    }

    /**
     * Make player look at chest.
     */
    private void lookAtChestAndOpen() {
        if(chestLoc == null) return;
        double moveSpeed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
        if(!(moveSpeed < 0.01)){
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    lookAtChestAndOpen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            return;
        }
        PlayerLookSmooth playerLookSmooth = new PlayerLookSmooth();

        if(chestLoc.getY() == mc.thePlayer.getPosition().getY()){
            playerLookSmooth.lookAtBlock(chestLoc.getX(), chestLoc.getY()-1f, chestLoc.getZ(), (float) DelClient.settingsManager.getSettingById("auto_powder_look_duration").getValDouble());
        }else if(chestLoc.getY() > mc.thePlayer.getPosition().getY()){
            if(chestLoc.getY()-mc.thePlayer.getPosition().getY() > 1.5){
                DelClient.sendChatToClient("vraiment haut");
            }
            playerLookSmooth.lookAtBlock(chestLoc.getX(), chestLoc.getY(), chestLoc.getZ(), (float) DelClient.settingsManager.getSettingById("auto_powder_look_duration").getValDouble());
        }else if(chestLoc.getY() < mc.thePlayer.getPosition().getY()){
            playerLookSmooth.lookAtBlock(chestLoc.getX(), chestLoc.getY()-1f, chestLoc.getZ(), (float) DelClient.settingsManager.getSettingById("auto_powder_look_duration").getValDouble());
        }
        isLookingDone(playerLookSmooth);
    }

    /**
     * Is Looking done?
     */
    private void isLookingDone(PlayerLookSmooth playerLookSmooth){
        if(!playerLookSmooth.finished){
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    isLookingDone(playerLookSmooth);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }else{
            // When look done
            // Click to open
            Thread thread = new Thread(() -> {
                try {
                    // DONE !!
                    Thread.sleep((long) (DelClient.settingsManager.getSettingById("auto_powder_look_duration").getValDouble()*1.5f));
                    DelClient.sendChatToClient("&eTrying to open a chest...");
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());

                    AutoPowder autoPowder1 = (AutoPowder) ModuleManager.getModuleById("auto_powder");
                    autoPowder1.currentChestDone();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }


    // ----------------------------------------------------- VISUAL ----------------------------------------------------


    /*
     * Make rainbow
     */
    private static int rainbow(int delay) {
        float v1 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_1").getValDouble();
        float v2 = (float) DelClient.settingsManager.getSettingById("module_list_bg_rainbow_2").getValDouble();

        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360), v1 / 10, v2 / 10).getRGB();
    }

    /**
     * Render the pathfinding with event.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (path != null && !path.isEmpty()) {
            renderPath3D(event.partialTicks);
        }
    }

    /**
     * Render Path 3D with openGL.
     */
    private void renderPath3D(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP viewer = mc.thePlayer;
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Draw path lines
        int color = rainbow(counter[1]);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, 0.3F);
        //GlStateManager.color(1.0F, 0.0F, 0.0F, 0.4F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        for (BlockPos pos : path) {
            worldrenderer.pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).endVertex();
        }
        tessellator.draw();

        // Draw direction change points
        GlStateManager.color(0.0F, 1.0F, 0.0F, 0.6F);
        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);
            if (!isInLine(prev, current, next)) {
                renderCube(current, 0.1, true);
            }
        }

        // Draw start point
        GlStateManager.color(0.0F, 0.0F, 1.0F, 0.6F);
        renderCube(path.get(0), 0.4, true);

        // Draw end point
        GlStateManager.color(1.0F, 1.0F, 0.0F, 0.6F);
        renderCube(path.get(path.size() - 1), 0.4, true);

        GlStateManager.color(1.0F, 1.0F, 0.0F, 1F);
        if(chestLoc != null){
            aa = chestLoc;
        }
        if(aa != null){
            renderCube(aa, 0.6, true);
        }

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        counter[1]++;
    }

    /**
     * Render cube 3D with openGL.
     */
    private void renderCube(BlockPos pos, double size, boolean drawOutline) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (drawOutline) {
            int color = rainbow(counter[0]);
            float red = (color >> 16 & 255) / 255.0F;
            float green = (color >> 8 & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;

            GlStateManager.color(red, green, blue, 0.6F);
            GL11.glLineWidth(1.2F);
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

            // Front face outline
            worldrenderer.pos(x - size, y - size, z - size).endVertex();
            worldrenderer.pos(x + size, y - size, z - size).endVertex();
            worldrenderer.pos(x + size, y + size, z - size).endVertex();
            worldrenderer.pos(x - size, y + size, z - size).endVertex();
            worldrenderer.pos(x - size, y - size, z - size).endVertex();

            // Back face outline
            worldrenderer.pos(x - size, y - size, z + size).endVertex();
            worldrenderer.pos(x + size, y - size, z + size).endVertex();
            worldrenderer.pos(x + size, y + size, z + size).endVertex();
            worldrenderer.pos(x - size, y + size, z + size).endVertex();
            worldrenderer.pos(x - size, y - size, z + size).endVertex();

            // Left face outline
            worldrenderer.pos(x - size, y - size, z - size).endVertex();
            worldrenderer.pos(x - size, y - size, z + size).endVertex();
            worldrenderer.pos(x - size, y + size, z + size).endVertex();
            worldrenderer.pos(x - size, y + size, z - size).endVertex();

            // Right face outline
            worldrenderer.pos(x + size, y - size, z - size).endVertex();
            worldrenderer.pos(x + size, y - size, z + size).endVertex();
            worldrenderer.pos(x + size, y + size, z + size).endVertex();
            worldrenderer.pos(x + size, y + size, z - size).endVertex();

            // Top face outline
            worldrenderer.pos(x - size, y + size, z - size).endVertex();
            worldrenderer.pos(x + size, y + size, z - size).endVertex();
            worldrenderer.pos(x + size, y + size, z + size).endVertex();
            worldrenderer.pos(x - size, y + size, z + size).endVertex();

            // Bottom face outline
            worldrenderer.pos(x - size, y - size, z - size).endVertex();
            worldrenderer.pos(x + size, y - size, z - size).endVertex();
            worldrenderer.pos(x + size, y - size, z + size).endVertex();
            worldrenderer.pos(x - size, y - size, z + size).endVertex();

            tessellator.draw();
        }
        counter[0]++;
    }


    /**
    EntityPlayerSP player = mc.thePlayer;
        if (path != null && !path.isEmpty() && pathIndex < path.size()) {

        int closestIndex = findClosestForwardPoint(player);
        if (closestIndex != -1) {
            pathIndex = closestIndex;
        }

        BlockPos target = path.get(pathIndex);

        // Use squared distance to avoid unnecessary square root calculation
        double deltaY = target.getY() - player.posY;
        double deltaZ = target.getZ() - player.posZ;
        double deltaX = target.getX() - player.posX;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double height = Math.abs(deltaY);

        // Is player next to goal? SNEAK Part
        if (chestLoc != null) {
            double chestDistance = player.getDistanceSq(chestLoc.getX() + 0.5, chestLoc.getY(), chestLoc.getZ() + 0.5);
            if (chestDistance < DelClient.settingsManager.getSettingById("auto_powder_sneak_tolerance").getValDouble() * DelClient.settingsManager.getSettingById("auto_powder_sneak_tolerance").getValDouble()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }

        if (distance < 1.5f && deltaY > 0.5 && mc.thePlayer.onGround) {
            player.jump();
        }

        // Next to the point path?
        if (distance < DelClient.settingsManager.getSettingById("auto_powder_tolerance").getValDouble() && height < 3 && height > -3) {
            pathIndex++;
            if (pathIndex >= path.size()) {
                lookAtChestAndOpen();
                resetPath();
                return;
            }
            target = path.get(pathIndex);
        }

        double dx = target.getX() + 0.5 - player.posX;
        double dz = target.getZ() + 0.5 - player.posZ;

        float targetYaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;

        // Déterminer la direction de mouvement
        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - player.rotationYaw);

        // Toujours avancer
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

        // Update player rotation and handle side movement
        player.rotationYaw = updateRotation(player.rotationYaw, targetYaw, 10);

        // Ajuster la direction latérale
        if (Math.abs(yawDiff) > 45) {
            if (yawDiff > 0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        }
    }
        **/

}