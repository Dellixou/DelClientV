package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ResetVelocity extends Module {

    public ResetVelocity(){
        super("Reset V", Keyboard.KEY_L, Category.MOVEMENT, true, "reset_v");
    }

    @Override
    public void onEnable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        Minecraft.getMinecraft().thePlayer.setVelocity(0, 0, 0);
        this.toggle();
    }

}