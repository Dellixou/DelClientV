package com.github.dellixou.delclientv3.utils.movements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class PlayerLook {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isLookDone = false;

    private float getYawToTarget(EntityPlayerSP player, double targetX, double targetZ) {
        double deltaX = targetX - player.posX;
        double deltaZ = targetZ - player.posZ;
        return (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
    }

    private float getPitchToTarget(EntityPlayerSP player, double targetX, double targetZ, double targetY) {
        double deltaX = targetX - player.posX;
        double deltaY = targetY - (player.posY + player.getEyeHeight());
        double deltaZ = targetZ - player.posZ;
        double distanceY = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        return (float) -(Math.atan2(deltaY, distanceY) * (180 / Math.PI));
    }

    public void setPlayerLook(EntityPlayerSP player, double targetX, double targetY, double targetZ) {
        player.rotationYaw = getYawToTarget(player, targetX, targetZ);
        player.rotationPitch = getPitchToTarget(player, targetX, targetZ, targetY);
    }


    // Set is Look Done
    public void setIsLookDone(boolean f) {
        isLookDone = f;
    }

    // Get is Look Done
    public boolean getIsLookDone() {
        return isLookDone;
    }
}