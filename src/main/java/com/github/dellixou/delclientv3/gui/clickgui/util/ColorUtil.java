package com.github.dellixou.delclientv3.gui.clickgui.util;

import com.github.dellixou.delclientv3.DelClient;

import java.awt.*;

public class ColorUtil {
	
	public static Color getClickGUIColor(){
		return new Color((int) DelClient.instance.settingsManager.getSettingByName("Color Red").getValDouble(), (int)DelClient.instance.settingsManager.getSettingByName("Color Green").getValDouble(), (int)DelClient.instance.settingsManager.getSettingByName("Color Blue").getValDouble());
	}
}