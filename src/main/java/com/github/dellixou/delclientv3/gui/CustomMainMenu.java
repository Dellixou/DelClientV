package com.github.dellixou.delclientv3.gui;

import com.github.dellixou.delclientv3.utils.Reference;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;

public class CustomMainMenu extends GuiMainMenu {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Custom text to replace the yellow version text
        FontRenderer fontRenderer = this.fontRendererObj;
        int yPos = this.height - 100;

        fontRenderer.drawString("DelClient-beta" + Reference.VERSION, this.width / 2 - fontRenderer.getStringWidth("DelClient-beta0.2.5") / 2, yPos, 0x9966CC, true);
        fontRenderer.drawString("If you have any suggestions/bugs make a post in the Discord!", this.width / 2 - fontRenderer.getStringWidth("If you have any suggestions/bugs make a post in the Discord!") / 2, yPos + 10, 0x5B3D7A, true);
    }
}