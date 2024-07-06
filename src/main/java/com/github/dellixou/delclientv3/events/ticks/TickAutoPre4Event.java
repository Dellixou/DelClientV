package com.github.dellixou.delclientv3.events.ticks;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class TickAutoPre4Event {

    // Fields for managing AutoPre4
    private int tickCounter = 0;
    private static final int INTERVAL = 4; // 0.2 seconds (4 ticks)
    // Device blocks
    private static final List<BlockPos> blockPositions = new ArrayList<>();

    /**
     * Sets up all devices blocks
     */
    static {
        blockPositions.add(new BlockPos(64, 126, 50));
        blockPositions.add(new BlockPos(66, 126, 50));
        blockPositions.add(new BlockPos(68, 126, 50));
        blockPositions.add(new BlockPos(64, 128, 50));
        blockPositions.add(new BlockPos(66, 128, 50));
        blockPositions.add(new BlockPos(68, 128, 50));
        blockPositions.add(new BlockPos(64, 130, 50));
        blockPositions.add(new BlockPos(66, 130, 50));
        blockPositions.add(new BlockPos(68, 130, 50));
    }

    /**
     * Every tick add a tick to counter and do search.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        tickCounter++;
        if (tickCounter >= INTERVAL) {
            tickCounter = 0;
            searchEmerald();
        }
    }

    /**
     * Search emerald at coords
     */
    private void searchEmerald() {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        for (BlockPos pos : blockPositions) {
            Block block = world.getBlockState(pos).getBlock();
            String blockName = block.getUnlocalizedName();
            DelClient.sendChatToClient("&7Found emerald block at coords --> X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());
        }
    }

}
