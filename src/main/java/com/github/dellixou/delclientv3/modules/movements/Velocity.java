package com.github.dellixou.delclientv3.modules.movements;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", Keyboard.KEY_P, Category.MOVEMENT, false, "velocity");
    }

    @Override
    public void setup() {
        DelClient.settingsManager.rSetting(new Setting("Power", this, 10, 1, 30, true, "velocity_power", "global"));
    }

    @Override
    public void onEnable(){
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.thePlayer;

        // Get player's pitch (up and down angle) and yaw (left and right angle)
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;

        // Calculate the direction the player is facing
        double xForward = -MathHelper.sin(yaw * 0.017453292F);
        //double yForward = -MathHelper.sin(pitch * 0.017453292F);
        double zForward = MathHelper.cos(yaw * 0.017453292F);

        // Set velocity in the direction the player is facing
        player.setVelocity(xForward*DelClient.settingsManager.getSettingById("velocity_power").getValDouble()*0.1, 0, zForward*DelClient.settingsManager.getSettingById("velocity_power").getValDouble()*0.1);
        this.toggle();
    }



}
