package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.utils.MathUtils;
import com.github.dellixou.delclientv3.utils.movements.MovementUtils;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.BlockState;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.ExecuterState;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.intefaces.IWorldProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.*;

public class PathExecuter {

    // Some values
    private final PathExecuterConfig config;
    private Minecraft mc = Minecraft.getMinecraft();
    private Stack<Node> currentPath;
    private Vec3 destination;
    private ExecuterState state;
    private final HashSet<KeyBinding> lastKeys = new HashSet<>();
    public boolean isOnline;
    private double initialDistance;
    private double totalDistanceBetweenNodes;
    private double walkedDistance;
    private double lastDistanceToNextNode;
    private int randomPitch = 0;

    // Settings
    float tolerance = 1;
    float finalReach = 2;
    float lookDelay = 0.8f;
    int offsetYaw = 20;
    Vec3 goal;
    BlockPos blockToSee;

    private boolean justStarted = true;

    /*
     * Constructor
     */
    public PathExecuter(PathExecuterConfig config) {
        this.config = config;
    }

    /*
     * Start the walking to the goal.
     */
    public void begin(Stack<Node> path, float tolerance, float finalReach, float lookDelay, BlockPos goal, int offsetYaw, BlockPos blockToSee) {
        this.currentPath = path;
        this.tolerance = tolerance;
        this.finalReach = finalReach;
        this.lookDelay = lookDelay;
        this.offsetYaw = offsetYaw;
        this.blockToSee = blockToSee;
        this.goal = MathUtils.blockPosToVec3(goal);

        if (currentPath.isEmpty()) {
            return;
        }

        Node firstNode = currentPath.pop();
        this.destination = nodeToVec3(firstNode);
        if (!currentPath.isEmpty()) {
            this.initialDistance = MathUtils.calculateDistanceXZ(destination, nodeToVec3(currentPath.peek()));
        }
        this.state = ExecuterState.WALKING;
        this.isOnline = true;
        this.walkedDistance = 0;
        this.totalDistanceBetweenNodes = 0;
        calculateTotalDistance(firstNode);

        justStarted = true;

        MinecraftForge.EVENT_BUS.register(this);
    }

