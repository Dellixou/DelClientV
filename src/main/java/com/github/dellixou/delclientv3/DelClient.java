package com.github.dellixou.delclientv3;

import com.github.dellixou.delclientv3.commands.*;
import com.github.dellixou.delclientv3.commands.misc.LookAtCommand;
import com.github.dellixou.delclientv3.commands.misc.OpenGUICommand;
import com.github.dellixou.delclientv3.commands.remote.CloseRemoteCommand;
import com.github.dellixou.delclientv3.commands.remote.OpenRemoteCommand;
import com.github.dellixou.delclientv3.commands.userroute.*;
import com.github.dellixou.delclientv3.gui.oldgui.ClickGUI;
import com.github.dellixou.delclientv3.gui.oldgui.elements.menu.ElementWriteBox;
import com.github.dellixou.delclientv3.gui.newgui.NewClickGUI;
import com.github.dellixou.delclientv3.modules.core.settings.SettingsManager;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.PathExecuter;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.PathExecuterConfig;
import com.github.dellixou.delclientv3.utils.misc.ClientProxy;
import com.github.dellixou.delclientv3.utils.ColorUtils;
import com.github.dellixou.delclientv3.utils.misc.CommonProxy;
import com.github.dellixou.delclientv3.utils.misc.Reference;
import com.github.dellixou.delclientv3.utils.FileUtils;
import com.github.dellixou.delclientv3.utils.misc.VersionVerifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
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
    public static NewClickGUI newClickGUI;
    public static FileUtils fileManager;
    public static Module userRoute;

    // Informations
    public double mouseXPos = 0;
    public double mouseYPos = 0;

    // Others
    public ElementWriteBox currentTextField = null;
    public String currentPlayerLocation = "";

    // Prefix
    private final String prefix = "&5[&d&lDelClient&r&5] &7: ";
    private final String prefixDebug = "&6[&f&lDelClient-Debug&r&6] &7: ";
    private final String prefixWarning = "&5[&d&lDelClient Warning&r&5] &c: ";
    private final String prefixRemote = "&8[&fDelRemote-Controller&8] &f--> &7";
    private final String prefixOld = "&dDelClient &5--> &7";
    private final String prefixDebugOld = "&cDelDebug &5--> &7";

    /*
     * Start Client Function ( Start Modules Checking / Set Application Name )
     */
    public static void startClient() throws InterruptedException {
        settingsManager = new SettingsManager();
        moduleManager = new ModuleManager();
        clickGUI = new ClickGUI();
        newClickGUI = new NewClickGUI();
        userRoute = moduleManager.userMod();

        fileManager = new FileUtils();
        fileManager.init();

        fileManager.loadRoutes();

        // Version Verifier
        try{VersionVerifier.getCurrentVersion();}catch (Exception ignored){ }
    }

    /*
     * Init Event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws InterruptedException {
        // Pathfinder
        PathExecuter executer = new PathExecuter(new PathExecuterConfig(1, 1,
                300, true));
        // Commands
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
        ClientCommandHandler.instance.registerCommand(new TestingCommand(executer));
        ClientCommandHandler.instance.registerCommand(new LookAtCommand());
        ClientCommandHandler.instance.registerCommand(new OpenRemoteCommand());
        ClientCommandHandler.instance.registerCommand(new CloseRemoteCommand());
        // Start client
        startClient();
    }

    /*
     * Pre Init Event
     */
    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event){
        proxy.registerEvents();
    }

    /*
     * Send Chat To Client
     */
    public static void sendChatToClient(String message){
        String finalMessage = instance.prefix + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

    /*
     * Send Debug
     */
    public static void sendDebug(String message){
        String finalMessage = instance.prefixDebug + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

    /*
     * Send Warning
     */
    public static void sendWarning(String message){
        String finalMessage = instance.prefixWarning + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

    /*
     * Send Warning
     */
    public static void sendRemoteChat(String message){
        String finalMessage = instance.prefixRemote + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(ColorUtils.chat(finalMessage)));
    }

}
