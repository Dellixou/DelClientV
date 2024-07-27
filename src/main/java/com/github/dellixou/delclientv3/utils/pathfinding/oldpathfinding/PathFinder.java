package com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoForaging;
import com.github.dellixou.delclientv3.utils.movements.MovementUtils;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class PathFinder {

    private static Minecraft mc = Minecraft.getMinecraft();

    private static Map<AStarNode, List<AStarNode>> pathCache;
    public List<AStarNode> currentPath;
    private static boolean cancelled = false;
    AutoForaging autofora;

    private static final double ACCEPTANCE_RADIUS = 1.0;
    private static int stuckCounter = 0;
    public Thread pathThread = null;

    private static boolean pathing = false;
    public static void cancel() {
        cancelled = true;
    }

    public static boolean isPathing() {
        return pathing;
    }

    // -------------------------- MAKE THE PATH FINDER --------------------------

    public PathFinder() {
        MinecraftForge.EVENT_BUS.register(this);
        pathCache = new HashMap<>();
        autofora = (AutoForaging) ModuleManager.getModuleById("auto_forav2");
    }

    public static List<AStarNode> compute(BlockPos end, int depth) {
        BlockPos playerPos = mc.thePlayer.getPosition();
        BlockPos start = new BlockPos(playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5);
        PriorityQueue<AStarNode> openQueue = new PriorityQueue<>(Comparator.comparingDouble(AStarNode::getTotalCost));
        List<AStarNode> closedList = new ArrayList<>();

        AStarNode endNode = new AStarNode(end);
        AStarNode startNode = new AStarNode(start, endNode);

        openQueue.add(startNode);

        for (int i = 0; i < depth; i++) {
            if (cancelled) {
                pathing = false;
                return Collections.emptyList();
            }
            if (openQueue.isEmpty()) {
                return new ArrayList<>();
            }

            AStarNode currentNode = openQueue.poll();
            closedList.add(currentNode);

            if (currentNode.equals(endNode)) {
                List<AStarNode> path = getPath(currentNode);
                return simplifyPath(path);
            }

            populateNeighbours(openQueue, closedList, currentNode, endNode);
        }

        return new ArrayList<>();
    }

    private static void populateNeighbours(PriorityQueue<AStarNode> openQueue,
                                           List<AStarNode> closedList,
                                           AStarNode current,
                                           AStarNode endNode) {
        if (pathCache.containsKey(current)) {
            List<AStarNode> cachedNeighbors = pathCache.get(current);
            for (AStarNode neighbour : cachedNeighbors) {
                if (!closedList.contains(neighbour)) {
                    if (neighbour.getTotalCost() < current.getTotalCost() || !openQueue.contains(neighbour)) {
                        if (neighbour.canBeTraversed()) {
                            openQueue.remove(neighbour);
                            openQueue.add(neighbour);
                        }
                    }
                }
            }
        } else {
            List<AStarNode> neighbours = new ArrayList<>();
            // Cardinal directions
            neighbours.add(new AStarNode(-1, 0, 0, current, endNode));
            neighbours.add(new AStarNode(1, 0, 0, current, endNode));
            neighbours.add(new AStarNode(0, 0, -1, current, endNode));
            neighbours.add(new AStarNode(0, 0, 1, current, endNode));
            neighbours.add(new AStarNode(0, 1, 0, current, endNode));
            neighbours.add(new AStarNode(0, -1, 0, current, endNode));

            // Diagonal directions
            neighbours.add(new AStarNode(-1, 0, -1, current, endNode));
            neighbours.add(new AStarNode(-1, 0, 1, current, endNode));
            neighbours.add(new AStarNode(1, 0, -1, current, endNode));
            neighbours.add(new AStarNode(1, 0, 1, current, endNode));

            // Diagonal directions with vertical movement
            neighbours.add(new AStarNode(-1, 1, 0, current, endNode));
            neighbours.add(new AStarNode(1, 1, 0, current, endNode));
            neighbours.add(new AStarNode(0, 1, -1, current, endNode));
            neighbours.add(new AStarNode(0, 1, 1, current, endNode));
            neighbours.add(new AStarNode(-1, -1, 0, current, endNode));
            neighbours.add(new AStarNode(1, -1, 0, current, endNode));
            neighbours.add(new AStarNode(0, -1, -1, current, endNode));
            neighbours.add(new AStarNode(0, -1, 1, current, endNode));

            pathCache.put(current, neighbours);

            for (AStarNode neighbour : neighbours) {
                if (!closedList.contains(neighbour)) {
                    if (neighbour.getTotalCost() < current.getTotalCost() || !openQueue.contains(neighbour)) {
                        if (neighbour.canBeTraversed()) {
                            openQueue.remove(neighbour);
                            openQueue.add(neighbour);
                        }
                    }
                }
            }
        }
    }

    private static List<AStarNode> getPath(AStarNode currentNode) {
        List<AStarNode> path = new ArrayList<>();
        path.add(currentNode);
        AStarNode parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }

    private static List<AStarNode> simplifyPath(List<AStarNode> path) {
        List<AStarNode> simplifiedPath = new ArrayList<>();
        if (path.isEmpty()) {
            return simplifiedPath;
        }

        AStarNode lastNode = path.get(0);
        simplifiedPath.add(lastNode);

        for (int i = 1; i < path.size() - 1; i++) {
            AStarNode currentNode = path.get(i);
            AStarNode nextNode = path.get(i + 1);

            if (!isDiagonalMove(lastNode, nextNode) || !isPathClear(lastNode, nextNode)) {
                simplifiedPath.add(currentNode);
                lastNode = currentNode;
            }
        }

        simplifiedPath.add(path.get(path.size() - 1));
        return simplifiedPath;
    }

    private static boolean isDiagonalMove(AStarNode node1, AStarNode node2) {
        int dx = Math.abs(node1.getX() - node2.getX());
        int dz = Math.abs(node1.getZ() - node2.getZ());
        return dx == 1 && dz == 1;
    }

    private static boolean isPathClear(AStarNode node1, AStarNode node2) {
        int x1 = node1.getX();
        int y1 = node1.getY();
        int z1 = node1.getZ();
        int x2 = node2.getX();
        int y2 = node2.getY();
        int z2 = node2.getZ();

        if (x1 == x2 || z1 == z2) {
            return true;
        }

        BlockPos pos1 = new BlockPos(x1, y1, z2);
        BlockPos pos2 = new BlockPos(x2, y1, z1);

        return isValidAirBlock(pos1) && isValidAirBlock(pos2);
    }

    private static boolean isValidAirBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().isAir(mc.theWorld, pos) &&
                mc.theWorld.getBlockState(pos.up()).getBlock().isAir(mc.theWorld, pos.up());
    }

    // -------------------------- WALK THE PATH FINDER --------------------------

    public void executePath(BlockPos goal, float reachDistance, float tol, int tickRotate, float offsetLook, float stuckDelay) {
        MovementUtils.stopMovements();
        pathing = true;
        cancelled = false;
        List<AStarNode> nodes = compute(goal, 10000);
        currentPath = nodes;
        pathThread = new Thread(() -> {

            for (int i = 0; i < nodes.size(); i++) {
                if (cancelled) {
                    pathing = false;
                    cancelled = false;
                    return;
                }
                AStarNode node = nodes.get(i);

                if(mc.thePlayer.getDistanceSq(goal) <= reachDistance*2f){
                    cancelled = true;
                    MovementUtils.moveForward(false);
                    currentPath = null;
                }

                while (Math.abs(node.getX() + 0.5 - mc.thePlayer.posX) > tol || Math.abs(node.getY() - mc.thePlayer.posY) > tol + 2 || Math.abs(node.getZ() + 0.5 - mc.thePlayer.posZ) > tol) {
                    if (cancelled) {
                        pathing = false;
                        cancelled = false;
                        return;
                    }
                    BlockPos lookPos = new BlockPos(node.getX(), node.getY() + mc.thePlayer.eyeHeight, node.getZ());

                    RotationUtils.smoothLook(RotationUtils.getRotationToBlock(lookPos), tickRotate, null);

                    if(RotationUtils.playerLookingAt(lookPos, offsetLook)){
                        if(mc.thePlayer.onGround){
                            MovementUtils.moveForward(true);
                        }else{
                            if(mc.thePlayer.fallDistance > 0){
                                MovementUtils.moveForward(true);
                            }else{
                                MovementUtils.moveForward(false);
                            }
                        }
                    }else{
                        if(!mc.thePlayer.onGround){
                            if(mc.thePlayer.fallDistance > 0){
                                MovementUtils.moveForward(true);
                            }else{
                                MovementUtils.moveForward(false);
                            }
                        }
                    }

                    if (mc.thePlayer.isCollidedHorizontally) {
                        if(stuckCounter >= stuckDelay){
                            MovementUtils.jump();
                            stuckCounter = 0;
                        }else{
                            stuckCounter++;
                        }
                    }

                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            MovementUtils.stopSneaking();
            MovementUtils.stopMovements();
            currentPath = null;
            pathing = true;
        });
        pathThread.start();
    }

    // -------------------------- RENDER THE PATH FINDER --------------------------

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (currentPath != null) {
            //PathRenderer.renderPath(getAsVec3Path(currentPath), getAsBlockPosPath(currentPath));
        }
    }

    // -------------------------- MISC FOR THE PATH FINDER ------------------------

    public List<BlockPos> getAsBlockPosPath(List<AStarNode> path) {
        List<BlockPos> blockPos = new ArrayList<>();
        for (AStarNode node : path) {
            blockPos.add(new BlockPos(node.getX(), node.getY() - 1, node.getZ()));
        }

        return blockPos;
    }

    private List<Vec3> getAsVec3Path(List<AStarNode> path) {
        List<Vec3> vec3s = new ArrayList<>();
        for (AStarNode node : path) {
            vec3s.add(node.asVec3(0.5, 0.5, 0.5));
        }

        return vec3s;
    }

    // -------------------------- VECTOR FOR PATH FINDER --------------------------

    static class Vector {

        double x, y, z;

        Vector(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Vector vector = (Vector) obj;
            return Double.compare(vector.x, x) == 0 &&
                    Double.compare(vector.y, y) == 0 &&
                    Double.compare(vector.z, z) == 0;
        }
    }
}
