package com.github.dellixou.delclientv3;

import com.github.dellixou.delclientv3.commands.*;
import com.github.dellixou.delclientv3.commands.userroute.*;
import com.github.dellixou.delclientv3.events.chats.ChatServerNameEvent;
import com.github.dellixou.delclientv3.events.misc.MainMenuEventHandler;
import com.github.dellixou.delclientv3.gui.clickgui.ClickGUI;
import com.github.dellixou.delclientv3.gui.clickgui.elements.menu.ElementWriteBox;
import com.github.dellixou.delclientv3.gui.settings.SettingsManager;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.ClientProxy;
import com.github.dellixou.delclientv3.utils.Color.ColorUtils;
import com.github.dellixou.delclientv3.utils.CommonProxy;
import com.github.dellixou.delclientv3.utils.Reference;
import com.github.dellixou.delclientv3.utils.extensions.FileManager;
import com.github.dellixou.delclientv3.utils.misc.RouteLoader;
import com.github.dellixou.delclientv3.utils.misc.VersionVerifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "delclientv", useMetadata=true)
public class DelClient {

    // Classes Main
    public static DelClient instance = new DelClient();

    @SidedProxy(serverSide = Reference.SERVER_PROXY_CLASS, clientSide = Reference.CLIENT_PROXY_CLASS)
    public static CommonProxy proxy;
    public static ClientProxy clientProxy;

    // Managers
    public static SettingsManager settingsManager;
    public static ModuleManager moduleManager;
    public static ClickGUI clickGUI;
    public static FileManager fileManager;
    public static Module userRoute;
    public static RouteLoader routeLoader;

    // Events
    public ChatServerNameEvent chatServerNameEvent = new ChatServerNameEvent();

    // Informations
    public double mouseXPos = 0;
    public double mouseYPos = 0;
    private boolean isAuthorized = false;

    // Others
    public ElementWriteBox currentTextField = null;
    public String currentPlayerLocation = "";

    // Prefix
    private final String prefix = "&5[&d&lDelClient&r&5] &7: ";
    private final String prefixDebug = "&6[&f&lDelClient-Debug&r&6] &7: ";
    private final String prefixOld = "&dDelClient &5--> &7";
    private final String prefixDebugOld = "&cDelDebug &5--> &7";

    /**
     * Start Client Function ( Start Modules Checking / Set Application Name )
     **/
    public static void startClient(){
        settingsManager = new SettingsManager();
        moduleManager = new ModuleManager();
        clickGUI = new ClickGUI();
        userRoute = moduleManager.userMod();

        fileManager = new FileManager();
        fileManager.init();

        routeLoader = new RouteLoader();
        //routeLoader.loadRoutes();
        fileManager.loadRoutes();

        try{
            VersionVerifier.getCurrentVersion();
        }catch (Exception ignored){ }
    }

    /**
     * Init Event
     **/
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // COMMANDS
        ClientCommandHandler.instance.registerCommand(new OpenGUICommand());
        ClientCommandHandler.instance.registerCommand(new PlaceWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new ResetRouteCommand());
        ClientCommandHandler.instance.registerCommand(new LookWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new ClickWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new RemoveLastWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new StartWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new SwitchRouteCommand());
        ClientCommandHandler.instance.registerCommand(new SaveRoutesCommand());
        ClientCommandHandler.instance.registerCommand(new LoadRoutesCommand());
        ClientCommandHandler.instance.registerCommand(new ShowRoutesCommand());
        ClientCommandHandler.instance.registerCommand(new JumpWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new BonzoWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new WaitWaypointCommand());
        ClientCommandHandler.instance.registerCommand(new TestCommand());
        ClientCommandHandler.instance.registerCommand(new LookAtCommand());
        // MAIN CLIENT
        startClient();
        // MENU
        MinecraftForge.EVENT_BUS.register(new MainMenuEventHandler());
        MinecraftForge.EVENT_BUS.register(instance.chatServerNameEvent);

    }

    /**
     * Pre Init Event
     **/
    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event){
        proxy.registerEvents();
    }

    /**
     * Send Chat To Client
     **/
    public static void sendChatToClient(String message){
        String finalMessage = instance.prefix + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

    /**
     * Send Debug
     **/
    public static void sendDebug(String message){
        String finalMessage = instance.prefixDebug + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

    /**
     * Set is authorized
     **/
    public void setIsAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
    }

    /**
     * Get is authorized
     **/
    public boolean getIsAuthorized(){
        return isAuthorized;
    }

}
