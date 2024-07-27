package com.github.dellixou.delclientv3.utils.movements;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class RotationUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static float pitchDifference;
    public static float yawDifference;
    private static int ticks = -1;
    private static int tickCounter = 0;
    private static Runnable callback = null;

    public static class Rotation {
        public float pitch;
        public float yaw;

        public Rotation(float pitch, float yaw) {
            this.pitch = pitch;
            this.yaw = yaw;
        }
    }

    public static double wrapAngleTo180(double angle) {
        return angle - Math.floor(angle / 360 + 0.5) * 360;
    }

    public static float wrapAngleTo180(float angle) {
        return (float) (angle - Math.floor(angle / 360 + 0.5) * 360);
    }

    public static Rotation vec3ToRotation(Vec3 vec) {
        Minecraft mc = Minecraft.getMinecraft();
        double diffX = vec.xCoord - mc.thePlayer.posX;
        double diffY = vec.yCoord - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        double diffZ = vec.zCoord - mc.thePlayer.posZ;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static Rotation getRotationToBlock(BlockPos block) {
        double diffX = block.getX() - Minecraft.getMinecraft().thePlayer.posX + 0.5;
        double diffY = block.getY() - Minecraft.getMinecraft().thePlayer.posY + 0.5
                - Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double diffZ = block.getZ() - Minecraft.getMinecraft().thePlayer.posZ + 0.5;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static Rotation getRotationToBlock(BlockPos block, float offset) {
        double diffX = block.getX() - Minecraft.getMinecraft().thePlayer.posX + 0.5;
        double diffY = block.getY() - Minecraft.getMinecraft().thePlayer.posY + 0.5
                - Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double diffZ = block.getZ() - Minecraft.getMinecraft().thePlayer.posZ + 0.5;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        Random rdm = new Random();
        return new Rotation((17.5f + rdm.nextFloat() * (22.0F - 17.5F)), yaw);
    }

    public static void smoothLook(Rotation rotation, int ticks, Runnable callback) {
        if (ticks == 0) {
            look(rotation);
            if (callback != null)
                callback.run();
            return;
        }

        RotationUtils.callback = callback;

        pitchDifference = wrapAngleTo180(rotation.pitch - Minecraft.getMinecraft().thePlayer.rotationPitch);
        yawDifference = wrapAngleTo180(rotation.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw);

        RotationUtils.ticks = ticks * 20;
        RotationUtils.tickCounter = 0;
    }

    public static void smartLook(Rotation rotation, int ticksPer180, Runnable callback) {
        float rotationDifference = Math.max(Math.abs(rotation.pitch - Minecraft.getMinecraft().thePlayer.rotationPitch),
                Math.abs(rotation.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw));
        smoothLook(rotation, (int) (rotationDifference / 180 * ticksPer180), callback);
    }

    public static void look(Rotation rotation) {
        Minecraft.getMinecraft().thePlayer.rotationPitch = rotation.pitch;
        Minecraft.getMinecraft().thePlayer.rotationYaw = rotation.yaw;
    }

    public static boolean playerLookingAt(BlockPos blockPos, float offset) {
        double deltaX = blockPos.getX() + 0.5 - mc.thePlayer.posX;
        double deltaY = blockPos.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double deltaZ = blockPos.getZ() + 0.5 - mc.thePlayer.posZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        float yawDiff = Math.abs(wrapAngleTo180(yaw - mc.thePlayer.rotationYaw));
        float pitchDiff = Math.abs(wrapAngleTo180(pitch - mc.thePlayer.rotationPitch));

        return yawDiff < offset && pitchDiff < offset; // Adjust tolerance for precision
    }

    public static boolean playerLookingAtYaw(BlockPos blockPos, float offset) {
        double deltaX = blockPos.getX() + 0.5 - mc.thePlayer.posX;
        double deltaY = blockPos.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double deltaZ = blockPos.getZ() + 0.5 - mc.thePlayer.posZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        float yawDiff = Math.abs(wrapAngleTo180(yaw - mc.thePlayer.rotationYaw));

        return yawDiff < offset; // Adjust tolerance for precision
    }

    public static Rotation getNeededChange(Rotation startRot, Rotation endRot) {
        float yawDiff = wrapAngleTo180(endRot.yaw) - wrapAngleTo180(startRot.yaw);

        if (yawDiff <= -180) {
            yawDiff += 360;
        } else if (yawDiff > 180) {
            yawDiff -= 360;
        }

        return new Rotation(endRot.pitch - startRot.pitch, yawDiff);
    }

    public static Rotation getNeededChange(Rotation endRot) {
        return getNeededChange(
                new Rotation(
                        mc.thePlayer.rotationPitch,
                        mc.thePlayer.rotationYaw
                ),
                endRot
        );
    }

    public static Rotation getRotation(final Vec3 from, final Vec3 to) {
        double diffX = to.xCoord - from.xCoord;
        double diffY = to.yCoord - from.yCoord;
        double diffZ = to.zCoord - from.zCoord;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static Rotation getRotation(Vec3 vec3) {
        return getRotation(
                new Vec3(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ
                ),
                vec3
        );
    }

    public static boolean playerLookingAt(Entity entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - ( mc.thePlayer.posY +  mc.thePlayer.getEyeHeight());
        double deltaZ = entity.posZ -  mc.thePlayer.posZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        float yawDiff = Math.abs(wrapAngleTo180(yaw -  mc.thePlayer.rotationYaw));
        float pitchDiff = Math.abs(wrapAngleTo180(pitch -  mc.thePlayer.rotationPitch));

        return yawDiff < 1 && pitchDiff < 1; // Adjust tolerance for precision
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (tickCounter < ticks) {
            if(mc.thePlayer != null){
                mc.thePlayer.rotationPitch += pitchDifference / ticks;
                mc.thePlayer.rotationYaw += yawDifference / ticks;
            }else{
                tickCounter = ticks;
            }
            tickCounter++;
        } else if (callback != null) {
            callback.run();
            callback = null;
        }
    }
}
