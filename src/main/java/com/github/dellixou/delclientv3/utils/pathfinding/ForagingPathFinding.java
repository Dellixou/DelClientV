package com.github.dellixou.delclientv3.utils.pathfinding;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoForaging;
import com.github.dellixou.delclientv3.modules.macro.AutoPowder;
import com.github.dellixou.delclientv3.utils.movements.PlayerLookSmooth;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import com.github.dellixou.delclientv3.utils.renderer.BlockOutlineRenderer;
import com.github.dellixou.delclientv3.utils.renderer.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.security.Key;
import java.util.*;
import java.util.List;

public class ForagingPathFinding {

    private static List<BlockPos> path;
    final int[] counter = {1, 2};
    private static int pathIndex = 1;
    private static Minecraft mc = Minecraft.getMinecraft();
    public BlockPos woodLoc;
    public BlockPos randomizedWoodLoc;
    private final AutoForaging autoForaging = (AutoForaging) ModuleManager.getModuleById("auto_fora");
    BlockPos aa = null;
    public BlockOutlineRenderer renderer;
    public boolean isNearTarget = false;
    public boolean blockNearTemp = false;
    public boolean far;
    private int stuckCounter = 0;
    private float randomPitchIgnore = 0;
    public boolean isLooking = false;

    public Map<BlockPos, BlockPos> realChestsPosition = new HashMap<>();

    /**
     * Update player movement along the path.
     */
    @SubscribeEvent
    public void onTick(LivingEvent.LivingUpdateEvent event) {
        if (randomizedWoodLoc != null && mc.thePlayer != null) {
            far = mc.thePlayer.getDistanceSq(woodLoc) >= 30;
            if (!far) {
                if(!isLooking){
                    RotationUtils.smoothLook(
                            RotationUtils.getRotationToBlock(randomizedWoodLoc),
                            31 - (int) DelClient.settingsManager.getSettingById("auto_fora_look_speed").getValDouble(),
                            () -> {
                                isLooking = false;
                            });
                    isLooking = true;
                }
                //smoothLookAt(mc.thePlayer, randomizedWoodLoc, false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }

        if (path != null && !path.isEmpty()) {
            float deltaX = (float) (mc.thePlayer.posX - path.get(path.size() - 1).getX());
            float deltaZ = (float) (mc.thePlayer.posZ - path.get(path.size() - 1).getZ());
            float distance = deltaX * deltaX + deltaZ * deltaZ;
            if (distance <= 8) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                isNearTarget = true;
                resetPath();
            } else {
                isNearTarget = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                moveTowardsNextPoint();
            }
        }
    }

    /*
     * Smooth look at the given position.
     */
    private void smoothLookAt(EntityPlayerSP player, BlockPos target, boolean ignorePitch) {
        double diffX = target.getX() + 0.5 - player.posX;
        double diffY = target.getY() + 0.5 - (player.posY + player.getEyeHeight());
        double diffZ = target.getZ() + 0.5 - player.posZ;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0 / Math.PI);
        float factor = (float)(DelClient.settingsManager.getSettingById("auto_fora_look_speed_far").getValDouble()) / 10f;
        player.rotationYaw = updateRotation(player.rotationYaw, yaw, factor);
        if (!ignorePitch || Math.abs(player.rotationPitch - pitch) >= 55) {
            player.rotationPitch = updateRotation(player.rotationPitch, pitch, factor);
        }else{
            player.rotationPitch = updateRotation(player.rotationPitch, randomPitchIgnore, factor);
        }
    }

    /*
     * Update the player's rotation smoothly.
     */
    private float updateRotation(float current, float intended, float factor) {
        float updatedRotation = MathHelper.wrapAngleTo180_float(intended - current);

        if (updatedRotation > factor) updatedRotation = factor;
        if (updatedRotation < -factor) updatedRotation = -factor;

        return current + updatedRotation;
    }

