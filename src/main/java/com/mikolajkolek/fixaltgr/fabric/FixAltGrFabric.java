//#if FABRIC
package com.mikolajkolek.fixaltgr.fabric;

import com.mikolajkolek.fixaltgr.FixAltGr;
import net.fabricmc.api.ClientModInitializer;

public class FixAltGrFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FixAltGr.init();
    }
}
//#endif
