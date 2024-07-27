package com.github.dellixou.delclientv3.gui.oldgui.elements;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.oldgui.Panel;
import com.github.dellixou.delclientv3.gui.oldgui.elements.menu.ElementCheckBox;
import com.github.dellixou.delclientv3.gui.oldgui.elements.menu.ElementComboBox;
import com.github.dellixou.delclientv3.gui.oldgui.elements.menu.ElementSlider;
import com.github.dellixou.delclientv3.gui.oldgui.elements.menu.ElementWriteBox;
import com.github.dellixou.delclientv3.utils.gui.SettingsArrow;
import com.github.dellixou.delclientv3.modules.core.settings.Setting;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.ColorUtils;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.shaders.misc.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;


public class ModuleButton {
	public Module mod;
	public ArrayList<Element> menuelements;
	public Panel parent;
	public double x;
	public double y;
	public double width;
	public double height;
	public boolean extended = false;
	public boolean listening = false;
	public boolean isRoundedModule = false;

	// Visual
	private SettingsArrow settingsArrow;
	public float currentOpacityHover = 0f;
	public float currentOpacityToggle = 0f;

	private static final ResourceLocation ARROW_IMAGE = new ResourceLocation("textures/arrowr.png");
	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
	float scaleText = 0.6f;

	/* Constructor */
	public ModuleButton(Module imod, Panel pl) {
		mod = imod;
		height = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2;
		parent = pl;
		menuelements = new ArrayList<Element>();
		settingsArrow = new SettingsArrow();

		if (DelClient.settingsManager.getSettingsByMod(imod) != null)
			for (Setting s : DelClient.settingsManager.getSettingsByMod(imod)) {
				if (s.isCheck()) {
					menuelements.add(new ElementCheckBox(this, s));
				} else if (s.isSlider()) {
					menuelements.add(new ElementSlider(this, s));
				} else if (s.isCombo()) {
					menuelements.add(new ElementComboBox(this, s));
				} else if(s.isText()) {
					menuelements.add(new ElementWriteBox(this, s));
				}
			}
	}

	/* Update settings */
	public void updateSettings(){
		menuelements.clear();
		for (Setting s : DelClient.settingsManager.getSettingsByMod(this.mod)) {
			if (s.isCheck()) {
				menuelements.add(new ElementCheckBox(this, s));
			} else if (s.isSlider()) {
				menuelements.add(new ElementSlider(this, s));
			} else if (s.isCombo()) {
				menuelements.add(new ElementComboBox(this, s));
			} else if(s.isText()) {
				menuelements.add(new ElementWriteBox(this, s));
			}
		}
	}

	/* Render */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Color
		Color temp = ColorUtils.getClickGUIColor();
		int alpha_ = (int) (currentOpacityToggle * 255);
		Color colorOpaToggle = new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), alpha_);

		// Toggle Rectangle
		if (mod.isToggled()) {
			currentOpacityToggle = Math.min(0.8f, currentOpacityToggle + partialTicks * 0.05f);
		}else{
			currentOpacityToggle = Math.min(0f, currentOpacityToggle + partialTicks * 0.05f);
		}
		if(isRoundedModule){
			DrawHelper.drawRoundedOnlyBottom(x - 2, y, width + 4, height + 1, 3, colorOpaToggle);
		}else{
			Gui.drawRect((int)x - 2, (int)y, (int)x +(int)width + 2, (int)y + (int)height + 1, colorOpaToggle.getRGB());
		}

		// Is Hovered Rectangle
		if (isHovered(mouseX, mouseY)) {
			currentOpacityHover = Math.min(0.6f, currentOpacityHover + partialTicks * 0.035f);
		} else {
			currentOpacityHover = Math.max(0f, currentOpacityHover - partialTicks * 0.035f);
		}
		int colorOpaHover = (int) (currentOpacityHover * 255) << 24 | 0x7B7B7B;
		int alpha = (int) (currentOpacityHover * 255);
		Color colorOpaHover_ = new Color(123, 123, 123, alpha);
		if(isRoundedModule){
			DrawHelper.drawRoundedOnlyBottom(x - 2, y, width + 4, height + 1, 3, colorOpaHover_);
		}else{
			Gui.drawRect((int) x - 2, (int) y, (int) x + (int) width + 2, (int) y + (int) height + 1, colorOpaHover);
		}

		// Arrow if settings
		try{
			if(mod.hasSettings()){
				//settingsArrow.drawModArrow((float) ((int)x + width - 10)+2, (float)y + 1.1f, (float) 7.6, (float) 9.5, ARROW_IMAGE, extended, partialTicks); // arrowRot
			}
		}catch (Exception ignored){ }

		//FontUtil.drawTotalCenteredStringWithShadowWithScale(mod.getName(), x+(width/2), y+1+(height/2), textcolor, 0.8F);

		// Calculate text dimensions
		int textWidth = glyphPageFontRenderer.getStringWidth(mod.getName());
		int textHeight = glyphPageFontRenderer.getFontHeight();

		// Center the text
		float textX = (float) (x + (width - textWidth * scaleText) / 2);
		float textY = (float ) (y + (height - textHeight * scaleText) / 2);

		GlStateManager.pushMatrix();
		GlStateManager.translate(textX, textY, 0);
		GlStateManager.scale(scaleText, scaleText, scaleText);
		glyphPageFontRenderer.drawString(mod.getName(), 0, 0, -1, true);
		GlStateManager.popMatrix();


	}

	/* Mouse Clicked Event */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isHovered(mouseX, mouseY))
			return false;


		if (mouseButton == 0) {
			mod.toggle();
			
			if(DelClient.settingsManager.getSettingByName("Sound").getValBoolean())
			Minecraft.getMinecraft().thePlayer.playSound("random.click", 0.5f, 0.5f);

		} else if (mouseButton == 1) {

			if (menuelements != null && menuelements.size() > 0) {
				boolean b = !this.extended;
				//DelClient.clickGUI.closeAllSettings();
				this.extended = b;
				if(!this.extended){
					for(Element elem : menuelements){
						if(elem instanceof ElementSlider){
							ElementSlider elementSlider = (ElementSlider) elem;
							elementSlider.currentPercentBar = 0;
						}
					}
				}
				if(DelClient.settingsManager.getSettingByName("Sound").getValBoolean())
				if(extended)Minecraft.getMinecraft().thePlayer.playSound("tile.piston.out", 1f, 1f);else Minecraft.getMinecraft().thePlayer.playSound("tile.piston.in", 1f, 1f);
			}
		} else if (mouseButton == 2) {
			listening = true;
		}
		return true;
	}

	/* key Typed Event */
	public boolean keyTyped(char typedChar, int keyCode) throws IOException {

		if (listening) {
			if (keyCode != Keyboard.KEY_ESCAPE) {
				//Client.sendChatMessage("Bound '" + mod.getName() + "'" + " to '" + Keyboard.getKeyName(keyCode) + "'");
				mod.setKey(keyCode);
			} else {
				//Client.sendChatMessage("Unbound '" + mod.getName() + "'");
				mod.setKey(Keyboard.KEY_NONE);
			}
			listening = false;
			return true;
		}
		return false;
	}

	/* Is Hovered */
	public boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= x - 2 && mouseX <= x + width + 2 && mouseY >= y && mouseY <= y + height;
	}

}
