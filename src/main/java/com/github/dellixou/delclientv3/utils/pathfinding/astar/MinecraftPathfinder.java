package com.github.dellixou.delclientv3.utils.pathfinding.astar;

import com.github.dellixou.delclientv3.DelClient;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;

import java.util.ArrayList;
import java.util.List;

public class MinecraftPathfinder {

    // Some values
    Minecraft mc = Minecraft.getMinecraft();
    PathEntity pathEntity = null;

    /**
     * Create the path.
     */
    public List<BlockPos> createPath(BlockPos goal){
        pathEntity = findPath(mc.theWorld, goal);
        return convertPathToBlockPosList(findPath(mc.theWorld, goal));
    }

    /**
     * Find a path.
     */
    private PathEntity findPath(World world, BlockPos end) {
        BlockPos start = mc.thePlayer.getPosition();
        EntityLiving tempEntity = new EntityLiving(world) {
            @Override
            public void moveEntityWithHeading(float strafe, float forward) {}
        };
        tempEntity.setPosition(start.getX(), start.getY(), start.getZ());

        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        net.minecraft.pathfinding.PathFinder pathFinder = new PathFinder(nodeProcessor);

        return pathFinder.createEntityPathTo(world, tempEntity, end, 32F);
    }

    /**
     * Convert to block pos.
     */
    private static List<BlockPos> convertPathToBlockPosList(PathEntity pathEntity) {
        if(pathEntity == null) return null;
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < pathEntity.getCurrentPathLength(); i++) {
            blockPosList.add(new BlockPos(pathEntity.getPathPointFromIndex(i).xCoord,
                    pathEntity.getPathPointFromIndex(i).yCoord,
                    pathEntity.getPathPointFromIndex(i).zCoord));
        }
        return blockPosList;
    }

    private static boolean isInLine(BlockPos a, BlockPos b, BlockPos c) {
        int dx1 = b.getX() - a.getX();
        int dy1 = b.getY() - a.getY();
        int dz1 = b.getZ() - a.getZ();

        int dx2 = c.getX() - a.getX();
        int dy2 = c.getY() - a.getY();
        int dz2 = c.getZ() - a.getZ();

        return dx1 * dy2 == dx2 * dy1 && dx1 * dz2 == dx2 * dz1 && dy1 * dz2 == dy2 * dz1;
    }

    /**
     * Optimize the path.
     */
    public static List<BlockPos> optimizePath(List<BlockPos> path) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }

        List<BlockPos> optimizedPath = new ArrayList<>();
        optimizedPath.add(path.get(0));

        BlockPos start = path.get(0);
        BlockPos end = path.get(path.size() - 1);

        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos current = path.get(i);
            if (!isInLine(start, current, end)) {
                optimizedPath.add(current);
                start = current;
            }
        }

        optimizedPath.add(end);
        return optimizedPath;
    }

}