    /*
     * Randomize a BlockPos within a given range.
     */
    private BlockPos randomizeBlockPos(BlockPos original, double range) {
        Random random = new Random();
        double offsetX = (random.nextDouble()) * range;
        double offsetY = (random.nextDouble()) * range;
        double offsetZ = (random.nextDouble()) * range;

        return new BlockPos(
                original.getX() + offsetX,
                original.getY() + offsetY,
                original.getZ() + offsetZ
        );
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
     * Calculate yaw to point.
     */
    private float calculateYawToTarget(BlockPos target) {
        EntityPlayerSP player = mc.thePlayer;
        double diffX = target.getX() + 0.5 - player.posX;
        double diffZ = target.getZ() + 0.5 - player.posZ;
        return (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
    }

    /**
     * Move player.
     */
    private void moveTowardsNextPoint() {
        if (path == null || path.isEmpty() || pathIndex >= path.size()) {
            resetMovementKeys();
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        BlockPos nextPoint = path.get(pathIndex);

        double distanceToNext = player.getDistanceSq(nextPoint.getX() + 0.5, nextPoint.getY(), nextPoint.getZ() + 0.5);
        double distanceToTarget = player.getDistanceSq(woodLoc.getX() + 0.5, woodLoc.getY(), woodLoc.getZ() + 0.5);

        if (distanceToTarget <= 3) {
            fineTunePosition();
            return;
        }

        if (distanceToNext > 0.5) {
            boolean far = distanceToTarget >= 8;
            boolean blockNear = areBlocksNear();

            if (far || blockNear) {

                float targetYaw = calculateYawToTarget(nextPoint);

                if(!isLooking){
                    smoothLookAt(player, nextPoint, true);
                }

                float angleDifference = MathHelper.wrapAngleTo180_float(targetYaw - player.rotationYaw);

                if (Math.abs(angleDifference) < 60) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                    //KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                } else {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    if(angleDifference < 0){
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                    }else{
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
                    }
                    //KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
                }

                if (blockNear) {
                    avoidObstacles();
                }
            } else {
                adjustPosition(nextPoint);
            }
        }
        if(distanceToNext <= 2){
            pathIndex++;
            if (pathIndex >= path.size()) {
                resetPath();
                resetMovementKeys();
            }
        }
    }

    private void fineTunePosition() {
        double dx = woodLoc.getX() + 0.5 - mc.thePlayer.posX;
        double dz = woodLoc.getZ() + 0.5 - mc.thePlayer.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 2) {
            // Si on est encore un peu loin, on se rapproche doucement
            adjustPosition(woodLoc);
        } else {
            // On est assez proche, on s'arrête et on se tourne vers la cible
            resetMovementKeys();
            smoothLookAt(mc.thePlayer, woodLoc, false);
            isNearTarget = true;
        }
    }

