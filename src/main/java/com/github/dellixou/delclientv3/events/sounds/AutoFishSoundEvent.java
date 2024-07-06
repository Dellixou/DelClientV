package com.github.dellixou.delclientv3.events.sounds;

import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.macro.AutoFish;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoFishSoundEvent {

    String PLING_NOTE = "note.pling";
    String SPLASH_NOTE = "random.splash";
    AutoFish autoFish = (AutoFish) ModuleManager.getModuleById("auto_fish");
    public boolean isSouding = false;

    private static final long COOLDOWN = 1000; // 0.5 second cooldown in milliseconds
    private long lastGotFishTime = 0;

    @SubscribeEvent
    public void onPlaySoundEvent(PlaySoundEvent event) {
        if (!ModuleManager.getModuleById("auto_fish").isToggled()) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;

        if (PLING_NOTE.equals(event.name) || SPLASH_NOTE.equals(event.name)) {
            long currentTime = Minecraft.getSystemTime();
            if (currentTime - lastGotFishTime >= COOLDOWN && !isSouding) {
                isSouding = true;
                lastGotFishTime = currentTime; // Update last execution time
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(150);
                        autoFish.gotFish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        }
    }
}
