package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onWorldLoad(WorldEvent.Load event){
		if(event != null && (event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote)) return;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onWorldUnload(WorldEvent.Unload event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote) return;
		Print.log("Unloading LandDev World Data...");
		ResManager.saveAll();
		ResManager.clear();
		Print.log("Unloaded LandDev World Data.");
	}
	
	@SubscribeEvent
	public static void onExplosion(ExplosionEvent event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote || !event.isCancelable()) return;
		Chunk_ chunk = ResManager.getChunkS(event.getExplosion().getPosition().x, event.getExplosion().getPosition().z);
		event.setCanceled(!chunk.district.norms.get("explosions").bool());
	}
	
}
