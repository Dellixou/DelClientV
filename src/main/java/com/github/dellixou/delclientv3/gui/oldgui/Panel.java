package com.github.dellixou.delclientv3.gui.oldgui;

import com.github.dellixou.delclientv3.gui.oldgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;
import com.github.dellixou.delclientv3.utils.gui.shaders.misc.DrawHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import static org.lwjgl.opengl.GL11.*;

import java.awt.*;
import java.util.ArrayList;

public class Panel {
	// Some values
	public String title;
	public double x;
	public double y;
	public int index;
	private double x2;
	private double y2;
	public double width;
	public double height;
	public boolean dragging;
	public boolean extended;
	public boolean visible;
	public ArrayList<ModuleButton> Elements = new ArrayList<ModuleButton>();
	public ClickGUI clickgui;

	// Text
	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 54, true, true, true);
	float scale = 0.37f;

	// Panel Hover Animation
	public float fadeOpa;
	public float fadeButtonOpa;

	// Panel Extended Scale Animation
	private float moduleAnimationProgress = 0f;

	/* Constructor */
	public Panel(String ititle, double ix, double iy, double iwidth, double iheight, boolean iextended, ClickGUI parent, int index) {
		this.title = ititle;
		this.x = ix;
		this.y = iy;
		this.width = iwidth;
		this.height = iheight;
		this.extended = iextended;
		this.dragging = false;
		this.visible = true;
		this.clickgui = parent;
		this.index = index;
		//this.startAnimationText = new MovingAnimation(this.y-((index+1)*1.3), this.y, 5+((index+1)*2));
		setup();
	}

	/* Setup */
	public void setup() {}

	/* Render Elements */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		// Animate Start

		// If not visible just do nothing
		if (!this.visible){
			return;
		}

		// If dragging stop animation
		if (this.dragging) {
			x = x2 + mouseX;
			y = y2 + mouseY;
		}

		fadeOpa = Math.min(1f, fadeOpa + partialTicks * 0.04f);
		Color color = new Color(31, 31, 31, 0);
		Color colorOpa = new Color(
				color.getRed(),
				color.getGreen(),
				color.getBlue(),
				(int) (fadeOpa * 255)
		);

		// Calculate text dimensions
		int textWidth = glyphPageFontRenderer.getStringWidth(title);
		int textHeight = glyphPageFontRenderer.getFontHeight();

		// Center the text
		float textX = (float) (x + (width - textWidth * scale) / 2);
		float textY = (float) (y + (height - textHeight * scale) / 2);

		// Calcul for blur
		double totalHeight = height;
		if (this.extended && !Elements.isEmpty()) {
			for (ModuleButton et : Elements) {
				totalHeight += et.height + 1;
				if (et.extended) {
					totalHeight += et.menuelements.size() * et.menuelements.get(0).height;
				}
			}
		}

		// Draw Blur
		DrawHelper.drawRoundedBlurredRect(x, y + totalHeight, width, totalHeight, 3, 4, colorOpa);

		// Draw Panel Top
		DrawHelper.drawRoundedOnlyTop(x, y, width, height, 3, colorOpa);

		// Title for panel
		GlStateManager.pushMatrix();
		GlStateManager.translate(textX, textY, 0);
		GlStateManager.scale(scale, scale, scale);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glyphPageFontRenderer.drawString(title, 0, 0, new Color(212, 212, 212).getRGB(), true);
		GlStateManager.popMatrix();

		if (this.extended) {
			moduleAnimationProgress = Math.min(1f, moduleAnimationProgress + partialTicks * 0.1f);
		} else {
			moduleAnimationProgress = Math.max(0f, moduleAnimationProgress - partialTicks * 0.1f);
		}

		// Do Scale and translate back
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + height, 0);
		GlStateManager.scale(1, moduleAnimationProgress, 1);
		GlStateManager.translate(-x, -(y + height), 0);

		// Extended
		if (moduleAnimationProgress > 0 && !Elements.isEmpty()) {
			double startY = y + height;
			fadeButtonOpa = Math.min(1f, fadeButtonOpa + partialTicks * 0.04f);
			int alpha = (int) (fadeButtonOpa * 255);
			Color epanelcolor = new Color(40, 40, 40, alpha);

			// Draw modules
			double currentY = startY;
			for (ModuleButton et : Elements) {
				if(et.mod.getId().equals("user_route")){
					UserRoute userRoute = (UserRoute) ModuleManager.getModuleById("user_route");
					userRoute.moduleButtonUser = et;
				}

				// Draw background
				if(Elements.get(Elements.size()-1) == et && !et.extended){
					et.isRoundedModule = true;
					DrawHelper.drawRoundedOnlyBottom(x, currentY, width, et.height + 1, 3, epanelcolor);
				}else{
					et.isRoundedModule = false;
					Gui.drawRect((int)x, (int)currentY, (int)(x + width), (int)(currentY + et.height + 1), epanelcolor.getRGB());
				}

				et.x = x + 2;
				et.y = currentY;
				et.width = width - 4;
				et.drawScreen(mouseX, mouseY, partialTicks);

				currentY += et.height + 1;
				if(et.extended){
					currentY += et.menuelements.size() * et.menuelements.get(0).height;
				}
			}
		}

		GlStateManager.popMatrix();
	}

	/* Mouse Clicked Event  */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!this.visible) {
			return false;
		}
		if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
			x2 = this.x - mouseX;
			y2 = this.y - mouseY;
			dragging = true;
			return true;
		} else if (mouseButton == 1 && isHovered(mouseX, mouseY)) {
			extended = !extended;
			return true;
		} else if (extended) {
			for (ModuleButton et : Elements) {
				if (et.mouseClicked(mouseX, mouseY, mouseButton)) {
					return true;
				}
			}
		}
		return false;
	}

	/* Drag Released Event */
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (!this.visible) {
			return;
		}
		if (state == 0) {
			this.dragging = false;
		}
	}

	/* Hover Check */
	public boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	private float easeOutQuad(float t) {
		return t * (2 - t);
	}

}
