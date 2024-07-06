package com.github.dellixou.delclientv3.gui.clickgui.elements;


import com.github.dellixou.delclientv3.gui.clickgui.ClickGUI;
import com.github.dellixou.delclientv3.gui.clickgui.util.FontUtil;
import com.github.dellixou.delclientv3.gui.settings.Setting;
import com.github.dellixou.delclientv3.utils.gui.glyph.GlyphPageFontRenderer;

public class Element {

	public ClickGUI clickgui;
	public ModuleButton parent;
	public Setting set;
	public double offset;
	public double x;
	public double y;
	public double width;
	public double height;
	public int scaledHeight;

	public String setstrg;

	private static final GlyphPageFontRenderer glyphPageFontRenderer = GlyphPageFontRenderer.create("Arial", 24, true, true, true);

	public boolean comboextended;

	public void setup() {
		clickgui = parent.parent.clickgui;
	}

	public void update() {
		x = parent.x - 2;
		y = parent.y + parent.height + offset + 1;
		width = parent.width + 4;
		height = 15;

		String sname = set.getName();
		if (set.isCheck()) {
			setstrg = sname.substring(0, 1).toUpperCase() + sname.substring(1);
			double textx = x + width - 0; // FontUtil.getStringWidth(setstrg)
			if (textx < x + 13) {
				width += (x + 13) - textx + 1;
			}
		} else if (set.isCombo()) {
			height = comboextended ? set.getOptions().size() * (glyphPageFontRenderer.getFontHeight() + 2) + 15 : 15;

			setstrg = sname.substring(0, 1).toUpperCase() + sname.substring(1);
			int longest = FontUtil.getStringWidth(setstrg);
			for (String s : set.getOptions()) {
				int temp = FontUtil.getStringWidth(s);
				if (temp > longest) {
					longest = temp;
				}
			}
			double textx = x + width - 0; // - longest
			if (textx < x) {
				width += x - textx + 1;
			}
		} else if (set.isSlider()) {
			setstrg = sname.substring(0, 1).toUpperCase() + sname.substring(1);
			String displayval = "" + Math.round(set.getValDouble() * 100D) / 100D;
			String displaymax = "" + Math.round(set.getMax() * 100D) / 100D;
			double textx = x + width - 0; // FontUtil.getStringWidth(setstrg) - FontUtil.getStringWidth(displaymax) - 4
			if (textx < x) {
				width += x - textx + 1;
			}
		} else if (set.isText()) {
			setstrg = sname.substring(0, 1).toUpperCase() + sname.substring(1);
			double textx = x + width - 0; //  FontUtil.getStringWidth(setstrg)
			if (textx < x + 13) {
				width += (x + 13) - textx + 1;
			}
			height = 27;
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		return isHovered(mouseX, mouseY);
	}

	public void mouseReleased(int mouseX, int mouseY, int state) {}

	public void keyTyped(char typedChar, int keyCode) {}

	public boolean isHovered(int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
