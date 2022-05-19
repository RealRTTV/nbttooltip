package ca.rttv.nbt.tooltip.mixin;

import ca.rttv.nbt.tooltip.duck.ScreenDuck;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Screen.class)
abstract class ScreenMixin implements ScreenDuck {
    @Unique
    private double yOffset;

    @ModifyVariable(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), argsOnly = true, index = 4)
    private int renderTooltipFromComponents(int y) {
        return Screen.hasShiftDown() ? y + (int) this.yOffset : y;
    }

    @Override
    public void yOffset(double yOffset) {
        this.yOffset += yOffset * 4;
    }

    @Override
    public void resetYOffset() {
        this.yOffset = 0.0d;
    }
}
