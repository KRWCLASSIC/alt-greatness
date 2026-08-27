package com.mikolajkolek.fixaltgr.mixin;

import com.mikolajkolek.fixaltgr.FixAltGr;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    @Dynamic
    @Inject(at = @At(value = "HEAD"), method = "isKeyDown(JI)Z", cancellable = true, require = 0)
    private static void isKeyDownLong(long window, int code, CallbackInfoReturnable<Boolean> cir) {
        checkAltGr(code, cir);
    }

    @Dynamic
    @Inject(at = @At(value = "HEAD"), method = "isKeyDown", cancellable = true, require = 0)
    private static void isKeyDownWindow(@Coerce Object window, int code, CallbackInfoReturnable<Boolean> cir) {
        checkAltGr(code, cir);
    }

    private static void checkAltGr(int code, CallbackInfoReturnable<Boolean> cir) {
        if (code != 341) return;

        if (!FixAltGr.listener.controlKeyPressed || FixAltGr.listener.altKeyPressed) {
            cir.setReturnValue(false);
            return;
        }

        if (FixAltGr.axiomLoaded) return;

        long timeSinceChange = System.currentTimeMillis() - FixAltGr.listener.lastStateChangeTime;
        if (timeSinceChange < 10) {
            cir.setReturnValue(false);
        }
    }
}
