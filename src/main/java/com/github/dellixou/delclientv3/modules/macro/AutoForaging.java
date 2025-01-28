package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.*;
import com.github.dellixou.delclientv3.utils.enums.SkyblockZone;
import com.github.dellixou.delclientv3.utils.movements.MovementUtils;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.*;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.BlockState;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.NodePickStyle;
import com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding.PathFinder;
import com.github.dellixou.delclientv3.utils.pathfinding.macros.ForagingUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

public class AutoForaging extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public AutoForaging() {
        super("Auto Foraging", Keyboard.KEY_J, Category.MACRO, true, "auto_fora");
    }

    // Some values
    public BlockPos nearestTree = null;
    private final Minecraft mc = Minecraft.getMinecraft();

    PathExecuter executer = new PathExecuter(new PathExecuterConfig(0.3, 1,
            100, true));
    public AStarPathfinder instance;
    public WorldProvider world;

    public boolean inPathFinding = false;

    private int currentTickToWaitForEachScan = 0;
    private int yawOffset = 0;
    private int pitchOffset = 0;
    private int tickRotRandom = 6;

    private RotationUtils.Rotation savedRotation = null;
    private boolean isNewCycle = true;

    private int currentStuck = 0;

    private Stack<Node> currentPath;

    private Vec3 rayTraceStart;
    private Vec3 rayTraceEnd;

    /*
     * Sets up initial settings for the Auto Foraging module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Scan range", this, 6, 3, 50, true, "auto_fora_scan_range", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Minimum log per tree", this, 7, 1, 30, true, "auto_fora_min_log", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Distance reached", this, 3.5f, 1f, 5f, false, "auto_fora_dist_reach", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Tolerance", this, 2, 0.1, 3, false, "auto_fora_tol", "pathfinding"));

        ArrayList<String> logToReach = new ArrayList<String>();
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()){
            logToReach.add(type.getName());
        }
        DelClient.settingsManager.rSetting(new Setting("Log to reach", this, "OAK", logToReach, "auto_fora_log", "pathfinding"));

        DelClient.settingsManager.rSetting(new Setting("Delay (sec)", this, 1, 0.1, 5, false, "auto_fora_delay", "movements"));
        DelClient.settingsManager.rSetting(new Setting("Look delay (sec)", this, 0.8, 0.1, 5, false, "auto_fora_look_delay", "movements"));
        DelClient.settingsManager.rSetting(new Setting("Look acceptance offset", this, 1, 1, 90, true, "auto_fora_look_acceptance", "movements"));
        DelClient.settingsManager.rSetting(new Setting("Stuck jump delay (sec)", this, 0.8, 0.1, 5, false, "auto_fora_stuck_delay", "movements"));

        DelClient.settingsManager.rSetting(new Setting("Log tracer", this, true, "auto_fora_tracer", "render"));
        DelClient.settingsManager.rSetting(new Setting("Tracer rainbow", this, false, "auto_fora_tracer_rainbow", "render"));
        DelClient.settingsManager.rSetting(new Setting("Draw pathfinding", this, false, "auto_fora_draw_pathfinding", "render"));
    }

    /*
     * Called when the module is enabled.
     */
    public void onEnable() {
        instance = new AStarPathfinder();
        world = new WorldProvider();
        MinecraftForge.EVENT_BUS.register(this);
        MovementUtils.stopMovements();
        MovementUtils.playerAttack(false);
        inPathFinding = false;
        isNewCycle = true;
        super.onEnable();
    }

    /*
     * Called when the module is disabled.
     */
    public void onDisable(){
        MinecraftForge.EVENT_BUS.unregister(this);
        inPathFinding = false;
        isNewCycle = true;
        MovementUtils.playerAttack(false);
        MovementUtils.stopMovements();
        currentPath = null;
        executer.stop();
        super.onDisable();
    }

    /*
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {

        if (this.isToggled() && mc.thePlayer != null && (SkyblockUtils.getCurrentZone().equals(SkyblockZone.HUB) || SkyblockUtils.getCurrentZone().equals(SkyblockZone.PARK))){

            float reach = (float) DelClient.settingsManager.getSettingById("auto_fora_dist_reach").getValDouble();
            float stuckDelay = (float) DelClient.settingsManager.getSettingById("auto_fora_stuck_delay").getValDouble();

            if(!inPathFinding){
                if(mc.thePlayer.onGround){
                    try{
                        int range = (int) DelClient.settingsManager.getSettingById("auto_fora_scan_range").getValDouble();
                        float tol = (float) DelClient.settingsManager.getSettingById("auto_fora_tol").getValDouble();
                        int minLog = (int) DelClient.settingsManager.getSettingById("auto_fora_min_log").getValDouble();
                        float lookDelay = (float) DelClient.settingsManager.getSettingById("auto_fora_look_delay").getValDouble();
                        int lookAcceptance = (int) DelClient.settingsManager.getSettingById("auto_fora_look_acceptance").getValDouble();

                        BlockPlanks.EnumType logType = BlockPlanks.EnumType.valueOf(DelClient.settingsManager.getSettingById("auto_fora_log").getValString().toUpperCase());
                        nearestTree = ForagingUtils.findLargeAccessibleTree(range, 3, logType, minLog);

                        if(nearestTree != null){

                            // Search spot to go
                            BlockPos goal = ForagingUtils.getNearestAccessibleBlockPos(nearestTree);

                            if(goal != null){
                                // Go to point
                                int tickRotate = (int) (lookDelay * 20);
                                int randomTickRotate = MathUtils.getRandomNumber(tickRotate, (int) (tickRotate * 1.1f));
                                float offsetLook = MathUtils.getRandomNumber(lookAcceptance, lookAcceptance+3);

                                Node path = instance.calculate1(world, NodePickStyle.SIDES, 2000, new int[]{mc.thePlayer.getPosition().getX(), mc.thePlayer.getPosition().getY(),
                                        mc.thePlayer.getPosition().getZ()}, new int[]{goal.getX(), goal.getY(), goal.getZ()});

                                if (path == null) {
                                    return;
                                }

                                currentPath = path.toStack();
                                executer.begin(PathExecuter.cutPath(path.toStack(), world), tol, reach, randomTickRotate, goal, (int) offsetLook, nearestTree, null);

                                inPathFinding = true;
                                currentTickToWaitForEachScan = 0;
                                yawOffset = MathUtils.getRandomNumber(-4, 4);
                                pitchOffset = MathUtils.getRandomNumber(-4, 4);
                                tickRotRandom = MathUtils.getRandomNumber(8, 10);
                            }
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                if(!executer.isOnline){

                    RotationUtils.Rotation rot = RotationUtils.getRotationToBlock(nearestTree);
                    rot.yaw += 3;
                    rot.pitch += 2;

                    RotationUtils.smoothLook(rot, tickRotRandom, null);

                    if(currentTickToWaitForEachScan >= (int) (DelClient.settingsManager.getSettingById("auto_fora_delay").getValDouble()*20)){
                        MovementUtils.playerAttack(true);

                        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
                        Vec3 playerPos = mc.thePlayer.getPositionEyes(1.0F);
                        double reachDistance = mc.playerController.getBlockReachDistance();
                        Vec3 rayTraceEnd = playerPos.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

                        MovingObjectPosition movingObjectPosition = mc.theWorld.rayTraceBlocks(playerPos, rayTraceEnd, false, true, true);

                        this.rayTraceStart = playerPos;
                        this.rayTraceEnd = movingObjectPosition != null ? movingObjectPosition.hitVec : rayTraceEnd;

                        if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            IBlockState blockState = mc.theWorld.getBlockState(movingObjectPosition.getBlockPos());
                            if (blockState != null) {
                                Block block = blockState.getBlock();
                                if (!(block instanceof BlockLog)) {
                                    RotationUtils.smoothLook(RotationUtils.getRotationToBlock(new BlockPos(nearestTree.getX(), nearestTree.getY() + 1, nearestTree.getZ())), tickRotRandom, null);
                                }
                            }
                        }

                    }else{
                        currentTickToWaitForEachScan++;
                    }

                    if(mc.theWorld.getBlockState(nearestTree).getBlock().getMaterial() == Material.air){
                        inPathFinding = false;
                        isNewCycle = true;
                        RotationUtils.smoothLook(new RotationUtils.Rotation(mc.thePlayer.rotationPitch+pitchOffset, mc.thePlayer.rotationYaw+yawOffset), tickRotRandom*10, null);
                        new Thread(() -> {
                            try{
                                Thread.sleep(100);
                                MovementUtils.playerAttack(false);
                                Thread.sleep(150);
                                MovementUtils.stopSneaking();
                            }catch (Exception ignored) { }
                        }).start();
                    }

                }else{

                    if(MathUtils.calculateDistanceXYZ(new Vec3(nearestTree.getX() + 0.5f, nearestTree.getY() + 0.5f, nearestTree.getZ() + 0.5f), mc.thePlayer.getPositionVector()) <= reach*reach*1.1f){
                        MovementUtils.sneak();
                    }

                    if(mc.thePlayer.isInWater()){
                        KeybindManager.setKeyBindState(mc.gameSettings.keyBindJump, true);
                    }

                    if(mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isInWater()){
                        if(currentStuck >= stuckDelay*20){
                            MovementUtils.jump();
                            currentStuck = 0;
                        }else{
                            currentStuck++;
                        }
                    }

                    if(PlayerUtils.isCloseToFall()){
                        MovementUtils.stopSneaking();
                    }
                }
            }
        }
    }

    /*
     * Render found blocks and path.
     */
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(nearestTree != null){
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            RenderUtils.highlightBlock(nearestTree, 2, false, false, 200, 0.7f, 0.1f, 0.9f, 0.2f);
            GlStateManager.popMatrix();
            GlStateManager.popAttrib();

            if(DelClient.settingsManager.getSettingById("auto_fora_tracer").getValBoolean()){
                Color c = DelClient.settingsManager.getSettingById("auto_fora_tracer_rainbow").getValBoolean() ? ColorUtils.rainbowEffect(200000000L, 1.0f) : new Color(255, 255, 255, 255);
                RenderUtils.tracerLineToBlock(nearestTree, event.partialTicks, 2, c);
            }

            onRender3D(event.partialTicks);
        }

        if(DelClient.settingsManager.getSettingById("auto_fora_draw_pathfinding").getValBoolean()){
            if(currentPath != null && currentPath.size() > 0 && executer.isOnline){
                Stack<Node> pathCopy = new Stack<>();
                pathCopy.addAll(currentPath);
                for(Node node : pathCopy){
                    RenderUtils.drawBlockBox(new BlockPos(node.x, node.y-1, node.z), new Color(15, 129, 161, 176), 2, event.partialTicks);
                }
            }
        }
    }

    public void onRender3D(float partialTicks) {
        if (rayTraceStart != null && rayTraceEnd != null) {
            renderRayTrace(rayTraceStart, rayTraceEnd, partialTicks);
        }
    }

    private void renderRayTrace(Vec3 start, Vec3 end, float partialTicks) {
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glLineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        worldrenderer.pos(start.xCoord - renderPosX, start.yCoord - renderPosY, start.zCoord - renderPosZ)
                .color(1.0F, 0.0F, 0.0F, 1.0F).endVertex();
        worldrenderer.pos(end.xCoord - renderPosX, end.yCoord - renderPosY, end.zCoord - renderPosZ)
                .color(1.0F, 0.0F, 0.0F, 1.0F).endVertex();

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

}