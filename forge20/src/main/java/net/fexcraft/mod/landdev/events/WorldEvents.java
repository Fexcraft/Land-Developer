package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(LevelEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(LevelEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
		LandDev.log("Unloading LandDev World Data...");
		ResManager.unload();
		ResManager.clear();
	}
	
}
