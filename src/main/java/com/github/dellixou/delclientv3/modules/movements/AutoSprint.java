package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class AutoSprint extends Module {

    /**
     * Sprint Auto big noob ;)
     **/
    public AutoSprint() {
        super("Auto Sprint", Keyboard.KEY_K, Category.MOVEMENT, true, "auto_sprint");
    }

    int SPRINT_KEY = mc.gameSettings.keyBindSprint.getKeyCode();

    @Override
    public void onEnable(){
        super.onEnable();
    }

    public void onDisable(){
        super.onDisable();
    }

    public void onUpdate(){
        if(this.isToggled()){
            KeyBinding.setKeyBindState(SPRINT_KEY, true);
        }
    }

}
