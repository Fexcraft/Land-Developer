package net.fexcraft.mod.landdev.events;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.ChunkCap;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ChunkCapabilityUtil;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
        if(event.getWorld().provider.getDimension() != 0) return;
    	if(!ResManager.LOADED){
    		ResManager.LOADED = true;
    		if(event.getWorld().isRemote) return;
    		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
    		LandDev.updateSaveDirectory(event.getWorld());
    	}
    	Chunk_ chunk = new Chunk_(event.getWorld(), event.getChunk().x, event.getChunk().z);
        if(ResManager.CHUNKS.containsKey(chunk.key)) return;
    	if(LandDev.DB.exists(chunk.saveTable(), chunk.saveId())){
    		chunk.load(LandDev.DB.load(chunk.saveTable(), chunk.saveId()));
    	}
    	else{
    		chunk.load(new JsonMap());
    		chunk.save();
    	}
        ResManager.CHUNKS.put(chunk.key, chunk);
    }
    
    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event){
        if(event.getWorld().provider.getDimension() != 0) return;
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
