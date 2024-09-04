package net.fexcraft.mod.landdev.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3D;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.ChunkRegion;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.world.EntityW;
import net.fexcraft.mod.uni.world.WrapperHolder;

import static net.fexcraft.mod.landdev.LDN.DB;

/**
 * Resource Manager / Data Manager
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ResManager implements Saveable {
	
	public boolean LOADED = false;
	public static ResManager INSTANCE = new ResManager();
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static ConcurrentHashMap<ChunkKey, Chunk_> CHUNKS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, District> DISTRICTS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, Municipality> MUNICIPALITIES = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, County> COUNTIES = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, State> STATES = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, LDPlayer> PLAYERS = new ConcurrentHashMap<>();
	//
	public static ConcurrentHashMap<Integer, ChunkKey> MUN_CENTERS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, ChunkKey> CT_CENTERS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, ChunkKey> ST_CENTERS = new ConcurrentHashMap<>();
	public static Account SERVER_ACCOUNT;

	public static Chunk_ getChunk(int x, int z){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == x && ck.key.z == z) return ck;
		}
		return ChunkRegion.get(x, z);
	}

	public static Chunk_ getChunkS(double x, double z){
		return getChunk((int)x >> 4, (int)z >> 4);
	}

	public static Chunk_ getChunkS(int x, int z){
		return getChunk(x >> 4, z >> 4);
	}

	public static Chunk_ getChunk(ChunkKey key){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.equals(key)) return ck;
		}
		return ChunkRegion.get(key);
	}

	public static Chunk_ getChunk(V3D pos){
		return getChunk((int)pos.x >> 4, (int)pos.z >> 4);
	}

	public static Chunk_ getChunk(EntityW player){
		return getChunk(player.getPos());
	}

	public static Chunk_ getChunkP(Object player){
		return getChunk(UniEntity.get(player).entity);
	}

	public static void remChunk(int x, int z){
		Chunk_ ck = getChunk(x, z);
		if(ck != null){
			CHUNKS.remove(ck.key);
			if(LDConfig.SAVE_CHUNKS_IN_REGIONS) ChunkRegion.unload(ck);
		}
	}

	public static District getDistrict(int idx){
		District dis = DISTRICTS.get(idx);
		if(dis == null && idx >= -2 /*&& LandDev.DB.exists("districts", idx + "")*/){
			DISTRICTS.put(idx, load(dis = new District(idx)));
		}
		return dis;
	}

	public static Municipality getMunicipality(int idx, boolean load){
		if(!load) return MUNICIPALITIES.get(idx);
		Municipality mun = MUNICIPALITIES.get(idx);
		if(mun == null) MUNICIPALITIES.put(idx, load(mun = new Municipality(idx)));
		return mun;
	}

	public static County getCounty(int idx, boolean load){
		if(!load) return COUNTIES.get(idx);
		County cty = COUNTIES.get(idx);
		if(cty == null) COUNTIES.put(idx, load(cty = new County(idx)));
		return cty;
	}

	public static State getState(int idx, boolean load){
		if(!load) return STATES.get(idx);
		State stt = STATES.get(idx);
		if(stt == null) STATES.put(idx, load(stt = new State(idx)));
		return stt;
	}

	public static LDPlayer getPlayer(UUID uuid, boolean load){
		if(!load) return PLAYERS.get(uuid);
		LDPlayer ply = PLAYERS.get(uuid);
		if(ply == null) PLAYERS.put(uuid, load(ply = new LDPlayer(uuid)));
		return ply;
	}

	public static LDPlayer getPlayer(UniEntity player){
		return PLAYERS.get(player.entity.getUUID());
	}

	public static LDPlayer getPlayer(EntityW entity){
		return PLAYERS.get(entity.getUUID());
	}

	public static LDPlayer getPlayer(Object player){
		return getPlayer(UniEntity.get(player));
	}

	public static void unloadIfOffline(LDPlayer player){
		if(!WrapperHolder.getOnlinePlayerIDs().contains(player.uuid)){
			player.save();
			PLAYERS.remove(player.uuid);
		}
	}

	public static LDPlayer getPlayer(String string, boolean load){
		UUID uuid = null;
		try{
			uuid = UUID.fromString(string);
		}
		catch(Exception e){
			if(EnvInfo.DEV) e.printStackTrace();
			uuid = WrapperHolder.getUUIDFor(string);
		}
		return uuid == null ? null : getPlayer(uuid, load);
	}

	public static <S> S load(Saveable save){
		if(!DB.exists(save.saveTable(), save.saveId())){
			save.gendef();
			return (S)save;
		}
		JsonMap map = DB.load(save.saveTable(), save.saveId());
		if(map != null) save.load(map);
		else save.gendef();
		return (S)save;
	}

	public void load(){
		SERVER_ACCOUNT = DataManager.getAccount("server:landdev", false, false);
		if(SERVER_ACCOUNT == null){
			SERVER_ACCOUNT = DataManager.getAccount("server:landdev", false, true);
			SERVER_ACCOUNT.setBalance(1000000000000l);
			SERVER_ACCOUNT.setName("LandDeveloper Server Account");
		}
		clear();
		JsonMap map = DB.load(saveTable(), saveId());
		if(map != null) load(map);
		getDistrict(-1);
		getDistrict(0);
		LOADED = true;
	}

	public static void unload(){
		ChunkRegion.saveAll();
		DISTRICTS.values().forEach(save -> DB.save(save));
		MUNICIPALITIES.values().forEach(save -> DB.save(save));
		COUNTIES.values().forEach(save -> DB.save(save));
		STATES.values().forEach(save -> DB.save(save));
		PLAYERS.values().forEach(save -> DB.save(save));
		INSTANCE.save();
		INSTANCE.LOADED = false;
	}

	public static void clear(){
		ChunkRegion.REGIONS.clear();
		DISTRICTS.clear();
		MUNICIPALITIES.clear();
		COUNTIES.clear();
		STATES.clear();
		PLAYERS.clear();
		MUN_CENTERS.clear();
		CT_CENTERS.clear();
		ST_CENTERS.clear();
	}

	public static String getPlayerName(UUID uuid){
		return WrapperHolder.getNameFor(uuid);
	}

	@Override
	public void save(JsonMap map){
		JsonMap mc = new JsonMap();
		MUN_CENTERS.forEach((key, val) -> {
			mc.add(key + "", val.toString());
		});
		map.add("municipality-centers", mc);
		JsonMap cc = new JsonMap();
		CT_CENTERS.forEach((key, val) -> {
			cc.add(key + "", val.toString());
		});
		map.add("county-centers", cc);
		JsonMap sc = new JsonMap();
		ST_CENTERS.forEach((key, val) -> {
			sc.add(key + "", val.toString());
		});
		map.add("state-centers", sc);
	}

	@Override
	public void load(JsonMap map){
		if(map.has("municipality-centers")){
			map.get("municipality-centers").asMap().value.forEach((key, val) -> {
				try{
					MUN_CENTERS.put(Integer.parseInt(key), new ChunkKey(val.string_value()));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
		if(map.has("county-centers")){
			map.get("county-centers").asMap().value.forEach((key, val) -> {
				try{
					CT_CENTERS.put(Integer.parseInt(key), new ChunkKey(val.string_value()));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
		if(map.has("state-centers")){
			map.get("state-centers").asMap().value.forEach((key, val) -> {
				try{
					ST_CENTERS.put(Integer.parseInt(key), new ChunkKey(val.string_value()));
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}
	}
	
	@Override
	public String saveId(){
		return "resources";
	}
	
	@Override
	public String saveTable(){
		return "general";
	}

	public static int getNewIdFor(String table){
		return DB.getNewEntryId(table);
	}

	public static void bulkSave(Saveable... saveables){
		for(Saveable save : saveables) save.save();
	}

}
