package com.github.dellixou.delclientv3.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;

import java.util.*;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static List<BlockPos> searchBlocksAroundPlayer(int range, int maxHeight, Block block) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player == null) { return foundBlocks; }

        BlockPos playerPos = player.getPosition();
        World world = player.getEntityWorld();

        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= maxHeight; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (world.getBlockState(pos).getBlock() == block) {
                        foundBlocks.add(pos);
                    }
                }
            }
        }

        // Sort by distance
        foundBlocks.sort(Comparator.comparingDouble(pos ->
                pos.distanceSq(playerPos.getX(), playerPos.getY(), playerPos.getZ())
        ));

        return foundBlocks;
    }

    public static List<BlockPos> searchLogsAroundPlayer(int range, int maxHeight, BlockPlanks.EnumType type) {
        List<BlockPos> foundLogs = new ArrayList<>();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player == null) { return foundLogs; }

        BlockPos playerPos = player.getPosition();
        World world = Minecraft.getMinecraft().theWorld;

        for (int x = -range; x <= range; x++) {
            for (int y = -1; y <= maxHeight; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if(state.getBlock() instanceof BlockLog){
                        BlockPlanks.EnumType woodType = state.getValue(BlockPlanks.VARIANT);
                        if (woodType == type) {
                            foundLogs.add(pos);
                        }
                    }
                }
            }
        }

        // Sort by distance with height priority
        foundLogs.sort(Comparator.comparingDouble(pos -> {
            double horizontalDistance = Math.sqrt(playerPos.distanceSq(pos.getX(), playerPos.getY(), pos.getZ()));
            double verticalDistance = Math.abs(playerPos.getY() - pos.getY());
            return horizontalDistance + verticalDistance * 2; // Prioritize height more
        }));

        return foundLogs;
    }

    public static BlockPos getBlockPosCenter(BlockPos pos) {
        return new BlockPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean isBlockInZone(BlockPos blockToCheck, BlockPos corner1, BlockPos corner2) {
        int xMin = Math.min(corner1.getX(), corner2.getX());
        int xMax = Math.max(corner1.getX(), corner2.getX());
        int zMin = Math.min(corner1.getZ(), corner2.getZ());
        int zMax = Math.max(corner1.getZ(), corner2.getZ());

        int blockX = blockToCheck.getX();
        int blockZ = blockToCheck.getZ();

        return blockX >= xMin && blockX <= xMax && blockZ >= zMin && blockZ <= zMax;
    }

}
