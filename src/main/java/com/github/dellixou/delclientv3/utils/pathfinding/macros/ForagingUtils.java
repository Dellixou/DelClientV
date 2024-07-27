package com.github.dellixou.delclientv3.utils.pathfinding.macros;

import com.github.dellixou.delclientv3.utils.BlockUtils;
import com.github.dellixou.delclientv3.utils.SkyblockUtils;
import com.github.dellixou.delclientv3.utils.enums.SkyblockZone;
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

public class ForagingUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static BlockPos getNearestAccessibleBlockPos(BlockPos goal){
        try{
            PathEntity pathEntity = findPath(mc.theWorld, mc.thePlayer.getPosition(), goal);
            List<BlockPos> converted = convertPathToBlockPosList(pathEntity);
            return converted.get(converted.size()-1);
        }catch (Exception ignored){
            return goal;
        }
    }

    private static PathEntity findPath(World world, BlockPos start, BlockPos end) {
        EntityLiving tempEntity = new EntityLiving(world) {
            @Override
            public void moveEntityWithHeading(float strafe, float forward) {}
        };
        tempEntity.setPosition(start.getX(), start.getY(), start.getZ());

        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        net.minecraft.pathfinding.PathFinder pathFinder = new net.minecraft.pathfinding.PathFinder(nodeProcessor);

        return pathFinder.createEntityPathTo(world, tempEntity, end, 100.0F);
    }

    private static List<BlockPos> convertPathToBlockPosList(PathEntity pathEntity) {
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < pathEntity.getCurrentPathLength(); i++) {
            blockPosList.add(new BlockPos(pathEntity.getPathPointFromIndex(i).xCoord,
                    pathEntity.getPathPointFromIndex(i).yCoord,
                    pathEntity.getPathPointFromIndex(i).zCoord));
        }
        return blockPosList;
    }

    public static boolean thereIsBlockAroundBlock(BlockPos pos){
        World world = mc.theWorld;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = pos.add(x, 0, z);
                if (!world.isAirBlock(checkPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static BlockPos findLargeAccessibleTree(int range, int maxHeight, BlockPlanks.EnumType type, int minLogCount) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        World world = Minecraft.getMinecraft().theWorld;
        if (player == null || world == null) return null;

        BlockPos playerPos = player.getPosition();
        List<BlockPos> potentialTrees = new ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos basePos = playerPos.add(x, 0, z);
                int yOffset = 0;

                // Find ground level
                while (yOffset > -5 && world.isAirBlock(basePos.down(yOffset))) {
                    yOffset--;
                }
                basePos = basePos.down(yOffset);
                boolean canCheck = true;

                // HUB Oak
                if(SkyblockUtils.getCurrentZone().equals(SkyblockZone.HUB) &&  type.equals(BlockPlanks.EnumType.OAK)){
                    if((BlockUtils.isBlockInZone(basePos, new BlockPos(-154, 73, -26), new BlockPos(-135, 73, -47))) || (BlockUtils.isBlockInZone(basePos, new BlockPos(-121, 71, 47), new BlockPos(-63, 70, -80))) || (BlockUtils.isBlockInZone(basePos, new BlockPos(-219, 71, -9), new BlockPos(-225, 70, -21)))){
                        canCheck = false;
                    }
                }

                // Check each vertical level for a tree trunk
                for (int y = 0; y <= maxHeight; y++) {
                    BlockPos checkPos = basePos.up(y);

                    if (isLogOfType(world, checkPos, type) && canCheck) {
                        int logCount = countConnectedLogs(world, checkPos, type, new HashSet<>());
                        if (logCount >= minLogCount) {
                            potentialTrees.add(checkPos);
                        }
                    }
                }
            }
        }

        // Sort by distance with height priority
        potentialTrees.sort(Comparator.comparingDouble(pos -> {
            double horizontalDistance = Math.sqrt(playerPos.distanceSq(pos.getX(), playerPos.getY(), pos.getZ()));
            double verticalDistance = Math.abs(playerPos.getY() - pos.getY());
            return horizontalDistance + verticalDistance * 2; // Prioritize height more
        }));

        // Find the nearest accessible tree base
        for (BlockPos treeBase : potentialTrees) {
            if (isAccessible(world, playerPos, treeBase)) {
                return treeBase;
            }
        }

        return null;
    }

    private static int countConnectedLogs(World world, BlockPos start, BlockPlanks.EnumType type, Set<BlockPos> visited) {
        if (visited.contains(start) || !isLogOfType(world, start, type)) {
            return 0;
        }

        visited.add(start);
        int count = 1;

        for (EnumFacing facing : EnumFacing.values()) {
            count += countConnectedLogs(world, start.offset(facing), type, visited);
        }

        return count;
    }

    private static boolean isLogOfType(World world, BlockPos pos, BlockPlanks.EnumType type) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLog) {
            BlockPlanks.EnumType woodType = state.getValue(BlockPlanks.VARIANT);
            return woodType == type;
        }
        return false;
    }

    private static boolean isAccessible(World world, BlockPos playerPos, BlockPos targetPos) {
        try {
            PathEntity pathEntity = findPath(world, playerPos, targetPos);
            return pathEntity != null && !pathEntity.isFinished();
        } catch (Exception ignored) {
            return false;
        }
    }


}
