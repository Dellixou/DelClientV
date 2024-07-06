package com.github.dellixou.delclientv3.modules.core;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.client.Minecraft;

public class Module {

    /**
     * Class of a module
     **/

    protected Minecraft mc = Minecraft.getMinecraft();
    private String name;
    private String id;
    public int key;
    private boolean toggled;
    private Category category;
    public boolean applyOnChange = true;

    public Module(String nm, int k, Category c, Boolean apc, String id){
        name = nm;
        key = k;
        category = c;
        toggled = false;
        applyOnChange = apc;
        this.id = id;
        setup();
    }

    public void toggle(){
        toggled = !toggled;
        if(toggled){
            onEnable();
        }else{
            onDisable();
        }
    }

    public void onEnable(){}
    public void onDisable(){}
    public void onUpdate(){}
    public void onRender(){}
    public void setup(){}

    public Minecraft getMc() {
        return mc;
    }

    public void setMc(Minecraft mc) {
        this.mc = mc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getId(){
        return id;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public Category getCategory(){
        return category;
    }

    public void setCategory(Category category){
        this.category = category;
    }

    public void enableOnStartUp(){
        toggled = true;
        try{
            toggle();
            onEnable();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean hasSettings(){
        return DelClient.settingsManager.getSettingsByMod(this).size() >= 1;
    }

}
