package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class AutoWalk extends Module {

    public AutoWalk(){
        super("Auto Walk", Keyboard.KEY_NUMLOCK, Category.MOVEMENT, true, "auto_walk");
    }

    @Override
    public void onUpdate() {
        if (this.isToggled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        }
    }

    @Override
    public void onDisable(){
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    }

}
