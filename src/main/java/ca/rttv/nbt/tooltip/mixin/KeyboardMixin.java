package ca.rttv.nbt.tooltip.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Keyboard.class)
abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/NarratorManager;isActive()Z", ordinal = 0))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action != 0 && key == 258 && Screen.hasShiftDown() && this.client.currentScreen instanceof HandledScreen<?> handledScreen && handledScreen.focusedSlot != null) {
            String stack = "/give @s " + Registry.ITEM.getId(handledScreen.focusedSlot.getStack().getItem()) + Objects.requireNonNullElse(handledScreen.focusedSlot.getStack().getNbt(), new NbtCompound()) + " " + handledScreen.focusedSlot.getStack().getCount();
            this.client.keyboard.setClipboard(stack);
            SystemToast.show(this.client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(handledScreen.focusedSlot.getStack().getTranslationKey()).append(Text.literal(" Copied!")), Text.literal(stack.length() + " bytes"));
        }
    }
}
