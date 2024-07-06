package com.github.dellixou.delclientv3.utils.movements;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class PlayerMove {

    // Fields for managing player move
    private boolean isWalking = false;
    private boolean isArrived = false;
    private double x;
    private double y;
    private double z;
    private double tolerance;
    private boolean canJump;
    private boolean stopVelocity;
    private boolean rotInstant;
    private float maxYawChange = 0.3f;

    Minecraft mc = Minecraft.getMinecraft();
    UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");

    /**
     * Constructor to initialize the PlayerMove class.
     */
    public PlayerMove(double x, double y, double z, double tolerance, boolean stopVelocity, boolean jump, boolean rotInstant){
        this.x = x;
        this.y = y;
        this.z = z;
        this.canJump = jump;
        this.tolerance = tolerance;
        this.stopVelocity = stopVelocity;
        this.rotInstant = rotInstant;
    }

    /**
     * Calculate the direction and wrap it between 180 and -180.
     */
    public static float calculateYawToPoint(Entity player, double targetX, double targetY, double targetZ) {
        double deltaX = targetX - player.posX;
        double deltaZ = targetZ - player.posZ;
        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
        return MathHelper.wrapAngleTo180_float(yaw);
    }

    /**
     * Set player yaw in game.
     */
    public void setPlayerYaw(float targetYaw) {
        targetYaw = normalizeYaw(targetYaw);
        float currentYaw = mc.thePlayer.rotationYaw;
        float yawDifference = MathHelper.wrapAngleTo180_float(targetYaw - currentYaw);

        mc.thePlayer.rotationYaw = smoothAngle(currentYaw, targetYaw, maxYawChange);
    }

    /**
     * Normalize yaw.
     */
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
     * Set max tick yaw change.
     */
    public void setMaxYawChange(float maxYawChange){
        this.maxYawChange = maxYawChange;
    }

    /**
     * Make smooth transition prevent 360 I got you snqwwy.
     */
    private static float smoothAngle(float current, float target, float maxChange) {
        float difference = MathHelper.wrapAngleTo180_float(target - current);
        if (difference > maxChange) {
            difference = maxChange;
        }
        if (difference < -maxChange) {
            difference = -maxChange;
        }
        return current + difference;
    }

    /**
     * Move the player.
     */
    public void movePlayer() {
        if (!isWalking) {
            isWalking = true;
            isArrived = false;
        }

        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        double deltaX = this.x - player.posX;
        double deltaZ = this.z - player.posZ;
        double deltaY = this.y - player.posY;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // 2D distance
        double height = Math.abs(deltaY);

        if (Math.abs(distance) <= this.tolerance && height < 2 && height > -1) {
            isArrived = true;
            stopMoving();
            return;
        }

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

        if(!(Math.abs(distance) < this.tolerance * 1.5F)){
            if(userRoute.isLooking){
                //return;
            }
            if(rotInstant){
                float targetYaw = calculateYawToPoint(player, this.x, this.y, this.z);
                mc.thePlayer.rotationYaw = targetYaw;

            }else{
                float targetYaw = calculateYawToPoint(player, this.x, this.y, this.z);
                setPlayerYaw(targetYaw);
            }
        }
    }

    /**
     * Stop the player by setting velocity to 0 if activated.
     */
    public void stopMoving() {
        isWalking = false;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        if (stopVelocity) {
            mc.thePlayer.setVelocity(0, 0, 0);
        }
    }

    /**
     * Reset the player move.
     */
    public void reset() {
        stopMoving();
        isArrived = false;
    }

    /**
     * Get is finished.
     */
    public boolean isFinished() {
        return isArrived;
    }

    /**
     * Get X.
     */
    public double getX() {
        return x;
    }

    /**
     * Get Y.
     */
    public double getY() {
        return y;
    }

    /**
     * Get Z.
     */
    public double getZ() {
        return z;
    }

}