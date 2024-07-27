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
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.NodePickStyle;
import com.github.dellixou.delclientv3.utils.pathfinding.oldpathfinding.PathFinder;
import com.github.dellixou.delclientv3.utils.pathfinding.macros.ForagingUtils;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import java.awt.*;
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

    private int currentStuck = 0;

    private Stack<Node> currentPath;

    /*
     * Sets up initial settings for the Auto Foraging module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Scan range", this, 6, 3, 50, true, "auto_fora_scan_range", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Minimum log per tree", this, 7, 1, 30, true, "auto_fora_min_log", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Distance reached", this, 3.5f, 1f, 5f, false, "auto_fora_dist_reach", "pathfinding"));
        DelClient.settingsManager.rSetting(new Setting("Tolerance", this, 2, 0.1, 3, false, "auto_fora_tol", "pathfinding"));

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
        super.onEnable();
    }

    /*
     * Called when the module is disabled.
     */
    public void onDisable(){
        MinecraftForge.EVENT_BUS.unregister(this);
        inPathFinding = false;
        MovementUtils.playerAttack(false);
        MovementUtils.stopMovements();
        currentPath = null;
        executer.stop();
        super.onDisable();
    }

    /*
     * Called periodically to update the module's state.
     */
    // TODO : si quand arrive ne regarde pas un tronc alors bouger un peu
    // TODO : humanizer le comportement quand proche de la cible desuite pitch 0 to 90 to 0 to 90...
    // TODO : faire relacher l'attack avec un peu de random et tenir plus longtemps
    // TODO : ne pas enlever le sneak de suite
    @Override
    public void onUpdate() {

        if (this.isToggled() && mc.thePlayer != null && SkyblockUtils.getCurrentZone().equals(SkyblockZone.HUB)) {

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


                        nearestTree = ForagingUtils.findLargeAccessibleTree(range, minLog+2, BlockPlanks.EnumType.OAK, minLog);

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
                                executer.begin(PathExecuter.cutPath(path.toStack(), world), tol, reach, randomTickRotate, goal, (int) offsetLook, nearestTree);

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
                    RotationUtils.smoothLook(RotationUtils.getRotationToBlock(nearestTree), tickRotRandom, null);
                    if(currentTickToWaitForEachScan >= (int) (DelClient.settingsManager.getSettingById("auto_fora_delay").getValDouble()*20)){
                        MovementUtils.playerAttack(true);
                    }else{
                        currentTickToWaitForEachScan++;
                    }
                    if(mc.theWorld.getBlockState(nearestTree).getBlock().getMaterial() == Material.air){
                        inPathFinding = false;
                        RotationUtils.smoothLook(new RotationUtils.Rotation(mc.thePlayer.rotationPitch+pitchOffset, mc.thePlayer.rotationYaw+yawOffset), tickRotRandom*10, null);
                        new Thread(() -> {
                            try{
                                Thread.sleep(3);
                                MovementUtils.playerAttack(false);
                                Thread.sleep(15);
                                MovementUtils.stopSneaking();
                            }catch (Exception ignored) { }
                        }).start();
                    }

                }else{

                    if(MathUtils.calculateDistanceXYZ(new Vec3(nearestTree.getX() + 0.5f, nearestTree.getY() + 0.5f, nearestTree.getZ() + 0.5f), mc.thePlayer.getPositionVector()) <= reach*reach*1.1f){
                        MovementUtils.sneak();
                    }

                    if(mc.thePlayer.isCollidedHorizontally){
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
}