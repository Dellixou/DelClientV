package com.github.dellixou.delclientv3.utils;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.Random;

public class MathUtils {

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static float getRandomNumberFloat(float min, float max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static int getRandomNumberPlusOne(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static Vec3 blockPosToVec3(BlockPos pos){
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static float tickToSeconds(int tick){
        return tick/20f;
    }

    public static double calculateDistanceXZ(Vec3 one, Vec3 two) {
        return Math.sqrt(Math.pow(one.xCoord - two.xCoord, 2) + Math.pow(one.zCoord - two.zCoord, 2));
    }

    public static double calculateDistanceXYZ(Vec3 one, Vec3 two) {
        return Math.sqrt(Math.pow(one.xCoord - two.xCoord, 2) + Math.pow(one.zCoord - two.zCoord, 2) + Math.pow(one.yCoord - two.yCoord, 2));
    }

}
