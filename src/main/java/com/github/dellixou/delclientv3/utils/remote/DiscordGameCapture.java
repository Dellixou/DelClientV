package com.github.dellixou.delclientv3.utils.remote;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordGameCapture {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static ScheduledExecutorService scheduler;
    private static Message screenshotMessage;
    private static volatile BufferedImage latestScreenshot;
    private static volatile BufferedImage screenshot;

    public static void startScreenshotUpdates(ButtonInteractionEvent event) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(() -> {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            mc.addScheduledTask(() -> {
                try {
                    Robot robot = new Robot();
                    Rectangle captureRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    latestScreenshot = robot.createScreenCapture(captureRect);
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            });
        }, 0, 500, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            if (latestScreenshot == null) return;

            try {
                BufferedImage resizedImage = resizeImage(latestScreenshot, 640, 360); // Reduce resolution here

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", baos);
                byte[] imageData = baos.toByteArray();

                EmbedBuilder screenshotEmbed = new EmbedBuilder()
                        .setTitle("Minecraft Live View")
                        .setColor(new Color(0x48486D))
                        .setImage("attachment://screenshot.png")
                        .setTimestamp(Instant.now());

                FileUpload file = FileUpload.fromData(imageData, "screenshot.png");

                if (screenshotMessage == null) {
                    event.getHook().sendMessageEmbeds(screenshotEmbed.build())
                            .addFiles(file)
                            .setComponents(ActionRow.of(
                                    Button.primary("close_embed", Emoji.fromUnicode("❌").getFormatted() + " Close")
                            ))
                            .queue(message -> screenshotMessage = message);
                } else {
                    screenshotMessage.editMessageEmbeds(screenshotEmbed.build())
                            .setFiles(file)
                            .queue(null, error -> stopScreenshotUpdates());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    public static void stopScreenshotUpdates() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        screenshotMessage = null;
    }

    public static void sendScreenshotEmbed(ButtonInteractionEvent event){
        mc.addScheduledTask(() -> {
            try {
                Robot robot = new Robot();
                Rectangle captureRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                screenshot = robot.createScreenCapture(captureRect);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", baos);
                byte[] imageData = baos.toByteArray();
                if (imageData != null) {
                    EmbedBuilder screenshotEmbed = new EmbedBuilder()
                            .setTitle("Screenshot")
                            .setColor(new Color(0x2D2D15))
                            .setImage("attachment://screenshot.png")
                            .setTimestamp(Instant.now());
                    FileUpload file = FileUpload.fromData(imageData, "screenshot.png");
                    event.getHook().sendMessageEmbeds(screenshotEmbed.build())
                            .addFiles(file)
                            .setComponents(ActionRow.of(
                                    Button.primary("close_embed", Emoji.fromUnicode("❌").getFormatted() + " Close")
                            ))
                            .queue();
                }
            } catch (AWTException | IOException e) {
                e.printStackTrace();
            }
        });
    }

}