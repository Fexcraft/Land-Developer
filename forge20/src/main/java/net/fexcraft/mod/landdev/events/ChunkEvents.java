package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkEvents {

	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
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
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		Chunk_ chunk = ResManager.getChunk(event.getChunk().getPos().x, event.getChunk().getPos().z);
		if(chunk != null) ResManager.remChunk(event.getChunk().getPos().x, event.getChunk().getPos().z);
	}

}
