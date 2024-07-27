package com.github.dellixou.delclientv3.gui.oldgui.util;

import com.github.dellixou.delclientv3.utils.misc.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;


public class FontUtil {
	private static FontRenderer fontRenderer;

	public static void setupFontUtils() {
		Minecraft mc = Minecraft.getMinecraft();
		ResourceLocation FONT = new ResourceLocation(Reference.MODID, "font/delclient");

		fontRenderer = mc.fontRendererObj;

	}

	public static int getStringWidth(String text) {
		return fontRenderer.getStringWidth(StringUtils.stripControlCodes(text));
	}

	public static int getFontHeight() {
		return fontRenderer.FONT_HEIGHT;
	}

	public static void drawString(String text, double x, double y, int color) {
		fontRenderer.drawString(text, (int)x, (int)y, color); // fontRenderer.drawString(text, (int)x, (int)y, color);
	}

	public static void drawStringWithShadow(String text, double x, double y, int color) {
		fontRenderer.drawStringWithShadow(text, (int)x, (int)y, color);
	}

	public static void drawStringWithShadowWithScale(String text, double x, double y, int color){
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(0.9, 0.9, 0.9);
		fontRenderer.drawStringWithShadow(text, 0, 0, color);
		GlStateManager.popMatrix();
	}

	public static void drawStringCheckBox(String text, double x, double y, int color){
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(0.9, 0.9, 0.9);
		fontRenderer.drawStringWithShadow(text, FontUtil.getStringWidth(text)/12F, 0, color);
		GlStateManager.popMatrix();
	}

	public static void drawCenteredString(String text, double x, double y, int color) {
		drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
	}

	public static void drawCenteredStringWithShadow(String text, double x, double y, int color) {
		drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
	}

	public static void drawTotalCenteredString(String text, double x, double y, int color) {
		drawString(text, x - fontRenderer.getStringWidth(text) / 2F, y - fontRenderer.FONT_HEIGHT / 2F, color);
	}

	public static void drawTotalCenteredStringWithShadow(String text, double x, double y, int color) {
		drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2F, y - fontRenderer.FONT_HEIGHT / 2F, color);
	}

	public static void drawTotalCenteredStringWithShadowWithScale(String text, double x, double y, int color, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x - fontRenderer.getStringWidth(text) / 2F, y - fontRenderer.FONT_HEIGHT / 2F, 0);
		GlStateManager.scale(scale, scale, scale);
		float negativeScale = 1 + (1-scale);
		double x2 = ((fontRenderer.getStringWidth(text))/8F);
		double y2 = fontRenderer.FONT_HEIGHT / 8F;
		drawStringWithShadow(text, x2, y2, color);
		GlStateManager.popMatrix();
	}

	public static void drawTotalCenteredStringWithScale(String text, double x, double y, int color, float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x - fontRenderer.getStringWidth(text) / 2F, y - fontRenderer.FONT_HEIGHT / 2F, 0);
		GlStateManager.scale(scale, scale, scale);
		float negativeScale = 1 + (1-scale);
		double x2 = x - fontRenderer.getStringWidth(text) / 2F - (x - fontRenderer.getStringWidth(text)*negativeScale/2);
		double y2 = y - fontRenderer.FONT_HEIGHT / 2F - (y - fontRenderer.FONT_HEIGHT*negativeScale/ 2F);
		drawString(text, x2, y2, color);
		GlStateManager.popMatrix();
	}

}
