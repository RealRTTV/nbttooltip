package ca.rttv.nbt.tooltip.mixin;

import ca.rttv.nbt.tooltip.duck.ScreenDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Unique
    private static Function<ItemStack, List<Text>> cachedItemStack = stack -> null;

    @Shadow
    public abstract @Nullable NbtCompound getNbt();

    @Shadow
    public abstract boolean hasNbt();

    @Inject(method = "getTooltip", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void getTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list, MutableText mutableText) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (Screen.hasShiftDown()) {
            List<Text> cache = cachedItemStack.apply((ItemStack) (Object) this);
            if (cache != null) {
                cir.setReturnValue(cache);
                return;
            }
            String text = this.hasNbt() ? toFormattedString(NbtHelper.toPrettyPrintedText(this.getNbt()), new StringBuilder()).toString() : "null";
            if (!text.equals("null")) {
                String[] strings = splitString(150, text);
                for (String str : strings) {
                    list.add(Text.literal(str));
                }
            } else {
                list.add(Text.literal("null").styled(style -> style.withColor(Formatting.DARK_GRAY)));
            }
            //noinspection ConstantConditions
            cachedItemStack = stack -> (Object) this == stack && this.getNbt() == stack.getNbt() ? list : null;
            if (client.currentScreen != null) {
                ((ScreenDuck) client.currentScreen).resetYOffset();
            }
            cir.setReturnValue(list);
            return;
        }
        if (client.currentScreen != null) {
            ((ScreenDuck) client.currentScreen).resetYOffset();
        }
    }

    private String[] splitString(int maxLength, String text) {
        List<String> strings = new ArrayList<>();
        while (text.length() > 0) {
            int cut = Math.min(text.length(), maxLength);
            String line = text.substring(0, cut);
            if (line.charAt(line.length() - 1) == '§') {
                line = line.substring(0, line.length() - 1);
                text = "§".concat(text.substring(cut));
            } else {
                text = text.substring(cut);
            }
            if (line.charAt(0) != '§') {
                line = getPreviousLineLastColor(strings.get(strings.size() - 1)).concat(line);
            }
            strings.add(line);
        }
        return strings.toArray(String[]::new);
    }

    private String getPreviousLineLastColor(String line) {
        for (int i = line.length() - 1; i >= 0; i--) {
            if (line.charAt(i) == '§') {
                return String.valueOf(line.charAt(i)).concat(String.valueOf(line.charAt(i + 1)));
            }
        }

        return "";
    }

    private StringBuilder toFormattedString(Text text, StringBuilder sb) {
        TextColor color = text.getStyle().getColor();
        sb.append(Formatting.byName(color == null ? TextColor.fromFormatting(Formatting.WHITE).getName() : color.getName()).toString().concat(text.asComponent().visit(Optional::of).orElse("")));
        text.getSiblings().forEach(sibling -> toFormattedString(sibling, sb));
        return sb;
    }
}
