package com.github.dellixou.delclientv3.utils.movements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Random;

public class PlayerLookSmoothV2 {

    // Some values
    private static boolean isRotating = false;
    public boolean finished = false;
    private Random random = null;
    private int animIndex = 0;
    private Minecraft mc = Minecraft.getMinecraft();

    /*
     * Constructor.
     */
    public void lookAtBlock(float x, float y, float z, float duration) {
        if (isRotating) return;

        new Thread(() -> {
            try {
                Thread.sleep(50); // Little delay before starting animation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            finished = false;
            isRotating = true;
            random = new Random();
            animIndex = random.nextInt(4);
            BlockPos blockPos = new BlockPos(x, y, z);
            Vec3 targetVec = getTargetVector(blockPos);
            float[] rotations = calculateRotations(targetVec);
            smoothRotation(rotations[0], rotations[1], duration);
            isRotating = false;
        }).start();
    }

    public void lookAtRotation(float targetYaw, float targetPitch, float duration) {
        if (isRotating) return;

        new Thread(() -> {
            try {
                Thread.sleep(50); // Petit délai avant de commencer la rotation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            finished = false;
            isRotating = true;
            random = new Random();
            animIndex = random.nextInt(4);
            smoothRotation(targetYaw, targetPitch, duration);
            isRotating = false;
        }).start();
    }

    /*
     * Get vector for block.
     */
    private static Vec3 getTargetVector(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 blockVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        double minDistance = Double.MAX_VALUE;
        EnumFacing closestFace = null;
        Vec3 closestFaceVec = null;

        for (EnumFacing facing : EnumFacing.values()) {
            Vec3 faceVec = new Vec3(
                    pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.5,
                    pos.getY() + 0.5 + facing.getFrontOffsetY() * 0.5,
                    pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.5
            );
            double distance = eyesPos.distanceTo(faceVec);
            if (distance < minDistance) {
                minDistance = distance;
                closestFace = facing;
                closestFaceVec = faceVec;
            }
        }

        // Use the closest face vector as the target vector
        return closestFaceVec != null ? closestFaceVec : blockVec;
    }

    /*
     * Get yaw and pitch.
     */
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

    /*
     * Do smooth rotation using system millis.
     */
    private void smoothRotation(float targetYaw, float targetPitch, float duration) {
        long startTime = System.currentTimeMillis();
        float startYaw = mc.thePlayer.rotationYaw;
        float startPitch = mc.thePlayer.rotationPitch;

        // Calculer la différence d'angle la plus courte
        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - startYaw);
        if (Math.abs(yawDiff) > 180) {
            yawDiff = yawDiff > 0 ? yawDiff - 360 : yawDiff + 360;
        }
        float pitchDiff = targetPitch - startPitch;

        while (System.currentTimeMillis() - startTime < duration) {
            float progress = (System.currentTimeMillis() - startTime) / duration;
            float smoothProgress = getSmoothedProgress(progress);

            float newYaw = startYaw + yawDiff * smoothProgress;
            float newPitch = startPitch + pitchDiff * smoothProgress;

            // Normaliser newYaw pour qu'il reste dans la plage 0-360
            newYaw = MathHelper.wrapAngleTo180_float(newYaw);

            float finalNewYaw = newYaw;
            mc.addScheduledTask(() -> {
                mc.thePlayer.rotationYaw = finalNewYaw;
                mc.thePlayer.rotationPitch = newPitch;
            });
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        finished = true;

        mc.addScheduledTask(() -> {
            mc.thePlayer.rotationYaw = MathHelper.wrapAngleTo180_float(targetYaw);
            mc.thePlayer.rotationPitch = targetPitch;
        });
    }

    /**
     * Take random animation.
     */
    private float getSmoothedProgress(float progress) {
        switch (animIndex) {
            case 0: return easeInOutQuad(progress);
            case 1: return easeOutBack(progress);
            case 2: return easeInOutQuint(progress);
            case 3: return easeInOutQuart(progress);
            default: return progress;
        }
    }



    // -------------------------------------------------- ANIMATIONS --------------------------------------------------


    // 0
    private static float easeInOutQuad(float t) {
        return (float) Math.sqrt(1 - Math.pow(t - 1, 2));
    }

    // 1
    private static float easeOutBack(float x){
        float c1 = (float) 1.70158;
        float c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }

    // 2
    private static float easeInOutQuint(float x){
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    // 3
    private static float easeInOutQuart(float x){
        return (float) (1 - Math.pow(1 - x, 4));
    }
}
