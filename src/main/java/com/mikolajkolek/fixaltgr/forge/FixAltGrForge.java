//#if FORGE
//$$ package com.mikolajkolek.fixaltgr.forge;
//$$ 
//$$ import com.mikolajkolek.fixaltgr.FixAltGr;
//#if MC >= 11700
//$$ import dev.architectury.platform.forge.EventBuses;
//#else
//$$ import me.shedaniel.architectury.platform.forge.EventBuses;
//#endif
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//$$ 
//$$ @Mod(FixAltGr.MODID)
//$$ public class FixAltGrForge {
//$$     public FixAltGrForge() {
//$$         EventBuses.registerModEventBus(FixAltGr.MODID, FMLJavaModLoadingContext.get().getModEventBus());
//$$         FixAltGr.init();
//$$     }
//$$ }
//#endif
