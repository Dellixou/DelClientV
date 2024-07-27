package com.github.dellixou.delclientv3.utils;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TickUtils {

  private static float partialTicks = 0;
  private static float partialRenderTicks = 0;

  public static float getPartialTicks() {
    return partialTicks;
  }
  public static float getPartialRenderTicks() {
    return partialRenderTicks;
  }

  @SubscribeEvent
  public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
    partialTicks = event.partialTicks;
  }

  @SubscribeEvent
  public void onRenderWorld(RenderWorldLastEvent event) {
    partialRenderTicks = event.partialTicks;
  }
}