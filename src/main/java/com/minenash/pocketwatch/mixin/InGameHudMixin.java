package com.minenash.pocketwatch.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow protected abstract PlayerEntity getCameraPlayer();
    @Shadow @Final private MinecraftClient client;

    @ModifyArg(method = "renderHotbar", index = 1, at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private int moveIndicator(int p) { return getX(); }

    @ModifyArg(method = "renderHotbar", index = 5, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"))
    private int moveIndicator2(int p) { return getX(); }

    @Unique private int getX() {
        int offhandShift = getCameraPlayer().getOffHandStack().isEmpty() ? 0 : 29;
        int scaledWidth = client.getWindow().getScaledWidth();

        if (getCameraPlayer().getMainArm() == Arm.LEFT)
            return scaledWidth/2 + 91 + 6 + offhandShift;
        return scaledWidth/2 - 91 - 22 - offhandShift;
    }

}