    private void adjustPosition(BlockPos target) {
        final double MOVEMENT_THRESHOLD = 0.1;
        double dx = target.getX() + 0.5 - mc.thePlayer.posX;
        double dz = target.getZ() + 0.5 - mc.thePlayer.posZ;

        boolean moveForward = dz < -MOVEMENT_THRESHOLD;
        boolean moveBack = dz > MOVEMENT_THRESHOLD;
        boolean moveRight = dx > MOVEMENT_THRESHOLD;
        boolean moveLeft = dx < -MOVEMENT_THRESHOLD;

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), moveForward);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), moveBack);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), moveRight);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), moveLeft);

        // Si on ne bouge pas, on s'arrête
        if (!moveForward && !moveBack && !moveRight && !moveLeft) {
            resetMovementKeys();
        }
    }

    private void avoidObstacles() {
        boolean obstacleDetected = areBlocksInFront();

        if (obstacleDetected) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);

            // Randomly choose left or right to avoid the obstacle
            Random rand = new Random();
            boolean goLeft = rand.nextBoolean();

            /**
            if (goLeft) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
            }
             **/

            stuckCounter++;
            if (stuckCounter > 500) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
                stuckCounter = 0;
            }
        } else {
            stuckCounter = 0;
        }
    }

    private boolean areBlocksNear() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        World world = Minecraft.getMinecraft().theWorld;

        int playerX = MathHelper.floor_double(player.posX);
        int playerY = MathHelper.floor_double(player.posY);
        int playerZ = MathHelper.floor_double(player.posZ);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) { // Vérifie au niveau des pieds et de la tête
                    BlockPos pos = new BlockPos(playerX + x, playerY + y, playerZ + z);
                    Block block = world.getBlockState(pos).getBlock();

                    if (block.getMaterial().isSolid() && !block.getMaterial().equals(Material.air)) {
                        blockNearTemp = true;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Calculate angle.
     */
    private float calculateAngle(double dx, double dz) {
        return (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
    }

    /**
     * Get current facing for the player.
     */
    public EnumFacing getPlayerFacing(EntityPlayerSP player) {
        float yaw = player.rotationYaw;

        // Normaliser l'angle entre 0 et 360 degrés
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        // Convertir l'angle en direction cardinale
        if (yaw >= 315 || yaw < 45) {
            return EnumFacing.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return EnumFacing.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return EnumFacing.NORTH;
        } else {
            return EnumFacing.EAST;
        }
    }

    /**
     * Reset keybindings.
     */
    private void resetMovementKeys() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
    }

    /**
     * Create the path.
     */
    public void createPath(World world, BlockPos start, BlockPos end) {
        new Thread(() -> {
            int attempts = 5; // Number of attempts to find a path
            randomPitchIgnore = (float) (1 + Math.random() * (10f - (1)));
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
                List<BlockPos> rawPath = convertPathToBlockPosList(pathEntity);
                path = optimizePath(rawPath);
                pathIndex = 1; // Reset path index when a new path is created
                isNearTarget = false;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                blockNearTemp = false;
                updateWoodTarget(end);
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
     * Optimize the path. ( Remove diagonal and unnecessary points ).
     */
    private static List<BlockPos> optimizePath(List<BlockPos> originalPath) {
        List<BlockPos> optimizedPath = new ArrayList<>();
        if (originalPath.size() < 3) {
            return originalPath;
        }

        optimizedPath.add(originalPath.get(0));
        BlockPos lastAdded = originalPath.get(0);

        for (int i = 1; i < originalPath.size() - 1; i++) {
            BlockPos current = originalPath.get(i);
            BlockPos next = originalPath.get(i + 1);

            if (!isInLine(lastAdded, current, next)) {
                if (canMoveDiagonally(lastAdded, next)) {
                    lastAdded = next;
                    i++;
                } else {
                    optimizedPath.add(current);
                    lastAdded = current;
                }
            }
        }

        // Little condition.
        if (!lastAdded.equals(originalPath.get(originalPath.size() - 1))) {
            optimizedPath.add(originalPath.get(originalPath.size() - 1));
        }
        if(!optimizedPath.get(optimizedPath.size()-1).equals(originalPath.get(originalPath.size()-1))){
            optimizedPath.add(originalPath.get(originalPath.size()-1));
        }

        return optimizedPath;
    }

    /*
     * Remove unnecessary points.
     */
    private static boolean isInLine(BlockPos a, BlockPos b, BlockPos c) {
        return (b.getX() - a.getX()) * (c.getZ() - a.getZ()) == (c.getX() - a.getX()) * (b.getZ() - a.getZ())
                && (b.getX() - a.getX()) * (c.getY() - a.getY()) == (c.getX() - a.getX()) * (b.getY() - a.getY());
    }

    /*
     * Check if the path contains diagonal movement.
     */
    private static boolean canMoveDiagonally(BlockPos start, BlockPos end) {
        int dx = Math.abs(end.getX() - start.getX());
        int dy = Math.abs(end.getY() - start.getY());
        int dz = Math.abs(end.getZ() - start.getZ());

        // Vérifie si le mouvement est diagonal (1 bloc dans deux directions)
        return (dx == 1 && dz == 1 && dy <= 1) ||
                (dx == 1 && dy == 1 && dz == 0) ||
                (dy == 1 && dz == 1 && dx == 0);
    }

    /*
     * Reset current path.
     */
    public void resetPath() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        if (path != null) {
            path.clear();
        }
        blockNearTemp = false;
        pathIndex = 1;
    }

    /*
     * Update wood target.
     */
    public void updateWoodTarget(BlockPos target) {
        this.woodLoc = target;
        this.randomizedWoodLoc = randomizeBlockPos(target, 0.2f);
    }

    /*
     * Reset target.
     */
    public void resetWoodTarget() {
        this.woodLoc = null;
        this.randomizedWoodLoc = null;
    }

    /**
     * There is block in front of player to stop?
     */
    private boolean areBlocksInFront() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        EnumFacing facing = player.getHorizontalFacing();

        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;

        double forwardOffset = 1.1; // Distance devant le joueur
        x += facing.getFrontOffsetX() * forwardOffset;
        z += facing.getFrontOffsetZ() * forwardOffset;

        BlockPos[] positions = new BlockPos[2]; // Seulement 2 blocs à vérifier

        // Calculer les positions des deux blocs superposés devant le joueur
        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
            positions[0] = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
            positions[1] = new BlockPos(Math.floor(x), Math.floor(y) + 1, Math.floor(z));
        }
        else{
            positions[0] = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
            positions[1] = new BlockPos(Math.floor(x), Math.floor(y) + 1, Math.floor(z));
        }

        renderer.setPositions(positions);

        // Vérifier si l'un de ces blocs n'est pas de l'air
        for (BlockPos pos : positions) {
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if (block.isOpaqueCube()) {
                return true;
            }
        }
        return false;
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
        if (path != null && !path.isEmpty() && path.size() != 0) {
            if(DelClient.settingsManager.getSettingById("auto_fora_pathfinding").getValBoolean())
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
                renderCube(current, 0.1, true, 0, true);
            }
        }

        // Draw start point
        GlStateManager.color(0.0F, 0.0F, 1.0F, 0.6F);
        renderCube(path.get(0), 0.4, true, Color.BLUE.getRGB(), false);

        // Draw end point
        GlStateManager.color(1.0F, 1.0F, 0.0F, 0.6F);
        renderCube(path.get(path.size() - 1), 0.4, true, Color.CYAN.getRGB(), false);

        GlStateManager.color(1.0F, 1.0F, 0.0F, 1F);
        if(woodLoc != null){
            aa = woodLoc;
        }
        if(aa != null){
            renderCube(aa, 0.6, true, 0, true);
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
    private void renderCube(BlockPos pos, double size, boolean drawOutline, int color, boolean rainbow) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        int colorUsed = rainbow ? rainbow(counter[0]) : color;

        if (drawOutline) {
            float red = (colorUsed >> 16 & 255) / 255.0F;
            float green = (colorUsed >> 8 & 255) / 255.0F;
            float blue = (colorUsed & 255) / 255.0F;

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

}