package com.mikolajkolek.fixaltgr.mixin;

import com.mikolajkolek.fixaltgr.FixAltGr;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.TimeUnit;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
    @Inject(at = @At(value = "HEAD"), method = "isKeyDown", cancellable = true)
    private static void isKeyDown(long window, int code, CallbackInfoReturnable<Boolean> cir) {
        if (code != 341) return;

        if (!FixAltGr.listener.controlKeyPressed || FixAltGr.listener.altKeyPressed)
            cir.setReturnValue(false);
        else {
            try {
                TimeUnit.MILLISECONDS.sleep(FixAltGr.axiomLoaded ? 0 : 10);
            } catch (InterruptedException e) {
                FixAltGr.LOGGER.error("The isKeyDown sleep was interrupted!");
            }

            if (!FixAltGr.listener.controlKeyPressed || FixAltGr.listener.altKeyPressed)
                cir.setReturnValue(false);
            else
                cir.setReturnValue(true);
        }
    }
}
