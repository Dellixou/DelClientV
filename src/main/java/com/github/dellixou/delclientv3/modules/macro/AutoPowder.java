package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.movements.SmoothPartialRotation;
import com.github.dellixou.delclientv3.utils.pathfinding.PlayerPathFinder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AutoPowder extends Module {

    /**
     * Constructor to initialize the AutoFish module.
     */
    public AutoPowder() {
        super("Auto Powder", Keyboard.KEY_0, Category.MACRO, false, "auto_powder");
    }

    // Fields for managing auto powder
    public Map<BlockPos, Boolean> detectedChests = new HashMap<>();

    private BlockPos targetChest = null;
    public PlayerPathFinder pathFinder = new PlayerPathFinder();
    public boolean inPathFinding = false;
    public static SmoothPartialRotation smoothPartialRotation;

    /**
     * Sets up initial settings for the AutoFish module.
     */
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Chest Range", this, 4, 1, 20, true, "auto_powder_chest_range", "1"));
        DelClient.settingsManager.rSetting(new Setting("Auto Chest", this, true, "auto_powder_chest", "1"));
        DelClient.settingsManager.rSetting(new Setting("Block Tolerance", this, 1, 0.7, 2, false, "auto_powder_tolerance", "1"));
        DelClient.settingsManager.rSetting(new Setting("Sneak Tolerance", this, 3, 1, 10, false, "auto_powder_sneak_tolerance", "1"));
        DelClient.settingsManager.rSetting(new Setting("Look Duration", this, 250, 10, 1000, true, "auto_powder_look_duration", "1"));
        DelClient.settingsManager.rSetting(new Setting("Auto Miner", this, true, "auto_powder_miner", "1"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look X Radius", this, 40, 1, 100, true, "auto_powder_miner_radius_x", "1"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look Y Radius", this, 18, 1, 100, true, "auto_powder_miner_radius_y", "1"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look Speed", this, 10, 1, 50, false, "auto_powder_miner_radius_speed", "1"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable(){
        float horizontale = (float) DelClient.settingsManager.getSettingById("auto_powder_miner_radius_x").getValDouble();
        float vertical = (float) DelClient.settingsManager.getSettingById("auto_powder_miner_radius_y").getValDouble();
        float speed = (float) DelClient.settingsManager.getSettingById("auto_powder_miner_radius_speed").getValDouble();
        if(DelClient.settingsManager.getSettingById("auto_powder_miner").getValBoolean()){
            //smoothPartialRotation = new SmoothPartialRotation(horizontale, vertical, speed);
            MinecraftForge.EVENT_BUS.register(smoothPartialRotation);
            smoothPartialRotation.activate();
        }
        MinecraftForge.EVENT_BUS.register(pathFinder);
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable(){
        if(smoothPartialRotation != null){
            smoothPartialRotation.deactivate();
        }
        targetChest = null;
        MinecraftForge.EVENT_BUS.unregister(pathFinder);
        pathFinder.resetPath();
        inPathFinding = false;
        super.onDisable();
    }

    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
        if (this.isToggled() && mc.thePlayer != null && mc.theWorld != null) {
            if(!DelClient.settingsManager.getSettingById("auto_powder_chest").getValBoolean()) return;
            scanChests();
            if (targetChest == null) {
                findNextChestToOpen();
            }else if (!inPathFinding){
                inPathFinding = true;

                pathFinder.createPath(mc.theWorld, mc.thePlayer.getPosition(), targetChest);
            }
        }
    }

    /**
     * Scan map for chest.
     */
    private void scanChests(){
        EntityPlayer player = mc.thePlayer;
        double range = DelClient.settingsManager.getSettingById("auto_powder_chest_range").getValDouble();

        BlockPos playerPos = player.getPosition();
        int radius = (int) Math.ceil(range);

        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                for(int z = -radius; z <= radius; z++){
                    BlockPos currentPos = playerPos.add(x, y, z);
                    BlockPos playerChestYPos = playerPos.add(x, 0, z);
                    BlockPos chosen;
                    if(y >= 1.5f){
                        chosen = playerChestYPos;
                    }else{
                        chosen = currentPos;
                    }
                    chosen = playerChestYPos;
                    if(playerPos.distanceSq(chosen) <= radius * radius){
                        Block block = mc.theWorld.getBlockState(currentPos).getBlock();

                        if(block instanceof BlockChest){
                            if(!pathFinder.realChestsPosition.containsValue(currentPos)){
                                DelClient.sendChatToClient("&aDetected a chest : Coords --> X: " + currentPos.getX() + ", Y: " + currentPos.getY() + ", Z: " + currentPos.getZ());
                                Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1, 1);
                                detectedChests.put(chosen, false);
                                pathFinder.realChestsPosition.put(chosen, currentPos);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Find the next chest.
     */
    private void findNextChestToOpen() {
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = player.getPosition();

        for (Map.Entry<BlockPos, Boolean> entry : detectedChests.entrySet()) {
            BlockPos chestPos = entry.getKey();
            boolean alreadyOpen = entry.getValue();
            double rangeChest = DelClient.settingsManager.getSettingById("auto_powder_chest_range").getValDouble();

            float x = chestPos.getX() + 0.5f;
            float y = chestPos.getY() + 0.5f;
            float z = chestPos.getZ() + 0.5f;

            if (!alreadyOpen && playerPos.distanceSq(chestPos) <= rangeChest * rangeChest) {
                targetChest = chestPos;
                return;
            }
        }
    }

    /**
     * Current chest done.
     */
    public void currentChestDone(){
        if (targetChest != null) {
            detectedChests.put(targetChest, true); // Mark the chest as processed
            targetChest = null;
            inPathFinding = false;
            DelClient.sendChatToClient("Change de coffre");
        }
    }

    /**
     * Find the pickaxe in hot bar.
     */
    public int findPickaxe(){
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if(itemStack != null && itemStack.getItem()==Items.fishing_rod){
                return i;
            }
        }
        return 0;
    }

}
