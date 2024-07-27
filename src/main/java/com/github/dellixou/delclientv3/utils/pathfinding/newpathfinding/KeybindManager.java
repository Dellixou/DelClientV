package com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding;

import com.github.dellixou.delclientv3.utils.InventoryUtils;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Credit to JellyLabs
 */

public class KeybindManager {

    static Minecraft mc = Minecraft.getMinecraft();

    public static KeyBinding keybindA = mc.gameSettings.keyBindLeft;
    public static KeyBinding keybindD = mc.gameSettings.keyBindRight;
    public static KeyBinding keybindW = mc.gameSettings.keyBindForward;
    public static KeyBinding keybindS = mc.gameSettings.keyBindBack;
    public static KeyBinding keybindAttack = mc.gameSettings.keyBindAttack;
    public static KeyBinding keybindUseItem = mc.gameSettings.keyBindUseItem;
    public static KeyBinding keyBindShift = mc.gameSettings.keyBindSneak;
    public static KeyBinding keyBindJump = mc.gameSettings.keyBindJump;

    private static Field mcLeftClickCounter;

    static {
        mcLeftClickCounter =
                ReflectionHelper.findField(
                        Minecraft.class,
                        "field_71429_W",
                        "leftClickCounter"
                );
        if (mcLeftClickCounter != null) mcLeftClickCounter.setAccessible(true);
    }

    private KeyBinding attackKey = mc.gameSettings.keyBindAttack;

    public static void holdLeftClick() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindAttack.getKeyCode(),
                true
        );
    }

    public static void releaseLeftClick() {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindAttack.getKeyCode(),
                false
        );
    }

    public static List<KeyBinding> getListKeybinds() {
        List<KeyBinding> keys = new ArrayList<>();

        keys.add(keybindA);
        keys.add(keybindW);
        keys.add(keybindD);
        keys.add(keybindS);

        keys.add(keyBindShift);
        keys.add(keyBindJump);

        return keys;
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.PlayerTickEvent event) {
        if (mcLeftClickCounter != null) {
            if (mc.inGameHasFocus) {
                try {
                    mcLeftClickCounter.set(mc, 0);
                } catch (IllegalAccessException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setKeyBindState(KeyBinding key, boolean pressed) {
        if (pressed) {
            if (mc.currentScreen != null) {
                realSetKeyBindState(key, false);
                return;
            }
        }
        realSetKeyBindState(key, pressed);
    }

    public static void onTick(KeyBinding key) {
        if (mc.currentScreen == null) {
            KeyBinding.onTick(key.getKeyCode());
        }
    }

    public static void updateKeys(
            boolean wBool,
            boolean sBool,
            boolean aBool,
            boolean dBool,
            boolean atkBool,
            boolean useBool,
            boolean shiftBool
    ) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        realSetKeyBindState(keybindW, wBool);
        realSetKeyBindState(keybindS, sBool);
        realSetKeyBindState(keybindA, aBool);
        realSetKeyBindState(keybindD, dBool);
        realSetKeyBindState(keybindAttack, atkBool);
        realSetKeyBindState(keybindUseItem, useBool);
        realSetKeyBindState(keyBindShift, shiftBool);
    }

    public static void updateKeys(
            boolean w,
            boolean s,
            boolean a,
            boolean d,
            boolean atk,
            boolean useItem,
            boolean shift,
            boolean jump
    ) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        realSetKeyBindState(keybindW, w);
        realSetKeyBindState(keybindS, s);
        realSetKeyBindState(keybindA, a);
        realSetKeyBindState(keybindD, d);
        realSetKeyBindState(keybindAttack, atk);
        realSetKeyBindState(keybindUseItem, useItem);
        realSetKeyBindState(keyBindShift, shift);
        realSetKeyBindState(keyBindJump, jump);
    }

    public static void updateKeys(
            boolean wBool,
            boolean sBool,
            boolean aBool,
            boolean dBool,
            boolean atkBool
    ) {
        if (mc.currentScreen != null) {
            resetKeybindState();
            return;
        }
        realSetKeyBindState(keybindW, wBool);
        realSetKeyBindState(keybindS, sBool);
        realSetKeyBindState(keybindA, aBool);
        realSetKeyBindState(keybindD, dBool);
        realSetKeyBindState(keybindAttack, atkBool);
    }

    public static void resetKeybindState() {
        realSetKeyBindState(keybindA, false);
        realSetKeyBindState(keybindS, false);
        realSetKeyBindState(keybindW, false);
        realSetKeyBindState(keybindD, false);
        realSetKeyBindState(keyBindShift, false);
        realSetKeyBindState(keyBindJump, false);
        realSetKeyBindState(keybindAttack, false);
        realSetKeyBindState(keybindUseItem, false);
    }

    public static void realSetKeyBindState(KeyBinding key, boolean pressed) {
        if (pressed) {
            if (!key.isKeyDown()) {
                KeyBinding.onTick(key.getKeyCode());
            }
            KeyBinding.setKeyBindState(key.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(key.getKeyCode(), false);
        }
    }

    private static final HashMap<Integer, KeyBinding> keyBindMap =
            new HashMap<Integer, KeyBinding>() {
                {
                    put(0, mc.gameSettings.keyBindForward);
                    put(90, mc.gameSettings.keyBindLeft);
                    put(180, mc.gameSettings.keyBindBack);
                    put(-90, mc.gameSettings.keyBindRight);
                }
            };

    public static HashSet<KeyBinding> getNeededKeyPresses(
            final Vec3 from,
            final Vec3 to
    ) {
        final HashSet<KeyBinding> e = new HashSet<>();
        final RotationUtils.Rotation neededRot = RotationUtils.getNeededChange(
                RotationUtils.getRotation(from, to)
        );
        final double neededYaw = neededRot.yaw * -1.0f;
        keyBindMap.forEach((k, v) -> {
            if (
                    Math.abs(k - neededYaw) < 67.5 ||
                            Math.abs(k - (neededYaw + 360.0)) < 67.5
            ) {
                e.add(v);
            }
        });
        return e;
    }

}

