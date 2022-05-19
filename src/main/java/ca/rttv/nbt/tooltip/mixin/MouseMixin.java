package ca.rttv.nbt.tooltip.mixin;

import ca.rttv.nbt.tooltip.duck.ScreenDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onMouseScroll(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double d, double e, double f) {
        //noinspection ConstantConditions
        ((ScreenDuck) this.client.currentScreen).yOffset(d);
    }
}
