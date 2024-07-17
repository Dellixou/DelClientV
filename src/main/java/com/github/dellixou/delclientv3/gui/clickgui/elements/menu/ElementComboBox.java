package com.github.dellixou.delclientv3.gui.clickgui.elements.menu;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.clickgui.util.FontUtil;
import com.github.dellixou.delclientv3.gui.clickgui.util.SettingsArrow;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.utils.Color.ColorUtils;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ElementComboBox extends Element {

	// Misc
	int bgColor = 0xBE232323;
	// Arrow image
	private static final ResourceLocation ARROW_IMAGE = new ResourceLocation("textures/arrowd.png");
	private final SettingsArrow settingsArrow = new SettingsArrow();

	// Text
	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
	float scaleText = 0.6f;
	public float currentOpacityText = 1f;

	// Animation progress
	private float comboAnimationProgress = 0f;

	/*
	 * Constructor
	 */
	public ElementComboBox(ModuleButton iparent, Setting iset) {
		parent = iparent;
		set = iset;
		super.setup();
	}

	/*
	 * Render Elements
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		String currentValue = set.getValString().substring(0, 1).toUpperCase()+set.getValString().substring(1);

		// Background
		Gui.drawRect((int)x, (int)y, (int)x + (int)width, (int)y + (int)height, new Color(bgColor, true).getRGB());

		if (isButtonHovered(mouseX, mouseY)) {
			currentOpacityText = Math.max(0.8f, currentOpacityText - partialTicks * 0.03f);
		} else {
			currentOpacityText = Math.max(1f, currentOpacityText - partialTicks * 0.03f);
		}

		// Calculate text dimensions
		int textHeight = glyphPageFontRenderer.getFontHeight();

		// Center the text
		float textX = (float) x + 1;
		float textY = (float ) (y + (15 - textHeight * scaleText) / 2);

		// Settings Name Text
		GlStateManager.pushMatrix();
		GlStateManager.translate(textX, textY , 0);
		GlStateManager.scale(scaleText, scaleText, scaleText);
		int alpha = (int) (currentOpacityText * 255);
		Color colorOpaHover = new Color(255, 255, 255, alpha);
		glyphPageFontRenderer.drawString(setstrg, 0, 0, colorOpaHover.getRGB(), false);
		GlStateManager.popMatrix();

		// Settings Value Text
		float displayValX = (float)(x + width - glyphPageFontRenderer.getStringWidth(currentValue) * (scaleText - 0.05) - 4 - 12);
		GlStateManager.pushMatrix();
		GlStateManager.translate(displayValX, textY , 0);
		GlStateManager.scale(scaleText, scaleText, scaleText);
		glyphPageFontRenderer.drawString(currentValue, 0, 0, colorOpaHover.getRGB(), false);
		GlStateManager.popMatrix();

		settingsArrow.drawComboArrow((float) (x+width-13), textY-1.5f, 12, (float) 12, ARROW_IMAGE, comboextended, partialTicks, 1f);

		// Update animation progress
		if (comboextended) {
			comboAnimationProgress = Math.min(1f, comboAnimationProgress + partialTicks * 0.14f);
		} else {
			comboAnimationProgress = Math.max(0f, comboAnimationProgress - partialTicks * 0.14f);
		}

		// Animate the extension of the combo box options
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y+15, 0);
		GlStateManager.scale(1, comboAnimationProgress, 1);
		GlStateManager.translate(-x, -(y+15), 0);

		// Extend options
		if (comboAnimationProgress > 0) {
			double startY = y + 20;
			double ay = startY;
			double maxWidth = 0;

			// Take all information for background
			for (String sld : set.getOptions()) {
				String elementtitle = sld.substring(0, 1).toUpperCase() + sld.substring(1);
				float textWidth = glyphPageFontRenderer.getStringWidth(elementtitle) * scaleText;
				maxWidth = Math.max(maxWidth, textWidth);
				ay += FontUtil.getFontHeight() + 2;
			}

			// Draw background options
			DrawHelper.drawRoundedRect(x + 5, startY - 1 + (ay - startY + 4), width - 10, ay - startY + 4, 3, new Color(50, 50, 50, 200));
			DrawHelper.drawRoundedRectOutline(x + 4, startY - 2 + (ay - startY + 6), width - 8, ay - startY + 6, 3, 1.5f, ColorUtil.getClickGUIColor());

			ay = startY;
			for (String sld : set.getOptions()) {
				String elementtitle = sld.substring(0, 1).toUpperCase() + sld.substring(1);

				// Text coordinates
				float elemX = (float)(x + width / 2);
				float textWidth = glyphPageFontRenderer.getStringWidth(elementtitle) * scaleText;
				float centeredX = elemX - textWidth / 2;

				// Selected effect
				if (sld.equalsIgnoreCase(set.getValString())) {
					DrawHelper.drawRoundedRect(x+5, ay+12, width-10, 13, 3, new Color(ColorUtil.getClickGUIColor().getRed(), ColorUtil.getClickGUIColor().getGreen(), ColorUtil.getClickGUIColor().getBlue(), 110));
				}

				// Hover effect
				if (mouseX >= x && mouseX <= x + width && mouseY >= ay && mouseY < ay + FontUtil.getFontHeight() + 2) {
					DrawHelper.drawRoundedRect(x+5, ay+12, width-10, 13, 3, new Color(123, 123, 123, 70));
				}

				// Text elements/options
				GlStateManager.pushMatrix();
				GlStateManager.translate(centeredX, ay + 1, 0);
				GlStateManager.scale(scaleText, scaleText, scaleText);
				glyphPageFontRenderer.drawString(elementtitle, 0, 0, Color.WHITE.getRGB(), true);
				GlStateManager.popMatrix();

				ay += FontUtil.getFontHeight() + 4;
			}
		}

		GlStateManager.popMatrix();
	}


	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (isButtonHovered(mouseX, mouseY)) {
				comboextended = !comboextended;
				return true;
			}

			if (!comboextended) return false;
			double ay = y + 20;
			for (String slcd : set.getOptions()) {
				if (mouseX >= x && mouseX <= x + width && mouseY >= ay && mouseY < ay + FontUtil.getFontHeight() + 2) {
					if(DelClient.settingsManager.getSettingByName("Sound").getValBoolean())
						Minecraft.getMinecraft().thePlayer.playSound("tile.piston.in", 20.0F, 20.0F);

					if(clickgui != null && clickgui.setmgr != null)
						clickgui.setmgr.getSettingByName(set.getName()).setValString(slcd.toLowerCase());
					if(parent.mod.applyOnChange){
						if(parent.mod.isToggled()){
							parent.mod.onEnable();
						}
					}
					return true;
				}
				ay += FontUtil.getFontHeight() + 4;
			}
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public boolean isButtonHovered(int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 15;
	}
}