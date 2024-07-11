package com.github.dellixou.delclientv3.modules.macro;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.movements.PlayerLookSmooth;
import com.github.dellixou.delclientv3.utils.movements.PlayerLookSmoothV2;
import com.github.dellixou.delclientv3.utils.movements.SmoothPartialRotation;
import com.github.dellixou.delclientv3.utils.pathfinding.PlayerPathFinder;
import com.github.dellixou.delclientv3.utils.renderer.BlockOutlineRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for the AutoPowderV2 module.
 */
public class AutoPowderV2 extends Module {

    /**
     * Constructor to initialize the AutoPowderV2 module.
     */
    public AutoPowderV2() {
        super("Auto PowderV2", Keyboard.KEY_0, Category.MACRO, false, "auto_powderv2");
    }

    // Fields for managing auto powder
    public Map<BlockPos, Boolean> detectedChests = new HashMap<>();

    public BlockPos targetChest = null;
    private static SmoothPartialRotation smoothPartialRotation;
    private PlayerLookSmoothV2 playerLookSmooth;
    BlockOutlineRenderer renderer;

    public boolean isLooking = false;
    public boolean isReturning = false;
    public boolean isMining = false;
    public boolean isClicking = false;

    public float originalYaw = 0;
    public float originalPitch = 0;

    private int aa = 0;

