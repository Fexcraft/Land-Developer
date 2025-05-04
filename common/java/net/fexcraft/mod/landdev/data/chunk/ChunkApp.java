package net.fexcraft.mod.landdev.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.db.JsonTagConverter;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.uni.Appendable;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.WrapperHolder;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkApp implements Appendable<UniChunk> {

	public final Chunk_ chunk;

	public ChunkApp(UniChunk type){
		if(type == null){
			chunk = null;
			return;
		}
		//if(LDConfig.SAVE_CHUNKS_IN_REGIONS){
			chunk = ChunkRegion.getFor(type);
		//}
		/*else{
			chunk = new Chunk_(type);
			chunk.load(new JsonMap());
			ResManager.CHUNKS.put(chunk.key, chunk);
		}*/
	}

	@Override
	public Appendable<UniChunk> create(UniChunk type){
		if(type.chunk.isOnClient() || (!EnvInfo.is112() && WrapperHolder.getServer() == null)) return null;
		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
		return new ChunkApp(type);
	}

	@Override
	public String id(){
		return "landdev:chunk";
	}

	@Override
	public void save(UniChunk type, TagCW com){
		/*JsonMap map = new JsonMap();
		chunk.save(map);
		JsonTagConverter.map(com, map);*/
	}

	@Override
	public void load(UniChunk type, TagCW com){
		if(com.empty()){
			//chunk.gendef();
			return;
		}
		JsonMap map = JsonTagConverter.demap(com);
		if(map.empty()) chunk.gendef();
		else chunk.load(map);
		chunk.save();//to region
	}

}
