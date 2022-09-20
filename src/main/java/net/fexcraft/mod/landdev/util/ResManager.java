package net.fexcraft.mod.landdev.util;

import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

/**
 * Resource Manager / Data Manager
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ResManager {
	
	public static boolean LOADED;
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static ConcurrentHashMap<ChunkKey, Chunk_> CHUNKS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, District> DISTRICTS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, Municipality> MUNICIPALITIES = new ConcurrentHashMap<>();

	public static Chunk_ getChunk(int x, int z){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == x && ck.key.z == z) return ck;
		}
		return null;
	}

	public static Chunk_ getChunk(Chunk chunk){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == chunk.x && ck.key.z == chunk.z) return ck;
		}
		return null;
	}

	public static Chunk_ getChunk(Vec3d pos){
		return getChunk((int)pos.x >> 4, (int)pos.z >> 4);
	}

	public static Chunk_ getChunk(EntityPlayer player){
		return getChunk((int)player.posX >> 4, (int)player.posZ >> 4);
	}

	public static void remChunk(Chunk chunk){
		CHUNKS.remove(new ChunkKey(chunk.x, chunk.z));
	}

	public static District getDistrict(int idx, boolean load){
		if(!load) return DISTRICTS.get(idx);
		District dis = DISTRICTS.get(idx);
		if(dis == null) DISTRICTS.put(idx, load(dis = new District(idx)));
		return dis;
	}

	public static Municipality getMunicipality(int idx, boolean load){
		if(!load) return MUNICIPALITIES.get(idx);
		Municipality mun = MUNICIPALITIES.get(idx);
		if(mun == null) MUNICIPALITIES.put(idx, load(mun = new Municipality(idx)));
		return mun;
	}

	private static <S> S load(Saveable save){
		if(!LandDev.DB.exists(save.saveTable(), save.saveId())) return (S)save;
		JsonMap map = LandDev.DB.load(save.saveTable(), save.saveId());
		if(map != null) save.load(map);
		else save.gendef();
		return (S)save;
	}

	public static void unload(){
		CHUNKS.values().forEach(save -> LandDev.DB.save(save));
		DISTRICTS.values().forEach(save -> LandDev.DB.save(save));
	}

	public static void clear(){
		CHUNKS.clear();
		DISTRICTS.clear();
	}

}