    /**
     * Sets up initial settings for the AutoPowderV2 module.
     */
    public void setup() {
        DelClient.settingsManager.rSetting(new Setting("Chest Range", this, 3, 1, 4, true, "auto_powderv2_chest_range"));
        DelClient.settingsManager.rSetting(new Setting("Auto Chest", this, true, "auto_powderv2_chest"));
        DelClient.settingsManager.rSetting(new Setting("Look Duration", this, 250, 10, 1000, true, "auto_powderv2_look_duration"));
        DelClient.settingsManager.rSetting(new Setting("Click Delay", this, 250, 10, 1000, true, "auto_powderv2_click_delay"));
        DelClient.settingsManager.rSetting(new Setting("Auto Miner", this, true, "auto_powderv2_miner"));
        DelClient.settingsManager.rSetting(new Setting("Debug", this, true, "auto_powderv2_debug"));
        DelClient.settingsManager.rSetting(new Setting("Pitch Start", this, 0, -90, 90, true, "auto_powderv2_miner_start_pitch"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look X Radius", this, 40, 1, 179.9, false, "auto_powderv2_miner_radius_x"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look Y Radius", this, 18, 1, 90, false, "auto_powderv2_miner_radius_y"));
        DelClient.settingsManager.rSetting(new Setting("Miner Look Speed", this, 10, 1, 500, false, "auto_powderv2_miner_radius_speed"));
        DelClient.settingsManager.rSetting(new Setting("Miner Min Pitch", this, -10, -90, 90, true, "auto_powderv2_miner_min_pitch"));
        DelClient.settingsManager.rSetting(new Setting("Miner Max Pitch", this, 10, -90, 90, true, "auto_powderv2_miner_max_pitch"));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable() {
        renderer = new BlockOutlineRenderer();
        MinecraftForge.EVENT_BUS.register(renderer);
        originalPitch = mc.thePlayer.rotationPitch;
        originalYaw = mc.thePlayer.rotationYaw;
        playerLookSmooth = new PlayerLookSmoothV2();
        float horizontale = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_radius_x").getValDouble();
        float vertical = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_radius_y").getValDouble();
        float speed = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_radius_speed").getValDouble();
        float minPitch = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_min_pitch").getValDouble();
        float maxPitch = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_max_pitch").getValDouble();
        if (DelClient.settingsManager.getSettingById("auto_powderv2_miner").getValBoolean()) {
            smoothPartialRotation = new SmoothPartialRotation(horizontale, vertical, speed, minPitch, maxPitch);
            MinecraftForge.EVENT_BUS.register(smoothPartialRotation);
        }
        startPlayerMine();
        super.onEnable();
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable() {
        if(smoothPartialRotation != null){
            MinecraftForge.EVENT_BUS.unregister(smoothPartialRotation);
        }
        stopPlayerMine();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        targetChest = null;
        isMining = false;
        isReturning = false;
        isLooking = false;
        isClicking = false;
        KeyBinding.unPressAllKeys();
        super.onDisable();
    }

    /**
     * Called periodically to update the module's state.
     */
    @Override
    public void onUpdate() {
        if (this.isToggled() && mc.thePlayer != null && mc.theWorld != null) {
            if (!DelClient.settingsManager.getSettingById("auto_powderv2_chest").getValBoolean()) return;
            scanChests();
            if(!isLooking && !isReturning && !isClicking){
                findNextChestToOpen();
                isMining = true;
                if(isMining && DelClient.settingsManager.getSettingById("auto_powderv2_miner").getValBoolean() && mc.currentScreen == null && !isLooking && !isReturning && !isClicking){
                    if(areBlocksInFront()){

                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                    }else{
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                    }
                }else{
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                }
            }else{
                stopPlayerMine();
            }
        }
    }

    /**
     * Scan map for chest.
     */
    private void scanChests() {
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = player.getPosition();
        int radius = (int) DelClient.settingsManager.getSettingById("auto_powderv2_chest_range").getValDouble();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = playerPos.add(x, y, z);

                    if (playerPos.distanceSq(currentPos) <= radius * radius) {
                        Block block = mc.theWorld.getBlockState(currentPos).getBlock();

                        if (block instanceof BlockChest) {
                            if (!detectedChests.containsKey(currentPos) && isChestAccessible(currentPos)) {
                                DelClient.sendChatToClient("&aDetected an accessible chest : Coords --> X: " + currentPos.getX() + ", Y: " + currentPos.getY() + ", Z: " + currentPos.getZ());
                                Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1, 1);
                                detectedChests.put(currentPos, false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Is Chest visible or in wall?
     */
    private boolean isChestAccessible(BlockPos chestPos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offsetPos = chestPos.offset(facing);
            if (mc.theWorld.getBlockState(offsetPos).getBlock().isTranslucent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the next chest to open.
     */
    private void findNextChestToOpen() {
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = player.getPosition();

        for (Map.Entry<BlockPos, Boolean> entry : detectedChests.entrySet()) {
            BlockPos chestPos = entry.getKey();
            boolean alreadyOpen = entry.getValue();

            // Look at the chest
            if (!alreadyOpen && playerPos.distanceSq(chestPos) <= 4 * 4) {

                targetChest = chestPos;
                isLooking = true;
                isReturning = false;
                //orginalYaw = mc.thePlayer.rotationYaw;
                //originalPitch = mc.thePlayer.rotationPitch;

                lookAtChest(targetChest);
                return;
            }
        }
    }

    /**
     * Look at the specified chest.
     */
    public void lookAtChest(BlockPos chestPos) {
        playerLookSmooth.lookAtBlock(chestPos.getX(), chestPos.getY(), chestPos.getZ(), (float) DelClient.settingsManager.getSettingById("auto_powderv2_look_duration").getValDouble());
        isLookFinished(chestPos);
    }

    /**
     * Handle the completion of looking at the chest.
     */
    private void isLookFinished(BlockPos pos) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep((long) (DelClient.settingsManager.getSettingById("auto_powderv2_look_duration").getValDouble() * 1.5));
                // Mark the chest as processed
                detectedChests.put(pos, true);
                targetChest = null;
                isLooking = false;
                tryingToOpenChest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * Handle the completion of returning at original position.
     */
    private void isReturningFinished(){
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep((long) (DelClient.settingsManager.getSettingById("auto_powderv2_look_duration").getValDouble() * 1.3));
                isReturning = false;
                startPlayerMine();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /*
     * Stop the player moving.
     */
    public void stopPlayerMoving(){
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
    }

    /*
     * Will try to open the chest and if block just mine it.
     */
    public void tryingToOpenChest() {
        new Thread(() -> {
            isClicking = true;
            boolean chestDetected = false;
            int attempts = 0;
            final int maxAttempts = 100; // Limite le nombre de tentatives pour éviter une boucle infinie

            while (!chestDetected && attempts < maxAttempts && this.isToggled()) {
                BlockPos lookingAt = mc.objectMouseOver.getBlockPos();
                Block block = mc.theWorld.getBlockState(lookingAt).getBlock();

                if (block instanceof BlockChest) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    DelClient.sendChatToClient("&aOpening chest.");
                    chestDetected = true;
                    int slot = findPrismarine();
                    if(slot != -1){
                        mc.thePlayer.inventory.currentItem = slot;
                    }
                    try {
                        Thread.sleep((long) DelClient.settingsManager.getSettingById("auto_powderv2_click_delay").getValDouble());
                        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                }

                attempts++;
                try {
                    Thread.sleep(100); // Attend 250ms avant la prochaine vérification
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!chestDetected) {
                mc.addScheduledTask(() -> {
                    DelClient.sendChatToClient("&cNo chest found.");
                });
            }


            DelClient.sendDebug("dddd");
            // Retourne à la position originale
            mc.addScheduledTask(() -> {
                if(this.isToggled()){
                    playerLookSmooth.lookAtRotation(originalYaw, originalPitch, (float) DelClient.settingsManager.getSettingById("auto_powderv2_look_duration").getValDouble());
                    isClicking = false;
                    isReturning = true;
                    isReturningFinished();
                }
            });
        }).start();
    }

    /*
     * Start player mine forward.
     */
    public void startPlayerMine(){
        isMining = true;
        int slot = findPickaxe();
        if(slot != -1){
            mc.thePlayer.inventory.currentItem = slot;
        }
        if(DelClient.settingsManager.getSettingById("auto_powderv2_miner").getValBoolean() && smoothPartialRotation != null){
            mc.thePlayer.rotationPitch = (float) DelClient.settingsManager.getSettingById("auto_powderv2_miner_start_pitch").getValDouble();
            smoothPartialRotation.isActive = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        }
    }

    /*
     * Stop player mine forward.
     */
    public void stopPlayerMine(){
        isMining = false;
        stopPlayerMoving();
        if(DelClient.settingsManager.getSettingById("auto_powderv2_miner").getValBoolean() && smoothPartialRotation != null){
            smoothPartialRotation.deactivate();
        }
    }

    /**
     * Find the pickaxe in the hotbar.
     */
    public int findPrismarine() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.getItem() == Items.prismarine_shard) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find the pickaxe in the hotbar.
     */
    public int findPickaxe() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.getItem() == Items.wooden_pickaxe) {
                return i;
            }
        }
        return -1;
    }

    /**
     * There is block in front of player to stop?
     */
    private boolean areBlocksInFront() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        EnumFacing facing = player.getHorizontalFacing();

        double x = player.posX;
        double y = player.posY;
        double z = player.posZ;

        double forwardOffset = 1.5; // Distance devant le joueur
        x += facing.getFrontOffsetX() * forwardOffset;
        z += facing.getFrontOffsetZ() * forwardOffset;

        BlockPos[] positions = new BlockPos[2]; // Seulement 2 blocs à vérifier

        // Calculer les positions des deux blocs superposés devant le joueur
        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
            positions[0] = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
            positions[1] = new BlockPos(Math.floor(x), Math.floor(y) + 1, Math.floor(z));
        }
        else{
            positions[0] = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
            positions[1] = new BlockPos(Math.floor(x), Math.floor(y) + 1, Math.floor(z));
        }

        // Mettre à jour le renderer avec les nouvelles positions
        renderer.setPositions(positions);

        // Vérifier si l'un de ces blocs n'est pas de l'air
        for (BlockPos pos : positions) {
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if (block.getMaterial() != Material.air) {
                return true;
            }
        }
        return false;
    }
}