package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
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
		LandDev.updateSaveDirectory(world);
		ResManager.INSTANCE.load();
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, TranslationUtil.translate("server.started", LandDev.VERSION));
	}

	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
		Chunk_ chunk = ResManager.getChunk(event.getChunk());
		if(chunk != null) ResManager.remChunk(event.getChunk());
	}

}
