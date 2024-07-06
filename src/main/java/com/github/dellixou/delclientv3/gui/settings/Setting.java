package com.github.dellixou.delclientv3.gui.settings;

import com.github.dellixou.delclientv3.modules.core.Module;

import java.util.ArrayList;

public class Setting {

	private String name;
	private String id;
	private Module parent;
	private String mode;

	private String sval;
	private ArrayList<String> options;

	private boolean bval;

	private double dval;
	private double min;
	private double max;
	private boolean onlyint = false;

	// Constructor for text field setting
	public Setting(String name, Module parent, String sval, String ids){
		this.name = name;
		this.parent = parent;
		this.sval = sval;
		this.mode = "Text";
		this.id = ids;
	}

	public Setting(String name, Module parent, String sval, ArrayList<String> options, String ids){
		this.name = name;
		this.parent = parent;
		this.sval = sval;
		this.options = options;
		this.mode = "Combo";
		this.id = ids;
	}

	public Setting(String name, Module parent, boolean bval, String ids){
		this.name = name;
		this.parent = parent;
		this.bval = bval;
		this.mode = "Check";
		this.id = ids;
	}

	public Setting(String name, Module parent, double dval, double min, double max, boolean onlyint, String ids){
		this.name = name;
		this.parent = parent;
		this.dval = dval;
		this.min = min;
		this.max = max;
		this.onlyint = onlyint;
		this.mode = "Slider";
		this.id = ids;
	}

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
