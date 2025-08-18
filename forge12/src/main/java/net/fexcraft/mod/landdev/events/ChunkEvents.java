package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.world.WrapperHolder;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {

	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
		if(!ResManager.INSTANCE.LOADED) load(event.getWorld());
	}

	private static void load(World world){
		if(world.isRemote) return;
		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
		LandDev.SAVE_DIR = WrapperHolder.getWorldFolder(WrapperHolder.getWorld(world), "landdev");
		ResManager.INSTANCE.load();
	}

	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
		Chunk_ chunk = ResManager.getChunk(event.getChunk().x, event.getChunk().z);
		if(chunk != null) ResManager.remChunk(event.getChunk().x, event.getChunk().z);
	}

}
