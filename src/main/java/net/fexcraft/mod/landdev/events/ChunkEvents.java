package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.ChunkCap;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.ChunkRegion;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ChunkCapabilityUtil;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
    	if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
    	if(!ResManager.INSTANCE.LOADED){
    		if(event.getWorld().isRemote) return;
    		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
    		LandDev.updateSaveDirectory(event.getWorld());
    		ResManager.INSTANCE.load();
    		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, TranslationUtil.translate("server.started", LandDev.VERSION));
    	}
		ChunkRegion reg = ResManager.getChunkRegion(new ChunkKey(event.getChunk().x, event.getChunk().z, true));
    	Chunk_ chunk = new Chunk_(reg, event.getChunk().x, event.getChunk().z);
        //if(reg.chunks.containsKey(chunk.key)) return;
		chunk.load(LandDev.DB.load(chunk.saveTable(), chunk.saveId()));
        reg.chunks.put(chunk.key, chunk);
    }
    
    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event){
    	if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;
        Chunk_ chunk = ResManager.getChunk(event.getChunk());
        if(chunk != null){
        	ResManager.remChunk(event.getChunk());
        	FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> chunk.save());
        }
    }
    
	@SubscribeEvent
	public static void onAttachEventChunk(AttachCapabilitiesEvent<net.minecraft.world.chunk.Chunk> event){
		if(event.getObject().getWorld().provider.getDimension() != 0) return;
		event.addCapability(ChunkCap.REGNAME, new ChunkCapabilityUtil(event.getObject()));
	}

}
