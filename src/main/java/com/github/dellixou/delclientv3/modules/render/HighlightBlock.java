package com.github.dellixou.delclientv3.modules.render;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.renderer.RenderUtils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HighlightBlock extends Module{

    // Some values
    final int[] counter = {1};

    /**
     * Highlight some blocks.
     **/
    public HighlightBlock() {
        super("Highlight Blocks", 0, Category.RENDER, false, "highlight_block");
    }

    @Override
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Ground Block", this, true, "highlight_block_ground", "global"));
    }

    public void onEnable(){
        if(this.isToggled()){
            MinecraftForge.EVENT_BUS.register(this);
            super.onEnable();
        }
    }

    @Override
    public void onDisable(){
        if(!this.isToggled()){
            MinecraftForge.EVENT_BUS.unregister(this);
            super.onDisable();
        }
    }

    @SubscribeEvent
    public void onHighlight(DrawBlockHighlightEvent event){
        if(mc != null && mc.thePlayer != null){
            if(mc.thePlayer.onGround){
                RenderUtils.highlightBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY-0.4, mc.thePlayer.posZ), RenderUtils.rainbow(counter[0] * 25), 0.3f, event.partialTicks);
                counter[0]++;
            }
        }
    }

}
