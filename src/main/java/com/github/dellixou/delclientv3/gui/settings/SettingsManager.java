package com.github.dellixou.delclientv3.gui.settings;


import com.github.dellixou.delclientv3.modules.core.Module;

import java.util.ArrayList;

public class SettingsManager {
	
	private ArrayList<Setting> settings;
	
	public SettingsManager(){
		this.settings = new ArrayList<Setting>();
	}
	
	public void rSetting(Setting in){
		this.settings.add(in);
	}
	
	public ArrayList<Setting> getSettings(){
		return this.settings;
	}
	
	public ArrayList<Setting> getSettingsByMod(Module mod){
		ArrayList<Setting> out = new ArrayList<Setting>();
		for(Setting s : getSettings()){
			if(s.getParentMod().equals(mod)){
				out.add(s);
			}
		}
		if(out.isEmpty()){
			return out;
		}
		return out;
	}
	
	public Setting getSettingByName(String name){
		for(Setting set : getSettings()){
			if(set.getName().equalsIgnoreCase(name)){
				return set;
			}
		}
		System.err.println("[ TDC ] Error Setting NOT found: '" + name +"'!");
		return null;
	}

	public Setting getSettingByModAndName(Module mod, String name){
		for(Setting s : getSettings()){
			if(s.getParentMod().equals(mod) && s.getName().equalsIgnoreCase(name)){
				return s;
			}
		}
		System.err.println("[ TDC ] Error Setting NOT found: '" + name +"'!");
		return null;
	}

	public Setting getSettingById(String id){
		for(Setting set : getSettings()){
			if(set.getId().equalsIgnoreCase(id)){
				return set;
			}
		}
		System.err.println("[ TDC ] Error Setting NOT found: '" + id +"'!");
		return null;
	}

	public ArrayList<String> getAllSettingsCategory(Module mod){
		ArrayList<String> out = new ArrayList<String>();
		for(Setting s : getSettings()){
			if(s.getParentMod().equals(mod)){
				if(!out.contains(s.getCategory())){
					out.add(s.getCategory());
				}
			}
		}
		if(out.isEmpty()){
			return null;
		}
		return out;
	}

	public ArrayList<Setting> getSettingsByModAndCategory(Module mod, String category){
		ArrayList<Setting> out = new ArrayList<Setting>();
		for(Setting s : getSettings()){
			if(s.getParentMod().equals(mod) && s.getCategory().equalsIgnoreCase(category)){
				out.add(s);
			}
		}
		if(out.isEmpty()){
			return null;
		}
		return out;
	}
}