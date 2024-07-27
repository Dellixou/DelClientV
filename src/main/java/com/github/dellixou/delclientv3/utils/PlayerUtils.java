package com.github.dellixou.delclientv3.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PlayerUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean thereIsBlockAroundPlayer(){
        World world = mc.theWorld;
        BlockPos playerPos = mc.thePlayer.getPosition();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = playerPos.add(x, 0, z);
                if (!world.isAirBlock(checkPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCloseToFall(){
        return mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes((Entity) mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -0.5D, 0.0D).expand(-0.015D, 0.0D, -0.015D)).isEmpty();
    }

    public static Block getBlockBelowPlayer() {
        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY;
        double playerZ = mc.thePlayer.posZ;

        BlockPos blockBelowPos = new BlockPos(playerX, Math.floor(playerY - 0.1), playerZ);

        return mc.theWorld.getBlockState(blockBelowPos).getBlock();
    }

}
