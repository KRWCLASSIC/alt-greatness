package com.mikolajkolek.fixaltgr;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
//#if MC >= 11700
import dev.architectury.platform.Platform;
//#else
//$$ import me.shedaniel.architectury.platform.Platform;
//#endif
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FixAltGr {
    public static final String MODID = "fixaltgr";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final GlobalKeyboardListener listener = new GlobalKeyboardListener();
    public static boolean axiomLoaded = false;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            if (Platform.isModLoaded("axiom")) {
                LOGGER.warn("FixAltGr detected that Axiom is loaded. This means that FixAltGr has to disable parts of its functionality, possibly causing it to work worse.");
                axiomLoaded = true;
            }

            CustomLibraryLocator.setAaDefaultLocator();
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            LOGGER.error("There was a problem registering the native hook: " + ex.getMessage());
            return;
        } catch (Throwable t) {
            LOGGER.error("Error initializing FixAltGr: " + t.getMessage(), t);
            return;
        }

        GlobalScreen.addNativeKeyListener(listener);
        LOGGER.info("Registered native key listener");
    }
}
