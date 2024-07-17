package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.pathfinding.PathRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.*;


public class AutoForagingV2 extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public AutoForagingV2() {
        super("Auto Foraging V2", Keyboard.KEY_J, Category.MACRO, true, "auto_forav2");
    }

    // Some values
    private PathRenderer pathRenderer;
    private List<BlockPos> scannedWoodBlocks = new ArrayList<>();

    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
        //DelClient.settingsManager.rSetting(new Setting("dist", this, 32, 1, 100, true, "auto_forav2_dist", "wtf pathfinding settings nigga"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        pathRenderer = new PathRenderer();
        MinecraftForge.EVENT_BUS.register(pathRenderer);

        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){

        if(pathRenderer != null){
            MinecraftForge.EVENT_BUS.unregister(pathRenderer);
            pathRenderer = null;
        }

        super.onDisable();
    }

    /**
     * Scan map for woods.
     */
    private void updateScannedWoodBlocks() {
        Iterator<BlockPos> iterator = scannedWoodBlocks.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockLog)) {
                iterator.remove();
                pathRenderer.detected.remove(pos);
            }
        }
    }

    private void scanForNewWoodBlocks() {
        BlockPos nearestTree = getNearestTree();
        if (nearestTree != null) {
            Set<BlockPos> newTreeBlocks = new HashSet<>();
            countConnectedWoodBlocks(nearestTree, newTreeBlocks);
            for (BlockPos pos : newTreeBlocks) {
                if (!scannedWoodBlocks.contains(pos)) {
                    scannedWoodBlocks.add(pos);
                    pathRenderer.detected.add(pos);
                }
            }
        }
    }

    private BlockPos getNearestTree() {
        EntityPlayer player = mc.thePlayer;
        double range = DelClient.settingsManager.getSettingById("auto_fora_wood_range").getValDouble();
        double minDistance = range * range;
        BlockPos closestTree = null;

        BlockPos playerPos = player.getPosition();
        int radius = (int) Math.ceil(range);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos basePos = playerPos.add(x, 0, z);
                if (isValidTree(basePos)) {
                    double distanceSq = playerPos.distanceSq(basePos);
                    if (distanceSq < minDistance && !isNearOtherDetectedTree(basePos)) {
                        minDistance = distanceSq;
                        closestTree = basePos;
                    }
                }
            }
        }

        return closestTree;
    }

    private boolean isValidTree(BlockPos pos) {
        if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockLog)) {
            return false;
        }

        int connectedWoodBlocks = countConnectedWoodBlocks(pos, new HashSet<>());
        return connectedWoodBlocks >= 2;
    }

    private int countConnectedWoodBlocks(BlockPos pos, Set<BlockPos> visited) {
        if (visited.contains(pos) || !(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockLog)) {
            return 0;
        }

        visited.add(pos);
        int count = 1;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos neighbor = pos.add(x, y, z);
                    count += countConnectedWoodBlocks(neighbor, visited);
                }
            }
        }

        return count;
    }

    private boolean isNearOtherDetectedTree(BlockPos pos) {
        int minDistance = 4;
        for (int x = -minDistance; x <= minDistance; x++) {
            for (int y = -minDistance; y <= minDistance; y++) {
                for (int z = -minDistance; z <= minDistance; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos checkPos = pos.add(x, y, z);
                    if (isValidTree(checkPos) && pos.distanceSq(checkPos) <= minDistance * minDistance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
        if (this.isToggled() && mc.thePlayer != null) {
            updateScannedWoodBlocks();
            scanForNewWoodBlocks();
        }
    }

}
