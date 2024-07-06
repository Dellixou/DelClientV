package com.github.dellixou.delclientv3.gui.clickgui.elements.menu;

import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.clickgui.util.FontUtil;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.utils.gui.DrawingUtils;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.misc.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class ElementSlider extends Element {

	// Misc
	int bgColor = 0xBE232323;
	public boolean dragging;

	// Text
	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 24, true, true, true);
	float scaleText = 0.6f;

	// Percent Bar Smooth
	public float currentPercentBar;
	private float targetPercentBar;
	private float lerpSpeed = 0.3f; // Vitesse de l'animation

	// Animations
	public float currentOpacityText = 1f;

	/*
	 * Constructor
	 */
	public ElementSlider(ModuleButton iparent, Setting iset) {
		parent = iparent;
		set = iset;
		dragging = false;
		super.setup();
	}

	public static float lerp(float start, float end, float amount) {
		return start + amount * (end - start);
	}

	/*
	 * Render Elements
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Some values
		String displayval = "" + Math.round(set.getValDouble() * 100D) / 100D;
		boolean hoveredORdragged = isSliderHovered(mouseX, mouseY) || dragging;

		// Some colors
		Color temp = ColorUtil.getClickGUIColor();
		Color colorBar = new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), hoveredORdragged ? 250 : 200);
		Color color = new Color(temp.getRed(), temp.getGreen(), temp.getBlue(), 255);
		int colorText = new Color(
				(int) (255 * Math.min(0.99, currentOpacityText)),
				(int) (255 * Math.min(0.99, currentOpacityText)),
				(int) (255 * Math.min(0.99, currentOpacityText)),
				0
		).getRGB();
		int colorOpaText = (int) (currentOpacityText * 255) << 24 | colorText;

		// Target bar percent
		targetPercentBar = (float) ((set.getValDouble() - set.getMin()) / (set.getMax() - set.getMin()));

		// Lerp for smooth
		currentPercentBar = lerp(currentPercentBar, targetPercentBar, lerpSpeed * partialTicks);

		// Background
		Gui.drawRect((int)x, (int)y, (int)x + (int)width, (int)y + (int)height, bgColor);

		// Text Name
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 1, y + 2, 0);
		GlStateManager.scale(scaleText, scaleText, scaleText);
		glyphPageFontRenderer.drawString(setstrg, 0, 0, colorOpaText, false);
		GlStateManager.popMatrix();

		// Calculate text value
		int padding = 3;
		float displayValX = (float)(x + width - glyphPageFontRenderer.getStringWidth(displayval) * (scaleText - 0.05) - padding);
		float displayValY = (float)y + 2;

		// Text Value
		GlStateManager.pushMatrix();
		GlStateManager.translate(displayValX, displayValY, 0);
		GlStateManager.scale(scaleText - 0.05, scaleText - 0.05, scaleText - 0.05);
		glyphPageFontRenderer.drawString(displayval, 0, 0, colorOpaText, false);
		GlStateManager.popMatrix();

		// Bar
		DrawHelper.drawRoundedRect(x + 1, y + 13, width - 3, 2, 1, new Color(0x232323));
		DrawHelper.drawRoundedRect(x + 1, y + 13, currentPercentBar * (width - 3), 2, 1, colorBar);

		if (set.getValDouble() > set.getMin() && set.getValDouble() < set.getMax()) {
			DrawHelper.drawCircle(x + 1 + currentPercentBar * (width - 3), y + 12, 2, color);
		}

		if (this.dragging) {
			double diff = set.getMax() - set.getMin();
			double val = set.getMin() + (MathHelper.clamp_double((mouseX - x) / width, 0, 1)) * diff;
			set.setValDouble(val); // Define value
			if (parent.mod.applyOnChange) {
				if (parent.mod.isToggled()) {
					parent.mod.onEnable();
				}
			}
		}
	}


	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0 && isSliderHovered(mouseX, mouseY)) {
			this.dragging = true;
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}


	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.dragging = false;
	}


	public boolean isSliderHovered(int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y + 11 && mouseY <= y + 14;
	}
}