package com.github.dellixou.delclientv3.modules.core.settings;

import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.utils.gui.animations.LinearAnimation;

import java.util.ArrayList;

public class Setting {

	private String name;
	private String id;
	private Module parent;
	private String mode;
	private String category;

	private String sval;
	private ArrayList<String> options;

	private boolean bval;

	private double dval;
	private double min;
	private double max;
	private boolean onlyint = false;

	// Check settings
	private boolean isClicked = false;
	public float xCircle = 22;
	public LinearAnimation clickAnimCircle;

	// Slider settings
	public float currentPercentBar = 0.5f;
	public float targetPercentBar;
	public float lerpSpeed = 0.1f; // Vitesse de l'animation
	public boolean dragging = false;

	// Combo settings
	public boolean isExpanded = false;
	public float currentRotationArrow = 0.0f;
	public float selectedOpacity = 0.0f;

	// Text settings
	public boolean isFocused = false;


	// Constructor for text field setting
	public Setting(String name, Module parent, String sval, String ids, String category){
		this.name = name;
		this.parent = parent;
		this.sval = sval;
		this.mode = "Text";
		this.id = ids;
		this.category = category;
	}

	public Setting(String name, Module parent, String sval, ArrayList<String> options, String ids, String category){
		this.name = name;
		this.parent = parent;
		this.sval = sval;
		this.options = options;
		this.mode = "Combo";
		this.id = ids;
		this.category = category;
	}

	public Setting(String name, Module parent, boolean bval, String ids, String category){
		this.name = name;
		this.parent = parent;
		this.bval = bval;
		this.mode = "Check";
		this.id = ids;
		this.category = category;
		this.xCircle = bval ? 14 : 22; // 17 si activé, 25 si désactivé
		this.isClicked = bval;
		refreshCheckBox();
	}

	public Setting(String name, Module parent, double dval, double min, double max, boolean onlyint, String ids, String category){
		this.name = name;
		this.parent = parent;
		this.dval = dval;
		this.min = min;
		this.max = max;
		this.onlyint = onlyint;
		this.mode = "Slider";
		this.id = ids;
		this.category = category;
	}

	// CHECK SETTINGS
	public boolean isClicked() {
		return isClicked;
	}

	public void setClicked(boolean clicked) {
		if (clicked != isClicked) {
			if (clicked) {
				clickAnimCircle = new LinearAnimation(xCircle, 14, 150, true);
			} else {
				clickAnimCircle = new LinearAnimation(xCircle, 22, 150, true);
			}
			isClicked = clicked;
			bval = clicked;
		}
	}

	public void refreshCheckBox(){
		if (bval) {
			xCircle = 14;
			isClicked = true;
			clickAnimCircle = new LinearAnimation(22, 14, 10, true);
		} else {
			xCircle = 22;
			isClicked = false;
			clickAnimCircle = new LinearAnimation(14, 22, 10, true);
		}
	}

	// OTHERS
	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public Module getParentMod(){
		return parent;
	}

	public String getValString(){
		return this.sval;
	}

	public void setValString(String in){
		this.sval = in;
	}

	public ArrayList<String> getOptions(){
		return this.options;
	}

	public boolean getValBoolean(){
		return this.bval;
	}

	public String getCategory(){
		return this.category;
	}

	public void setValBoolean(boolean in){
		this.bval = in;
	}

	public double getValDouble(){
		if(this.onlyint){
			this.dval = (int)dval;
		}
		return this.dval;
	}

	public void setValDouble(double in){
		this.dval = in;
	}

	public double getMin(){
		return this.min;
	}

	public double getMax(){
		return this.max;
	}

	public boolean isCombo(){
		return this.mode.equalsIgnoreCase("Combo");
	}

	public boolean isCheck(){
		return this.mode.equalsIgnoreCase("Check");
	}

	public boolean isSlider(){
		return this.mode.equalsIgnoreCase("Slider");
	}

	public boolean isText() {
		return this.mode.equalsIgnoreCase("Text");
	}

	public boolean onlyInt(){
		return this.onlyint;
	}
}
