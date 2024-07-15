package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.misc.Route;
import com.github.dellixou.delclientv3.utils.misc.RouteItem;
import com.github.dellixou.delclientv3.utils.misc.Waypoint;
import com.github.dellixou.delclientv3.utils.movements.PlayerMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserRoute extends Module{

    /**
     * Constructor to initialize the UserRoute module.
     */
    public UserRoute(){
        super("User Route", Keyboard.KEY_N, Category.MOVEMENT, true, "user_route");
    }

    /**
     * Sets up initial settings for the UserRoute module.
     */
    @Override
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Tolerance", this, 5, 1, 30, true, "user_route_tol", "tolerance"));
        DelClient.settingsManager.rSetting(new Setting("Jump T", this, 5, 1, 30, true, "user_route_jump_tol", "tolerance"));
        DelClient.settingsManager.rSetting(new Setting("Wait T", this, 5, 1, 30, true, "user_route_wait_tol", "tolerance"));
        DelClient.settingsManager.rSetting(new Setting("Look Delay", this, 100, 5, 1500, true, "user_route_look_delay", "look"));
        DelClient.settingsManager.rSetting(new Setting("Click Tol", this, 25, 1, 100, true, "user_route_click_tol", "tolerance"));
        DelClient.settingsManager.rSetting(new Setting("Line Width", this, 10, 10, 100, true, "user_route_width", "visual"));
        DelClient.settingsManager.rSetting(new Setting("Route Toggle", this, 3, 1, 10, true, "user_route_toggle", "global"));
        DelClient.settingsManager.rSetting(new Setting("Camera T", this, 3, 1, 100, true, "user_route_camera_tick", "camera"));
        DelClient.settingsManager.rSetting(new Setting("Rot Instant", this, false, "user_route_rot_instant", "camera"));
        DelClient.settingsManager.rSetting(new Setting("Render Dist", this, 50, 2, 150, true, "user_route_render_distance", "visual"));
        DelClient.settingsManager.rSetting(new Setting("Through Wall", this, true, "user_route_render_wall", "visual"));
    }

    // Fields for managing routes and movement
    public Route currentRoute = null;
    public Route currentEditRoute = null;
    public List<Route> routes = new ArrayList<>();
    public ModuleButton moduleButtonUser;
    private PlayerMove pm;
    private double tolerance = 0.5;
    private int tickToWait = 1500;
    private int currentTick = 0;
    private double clickTolerance = 0.3;
    private double toggleTolerance = 0.3;
    private double cameraTick = 0.3;
    private double jumpTol = 0.5;
    private double waitTol = 0.5;
    private boolean rotInstant = false;
    public double Xpos;
    public double Ypos;
    public double Zpos;
    public boolean isWaiting;
    public float timeToWait;

    public boolean isLooking = false;
    public int yawToLook = 0;
    public int pitchToLook = 0;


    private ScheduledExecutorService executorService;
    private ScheduledExecutorService waiter;
    private ScheduledExecutorService looker;


    /**
     * Called when the module is enabled.
     */
    @Override
    public void onEnable(){
        currentRoute = null;
        pm = null; // Reset the PlayerMove instance on enable
        this.tolerance = DelClient.settingsManager.getSettingById("user_route_tol").getValDouble() * 0.1;
        this.tickToWait = (int)DelClient.settingsManager.getSettingById("user_route_look_delay").getValDouble();
        this.clickTolerance = DelClient.settingsManager.getSettingById("user_route_click_tol").getValDouble() * 0.01;
        this.toggleTolerance = DelClient.settingsManager.getSettingById("user_route_toggle").getValDouble() * 0.1;
        this.cameraTick = DelClient.settingsManager.getSettingById("user_route_camera_tick").getValDouble() * 0.1;
        this.jumpTol = DelClient.settingsManager.getSettingById("user_route_jump_tol").getValDouble() * 0.1;
        this.waitTol = DelClient.settingsManager.getSettingById("user_route_wait_tol").getValDouble() * 0.1;
        this.rotInstant = DelClient.settingsManager.getSettingById("user_route_rot_instant").getValBoolean();
        try{
            for (Route route : routes){
                for (Waypoint waypoint : route.getWaypoints()) {
                    waypoint.setDone(false);
                }
            }
        } catch (Exception ignored){}

        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::getPlayerPos, 0, 25, TimeUnit.MILLISECONDS);
    }

    /**
     * Get Player Pos.
     */
    public void getPlayerPos(){
        Xpos = mc.thePlayer.posX;
        Ypos = mc.thePlayer.posY;
        Zpos = mc.thePlayer.posZ;
    }

    /**
     * Called when the module is disabled.
     */
    @Override
    public void onDisable(){
        if (pm != null) {
            pm.stopMoving();
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        try{
            for (Route route : routes){
                for (Waypoint waypoint : route.getWaypoints()) {
                    waypoint.setDone(false);
                }
            }
        } catch (Exception e){}
        currentRoute = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    /**
     * Simulates a client-side right-click action.
     */
    public void doClientRightClick() {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
    }


    /**
     * Simulates a client-side left-click action.
     */
    public void doClientLeftClick() {
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
    }

    public float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw > 180) {
            yaw -= 360;
        } else if (yaw < -180) {
            yaw += 360;
        }
        return yaw;
    }

    /**
     * Use this function to force a right click ( can be used in gui )
     */
    public static void forceRightClick() {
        try {
            Method rightClickMethod = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147121_ag");
            rightClickMethod.setAccessible(true);
            rightClickMethod.invoke(Minecraft.getMinecraft());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {

        if (Minecraft.getMinecraft().thePlayer == null) return;

        if(isLooking){
            if(normalizeYaw((int)mc.thePlayer.rotationYaw) == yawToLook && (int)mc.thePlayer.rotationPitch == pitchToLook){
                isLooking = false;
            }
        }

        if (this.isToggled() && !isWaiting) {

            //if(!DelClient.instance.currentPlayerLocation.equalsIgnoreCase("dungeon")) return;

            // There are routes?
            if (routes.size() <= 0) return;

            // Start toggle tolerance
            for (Route route : routes) {
                if (route.getWaypoints().size() >= 2) {
                    double deltaY = route.getWaypoints().get(0).getY() - Ypos;
                    double deltaZ = route.getWaypoints().get(0).getZ() - Zpos;
                    double deltaX = route.getWaypoints().get(0).getX() - Xpos;
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    double height = Math.abs(deltaY);

                    if (distance < toggleTolerance && mc.thePlayer.onGround && height < 2 && height > -2) {
                        for(Waypoint waypoint : route.getWaypoints()){
                            waypoint.setDone(false);
                        }
                        currentRoute = route;
                        currentRoute.getWaypoints().get(0).setDone(true);
                    }
                }
            }

            // In a Route
            if (currentRoute == null) return;

            // The current rouge is empty?
            if (currentRoute.getWaypoints().isEmpty()) return;

            // Update MovePlayer and check if done
            try {
                if (pm != null) {
                    pm.movePlayer();
                    if (pm.isFinished()) {
                        Waypoint currentWaypoint = getCurrentWaypoint();
                        if (currentWaypoint != null) {
                            currentWaypoint.setDone(true);
                            //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aMovement Done");
                            try{
                                Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1, 1);
                            }catch (Exception ignored){}
                        }
                        pm = null; // Prepare for the next waypoint
                    }
                }
            } catch (Exception ignored) {}

            // Independents points
            for (Waypoint waypoint : currentRoute.getWaypoints()) {
                if (waypoint.getIndependent() && !waypoint.getDone()) {
                    double deltaY = waypoint.getY() - Ypos;
                    double deltaZ = waypoint.getZ() - Zpos;
                    double deltaX = waypoint.getX() - Xpos;
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    double height = Math.abs(deltaY);

                    // LOOK INDEPENDENTS
                    if (waypoint.getLookOnly()) {
                        if (distance < 1 && height < 2 && height > -2) {
                            mc.thePlayer.rotationPitch = (float) waypoint.getPitch();
                            mc.thePlayer.rotationYaw = (float) waypoint.getYaw();
                            isLooking = true;

                            looker = Executors.newScheduledThreadPool(1);
                            looker.schedule(() -> {
                                yawToLook = (int) waypoint.getYaw();
                                pitchToLook = (int) waypoint.getPitch();
                            }, 50, TimeUnit.MILLISECONDS);
                            waypoint.setDone(true);
                            return;
                            //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aIndependent done! &cLooked");
                        }
                    }

                    // BONZO
                    if(waypoint.getBonzo()){
                        if (distance <= 1 && height < 2 && height > -2) { //  && mc.thePlayer.onGround
                            waypoint.setDone(true);
                            Thread thread = new Thread(() -> {
                                try {
                                    int slotItem = findItemIndex(false, true);
                                    //Thread.sleep(30);
                                    mc.thePlayer.rotationPitch = 90;
                                    if (slotItem != -1) {
                                        try {
                                            mc.thePlayer.inventory.currentItem = slotItem;
                                        } catch (Exception ignored) {
                                            DelClient.sendChatToClient("&cBonzo &7not found!");
                                        }
                                    }
                                    if (slotItem == -1) {
                                        DelClient.sendChatToClient("&dDelClient &5--> &cError : &8Bonzo is not found on hot bar!");
                                    }
                                    ItemStack itemHand = mc.thePlayer.getHeldItem();
                                    Thread.sleep(30);
                                    forceRightClick();
                                    //mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                                    int waitTime = 150;
                                    Thread.sleep(waitTime);
                                    mc.thePlayer.rotationPitch = 0;
                                } catch (InterruptedException ignored) {
                                }
                            });
                            thread.start();

                            // BAN !!!
                            //sendHotbarChangePacket(findItemIndex(false, true));
                            //sendLookPacket(mc.thePlayer.rotationYaw, 90);
                            //sendRightClickPacket();
                        }
                    }

                    // JUMPING
                    if(waypoint.getUseJump()){
                        if(waypoint.getEdgeJump()){
                            if (distance < jumpTol && height < 2 && height > -1 && mc.thePlayer.onGround && !waypoint.isWaitingJump) {
                                waypoint.isWaitingJump = true;
                            }else if(mc.thePlayer.onGround && !mc.thePlayer.isSneaking() && !mc.gameSettings.keyBindSneak.isPressed()
                                    && this.mc.theWorld.getCollidingBoundingBoxes((Entity) mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -0.5D, 0.0D).expand(-0.001D, 0.0D, -0.001D)).isEmpty()){
                                mc.thePlayer.jump();
                                //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aIndependent done! &cJumped Edge");
                                waypoint.setDone(true);
                                waypoint.isWaitingJump = false;
                            }
                        }else{
                            if (distance < jumpTol && height < 2 && height > -1 && mc.thePlayer.onGround) {
                                mc.thePlayer.jump();
                                //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aIndependent done! &cJumped");
                                waypoint.setDone(true);
                            }
                        }
                    }

                    // CLICK INDEPENDENTS
                    if(waypoint.getClick() && !isLooking) {
                        if ((distance < clickTolerance && height < 2 && height > -2)) {
                            // SWAP ITEM HANDS
                            int slotItem = -1;
                            if (String.valueOf(waypoint.getRouteItem()).equalsIgnoreCase("tnt")) {
                                slotItem = findItemIndex(true, false);
                                if (slotItem != -1) {
                                    try{
                                        mc.thePlayer.inventory.currentItem = slotItem;
                                        doClientLeftClick();
                                    }catch (Exception ignored){
                                    }
                                }
                                if (slotItem == -1) {
                                    ItemStack itemHand = mc.thePlayer.getHeldItem();
                                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                                    //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &cThe item : TNT is not found!");
                                    DelClient.sendChatToClient("&dDelClient &5--> &cError : &8TNT is not found on hot bar!");
                                }
                            } else if (String.valueOf(waypoint.getRouteItem()).equalsIgnoreCase("bonzo")) {
                                slotItem = findItemIndex(false, true);
                                if (slotItem != -1) {
                                    try{
                                        mc.thePlayer.inventory.currentItem = slotItem;
                                        ItemStack itemHand = mc.thePlayer.getHeldItem();
                                        forceRightClick();
                                        //mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                                    }catch (Exception ignored){
                                    }
                                }

                                if (slotItem == -1) {
                                    ItemStack itemHand = mc.thePlayer.getHeldItem();
                                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                                    //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &cThe item : Bonzo Staff is not found!");
                                    DelClient.sendChatToClient("&dDelClient &5--> &cError : &8Bonzo is not found on hot bar!");
                                }
                            }else{
                                ItemStack itemHand = mc.thePlayer.getHeldItem();
                                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                                //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &cThe item : Hand");
                                DelClient.sendChatToClient("&eWarning : &8Your click doesn't have item assigned.");
                            }
                            // CLICK HAND
                            waypoint.setDone(true);
                            //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aIndependent done! &cClicked");
                        }
                    }

                    // WAIT
                    if(waypoint.getWait()){
                        if (distance <= waitTol && height < 2 && height > -1) { //  && mc.thePlayer.onGround
                            waypoint.setDone(true);
                            isWaiting = true;
                            if(pm != null){
                                pm.stopMoving();
                            }
                            timeToWait = waypoint.getTime() * 1000;
                            DelClient.sendDebug("&aStarted to wait!");
                            waiter = Executors.newScheduledThreadPool(1);
                            waiter.schedule(() -> {
                                isWaiting = false;
                                DelClient.sendDebug("&aFinished to wait!");
                            }, (long) timeToWait, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            }

            // Is before waypoint finished ?
            if(pm == null){

                // Get Next Waypoint
                Waypoint nextWaypoint = getNextWaypoint();
                if(nextWaypoint == null){
                    if (pm != null) {
                        pm.stopMoving();
                    }
                    this.toggle();
                    this.toggle();
                    return;
                }

                // WALKING
                if(!nextWaypoint.getIndependent() && !nextWaypoint.getClick() && !nextWaypoint.getLookOnly()){
                    if(getWaypoints().indexOf(nextWaypoint) == 1){
                        DelClient.sendChatToClient("&7Started route : &8" + currentRoute.getName());
                        pm = new PlayerMove(nextWaypoint.getX(), nextWaypoint.getY(), nextWaypoint.getZ(), tolerance, nextWaypoint.getStopVelocity(), nextWaypoint.getUseJump(), true);
                    }else{
                        pm = new PlayerMove(nextWaypoint.getX(), nextWaypoint.getY(), nextWaypoint.getZ(), tolerance, nextWaypoint.getStopVelocity(), nextWaypoint.getUseJump(), rotInstant);
                        pm.setMaxYawChange((float) cameraTick);
                    }
                }

                // LOOKING
                if(!nextWaypoint.getIndependent() && nextWaypoint.getLookOnly()){
                    double deltaY = nextWaypoint.getY() - Ypos;
                    double deltaZ = nextWaypoint.getZ() - Zpos;
                    double deltaX = nextWaypoint.getX() - Xpos;
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    double height = Math.abs(deltaY);
                    if (distance < tolerance && height < 2 && height > -2) {
                        if(currentTick >= tickToWait){
                            nextWaypoint.setDone(true);
                            currentTick = 0;
                        }else{
                            mc.thePlayer.rotationPitch = (float) nextWaypoint.getPitch();
                            mc.thePlayer.rotationYaw = (float) nextWaypoint.getYaw();
                            currentTick++;
                        }
                    }
                }

                // CLICKING
                if(!nextWaypoint.getIndependent() && nextWaypoint.getClick()){
                    double deltaY = nextWaypoint.getY() - Ypos;
                    double deltaZ = nextWaypoint.getZ() - Zpos;
                    double deltaX = nextWaypoint.getX() - Xpos;
                    double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    double height = Math.abs(deltaY);
                    if (distance < tolerance && height < 2 && height > -2) {
                        if(currentTick >= 10){
                            ItemStack itemHand = mc.thePlayer.getHeldItem();
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemHand, new BlockPos(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ()), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
                            nextWaypoint.setDone(true);
                            currentTick = 0;
                        }else{
                            currentTick++;
                        }
                    }
                }

            }
        }
    }

    /**
     * Return the next waypoint
     */
    private Waypoint getNextWaypoint() {
        for (Waypoint waypoint : currentRoute.getWaypoints()) {
            if (!waypoint.getDone()) {
                return waypoint;
            }
        }
        return null;
    }

    /**
     * Return the current waypoint
     */
    private Waypoint getCurrentWaypoint() {
        for (Waypoint waypoint : currentRoute.getWaypoints()) {
            if (pm != null && waypoint.getX() == pm.getX() && waypoint.getY() == pm.getY() && waypoint.getZ() == pm.getZ()) {
                return waypoint;
            }
        }
        return null;
    }

    /**
     * Return all waypoints
     */
    public List<Waypoint> getWaypoints(){
        try{
            return currentRoute.getWaypoints();
        }catch (Exception ignored){
            return null;
        }
    }

    /**
     * Add a waypoint to the current editing route
     */
    public void addWaypoints(Route route, double x, double y, double z, boolean stopVelocity, boolean useJump, boolean lookOnly, boolean useRightClick, double yaw, double pitch,
                             boolean independent, RouteItem routeItem, boolean edgeJump, boolean bonzo, boolean wait, float time){
        currentEditRoute.getWaypoints().add(new Waypoint(x, y, z, stopVelocity, useJump, lookOnly, useRightClick, yaw, pitch, independent, routeItem, edgeJump, bonzo, wait, time));
    }

    /**
     * Create a new route
     */
    public Route createNewRoute(float red, float green, float blue, String name){
        Route route = new Route(red, green, blue, name);
        routes.add(route);
        currentEditRoute = route;
        DelClient.sendChatToClient("&7New route created : &a" + currentEditRoute.getName());
        //DelClient.sendChatToClient("&eDelClient >> &9User Route! --> &aNew route created! Name: " + currentEditRoute.getName());
        return route;
    }

    /**
     * Find a route with name
     */
    public Route findRoute(String name){
        for(Route route : routes){
            if(route.getName().equalsIgnoreCase(name)){
                return route;
            }
        }
        return null;
    }

    /**
     * Find an item (tnt / bonzo staff)
     */
    public int findItemIndex(boolean tnt, boolean bonzo){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if(tnt){
                if (itemStack != null && itemStack.getItem() == Item.getItemFromBlock(Blocks.tnt)) {
                    return i;
                }
            }
            if(bonzo){
                if (itemStack != null && itemStack.getItem() == Items.blaze_rod) {
                    return i;
                }
            }
        }
        return -1;
    }
}
