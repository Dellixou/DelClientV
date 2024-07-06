package com.github.dellixou.delclientv3.gui.clickgui.elements.menu;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.clickgui.util.FontUtil;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.utils.gui.animations.FadeInAnimation;
import com.github.dellixou.delclientv3.utils.gui.animations.MovingAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

public class ElementWriteBox extends Element {
	private int bgColor = 0xBE232323;
	private GuiTextField textField;

	// Animations
	MovingAnimation clickAnim;
	FadeInAnimation hoverAnim = new FadeInAnimation(90, 25);

	/* Constructor */
	public ElementWriteBox(ModuleButton iparent, Setting iset) {
		parent = iparent;
		set = iset;
		super.setup();

		textField = new CustomGuiTextField(0, Minecraft.getMinecraft().fontRendererObj, (int)x, (int)y, (int)width, (int)height);
		textField.setMaxStringLength(50);
		textField.setText(set.getValString());
	}

	/* Render Write Box */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		textField.xPosition = (int)x + 2;
		textField.yPosition = (int)y + 2 + 14;
		textField.width = (int)width - 4;
		textField.height = (int)height - 4 - 10;

		// Colors
		Color temp = ColorUtil.getClickGUIColor();
		int color = new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), 170).getRGB();

		// Background
		Gui.drawRect((int)x, (int)y, (int)x + (int)width, (int)y + (int)height, bgColor);

		textField.setText(set.getValString());

		// Render the text field
		textField.drawTextBox();

		// Draw title
		FontUtil.drawStringWithShadowWithScale(setstrg, x + 2, y - (float) FontUtil.getFontHeight() - 2 + 14, 0xffffffff);
	}

	/* Mouse Clicked Event */
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
			textField.setFocused(true);
			if(DelClient.instance.currentTextField != null){
				DelClient.instance.currentTextField.textField.setFocused(false);
			}
			DelClient.instance.currentTextField = this;
			return true;
		} else {
			textField.setFocused(false);
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/* Key Typed Event */
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (textField.isFocused()) {
			textField.textboxKeyTyped(typedChar, keyCode);
			set.setValString(textField.getText());

			// Apply Change
			if (parent.mod.applyOnChange) {
				if(parent.mod.isToggled()){
					parent.mod.onEnable();
				}
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	/* Hover Check */
	public boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
