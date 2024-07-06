package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.sounds.AutoFishSoundEvent;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;

public class AutoFish extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public AutoFish() {
        super("Auto Fish", Keyboard.KEY_0, Category.MACRO, true, "auto_fish");
    }

    // Fields for managing auto fish
    public boolean isFishing = false;
    private static final long COOLDOWN_AFK = 15000; // 0.5 second cooldown in milliseconds
    private long lastAFK = 0;
    private int tickAFK = 0;
    private boolean sideLeft = true;
    private boolean hasAntiAFK = false;
    private int rodDelay;
    private int catchDelay;
    private AutoFishSoundEvent onSoundEvent;
    private Thread thread;


    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Anti AFK", this, false, "auto_fish_anti_afk"));
        DelClient.settingsManager.rSetting(new Setting("Delay", this, 1000, 100, 1000, true, "auto_fish_rod_throw"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        hasAntiAFK = DelClient.settingsManager.getSettingById("auto_fish_anti_afk").getValBoolean();
        rodDelay = (int) DelClient.settingsManager.getSettingById("auto_fish_rod_throw").getValDouble();
        onSoundEvent = new AutoFishSoundEvent();
        MinecraftForge.EVENT_BUS.register(onSoundEvent);
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        isFishing = false;
        try{
            MinecraftForge.EVENT_BUS.unregister(onSoundEvent);
        }catch (Exception ignored) { }
        try{
            thread.stop();
        }catch (Exception ignored) { }
        super.onDisable();
    }


    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
        if (this.isToggled() && mc.thePlayer != null) {
            if (hasAntiAFK) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAFK >= COOLDOWN_AFK) {
                    if(sideLeft){
                        mc.thePlayer.rotationPitch += 1;
                        mc.thePlayer.rotationYaw += 1;
                        sideLeft = false;
                    }else{
                        mc.thePlayer.rotationPitch -= 1;
                        mc.thePlayer.rotationYaw -= 1;
                        sideLeft = true;
                    }
                    lastAFK = currentTime;
                }
            }

            try {
                // Vérifier si la canne à pêche est dans la main
                if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) == null) {
                    mc.thePlayer.inventory.currentItem = findFishingRod();
                }
                if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).getItem() != Items.fishing_rod) {
                    mc.thePlayer.inventory.currentItem = findFishingRod();
                }

            } catch (Exception ignored) { }
        }
    }


    public void gotFish() {
        //try{ thread.stop(); }catch (Exception ignored) { }
        thread = new Thread(() -> {
            try {
                Thread.sleep(200);
                //KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                forceRightClick();
                Thread.sleep(rodDelay);
                //KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                forceRightClick();
                onSoundEvent.isSouding = false;
            } catch (InterruptedException ignored) { }
        });
        thread.start();
    }

    /**
     * Use this function to force a right click ( can be used in gui )
     */
    public static void forceRightClick() {
        try {
            Method rightClickMethod = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147121_ag");
            rightClickMethod.setAccessible(true);
            rightClickMethod.invoke(Minecraft.getMinecraft());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the fishing rod in hot bar.
     */
    public int findFishingRod(){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if(itemStack != null && itemStack.getItem()==Items.fishing_rod){
                return i;
            }
        }
        return 0;
    }

}