    /*
     * Generate bezier curve.
     */
    public List<Vec3> generateBezierCurve(Vec3 p0, Vec3 p1, Vec3 p2, int steps) {
        List<Vec3> bezierPoints = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            Vec3 point = calculateBezierPoint(p0, p1, p2, t);
            bezierPoints.add(point);
        }
        return bezierPoints;
    }

    /*
     * Calculate bezier point.
     */
    private Vec3 calculateBezierPoint(Vec3 p0, Vec3 p1, Vec3 p2, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;

        double x = (uu * p0.xCoord) + (2 * u * t * p1.xCoord) + (tt * p2.xCoord);
        double y = (uu * p0.yCoord) + (2 * u * t * p1.yCoord) + (tt * p2.yCoord);
        double z = (uu * p0.zCoord) + (2 * u * t * p1.zCoord) + (tt * p2.zCoord);

        return new Vec3(x, y, z);
    }

    /*
     * Calculate total distance.
     */
    private void calculateTotalDistance(Node last) {
        Node lastNode = last;

        for (int i = 0; i < currentPath.size(); i++) {
            Node node = currentPath.get(currentPath.size() - i - 1);
            if (lastNode != null) {
                totalDistanceBetweenNodes += MathUtils.calculateDistanceXZ(nodeToVec3(lastNode), nodeToVec3(node));
            }
            lastNode = node;
        }
    }

    /*
     * Tick event, for manage walking state, stuck state, rotating state.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isOnline) return;

        switch (state) {
            case WALKING:
                handleWalkingState();
                break;
            case STUCK:
                handleStuckState();
                break;
        }

        // Enough distance for attack block
        if(MathUtils.calculateDistanceXYZ(mc.thePlayer.getPositionVector(), goal) <= finalReach){
            this.stop();
        }
    }

    /*
     * Handle walking state.
     */
    private void handleWalkingState() {
        double distanceToNextNode = MathUtils.calculateDistanceXZ(destination, mc.thePlayer.getPositionVector());
        updateWalkedDistance(distanceToNextNode);

        if (justStarted) {
            BlockPos a = new BlockPos(goal.xCoord, goal.yCoord, goal.zCoord);
            if(RotationUtils.playerLookingAtYaw(a, offsetYaw)){
                justStarted = false;
            }else{
                RotationUtils.Rotation rotation = RotationUtils.getRotation(goal);
                RotationUtils.smoothLook(rotation, (int) (lookDelay), null);
            }
        }
        else{
            if (distanceToNextNode < tolerance) {
                moveToNextNode();
                return;
            }

            manageKeyBindings(distanceToNextNode);

            if(MathUtils.calculateDistanceXYZ(mc.thePlayer.getPositionVector(), goal) <= finalReach*2){
                RotationUtils.Rotation rotation = RotationUtils.getRotationToBlock(blockToSee);
                RotationUtils.smoothLook(rotation, (int) (lookDelay), null);
            }else{
                RotationUtils.Rotation rotation = RotationUtils.getRotation(destination);
                if (randomPitch == 0) {
                    randomPitch = MathUtils.getRandomNumberPlusOne(10, 20);
                }
                rotation.pitch = randomPitch;
                RotationUtils.smoothLook(rotation, (int) (lookDelay), null);
            }
        }
    }

    /*
     * Update total walked distance.
     */
    private void updateWalkedDistance(double distanceToNextNode) {
        if (lastDistanceToNextNode != 0) {
            double distanceChange = lastDistanceToNextNode - distanceToNextNode;
            walkedDistance += distanceChange;
        }

        lastDistanceToNextNode = distanceToNextNode;
    }

    /*
     * Manege key binds.
     */
    private void manageKeyBindings(double distanceToNextNode) {
        if(!MovementUtils.isCollidedHorizontally()){
            KeybindManager.setKeyBindState(KeybindManager.keyBindJump, (destination.yCoord > mc.thePlayer.getPositionVector().yCoord && distanceToNextNode <= 2));
        }

        HashSet<KeyBinding> keysToPress = KeybindManager.getNeededKeyPresses(mc.thePlayer.getPositionVector(), destination);
        for (KeyBinding key : KeybindManager.getListKeybinds()) {
            if (key != KeybindManager.keyBindJump) {
                KeybindManager.setKeyBindState(key, keysToPress.contains(key));
            }
        }
    }

    /*
     * Handle stuck state.
     */
    private void handleStuckState() {
        if (!config.rePathfindOnStuck) {
            DelClient.sendWarning("stucked");
            stop();
            return;
        }

        // TODO: Implement re-pathfinding on stuck.
    }

    /*
     * Stop the walking.
     */
    public void stop() {
        isOnline = false;
        KeybindManager.resetKeybindState();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    /*
     * Some change when current node reached, for the next one.
     */
    private void moveToNextNode() {
        if (currentPath.isEmpty()) {
            stop();
            return;
        }

        destination = nodeToVec3(currentPath.pop());
        lastDistanceToNextNode = 0;
        randomPitch = 0;
    }

    /*
     * Convert the Node to Vec3.
     */
    private Vec3 nodeToVec3(Node node) {
        return new Vec3(node.x + 0.5, node.y, node.z + 0.5);
    }

    /*
     * Cut the path.
     */
    public static Stack<Node> cutPath(Stack<Node> currentPath, IWorldProvider world) {
        Stack<Node> newPath = new Stack<>();
        Node lastNode = null;
        for (int i = 0; i < currentPath.size(); i++) {
            Node currentNode = currentPath.get(i);
            if (lastNode == null) {
                lastNode = currentNode;
                newPath.push(lastNode);
                continue;
            }

            if (isBlockObstructed(world, lastNode, currentNode) || i == currentPath.size() - 1) {
                newPath.push(currentPath.get(i - 1));
                lastNode = currentNode;
            }
        }

        return newPath;
    }

    /*
     * Is block in the path?
     */
    private static boolean isBlockObstructed(IWorldProvider world, Node startNode, Node endNode) {
        if (bresenham(startNode, endNode, world) != null || endNode.y > startNode.y) {
            return true;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (world.getBlockState(new int[]{endNode.x + x, endNode.y, endNode.z + z}) == BlockState.OBSTRUCTED) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Bresenham algorithm.
     */
    static BlockPos bresenham(final Node n1, final Node n2, IWorldProvider world) {
        Vec3 start = new Vec3(n1.x, n1.y, n1.z);
        Vec3 end = new Vec3(n2.x, n2.y, n2.z);
        int x1 = MathHelper.floor_double(end.xCoord);
        int y1 = MathHelper.floor_double(end.yCoord);
        int z1 = MathHelper.floor_double(end.zCoord);
        int x0 = MathHelper.floor_double(start.xCoord);
        int y0 = MathHelper.floor_double(start.yCoord);
        int z0 = MathHelper.floor_double(start.zCoord);

        if (world.getBlockState(new int[]{x0, y0, z0}) == BlockState.OBSTRUCTED) {
            return new BlockPos(x0, y0, z0);
        }

        int iterations = 200;

        while (iterations-- >= 0) {
            //RenderMultipleBlocksMod.renderMultipleBlocks(BlockUtils.fromBPToVec(new BlockPos(x0, y0, z0)), true);
            if (x0 == x1 && y0 == y1 && z0 == z1) {
                return null; //new BlockPos(end);
            }

            boolean hasNewX = true;
            boolean hasNewY = true;
            boolean hasNewZ = true;

            double newX = 999.0;
            double newY = 999.0;
            double newZ = 999.0;

            if (x1 > x0) {
                newX = (double) x0 + 1.0;
            } else if (x1 < x0) {
                newX = (double) x0 + 0.0;
            } else {
                hasNewX = false;
            }
            if (y1 > y0) {
                newY = (double) y0 + 1.0;
            } else if (y1 < y0) {
                newY = (double) y0 + 0.0;
            } else {
                hasNewY = false;
            }
            if (z1 > z0) {
                newZ = (double) z0 + 1.0;
            } else if (z1 < z0) {
                newZ = (double) z0 + 0.0;
            } else {
                hasNewZ = false;
            }

            double stepX = 999.0;
            double stepY = 999.0;
            double stepZ = 999.0;

            double dx = end.xCoord - start.xCoord;
            double dy = end.yCoord - start.yCoord;
            double dz = end.zCoord - start.zCoord;

            if (hasNewX) {
                stepX = (newX - start.xCoord) / dx;
            }
            if (hasNewY) {
                stepY = (newY - start.yCoord) / dy;
            }
            if (hasNewZ) {
                stepZ = (newZ - start.zCoord) / dz;
            }
            if (stepX == -0.0) {
                stepX = -1.0E-4;
            }
            if (stepY == -0.0) {
                stepY = -1.0E-4;
            }
            if (stepZ == -0.0) {
                stepZ = -1.0E-4;
            }

            EnumFacing enumfacing;
            if (stepX < stepY && stepX < stepZ) {
                enumfacing = x1 > x0 ? EnumFacing.WEST : EnumFacing.EAST;
                start =
                        new Vec3(newX, start.yCoord + dy * stepX, start.zCoord + dz * stepX);
            } else if (stepY < stepZ) {
                enumfacing = y1 > y0 ? EnumFacing.DOWN : EnumFacing.UP;
                start =
                        new Vec3(start.xCoord + dx * stepY, newY, start.zCoord + dz * stepY);
            } else {
                enumfacing = z1 > z0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                start =
                        new Vec3(start.xCoord + dx * stepZ, start.yCoord + dy * stepZ, newZ);
            }
            x0 =
                    MathHelper.floor_double(start.xCoord) -
                            (enumfacing == EnumFacing.EAST ? 1 : 0);
            y0 =
                    MathHelper.floor_double(start.yCoord) -
                            (enumfacing == EnumFacing.UP ? 1 : 0);
            z0 =
                    MathHelper.floor_double(start.zCoord) -
                            (enumfacing == EnumFacing.SOUTH ? 1 : 0);

            //RenderMultipleBlocksMod.renderMultipleBlocks(BlockUtils.fromBPToVec(new BlockPos(x0, y0, z0)), true);
            if (
                    world.getBlockState(new int[]{x0, y0, z0}) == BlockState.OBSTRUCTED
            ) {
                return new BlockPos(x0, y0, z0);
            }
        }

        return null;
    }
}