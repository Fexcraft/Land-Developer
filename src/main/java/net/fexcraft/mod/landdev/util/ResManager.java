package net.fexcraft.mod.landdev.util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.GameProfile;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;

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
	public static ConcurrentHashMap<UUID, Player> PLAYERS = new ConcurrentHashMap<>();
	//
	public static ConcurrentHashMap<Integer, ChunkKey> MUN_CENTERS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, ChunkKey> CT_CENTERS = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Integer, ChunkKey> ST_CENTERS = new ConcurrentHashMap<>();
	public static Account SERVER_ACCOUNT;

	public static Chunk_ getChunk(int x, int z){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == x && ck.key.z == z) return ck;
		}
		return null;
	}

	public static Chunk_ getChunk(ChunkKey key){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.equals(key)) return ck;
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

	public static Chunk_ getChunk(Entity player){
		return getChunk((int)player.posX >> 4, (int)player.posZ >> 4);
	}

	public static Chunk_ getChunk(Vec3i pos){
		return getChunk((int)pos.getX() >> 4, (int)pos.getZ() >> 4);
	}

	public static void remChunk(Chunk chunk){
		Chunk_ ck = getChunk(chunk.x, chunk.z);
		if(ck != null) CHUNKS.remove(ck.key);
	}

	public static District getDistrict(int idx){
		District dis = DISTRICTS.get(idx);
		if(dis == null && idx >= -2 && LandDev.DB.exists("districts", idx + "")){
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

	public static Player getPlayer(UUID uuid, boolean load){
		if(!load) return PLAYERS.get(uuid);
		Player ply = PLAYERS.get(uuid);
		if(ply == null) PLAYERS.put(uuid, load(ply = new Player(uuid)));
		return ply;
	}

	public static Player getPlayer(EntityPlayer player){
		return PLAYERS.get(player.getGameProfile().getId());
	}

	public static UUID getUUIDof(String string){
		GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(string);
		return gp == null ? null : gp.getId();
	}

	public static Player getPlayer(String string, boolean load){
		UUID uuid = null;
		try{
			uuid = UUID.fromString(string);
		}
		catch(Exception e){
			if(Static.dev()) e.printStackTrace();
			GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(string);
			if(gp != null) uuid = gp.getId();
		}
		return uuid == null ? null : getPlayer(uuid, load);
	}

	public static <S> S load(Saveable save){
		if(!LandDev.DB.exists(save.saveTable(), save.saveId())){
			save.gendef();
			return (S)save;
		}
		JsonMap map = LandDev.DB.load(save.saveTable(), save.saveId());
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
		JsonMap map = LandDev.DB.load(saveTable(), saveId());
		if(map != null) load(map);
		LOADED = true;
	}

	public static void unload(){
		DISTRICTS.values().forEach(save -> LandDev.DB.save(save));
		MUNICIPALITIES.values().forEach(save -> LandDev.DB.save(save));
		COUNTIES.values().forEach(save -> LandDev.DB.save(save));
		STATES.values().forEach(save -> LandDev.DB.save(save));
		PLAYERS.values().forEach(save -> LandDev.DB.save(save));
		INSTANCE.save();
		INSTANCE.LOADED = false;
	}

	public static void clear(){
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
		return Static.getPlayerNameByUUID(uuid);
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
		return LandDev.DB.getNewEntryId(table);
	}

	public static void bulkSave(Saveable... saveables){
		for(Saveable save : saveables) save.save();
	}

}
