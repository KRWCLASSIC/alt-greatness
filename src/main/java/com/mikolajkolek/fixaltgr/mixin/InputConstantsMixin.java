package com.mikolajkolek.fixaltgr.mixin;

import com.mikolajkolek.fixaltgr.FixAltGr;
import com.mojang.blaze3d.platform.InputConstants;
//#if MC >= 12100
//$$ import org.spongepowered.asm.mixin.Dynamic;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    @Inject(at = @At(value = "HEAD"), method = "isKeyDown(JI)Z", cancellable = true, require = 0)
    private static void isKeyDown(long window, int code, CallbackInfoReturnable<Boolean> cir) {
        checkAltGr(code, cir);
    }

//#if MC >= 12100
//$$     @Dynamic
//$$     @Inject(at = @At(value = "HEAD"), method = "isKeyDown(Lcom/mojang/blaze3d/platform/Window;I)Z", cancellable = true, require = 0)
//$$     private static void isKeyDownWindow(com.mojang.blaze3d.platform.Window window, int code, CallbackInfoReturnable<Boolean> cir) {
//$$         checkAltGr(code, cir);
//$$     }
//#endif

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
