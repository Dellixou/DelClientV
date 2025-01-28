package com.github.dellixou.delclientv3.utils.remote;

import com.github.dellixou.delclientv3.DelClient;
import com.github.dellixou.delclientv3.events.chats.ChatRemoteListenerEvent;
import com.github.dellixou.delclientv3.modules.core.Module;
import com.github.dellixou.delclientv3.modules.core.ModuleManager;
import com.github.dellixou.delclientv3.utils.*;
import com.github.dellixou.delclientv3.utils.movements.RotationUtils;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.*;
import com.github.dellixou.delclientv3.utils.pathfinding.newpathfinding.enums.NodePickStyle;
import com.github.dellixou.delclientv3.utils.remote.enums.ActionState;
import com.github.dellixou.delclientv3.utils.remote.enums.StatusRemote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nonnull;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordEventListener extends ListenerAdapter {

    private final Minecraft mc = Minecraft.getMinecraft();
    private PathExecuter executer = new PathExecuter(new PathExecuterConfig(0.6, 1, 500, true));
    private AStarPathfinder instance;
    public WorldProvider world;

    public ActionState currentAction = ActionState.IDLE;

    private ScheduledExecutorService chatUpdateExecutor;
    private boolean isChatUpdateActive = false;

    private ScheduledExecutorService dashboardUpdateExecutor;
    private boolean isDashboardUpdateActive = false;

    private ScheduledExecutorService movementUpdateExecutor;
    private boolean isMovementUpdateActive = false;

    private ScheduledExecutorService macroUpdateExecutor;
    private boolean isMacroUpdateActive = false;

    /*
     * Events
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;
        if (!event.getAuthor().getId().equalsIgnoreCase(DiscordBotInstance.ownerID)) return;
        // Is from guild?
        if (!event.isFromType(ChannelType.TEXT)) return;

        String message = event.getMessage().getContentRaw();
        if (message.startsWith("del$")) {
            String[] parts = message.substring(4).split("\\s+", 2);
            String command = parts[0];

            switch (command){
                case "dashboard":
                    dashboardCommand(event);
                    break;
                case "help":
                    helpCommand(event);
                    break;
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        if (!event.getUser().getId().equalsIgnoreCase(DiscordBotInstance.ownerID)) return;

        EmbedBuilder movementEmbed;
        EmbedBuilder dashboardEmbed;
        EmbedBuilder chatEmbed;
        EmbedBuilder macroEmbed;
        boolean isRemote = DiscordBotInstance.getStatusRemote() == StatusRemote.REMOTE;
        boolean cantAction = mc.thePlayer == null || !isRemote;

        if (event.getComponentId().equals("refresh")) {
            dashboardEmbed = getDashboardEmbed(event);
            event.editMessageEmbeds(dashboardEmbed.build()).setComponents(event.getMessage().getComponents()).queue();
        }

        if (event.getComponentId().equals("refresh_mov")) {
            movementEmbed = getMovementEmbed(event);
            event.editMessageEmbeds(movementEmbed.build()).setComponents(event.getMessage().getComponents()).queue();
        }

        if (event.getComponentId().equals("toggle_remote")) {
            if (isRemote) {
                DelClient.sendRemoteChat("You are no longer able to control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.NO_REMOTE);
                UngrabUtils.regrabMouse();
            } else {
                DelClient.sendRemoteChat("You can now control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.REMOTE);
                UngrabUtils.ungrabMouse();
            }
            dashboardEmbed = getDashboardEmbed(event);
            event.editMessageEmbeds(dashboardEmbed.build()).queue(
                    hook -> startDashboardUpdateTask(event.getChannel(), event.getMessage(), event)
            );
        }

        if (event.getComponentId().equals("toggle_remote_mov")) {
            if (isRemote) {
                DelClient.sendRemoteChat("You are no longer able to control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.NO_REMOTE);
                UngrabUtils.regrabMouse();
            } else {
                DelClient.sendRemoteChat("You can now control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.REMOTE);
                UngrabUtils.ungrabMouse();
            }
            movementEmbed = getMovementEmbed(event);

            // We do that because we need to update the string select menu to disabled or not
            event.editMessageEmbeds(movementEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                                    Button.primary("toggle_remote_mov", "Toggle Remote")),
                            ActionRow.of(StringSelectMenu.create("movement-action")
                                    .addOptions(SelectOption.of("Move", "goto")
                                            .withDescription("Move your player with coords (X, Y, Z).")
                                            .withEmoji(Emoji.fromUnicode("🚗")))
                                    .addOptions(SelectOption.of("Rotate", "rotate")
                                            .withDescription("Rotate your player with yaw and pitch values.")
                                            .withEmoji(Emoji.fromUnicode("↩️")))
                                    .addOptions(SelectOption.of("Start sneaking", "startsneak")
                                            .withDescription("Make your player hold sneak.")
                                            .withEmoji(Emoji.fromUnicode("🧎")))
                                    .addOptions(SelectOption.of("Stop sneaking", "stopsneak")
                                            .withDescription("Make your player stop sneaking.")
                                            .withEmoji(Emoji.fromUnicode("🧍")))
                                    .setMaxValues(1)
                                    .setMinValues(0)
                                    .build()).withDisabled(!cantAction)
                    ).queue();
        }

        if (event.getComponentId().equals("toggle_remote_chat")) {
            if (isRemote) {
                DelClient.sendRemoteChat("You are no longer able to control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.NO_REMOTE);
                UngrabUtils.regrabMouse();
            } else {
                DelClient.sendRemoteChat("You can now control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.REMOTE);
                UngrabUtils.ungrabMouse();
            }
            chatEmbed = getChatEmbed(event);
            event.editMessageEmbeds(chatEmbed.build()).setComponents(
                    ActionRow.of(
                            Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                            Button.secondary("chat_send", Emoji.fromUnicode("➕").getFormatted() + " Send Chat").withDisabled(isRemote),
                            Button.primary("toggle_remote_chat", "Toggle Remote"))
            ).queue();
        }

        if (event.getComponentId().equals("toggle_remote_macro")) {
            if (isRemote) {
                DelClient.sendRemoteChat("You are no longer able to control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.NO_REMOTE);
                UngrabUtils.regrabMouse();
            } else {
                DelClient.sendRemoteChat("You can now control your player with your BOT.");
                DiscordBotInstance.setStatusRemote(StatusRemote.REMOTE);
                UngrabUtils.ungrabMouse();
            }
            macroEmbed = getMacroEmbed(event);
            event.editMessageEmbeds(macroEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                                    Button.primary("toggle_remote_macro", "Toggle Remote"),
                                    Button.danger("stop_macro", "Stop Macro")),
                            ActionRow.of(StringSelectMenu.create("macro-action")
                                    .addOptions(SelectOption.of("Start foraging", "macro-foraging")
                                            .withDescription("Make your player cut logs in hub or park.")
                                            .withEmoji(Emoji.fromUnicode("\uD83E\uDE93")))
                                    .addOptions(SelectOption.of("Start fishing", "macro-fishing")
                                            .withDescription("Make your player fish in hub or park or crimson.")
                                            .withEmoji(Emoji.fromUnicode("\uD83C\uDFA3")))
                                    .setMaxValues(1)
                                    .setMinValues(0)
                                    .build()).withDisabled(!cantAction)
                    ).queue();
        }

        if (event.getComponentId().equals("back")) {
            stopChatUpdateTask();
            stopMovementUpdateTask();
            stopMacroUpdateTask();

            dashboardEmbed = getDashboardEmbed(event);
            event.editMessageEmbeds(dashboardEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.secondary("movements", Emoji.fromUnicode("🏃").getFormatted() + " Movements"),
                                    Button.secondary("macros", Emoji.fromUnicode("♻️").getFormatted() + " Macro(s)"),
                                    Button.secondary("chat", Emoji.fromUnicode("💬").getFormatted() + " Game Chat")
                            ),
                            ActionRow.of(
                                    Button.primary("toggle_remote", "Toggle Remote"),
                                    Button.success("screenshot", Emoji.fromUnicode("📷").getFormatted() + " Screenshot"),
                                    Button.danger("live", Emoji.fromUnicode("💻").getFormatted() + " Live (LAGGY)")
                            )
                    ).queue(hook -> startDashboardUpdateTask(event.getChannel(), event.getMessage(), event));

            DiscordBotInstance.listeningGameChat = false;
        }

        if (event.getComponentId().equals("movements")) {
            stopDashboardUpdateTask();
            stopChatUpdateTask();
            movementEmbed = getMovementEmbed(event);
            event.editMessageEmbeds(movementEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                                    Button.primary("toggle_remote_mov", "Toggle Remote")
                            ),
                            ActionRow.of(StringSelectMenu.create("movement-action")
                                    .addOptions(SelectOption.of("Move", "goto")
                                            .withDescription("Move your player with coords (X, Y, Z).")
                                            .withEmoji(Emoji.fromUnicode("🚗")))
                                    .addOptions(SelectOption.of("Rotate", "rotate")
                                            .withDescription("Rotate your player with yaw and pitch values.")
                                            .withEmoji(Emoji.fromUnicode("↩️")))
                                    .addOptions(SelectOption.of("Start sneaking", "startsneak")
                                            .withDescription("Make your player hold sneak.")
                                            .withEmoji(Emoji.fromUnicode("🧎")))
                                    .addOptions(SelectOption.of("Stop sneaking", "stopsneak")
                                            .withDescription("Make your player stop sneaking.")
                                            .withEmoji(Emoji.fromUnicode("🧍")))
                                    .setMaxValues(1)
                                    .setMinValues(0)
                                    .build()).withDisabled(cantAction)
                    ).queue(interactionHook ->
                            startMovementUpdateTask(event.getChannel(), event.getMessage(), event)
                    );
        }

        if (event.getComponentId().equals("chat")) {
            stopDashboardUpdateTask();
            chatEmbed = getChatEmbed(event);
            event.editMessageEmbeds(chatEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                                    Button.secondary("chat_send", Emoji.fromUnicode("➕").getFormatted() + " Send Chat").withDisabled(!isRemote),
                                    Button.primary("toggle_remote_chat", "Toggle Remote")
                            )
                    ).queue(hook -> startEmbedUpdateTask(event.getChannel(), hook, event));
            DiscordBotInstance.listeningGameChat = true;
        }

        if (event.getComponentId().equals("chat_send")) {

            TextInput content = TextInput.create("content", "Content (add " + '/' + " to make a command", TextInputStyle.SHORT).setPlaceholder("This is a message from Discord!").setMinLength(1).setMaxLength(80).build();
            Modal modal = Modal.create("chat_send", "Send a message/command in the game chat.").addComponents(ActionRow.of(content)).build();

            event.replyModal(modal).queue();
        }

        if (event.getComponentId().equals("live")) {
            event.deferReply().queue();
            DiscordGameCapture.startScreenshotUpdates(event);
        }

        if (event.getComponentId().equals("screenshot")) {

            event.deferReply().queue();

            DiscordGameCapture.sendScreenshotEmbed(event);

        }

        if (event.getComponentId().equals("close_embed")) {
            event.deferEdit().queue();

            event.getHook().deleteOriginal().queue();
        }

        if(event.getComponentId().equals("reload_dash")){
            stopChatUpdateTask();
            stopMovementUpdateTask();

            dashboardEmbed = getDashboardEmbed(event);

            event.editMessageEmbeds(dashboardEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.secondary("movements", Emoji.fromUnicode("🏃").getFormatted() + " Movements"),
                                    Button.secondary("macros", Emoji.fromUnicode("♻️").getFormatted() + " Macro(s)"),
                                    Button.secondary("chat", Emoji.fromUnicode("💬").getFormatted() + " Game Chat")
                            ),
                            ActionRow.of(
                                    Button.primary("toggle_remote", "Toggle Remote"),
                                    Button.success("screenshot", Emoji.fromUnicode("📷").getFormatted() + " Screenshot"),
                                    Button.danger("live", Emoji.fromUnicode("💻").getFormatted() + " Live (LAGGY)")
                            )
                    ).queue(hook -> {
                        // Check if there's an existing dashboard message
                        if (DiscordBotInstance.dashboardMessage != null) {
                            try {
                                // Edit the existing dashboard to show it's unloaded
                                DiscordBotInstance.editDashToUnloadEmbed(DiscordBotInstance.getJda(),
                                        DiscordBotInstance.dashboardMessage.getChannelId(),
                                        DiscordBotInstance.dashboardMessage.getId());
                            } catch (Exception ignored) { }
                        }
                        DiscordBotInstance.dashboardMessage = event.getMessage();
                        DelClient.fileManager.saveDashboardMessage(event.getMessage().getId(), event.getMessage().getChannel().getId());
                        startDashboardUpdateTask(event.getChannel(), event.getMessage(), event);
                    });

            DiscordBotInstance.listeningGameChat = false;
        }

        if (event.getComponentId().equals("macros")) {
            stopDashboardUpdateTask();
            macroEmbed = getMacroEmbed(event);
            event.editMessageEmbeds(macroEmbed.build())
                    .setComponents(
                            ActionRow.of(
                                    Button.primary("back", Emoji.fromUnicode("◀️").getFormatted() + " Back"),
                                    Button.primary("toggle_remote_macro", "Toggle Remote"),
                                    Button.danger("stop_macro", "Stop Macro")),
                            ActionRow.of(StringSelectMenu.create("macro-action")
                                    .addOptions(SelectOption.of("Start foraging", "macro-foraging")
                                            .withDescription("Make your player cut logs in hub or park.")
                                            .withEmoji(Emoji.fromUnicode("\uD83E\uDE93")))
                                    .addOptions(SelectOption.of("Start fishing", "macro-fishing")
                                            .withDescription("Make your player fish in hub or park or crimson.")
                                            .withEmoji(Emoji.fromUnicode("\uD83C\uDFA3")))
                                    .setMaxValues(1)
                                    .setMinValues(0)
                                    .build()).withDisabled(cantAction)
                    ).queue(interactionHook ->
                            startMacroUpdateTask(event.getChannel(), event.getMessage(), event)
                    );
        }

        if (event.getComponentId().equals("stop_macro")) {
            event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("You just stopped your current macro!", DiscordBotInstance.successColor).build())
                    .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

            currentAction = ActionState.IDLE;

            Module macro;
            if(currentAction.equals(ActionState.FORAGING)){
                macro = ModuleManager.getModuleById("auto_fora");
                if(macro.isToggled()){
                    macro.toggle();
                }
            }
            if(currentAction.equals(ActionState.FISHING)){
                macro = ModuleManager.getModuleById("auto_fish");
                if(macro.isToggled()){
                    macro.toggle();
                }
            }
        }

    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        if (!event.getUser().getId().equalsIgnoreCase(DiscordBotInstance.ownerID)) return;

        // MOVEMENT PANEL
        if (event.getComponentId().equals("movement-action")) {
            event.getMessage().editMessageEmbeds(event.getMessage().getEmbeds()).setComponents(event.getMessage().getComponents()).queue();

            // Move the player to a point
            if(event.getValues().get(0).equals("goto")){

                TextInput x = TextInput.create("x", "Coords X", TextInputStyle.SHORT).setPlaceholder(String.valueOf(mc.thePlayer.getPosition().getX())).setMinLength(1).setMaxLength(10).build();
                TextInput y = TextInput.create("y", "Coords Y", TextInputStyle.SHORT).setPlaceholder(String.valueOf(mc.thePlayer.getPosition().getY())).setMinLength(1).setMaxLength(10).build();
                TextInput z = TextInput.create("z", "Coords Z", TextInputStyle.SHORT).setPlaceholder(String.valueOf(mc.thePlayer.getPosition().getZ())).setMinLength(1).setMaxLength(10).build();
                Modal modal = Modal.create("goto", "Move to coordinates.").addComponents(ActionRow.of(x), ActionRow.of(y), ActionRow.of(z)).build();

                event.replyModal(modal).queue();
            }
            // Rotate the player with yaw and pitch
            if(event.getValues().get(0).equals("rotate")){

                TextInput yaw = TextInput.create("yaw", "Yaw", TextInputStyle.SHORT).setPlaceholder(String.valueOf(Math.round(RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) * 100.0) / 100.0)).setMinLength(1).setMaxLength(10).build();
                TextInput pitch = TextInput.create("pitch", "Pitch", TextInputStyle.SHORT).setPlaceholder(String.valueOf(Math.round(RotationUtils.wrapAngleTo180(mc.thePlayer.rotationPitch) * 100.0) / 100.0)).setMinLength(1).setMaxLength(10).build();
                TextInput tick = TextInput.create("tick", "Tick to rotate (20 = 1 second)", TextInputStyle.SHORT).setPlaceholder("10").setMinLength(1).setMaxLength(10).build();
                Modal modal = Modal.create("rotate", "Rotate with yaw and pitch.").addComponents(ActionRow.of(yaw), ActionRow.of(pitch), ActionRow.of(tick)).build();

                event.replyModal(modal).queue();
            }
            // Start sneaking
            if(event.getValues().get(0).equals("startsneak")){
                // Close screen when moving
                if(mc.currentScreen != null){
                    InventoryUtils.closeCurrentScreen();
                }

                event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("Player is sneaking!", DiscordBotInstance.successColor).build())
                        .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
            // Stop sneaking
            if(event.getValues().get(0).equals("stopsneak")){
                // Close screen when moving
                if(mc.currentScreen != null){
                    InventoryUtils.closeCurrentScreen();
                }

                event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("Player stopped sneaking!", DiscordBotInstance.successColor).build())
                        .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }

        }

        // MACRO PANEL
        if (event.getComponentId().equals("macro-action")){
            event.getMessage().editMessageEmbeds(event.getMessage().getEmbeds()).setComponents(event.getMessage().getComponents()).queue();

            if(currentAction.equals(ActionState.FORAGING) || currentAction.equals(ActionState.FISHING)){
                event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("You are not able to enable a `Macro` since you already in one.", DiscordBotInstance.errorColor).build())
                        .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
                return;
            }

            // Macro Foraging
            if(event.getValues().get(0).equals("macro-foraging")){
                currentAction = ActionState.FORAGING;
                Module autoForaging = ModuleManager.getModuleById("auto_fora");
                if(!autoForaging.isToggled()){
                    autoForaging.toggle();
                    event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("You started `Macro Foraging`", DiscordBotInstance.successColor).build())
                            .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
                }
            }

            // Macro Fishing
            if(event.getValues().get(0).equals("macro-fishing")){
                currentAction = ActionState.FISHING;
                Module autoFishing = ModuleManager.getModuleById("auto_fish");
                if(!autoFishing.isToggled()){
                    autoFishing.toggle();
                    event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("You started `Macro Fishing`", DiscordBotInstance.successColor).build())
                            .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
                }
            }
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {

        if (!event.getUser().getId().equalsIgnoreCase(DiscordBotInstance.ownerID)) return;

        // Move the player to a point
        if (event.getModalId().equals("goto")) {
            int x = Integer.parseInt(Objects.requireNonNull(event.getValue("x")).getAsString());
            int y = Integer.parseInt(Objects.requireNonNull(event.getValue("y")).getAsString());
            int z = Integer.parseInt(Objects.requireNonNull(event.getValue("z")).getAsString());

            DelClient.sendRemoteChat("Moving player to : X = " + x + " Y = " + y + " Z = " + z);

            instance = new AStarPathfinder();
            world = new WorldProvider();

            Node path = instance.calculate1(world, NodePickStyle.SIDES, 2000, new int[]{mc.thePlayer.getPosition().getX(), mc.thePlayer.getPosition().getY(),
                    mc.thePlayer.getPosition().getZ()}, new int[]{x, y, z});

            if (path == null) {
                event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("Player didn't find any path to go at that point... FAILED", DiscordBotInstance.errorColor).build())
                        .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
            }else{
                event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("Player is moving!", DiscordBotInstance.successColor).build())
                        .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

                int randomTickRotate = MathUtils.getRandomNumber(8, (int) (8 * 1.1f));
                BlockPos goal = new BlockPos(x, y, z);

                // Close screen when moving
                if(mc.currentScreen != null){
                    InventoryUtils.closeCurrentScreen();
                }

                executer.begin(PathExecuter.cutPath(path.toStack(), world), 0.3f, 1, randomTickRotate, goal, (int) 30, goal, () ->{
                    currentAction = ActionState.IDLE;
                });

                currentAction = ActionState.WALKING;
            }

        }

        // Rotate the player with yaw and pitch
        if (event.getModalId().equals("rotate")){
            float yaw = Float.parseFloat(Objects.requireNonNull(event.getValue("yaw")).getAsString());
            float pitch = Float.parseFloat(Objects.requireNonNull(event.getValue("pitch")).getAsString());
            int tick = Integer.parseInt(Objects.requireNonNull(event.getValue("tick")).getAsString());

            DelClient.sendRemoteChat("Rotating player to : YAW = " + yaw + " PITCH = " + pitch);

            event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("Player is rotating!", DiscordBotInstance.successColor).build())
                    .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

            // Close screen when moving
            if(mc.currentScreen != null){
                InventoryUtils.closeCurrentScreen();
            }

            RotationUtils.smoothLook(new RotationUtils.Rotation(pitch, yaw), tick, () -> {
                currentAction = ActionState.IDLE;
            });

            currentAction = ActionState.ROTATING;
        }

        // Send message/command in game chat
        if (event.getModalId().equals("chat_send")){
            String content = Objects.requireNonNull(event.getValue("content")).getAsString();

            DelClient.sendRemoteChat("You just sent a message in game chat!");

            event.replyEmbeds(DelClientEmbedBuilder.simpleEmbed("You just sent a message in game chat : \n```diff\n" + "+ " + content + "\n```", DiscordBotInstance.successColor).build())
                    .queue(message -> message.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));

            // Close screen when moving
            if(mc.currentScreen != null){
                InventoryUtils.closeCurrentScreen();
            }

            mc.thePlayer.sendChatMessage(content);
        }

    }

    // ------------------------------------------------ COMMANDS ------------------------------------------------

    private void dashboardCommand(MessageReceivedEvent event) {
        EmbedBuilder dashboardEmbed = getDashboardEmbed(event);

        event.getMessage().getChannel().sendMessageEmbeds(dashboardEmbed.build())
                .addActionRow(
                        Button.secondary("movements", Emoji.fromUnicode("🏃").getFormatted() + " Movements"),
                        Button.secondary("macros", Emoji.fromUnicode("♻️").getFormatted() + " Macro(s)"),
                        Button.secondary("chat", Emoji.fromUnicode("💬").getFormatted() + " Game Chat")
                )
                .addActionRow(
                        Button.primary("toggle_remote", "Toggle Remote"),
                        Button.success("screenshot", Emoji.fromUnicode("📷").getFormatted() + " Screenshot"),
                        Button.danger("live", Emoji.fromUnicode("💻").getFormatted() + " Live (LAGGY)")
                )
                .queue(message -> {
                    try{
                        if (DiscordBotInstance.dashboardMessage != null) {
                            DiscordBotInstance.editDashToUnloadEmbed(DiscordBotInstance.getJda(), DiscordBotInstance.dashboardMessage.getChannelId(), DiscordBotInstance.dashboardMessage.getId());
                        }
                    }catch (Exception ignored) {}

                    DiscordBotInstance.dashboardMessage = message;

                    startDashboardUpdateTask(message.getChannel(), message, event);

                    DelClient.fileManager.saveDashboardMessage(message.getId(), message.getChannel().getId());
                });

        try{
            event.getMessage().delete().queue();
        }catch (Exception ignored) { }
    }

    private void helpCommand(MessageReceivedEvent event){
        EmbedBuilder helpEmbed = new EmbedBuilder();

        helpEmbed.setAuthor("Help Commands Remote Controller: ");
        helpEmbed.setTitle("Current prefix is : `del$<your-command>`");

        helpEmbed.addField("See the dashboard : ", "`dashboard`", false);
        helpEmbed.addField("Get help : ", "`help`", false);

        helpEmbed.setFooter("Don't put the characters when commanding : <, >, /");

        event.getMessage().reply("").setEmbeds(helpEmbed.build()).queue();
    }

    // ------------------------------------------------ EMBEDS ------------------------------------------------

    @NotNull
    private EmbedBuilder getDashboardEmbed(Event event){
        boolean isRemote = DiscordBotInstance.getStatusRemote() == StatusRemote.REMOTE;
        EmbedBuilder dashboardEmbed;

        dashboardEmbed = new EmbedBuilder();

        String authorName = mc.thePlayer == null ? "?" : mc.thePlayer.getName();

        if(mc.thePlayer != null){
            String authorUrl = "https://mc-heads.net/head/" + mc.thePlayer.getUniqueID();
            dashboardEmbed.setAuthor("Dashboard Controller : " + authorName, "https://discord.gg/fqfNvVwP5u", authorUrl);
        }

        String status = isRemote ? "`Connected " + Emoji.fromUnicode("✅").getFormatted() + "`" : "`Disconnected " + Emoji.fromUnicode("❌").getFormatted() + "`";
        dashboardEmbed.setTitle("Current state of remote control: " + status);
        dashboardEmbed.setColor(new Color(49, 98, 9, 255));


        if(mc.thePlayer != null){
            dashboardEmbed.addField("Zone Infos : ", "`" + Emoji.fromUnicode("🗺️").getFormatted() + "`" + " Current zone: `" + SkyblockUtils.getZoneNameFromEnum(SkyblockUtils.getCurrentZone()) + "`\n" + "`" +
                    Emoji.fromUnicode("📌").getFormatted() + "`" + " Current sub-zone: `" + SkyblockUtils.getCurrentArea() + "`" , false);

            String heldItem = mc.thePlayer.getHeldItem() == null ? "No item held" : ColorUtils.cleanMinecraftText(mc.thePlayer.getHeldItem().getDisplayName() + "` | `x" + mc.thePlayer.getHeldItem().stackSize);
            ActionBarReader.PlayerStats playerStats = SkyblockUtils.getPlayerStatsActionBar();
            int healthPercent = playerStats.health.equals("N/A") || playerStats.maxHealth.equals("N/A") ? 0 : (int)Math.ceil((Float.parseFloat(playerStats.health)/Float.parseFloat(playerStats.maxHealth))*100);
            int manaPercent = playerStats.health.equals("N/A") || playerStats.maxHealth.equals("N/A") ? 0 : (int)Math.ceil((Float.parseFloat(playerStats.mana)/Float.parseFloat(playerStats.maxMana))*100);
            String healthPercentStr = playerStats.health.equals("N/A") || playerStats.maxHealth.equals("N/A") ? " (N/A)" : " (" + healthPercent + "%)";
            String manaPercentStr = playerStats.mana.equals("N/A") || playerStats.maxMana.equals("N/A") ? " (N/A)" : " (" + manaPercent + "%)";

            dashboardEmbed.addField("Player Infos : ", "`" + Emoji.fromUnicode("\uD83C\uDF92").getFormatted() + "`" + " Held item: `" + heldItem + "`\n"
                    + "`" + Emoji.fromUnicode("\uD83D\uDC89").getFormatted() + "`" + " Current health: `" + playerStats.health + "/" + playerStats.maxHealth + healthPercentStr + "`\n"
                    + "`" + Emoji.fromUnicode("🛡️").getFormatted() + "`" + " Current defense: `" + playerStats.defense + "`\n"
                    + "`" + Emoji.fromUnicode("🔵").getFormatted() + "`" + " Current mana: `" + playerStats.mana + "/" + playerStats.maxMana + manaPercentStr + "`\n", false);

            if(currentAction == ActionState.MENU){
                currentAction = ActionState.IDLE;
            }

        } else {
            dashboardEmbed.setDescription("Player data not `available`");
            currentAction = ActionState.MENU;
        }

        dashboardEmbed.addField("Action State : ", "`" + Emoji.fromUnicode("\uD83C\uDFAF").getFormatted() + "`" + " Current action: `" + currentAction + "`\n" + "`" +
                Emoji.fromUnicode("⏱").getFormatted() + "`" + " Timer: `" + "Unknown" + "`" , false);

        dashboardEmbed.setFooter("DelClient Remote Controller - Version 0.1");
        return dashboardEmbed;
    }

    @NotNull
    private EmbedBuilder getMovementEmbed(Event event) {
        boolean isRemote = DiscordBotInstance.getStatusRemote() == StatusRemote.REMOTE;
        EmbedBuilder movementEmbed = new EmbedBuilder();

        String authorName = mc.thePlayer == null ? "?" : mc.thePlayer.getName();

        if(mc.thePlayer != null){
            String authorUrl = "https://mc-heads.net/head/" + mc.thePlayer.getUniqueID();
            movementEmbed.setAuthor("Dashboard Controller : " + authorName, "https://discord.gg/fqfNvVwP5u", authorUrl);
        }

        String status = isRemote ? "`Connected " + Emoji.fromUnicode("✅").getFormatted() + "`" : "`Disconnected " + Emoji.fromUnicode("❌").getFormatted() + "`";
        movementEmbed.setTitle("Current state of remote control: " + status);

        movementEmbed.setColor(new Color(22, 102, 154));

        if (mc.thePlayer != null) {

            movementEmbed.setDescription("You can do different **action** by searching in the `select menu` just below!");

            double x = Math.round(mc.thePlayer.posX * 100.0) / 100.0;
            double y = Math.round(mc.thePlayer.posY * 100.0) / 100.0;
            double z = Math.round(mc.thePlayer.posZ * 100.0) / 100.0;
            float yaw = Math.round(RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) * 100.0f) / 100.0f;
            float pitch = Math.round(mc.thePlayer.rotationPitch * 100.0f) / 100.0f;

            movementEmbed.addField("Player State",
                    "`" + Emoji.fromUnicode("⬇️").getFormatted() + "`" + " On Ground: `" + mc.thePlayer.onGround + "`\n" +
                            "`" + Emoji.fromUnicode("\uD83E\uDDCE\u200D♂️").getFormatted() + "`" + " Sneaking: `" + mc.thePlayer.isSneaking() + "`\n" +
                            "`" + Emoji.fromUnicode("\uD83C\uDFC3\u200D♂️").getFormatted() + "`" + " Sprinting: `" + mc.thePlayer.isSprinting() + "`\n" +
                            "`" + Emoji.fromUnicode("\uD83C\uDF0A").getFormatted() + "`" + " In Water: `" + mc.thePlayer.isInWater() + "`",
                    true);

            movementEmbed.addField("Coordinates",
                    "X: `" + x + "`\n" +
                            "Y: `" + y + "`\n" +
                            "Z: `" + z + "`",
                    true);

            movementEmbed.addField("Rotation",
                    "Yaw: `" + yaw + "`\n" +
                            "Pitch: `" + pitch + "`",
                    true);

        } else {
            movementEmbed.setDescription("Player data not `available`");
        }

        movementEmbed.setFooter("DelClient Remote Controller - Version 0.1");
        return movementEmbed;
    }

    @NotNull
    private EmbedBuilder getChatEmbed(Event event) {
        boolean isRemote = DiscordBotInstance.getStatusRemote() == StatusRemote.REMOTE;
        EmbedBuilder chatEmbed = new EmbedBuilder();

        String authorName = mc.thePlayer == null ? "?" : mc.thePlayer.getName();

        if(mc.thePlayer != null){
            String authorUrl = "https://mc-heads.net/head/" + mc.thePlayer.getUniqueID();
            chatEmbed.setAuthor("Dashboard Controller : " + authorName, "https://discord.gg/fqfNvVwP5u", authorUrl);
        }

        String status = isRemote ? "`Connected " + Emoji.fromUnicode("✅").getFormatted() + "`" : "`Disconnected " + Emoji.fromUnicode("❌").getFormatted() + "`";
        chatEmbed.setTitle("Current state of remote control: " + status);

        chatEmbed.setColor(new Color(186, 186, 186));

        if (mc.thePlayer != null) {
            if(!isRemote){
                chatEmbed.setDescription("You need to activate the remote control to start seeing the game chat.");
            }else{
                chatEmbed.setDescription("You can see the current game chat : \n" +
                        ChatRemoteListenerEvent.ChatManager.formatMessagesAsCodeBlock());
            }
        } else {
            chatEmbed.setDescription("Player data not `available`");
        }

        chatEmbed.setFooter("DelClient Remote Controller - Version 0.1");
        return chatEmbed;
    }

    @NotNull
    private EmbedBuilder getMacroEmbed(Event event) {
        boolean isRemote = DiscordBotInstance.getStatusRemote() == StatusRemote.REMOTE;
        EmbedBuilder macroEmbed = new EmbedBuilder();

        String authorName = mc.thePlayer == null ? "?" : mc.thePlayer.getName();

        if(mc.thePlayer != null){
            String authorUrl = "https://mc-heads.net/head/" + mc.thePlayer.getUniqueID();
            macroEmbed.setAuthor("Dashboard Controller : " + authorName, "https://discord.gg/fqfNvVwP5u", authorUrl);
        }

        String status = isRemote ? "`Connected " + Emoji.fromUnicode("✅").getFormatted() + "`" : "`Disconnected " + Emoji.fromUnicode("❌").getFormatted() + "`";
        macroEmbed.setTitle("Current state of remote control: " + status);

        macroEmbed.setColor(new Color(154, 22, 112));

        if (mc.thePlayer != null) {

            boolean inMacro = currentAction == ActionState.FISHING || currentAction == ActionState.FORAGING;

            macroEmbed.addField("Global", "`" + Emoji.fromUnicode("️\uD83D\uDCE1").getFormatted() + "`" + " In Macro: `" + inMacro + "`\n`" +
                    Emoji.fromUnicode("⏱").getFormatted() + "`" + " Timer: `" + "Unknown" + "`", true);

            boolean inForaging = ModuleManager.getModuleById("auto_fora").isToggled();
            boolean inFishing = ModuleManager.getModuleById("auto_fishing").isToggled();

            macroEmbed.addField("Macros",
                    "`" + Emoji.fromUnicode("️\uD83E\uDE93").getFormatted() + "`" + " Foraging: `" + inForaging + "`\n" +
                            "`" + Emoji.fromUnicode("️\uD83C\uDFA3").getFormatted() + "`" + " Fishing: `" + inFishing + "`\n"
                    , true);


            macroEmbed.setDescription("You can start **macros** by searching in the `select menu` just below!\nEvery *macro* will use the module settings.");

        } else {
            macroEmbed.setDescription("Player data not `available`");
        }

        macroEmbed.setFooter("DelClient Remote Controller - Version 0.1");
        return macroEmbed;
    }

    // ------------------------------------------------ OTHERS ------------------------------------------------

    private void startEmbedUpdateTask(MessageChannelUnion channel, InteractionHook hook, Event event) {
        stopChatUpdateTask();  // Arrêter toute tâche précédente

        chatUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        isChatUpdateActive = true;

        chatUpdateExecutor.scheduleAtFixedRate(() -> {
            if (!isChatUpdateActive) {
                chatUpdateExecutor.shutdown();
                return;
            }

            EmbedBuilder updatedEmbed = getChatEmbed(event);
            hook.editOriginalEmbeds(updatedEmbed.build()).queue(null, error -> {
                isChatUpdateActive = false;
                chatUpdateExecutor.shutdown();
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void startDashboardUpdateTask(MessageChannel channel, Message message, Event event) {
        stopDashboardUpdateTask();  // Arrêter toute tâche précédente

        dashboardUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        isDashboardUpdateActive = true;

        dashboardUpdateExecutor.scheduleAtFixedRate(() -> {
            if (!isDashboardUpdateActive) {
                dashboardUpdateExecutor.shutdown();
                return;
            }

            EmbedBuilder updatedEmbed = getDashboardEmbed(event);
            message.editMessageEmbeds(updatedEmbed.build()).queue(null, error -> {
                isDashboardUpdateActive = false;
                dashboardUpdateExecutor.shutdown();
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void startMovementUpdateTask(MessageChannel channel, Message message, Event event) {
        stopMovementUpdateTask();  // Arrêter toute tâche précédente

        movementUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        isMovementUpdateActive = true;

        movementUpdateExecutor.scheduleAtFixedRate(() -> {
            if (!isMovementUpdateActive) {
                movementUpdateExecutor.shutdown();
                return;
            }

            EmbedBuilder updatedEmbed = getMovementEmbed(event);
            message.editMessageEmbeds(updatedEmbed.build()).queue(null, error -> {
                isMovementUpdateActive = false;
                movementUpdateExecutor.shutdown();
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void startMacroUpdateTask(MessageChannel channel, Message message, Event event) {
        stopMacroUpdateTask();  // Arrêter toute tâche précédente

        macroUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        isMacroUpdateActive = true;

        macroUpdateExecutor.scheduleAtFixedRate(() -> {
            if (!isMacroUpdateActive) {
                macroUpdateExecutor.shutdown();
                return;
            }

            EmbedBuilder updatedEmbed = getMacroEmbed(event);
            message.editMessageEmbeds(updatedEmbed.build()).queue(null, error -> {
                isMacroUpdateActive = false;
                macroUpdateExecutor.shutdown();
            });
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void stopDashboardUpdateTask() {
        isDashboardUpdateActive = false;
        if (dashboardUpdateExecutor != null && !dashboardUpdateExecutor.isShutdown()) {
            dashboardUpdateExecutor.shutdown();
        }
    }

    private void stopChatUpdateTask() {
        isChatUpdateActive = false;
        if (chatUpdateExecutor != null && !chatUpdateExecutor.isShutdown()) {
            chatUpdateExecutor.shutdown();
        }
    }

    private void stopMovementUpdateTask() {
        isMovementUpdateActive = false;
        if (movementUpdateExecutor != null && !movementUpdateExecutor.isShutdown()) {
            movementUpdateExecutor.shutdown();
        }
    }

    private void stopMacroUpdateTask() {
        isMacroUpdateActive = false;
        if (macroUpdateExecutor != null && !macroUpdateExecutor.isShutdown()) {
            macroUpdateExecutor.shutdown();
        }
    }

}