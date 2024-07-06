package com.github.dellixou.delclientv3.gui.clickgui.elements.menu;

import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ElementCheckBox extends Element {

	// Circle
	int bgColor = 0xBE232323;
	double xCircle;
	double targetXCircle;
	double startXCircle;
	float animationProgress = 0f;
	boolean animating = false;

	// Text
	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
	float scaleText = 0.6f;

	// Opacity
	public float currentOpacityHover = 0f;
	public float currentOpacityText = 1f;

	/* Constructor */
	public ElementCheckBox(ModuleButton iparent, Setting iset) {
		parent = iparent;
		set = iset;
		xCircle = x + 2;
		targetXCircle = x + 2;
		startXCircle = x + 2;
		super.setup();
	}

	/* Render Check Box */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Calculate target positions based on current x
		double xMin = x + 2;
		double xMax = x + 9;

		xCircle = lerp(startXCircle, targetXCircle, animationProgress);

		if (animating) {
			animationProgress += 0.1f * partialTicks;
			if (animationProgress >= 1) {
				animationProgress = 1;
				animating = false;
			}
		} else {
			if (set.getValBoolean()) {
				xCircle = xMax;
			} else {
				xCircle = xMin;
			}
		}

		// Colors
		Color temp = ColorUtil.getClickGUIColor();
		Color colorA = new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), 255);

		// Background
		Gui.drawRect((int)x, (int)y, (int)x + (int)width, (int)y + (int)height, new Color(bgColor, true).getRGB());

		// Background Check Box
		float checkboxX = (float) (x + width - 3 - 14);
		float checkboxY = (float) y + 3;
		DrawHelper.drawRoundedRect(checkboxX, checkboxY + 9, 15, 9, 3, set.getValBoolean() ? colorA : new Color(156, 156, 156, 255));

		// Calculate text dimensions
		int textHeight = glyphPageFontRenderer.getFontHeight();

		// Center the text
		float textX = (float) x + 1;
		float textY = (float ) (y + (height - textHeight * scaleText) / 2);

		int colorText = new Color(
				(int) (255 * Math.min(0.99, currentOpacityText)),
				(int) (255 * Math.min(0.99, currentOpacityText)),
				(int) (255 * Math.min(0.99, currentOpacityText)),
				0
		).getRGB();
		int colorOpaText = (int) (currentOpacityText * 255) << 24 | colorText;

		GlStateManager.pushMatrix();
		GlStateManager.translate(textX, textY, 0);
		GlStateManager.scale(scaleText, scaleText, scaleText);
		glyphPageFontRenderer.drawString(setstrg, 0, 0, colorOpaText, false);
		GlStateManager.popMatrix();

		// Circle
		DrawHelper.drawCircle(xCircle + 2 + width - 17, y + 7.5, 3, new Color(255, 255, 255, 230));

		// Is Hovered
		if (isCheckHovered(mouseX, mouseY)) {
			// Calculus
			currentOpacityHover = Math.min(0.3f, currentOpacityHover + partialTicks * 0.03f);
			currentOpacityText = Math.max(0.9f, currentOpacityText - partialTicks * 0.03f);
		} else {
			currentOpacityHover = Math.max(0f, currentOpacityHover - partialTicks * 0.03f);
			currentOpacityText = Math.min(1f, currentOpacityText + partialTicks * 0.03f);
		}

		// Convert colorOpaHover to Color object
		int alpha = (int) (currentOpacityHover * 255);
		Color colorOpaHover = new Color(17, 17, 17, alpha);
		// Draw Circle Hover
		DrawHelper.drawCircle(xCircle + 2 + width - 17, y + 7.5, 3.5, colorOpaHover);
	}

	/* Mouse Clicked Event */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0 && isCheckHovered(mouseX, mouseY)) {
			set.setValBoolean(!set.getValBoolean());

			// Set target position for the circle
			startXCircle = xCircle;
			animationProgress = 0;
			animating = true;

			if (set.getValBoolean()) {
				targetXCircle = x + 9;
			} else {
				targetXCircle = x + 2;
			}

			// Apply Change
			if (parent.mod.applyOnChange) {
				if (parent.mod.isToggled()) {
					parent.mod.onEnable();
				}
			}
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/* Hover Check */
	public boolean isCheckHovered(int mouseX, int mouseY) {
		return mouseX >= x - 17 + width && mouseX <= x + width - 2 && mouseY >= y + 2 && mouseY <= y + 13; // 1 , 12
	}

	/* Lerp Function */
	private double lerp(double start, double end, float amount) {
		return start + amount * (end - start);
	}
}
