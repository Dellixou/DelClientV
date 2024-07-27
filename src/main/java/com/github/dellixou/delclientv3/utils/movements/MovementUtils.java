package com.github.dellixou.delclientv3.utils.movements;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;

public class MovementUtils {

  private static Minecraft mc = Minecraft.getMinecraft();

  private MovementUtils() {
    /*
     * Private constructor to hide the implicit public one
     */
  }

  public static void moveForward(boolean val) {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), val);
  }

  public static void moveBack(boolean val) {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), val);
  }

  public static void moveLeft(boolean val) {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), val);
  }

  public static void moveRight(boolean val) {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), val);
  }

  public static void playerAttack(boolean val) {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), val);
  }

  public static void sneak() {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
  }

  public static void stopSneaking() {
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
  }

  public static void jump() {
    new Thread(() -> {
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
    }).start();
  }

  public static BlockPos getPlayerPos() {
    return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
  }

  public static boolean isCollidedHorizontally() {
    return mc.thePlayer.isCollidedHorizontally;
  }

  public static void stopMovements() {
    moveBack(false);
    moveForward(false);
    moveRight(false);
    moveLeft(false);
    stopSneaking();
  }

}