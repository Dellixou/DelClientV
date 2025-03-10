package com.github.dellixou.delclientv3.modules.core;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.modules.floor7.AutoLeaps;
import com.github.dellixou.delclientv3.modules.floor7.AutoPre4;
import com.github.dellixou.delclientv3.modules.macro.*;
import com.github.dellixou.delclientv3.modules.misc.RemoteControl;
import com.github.dellixou.delclientv3.modules.player.AutoGFS;
import com.github.dellixou.delclientv3.modules.movements.AutoSprint;
import com.github.dellixou.delclientv3.modules.movements.AutoWalk;
import com.github.dellixou.delclientv3.modules.movements.UserRoute;
import com.github.dellixou.delclientv3.modules.render.ClickGui;
import com.github.dellixou.delclientv3.modules.render.FOVChanger;
import com.github.dellixou.delclientv3.modules.render.HighlightBlock;
import com.github.dellixou.delclientv3.modules.render.ModuleList;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

import java.util.ArrayList;

public class ModuleManager {

    /**
     * Module manager
     **/

    private static ArrayList<Module> mods;

    public ModuleManager(){
        mods = new ArrayList<Module>();
        // MOVEMENTS
        newMod(new UserRoute());
        newMod(new AutoSprint());
        //newMod(new AutoWalk());
        // RENDER
        newMod(new ClickGui());
        newMod(new FOVChanger());
        newMod(new ModuleList());
        newMod(new HighlightBlock());
        // FLOOR 7
        //newMod(new AutoPre4());
        newMod(new AutoLeaps());
        // MACRO
        newMod(new AutoFish());
        newMod(new AutoForaging());
        // PLAYER
        newMod(new AutoGFS());
        // MISC
        newMod(new RemoteControl());
    }

    public static void newMod(Module m){
        mods.add(m);
    }

    public static ArrayList<Module> getModules(){
        return mods;
    }

    public static void onUpdate(){
        for(Module m : mods){
            if(m.getId() == "user_route") continue;
            m.onUpdate();
        }
    }

    public static void onRender(){
        for(Module m : mods){
            m.onRender();
        }
    }

    public static void onKey(int k){
        for (Module m : mods){
            if(m.getKey() == k){
                m.toggle();
            }
        }
    }

    public static Module getModuleByName(String moduleName){
        for(Module m : getModules()){
            if(!m.getName().trim().equalsIgnoreCase(moduleName) && !m.toString().equalsIgnoreCase(moduleName.trim())) continue;
            return m;
        }
        return null;
    }

    public static Module getModuleById(String id){
        for(Module m : DelClient.moduleManager.getModules()){
            if(m.getId().trim().equalsIgnoreCase(id))
            return m;
        }
        return null;
    }

    public Module userMod(){
        for (Module mod : mods){
            if(mod.getId().equalsIgnoreCase("user_route")) return mod;
        }
        return null;
    }
}
