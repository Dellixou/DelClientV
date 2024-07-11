package com.github.dellixou.delclientv3.utils.movements;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class PlayerLookSmooth {

    private static boolean isRotating = false;
    public boolean finished = false;

    public void lookAtBlock(float x, float y, float z, float duration) {

        if (isRotating) return; // Évite les rotations simultanées

        new Thread(() -> {
            finished = false;
            isRotating = true;
            BlockPos blockPos = new BlockPos(x, y, z);
            Vec3 targetVec = getTargetVector(blockPos);
            float[] rotations = calculateRotations(targetVec);
            smoothRotation(rotations[0], rotations[1], duration);
            isRotating = false;
        }).start();
    }

    private static Vec3 getTargetVector(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 blockVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        double minDistance = Double.MAX_VALUE;
        EnumFacing closestFace = null;

        for (EnumFacing facing : EnumFacing.values()) {
            Vec3 faceVec = new Vec3(
                    blockVec.xCoord + facing.getFrontOffsetX() * 0.5,
                    blockVec.yCoord + facing.getFrontOffsetY() * 0.5,
                    blockVec.zCoord + facing.getFrontOffsetZ() * 0.5
            );
            double distance = eyesPos.distanceTo(faceVec);
            if (distance < minDistance) {
                minDistance = distance;
                closestFace = facing;
            }
        }

        return new Vec3(
                blockVec.xCoord + closestFace.getFrontOffsetX() * 0.5,
                blockVec.yCoord + closestFace.getFrontOffsetY() * 0.5,
                blockVec.zCoord + closestFace.getFrontOffsetZ() * 0.5
        );
    }

    private static float[] calculateRotations(Vec3 target) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0f);
        double diffX = target.xCoord - eyesPos.xCoord;
        double diffY = target.yCoord - eyesPos.yCoord;
        double diffZ = target.zCoord - eyesPos.zCoord;

        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distance));

        return new float[]{yaw, pitch};
    }

    private void smoothRotation(float targetYaw, float targetPitch, float duration) {
        Minecraft mc = Minecraft.getMinecraft();
        long startTime = System.currentTimeMillis();
        float startYaw = mc.thePlayer.rotationYaw;
        float startPitch = mc.thePlayer.rotationPitch;

        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - startYaw);
        float pitchDiff = targetPitch - startPitch;

        // Normalize the yaw difference to be within -180 to 180 degrees
        while (yawDiff < -180.0f) yawDiff += 360.0f;
        while (yawDiff >= 180.0f) yawDiff -= 360.0f;

        while (System.currentTimeMillis() - startTime < duration) {
            float progress = (System.currentTimeMillis() - startTime) / duration;
            float smoothProgress = easeInOutQuad(progress);

            float newYaw = startYaw + yawDiff * smoothProgress;
            float newPitch = startPitch + pitchDiff * smoothProgress;

            mc.addScheduledTask(() -> {
                mc.thePlayer.rotationYaw = newYaw;
                mc.thePlayer.rotationPitch = newPitch;
            });
            try {
                Thread.sleep(1); // Mise à jour toutes les 10 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        finished = true;

        // Assure que la rotation finale est exactement la cible
        mc.addScheduledTask(() -> {
            mc.thePlayer.rotationYaw = targetYaw;
            mc.thePlayer.rotationPitch = targetPitch;
        });
    }

    private static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
}
