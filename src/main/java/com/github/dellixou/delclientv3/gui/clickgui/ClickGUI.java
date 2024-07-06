package com.github.dellixou.delclientv3.gui.clickgui;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.gui.clickgui.elements.Element;
import com.github.dellixou.delclientv3.gui.clickgui.elements.ModuleButton;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementSlider;
import com.github.dellixou.delclientv3.gui.clickgui.util.ColorUtil;
import com.github.dellixou.delclientv3.gui.clickgui.util.FontUtil;
import com.github.dellixou.delclientv3.gui.settings.SettingsManager;
import com.github.dellixou.delclientv3.modules.core.Category;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ClickGUI extends GuiScreen {

	// GUI
	public static ArrayList<Panel> panels;
	public static ArrayList<Panel> rpanels;
	private ModuleButton mb = null;
	public SettingsManager setmgr;

	// Scale animation
	private float animationProgress = 0.0f;
	private static final float ANIMATION_DURATION = 30.0f; // Duration of the animation in ticks
	private static final float INITIAL_SCALE = 0.4f; // Starting scale
	private static final float FINAL_SCALE = 1.0f; // Ending scale


	/**
	 * Constructor
	 **/
	public ClickGUI() {

		setmgr = DelClient.settingsManager;

		animationProgress = 0.0f; // Initialize animation progress

		FontUtil.setupFontUtils();
		panels = new ArrayList<Panel>();

		double pwidth = 95;
		double pheight = 15;
		double px = 10;
		double py = 10;
		double pyplus = pwidth + 4;


		int index = 0;
		for (final Category c : Category.values()) {
			String title = Character.toUpperCase(c.name().toLowerCase().charAt(0)) + c.name().toLowerCase().substring(1);
			ClickGUI.panels.add(new Panel(title, px, py, pwidth, pheight, true, this, index) {
						@Override
						public void setup() {
							for (Module m : ModuleManager.getModules()) {
								if (!m.getCategory().equals(c))continue;
								this.Elements.add(new ModuleButton(m, this));
							}
						}
			});
			index++;
			px += pyplus;
		}


		rpanels = new ArrayList<Panel>();
		rpanels.addAll(panels);
		Collections.reverse(rpanels);

	}

	public boolean doesGuiPauseGame(){
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		//if(!DelClient.instance.getIsAuthorized()){
		//	return;
		//}

		// Increment the animation progress
		if (animationProgress < ANIMATION_DURATION) {
			animationProgress += 1.0f;
		}

		// Calculate the scale factor based on the animation progress using ease-out interpolation
		float progressRatio = animationProgress / ANIMATION_DURATION;
		if (progressRatio > 1.0f) progressRatio = 1.0f; // Ensure the progress ratio doesn't exceed 1.0
		float easedProgress = easeInOutBack(progressRatio);
		float scaleFactor = INITIAL_SCALE + (FINAL_SCALE - INITIAL_SCALE) * easedProgress;

		ScaledResolution s = new ScaledResolution(mc);
		float guiScaleX = s.getScaledWidth() / 2.0f;
		float guiScaleY = s.getScaledHeight() / 2.0f;

		GL11.glPushMatrix();
		GL11.glTranslatef(guiScaleX, guiScaleY, 0);
		GL11.glScalef(scaleFactor, scaleFactor, 1.0f);
		GL11.glTranslatef(-guiScaleX, -guiScaleY, 0);

		for (Panel p : panels) {
			p.drawScreen(mouseX, mouseY, partialTicks);
		}

		mb = null;

		listen:
		for (Panel p : panels) {
			if (p != null && p.visible && p.extended && p.Elements != null
					&& p.Elements.size() > 0) {
				for (ModuleButton e : p.Elements) {
					if (e.listening) {
						mb = e;
						break listen;
					}
				}
			}
		}

		/*
		* Render Modules !! WIP
		**/

		for (Panel panel : panels) {
			if (panel.extended && panel.visible && panel.Elements != null) {
				for (ModuleButton b : panel.Elements) {
					if (b.extended && b.menuelements != null && !b.menuelements.isEmpty()) {

						// Settings Drawing
						double off = 0;
						for (Element e : b.menuelements) {
							e.offset = off;
							e.update();
							e.drawScreen(mouseX, mouseY, partialTicks);
							off += e.height;
						}
					}
				}
			}

		}

		GL11.glPopMatrix();

		if(mb != null){
			drawRect(0, 0, this.width, this.height, 0x88101010);
			GL11.glPushMatrix();
			GL11.glTranslatef(s.getScaledWidth() / 2F, s.getScaledHeight() / 2F, 0.0F);
			GL11.glScalef(4.0F, 4.0F, 0F);
			FontUtil.drawTotalCenteredStringWithShadow("Listening...", 0, -10, 0xffffffff);
			GL11.glScalef(0.5F, 0.5F, 0F);
			FontUtil.drawTotalCenteredStringWithShadow("Press 'ESCAPE' to unbind " + mb.mod.getName() + (mb.mod.getKey() > -1 ? " (" + Keyboard.getKeyName(mb.mod.getKey())+ ")" : ""), 0, 0, 0xffffffff);
			GL11.glScalef(0.25F, 0.25F, 0F);
			GL11.glPopMatrix();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

		if(mb != null)return;

		for (Panel panel : rpanels) {
			if (panel.extended && panel.visible && panel.Elements != null) {
				for (ModuleButton b : panel.Elements) {
					if (b.extended) {
						for (Element e : b.menuelements) {
							if (e.mouseClicked(mouseX, mouseY, mouseButton))
								return;
						}
					}
				}
			}
		}


		for (Panel p : rpanels) {
			if (p.mouseClicked(mouseX, mouseY, mouseButton))
				return;
		}


		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {

		if(mb != null)return;

		for (Panel panel : rpanels) {
			if (panel.extended && panel.visible && panel.Elements != null) {
				for (ModuleButton b : panel.Elements) {
					if (b.extended) {
						for (Element e : b.menuelements) {
							e.mouseReleased(mouseX, mouseY, state);
						}
					}
				}
			}
		}


		for (Panel p : rpanels) {
			p.mouseReleased(mouseX, mouseY, state);
		}


		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {

		if(DelClient.instance.currentTextField != null){
			DelClient.instance.currentTextField.keyTyped(typedChar, keyCode);
		}

		for (Panel p : rpanels) {
			if (p != null && p.visible && p.extended && p.Elements != null && p.Elements.size() > 0) {
				for (ModuleButton e : p.Elements) {
					try {
						if (e.keyTyped(typedChar, keyCode))return;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}


		try {
			super.keyTyped(typedChar, keyCode);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	@Override
	public void initGui() {
		// Reset Scale
		animationProgress = 0;
		// Start Blur
		if (mc.entityRenderer.isShaderActive()) {
			mc.entityRenderer.stopUseShader();
		}
		//mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
		try{
			//mc.entityRenderer.loadShader(new ResourceLocation(Reference.MODID, "shaders/blur.json"));
			mc.entityRenderer.loadShader(new ResourceLocation("shaders/blur.json"));
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onGuiClosed() {
		// End Blur
		if(mc.entityRenderer.isShaderActive()){
			mc.entityRenderer.stopUseShader();
		}

		for (Panel panel : ClickGUI.rpanels) {

			// Reset all animations
			panel.fadeOpa = 0f;
			panel.fadeButtonOpa = 0f;

			// Set all dragging to false
			if (panel.extended && panel.visible && panel.Elements != null) {
				for (ModuleButton b : panel.Elements) {
					b.currentOpacityToggle = 0;
					b.currentOpacityHover = 0;
					if (b.extended) {
						for (Element e : b.menuelements) {
							if(e instanceof ElementSlider){
								((ElementSlider)e).dragging = false;
							}
						}
					}
				}
			}
		}

		// Save Mods
		DelClient.fileManager.saveMods();
	}

	public void closeAllSettings() {
		for (Panel p : rpanels) {
			if (p != null && p.visible && p.extended && p.Elements != null
					&& p.Elements.size() > 0) {
				for (ModuleButton e : p.Elements) {
					e.extended = false;
				}
			}
		}
	}

	// [ 0 - 1 ]
	private float easeInOutBack(float t) {
		double c1 = 1.70158F;
		double c2 = c1 * 1.525F;
		return (float) (t < 0.5
						? (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
						: (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2);
	}

}
