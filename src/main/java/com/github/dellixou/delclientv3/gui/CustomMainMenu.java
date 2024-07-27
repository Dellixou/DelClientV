package com.github.dellixou.delclientv3.gui;

import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.misc.Reference;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class CustomMainMenu extends GuiMainMenu {

    private static final GlyphPageFontRenderer font = GlyphPageFontRenderer.create("Arial", 24, true, true, true);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int yPos = this.height - 100;

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2 - font.getStringWidth("§7You are using : §f§lDelClient-beta"+ Reference.VERSION+ Reference.VERSION)/2*0.6f, yPos, 0);
        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        font.drawString("§7You are using : §f§lDelClient-beta"+ Reference.VERSION, 0, 0, 0x9966CC, true);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2 - font.getStringWidth("§fThis mod is currently in development. You may have some bugs.")/2*0.6f, yPos+10, 0);
        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        font.drawString("§7This mod is currently in development. You may have some bugs.", 0, 0, 0x9966CC, true);
        GlStateManager.popMatrix();
    }
}