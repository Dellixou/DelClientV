package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.misc.Waypoint;
import com.github.dellixou.delclientv3.utils.movements.PlayerMove;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AutoP3 extends Module {

    List<Waypoint> waypoints = new ArrayList<>();
    private PlayerMove pm;

    public AutoP3() {
        super("Auto P3", Keyboard.KEY_9, Category.MOVEMENT, true, "auto_p3");
    }

    @Override
    public void setup(){ }

    @Override
    public void onEnable(){
        pm = null; // Reset the PlayerMove instance on enable
    }

    @Override
    public void onDisable(){
        if (pm != null) {
            pm.stopMoving();
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        for (Waypoint waypoint : waypoints) {
            waypoint.setDone(false);
        }
    }

}