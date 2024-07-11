package com.github.dellixou.delclientv3.utils.movements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SmoothPartialRotation {
    private float centerYaw, centerPitch;
    private float currentYaw, currentPitch;
    private float horizontalRadius, verticalRadius;
    private float angleSpeed;
    private float minPitch, maxPitch;
    public boolean isActive;
    private float angle;

    /**
     * Constructeur pour initialiser les paramètres de rotation.
     */
    public SmoothPartialRotation(float horizontalRadius, float verticalRadius, float angleSpeed, float minPitch, float maxPitch) {
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.angleSpeed = angleSpeed;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.isActive = false;
        this.angle = 0;
    }

    /**
     * Active la rotation circulaire de la caméra.
     */
    public void activate() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            this.centerYaw = player.rotationYaw;
            this.centerPitch = player.rotationPitch;
            this.currentYaw = this.centerYaw;
            this.currentPitch = this.centerPitch;
            this.isActive = true;
        }
    }

    /**
     * Désactive la rotation circulaire de la caméra.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Méthode appelée à chaque tick du joueur.
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        if (isActive) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                // Met à jour les angles de la caméra
                angle += angleSpeed;
                if (angle >= 360) {
                    angle -= 360;
                }

                // Met à jour les centres de la caméra pour s'adapter à la rotation actuelle
                float centerYaw = player.rotationYaw;
                float centerPitch = player.rotationPitch;

                // Calculer les nouvelles positions de la caméra
                float radianAngle = (float) Math.toRadians(angle);
                currentYaw = centerYaw + (float) Math.cos(radianAngle) * horizontalRadius;
                currentPitch = centerPitch + (float) Math.sin(radianAngle) * verticalRadius;

                // Appliquer les nouvelles rotations
                currentPitch = constrainPitch(currentPitch);
                player.rotationYaw = interpolateAngle(player.rotationYaw, currentYaw);
                player.rotationPitch = interpolateAngle(player.rotationPitch, currentPitch);

                // Restaurer les angles initiaux périodiquement
                restoreInitialAngles();
            }
        }
    }

    /**
     * Constrain the pitch angle within the specified limits.
     */
    private float constrainPitch(float pitch) {
        if (pitch < minPitch) {
            return minPitch;
        } else if (pitch > maxPitch) {
            return maxPitch;
        }
        return pitch;
    }

    /**
     * Interpole entre deux angles pour assurer une transition en douceur.
     */
    private float interpolateAngle(float from, float to) {
        float difference = normalizeAngle(to - from);
        return from + difference * 0.1f;  // Facteur de lissage
    }

    /**
     * Normalise un angle pour qu'il soit compris entre -180 et 180 degrés.
     */
    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    /**
     * Restaurer les angles de rotation initiaux périodiquement.
     */
    private void restoreInitialAngles() {
        if (Math.abs(currentYaw - centerYaw) > horizontalRadius || Math.abs(currentPitch - centerPitch) > verticalRadius) {
            currentYaw = centerYaw;
            currentPitch = centerPitch;
        }
    }
}
