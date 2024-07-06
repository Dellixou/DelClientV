package com.github.dellixou.delclientv3.events.keys;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyInputEvent {

    private boolean canPress = true; // Flag to track if key press is allowed
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Scheduler to reset flag

    /**
     * Function loaded every input keyboard
     **/

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        int code = Keyboard.getEventKey();

        if(Keyboard.isKeyDown(code)){
            if (canPress) {
                canPress = false;
                ModuleManager.onKey(code);

                // Schedule resetting the canPress flag after 1000 ms
                scheduler.schedule(() -> canPress = true, 100, TimeUnit.MILLISECONDS);
            }
        }
    }


}

