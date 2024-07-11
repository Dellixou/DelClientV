package com.github.dellixou.delclientv3.mixin;

import com.github.dellixou.delclientv3.DelClient;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {EntityPlayer.class})
public class MixinEntityPlayer {

    //@Inject(method = "onUpdate", at = @At("HEAD"))
    private void onMoving(CallbackInfo ci) {
        DelClient.userRoute.onUpdate();
    }
}
