package com.github.dellixou.delclientv3.gui.oldgui.elements.menu;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class CustomGuiTextField extends GuiTextField {

    int x;
    int y;


    public CustomGuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
        super(componentId, fontrendererObj, x, y, width, height);
        this.x = x;
        this.y = y;
    }

    @Override
    public void drawTextBox() {
        super.xPosition = super.xPosition + 2;
        // Draw outline
        //DrawingUtils.drawRoundedRect(super.xPosition-3, super.yPosition - 4, super.xPosition+width-1, super.yPosition+height-3, 1, new Color(68, 68, 68, 200).getRGB());
        // Draw text field
        //DrawingUtils.drawRoundedRect(super.xPosition-2, super.yPosition - 3, super.xPosition+width-2, super.yPosition+height-4, 1, new Color(35, 35, 35, 230).getRGB());
        super.setEnableBackgroundDrawing(false);
        super.drawTextBox();
    }

}
