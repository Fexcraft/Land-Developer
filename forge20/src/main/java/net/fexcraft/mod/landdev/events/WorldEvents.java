package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.minecraftforge.event.level.ExplosionEvent;
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
		ChunkEvents.load(event.getLevel());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(LevelEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
		LandDev.log("Unloading LandDev World Data...");
		ResManager.saveAll();
		ResManager.clear();
		LandDev.log("Unloaded LandDev World Data.");
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
		Chunk_ chunk = ResManager.getChunkS(event.getExplosion().getPosition().x, event.getExplosion().getPosition().z);
		if(!chunk.district.norms.get("explosions").bool()) event.getExplosion().clearToBlow();
	}
	
}
