package com.github.dellixou.delclientv3.modules.render;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.RenderUtils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class HighlightBlock extends Module{

    /**
     * Highlight some blocks.
     **/
    public HighlightBlock() {
        super("Highlight Blocks", 0, Category.RENDER, false, "highlight_block");
    }

    @Override
    public void setup(){
        DelClient.settingsManager.rSetting(new Setting("Hover block", this, true, "highlight_hover_block", "hover"));
        DelClient.settingsManager.rSetting(new Setting("Opacity", this, 0.5f, 0f, 1f, false, "highlight_hover_block_a", "hover"));
        DelClient.settingsManager.rSetting(new Setting("Outline", this, true, "highlight_hover_block_outline", "hover"));
        DelClient.settingsManager.rSetting(new Setting("Outline width", this, 2f, 0.1f, 5f, false, "highlight_hover_block_outline_width", "hover"));
        DelClient.settingsManager.rSetting(new Setting("Red", this, 0.5f, 0f, 1f, false, "highlight_hover_block_r", "hover_color"));
        DelClient.settingsManager.rSetting(new Setting("Green", this, 0.5f, 0f, 1f, false, "highlight_hover_block_g", "hover_color"));
        DelClient.settingsManager.rSetting(new Setting("Blue", this, 0.5f, 0f, 1f, false, "highlight_hover_block_b", "hover_color"));
        DelClient.settingsManager.rSetting(new Setting("Chroma", this, true, "highlight_hover_block_chroma", "hover_chroma"));
        DelClient.settingsManager.rSetting(new Setting("Chroma speed", this, 10, 1, 20, true, "highlight_hover_block_chroma_speed", "hover_chroma"));

        DelClient.settingsManager.rSetting(new Setting("Display block below player", this, true, "highlight_block_ground", "misc"));
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
        if(mc != null && mc.thePlayer != null && mc.theWorld != null){
            // Highlight ground
            if(mc.thePlayer.onGround && DelClient.settingsManager.getSettingById("highlight_block_ground").getValBoolean()){
                RenderUtils.highlightBlockOld(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY-0.4, mc.thePlayer.posZ), Color.white, 0.3f, event.partialTicks);
            }
            // Highlight hover
            if(DelClient.settingsManager.getSettingById("highlight_hover_block").getValBoolean()){
                // Why I need this tf?
                event.setCanceled(true);

                // Settings
                boolean outline = DelClient.settingsManager.getSettingById("highlight_hover_block_outline").getValBoolean();
                boolean chroma = DelClient.settingsManager.getSettingById("highlight_hover_block_chroma").getValBoolean();
                double chroma_speed = DelClient.settingsManager.getSettingById("highlight_hover_block_chroma_speed").getValDouble();
                float red = (float) DelClient.settingsManager.getSettingById("highlight_hover_block_r").getValDouble();
                float green = (float) DelClient.settingsManager.getSettingById("highlight_hover_block_g").getValDouble();
                float blue = (float) DelClient.settingsManager.getSettingById("highlight_hover_block_b").getValDouble();
                float alpha = (float) DelClient.settingsManager.getSettingById("highlight_hover_block_a").getValDouble();
                float width = (float) DelClient.settingsManager.getSettingById("highlight_hover_block_outline_width").getValDouble();

                RenderUtils.drawOverlay(width, outline, chroma, (float) chroma_speed, alpha, red, green, blue);
            }
        }
    }

}
