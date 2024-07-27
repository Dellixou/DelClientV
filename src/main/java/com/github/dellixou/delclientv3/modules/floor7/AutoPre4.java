package com.github.dellixou.delclientv3.modules.floor7;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.chats.ChatAutoPre4Event;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class AutoPre4 extends Module {

    /**
     * Constructor to initialize the AutoPre4 module.
     */
    public AutoPre4() {
        super("Auto Pre 4 (W.I.P)", Keyboard.KEY_0, Category.FLOOR7, true, "auto_pre4");
    }

    // Fields for managing AutoPre4
    private final ChatAutoPre4Event chatAutoPre4Event = new ChatAutoPre4Event();

    /**
     * Sets up initial settings for the AutoPre4 module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Smooth", this, false, "auto_pre4_smooth", "mode"));
        DelClient.settingsManager.rSetting(new Setting("Legit mode", this, false, "auto_pre4_legit", "mode"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        MinecraftForge.EVENT_BUS.register(chatAutoPre4Event);
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        MinecraftForge.EVENT_BUS.unregister(chatAutoPre4Event);
        super.onDisable();
    }

    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() { }

    /**
     * Find the fishing rod in hot bar.
     */
    public int findBow(){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if(itemStack != null && itemStack.getItem()==Items.bow){
                return i;
            }
        }
        return 0;
    }
}
