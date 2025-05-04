package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkEvents {

	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
		if(!ResManager.INSTANCE.LOADED) load((Level)event.getLevel());
	}

	private static void load(Level level){
		if(level.isClientSide) return;
		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
		LandDev.SAVE_DIR = new File(level.getServer().getServerDirectory(), "landdev/");
		ResManager.INSTANCE.load();
	}

	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != FCL.SERVER.get().overworld()) return;
		Chunk_ chunk = ResManager.getChunk(event.getChunk().getPos().x, event.getChunk().getPos().z);
		if(chunk != null) ResManager.remChunk(event.getChunk().getPos().x, event.getChunk().getPos().z);
	}

}
