package net.fexcraft.mod.landdev.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.db.JsonTagConverter;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.WrapperHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkRegion {

	public static ConcurrentHashMap<ChunkKey, ChunkRegion> REGIONS = new ConcurrentHashMap<>();
	public ConcurrentHashMap<ChunkKey, Chunk_> chunks = new ConcurrentHashMap<>();
	private TagCW compound;
	private ChunkKey key;
	private File file;
	public long last_access;

	public ChunkRegion(ChunkKey rkey){
		key = rkey;
		file = new File(LandDev.SAVE_DIR + "/chunks/" + key.toString() + ".nbt");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		compound = WrapperHolder.read(file);
		setLastAccess();
	}

	public static void save(Chunk_ ck){
		getRegion(ck.region).saveChunk(ck);
	}

	private void saveChunk(Chunk_ ck){
		chunks.put(ck.key, ck);
		JsonMap map = new JsonMap();
		ck.save(map);
		compound.set(ck.key.toString(), JsonTagConverter.map(TagCW.create(), map));
		setLastAccess();
	}

	private void loadChunk(Chunk_ ck){
		chunks.put(ck.key, ck);
		TagCW tag = compound.getCompound(ck.key.toString());
		if(tag.empty()) ck.gendef();
		ck.load(JsonTagConverter.demap(tag));
		ResManager.CHUNKS.put(ck.key, ck);
		setLastAccess();
	}

	private void setLastAccess(){
		last_access = Time.getDate();
	}

	public static Chunk_ getFor(UniChunk uck){
		ChunkKey key = new ChunkKey(uck.chunk.getX(), uck.chunk.getZ());
		ChunkRegion region = getRegion(key.asRegion());
		if(region.chunks.containsKey(key)){
			Chunk_ ck = region.chunks.get(key);
			ck.uck = uck;
			ck.loaded = Time.getDate();
			region.setLastAccess();
			return ck;
		}
		Chunk_ ck = new Chunk_(uck);
		region.loadChunk(ck);
		region.setLastAccess();
		return ck;
	}

	public static void saveAll(){
		for(ChunkRegion value : REGIONS.values()){
			value.save();
		}
	}

	private void save(){
		WrapperHolder.write(compound, file);
	}

	public static void saveRegions(){
		long offset = Time.MIN_MS * 5;
		long date = Time.getDate();
		ArrayList<ChunkKey> remreg = new ArrayList<>();
		for(ChunkRegion region : REGIONS.values()){
			if(region.last_access + offset < date){
				region.chunks.values().removeIf(ck -> {
					if(ck.uck == null && ck.loaded + offset < date){
						region.saveChunk(ck);
						return true;
					}
					return false;
				});
				region.save();
				if(region.chunks.isEmpty()) remreg.add(region.key);
			}
		}
		for(ChunkKey key : remreg) REGIONS.remove(key);
	}

	public static void unload(Chunk_ ck){
		ChunkRegion region = REGIONS.get(ck);
		if(region != null){
			region.saveChunk(ck);
			ck.uck = null;
			ck.loaded = Time.getDate();
		}
	}

	public static Chunk_ get(int x, int z){
		return get(new ChunkKey(x, z));
	}

	public static Chunk_ get(ChunkKey key){
		ChunkRegion region = getRegion(key.asRegion());
		if(region.chunks.containsKey(key)){
			region.setLastAccess();
			return region.chunks.get(key);
		}
		Chunk_ ck = new Chunk_(key);
		region.loadChunk(ck);
		return ck;
	}

	private static ChunkRegion getRegion(ChunkKey key){
		ChunkRegion region = REGIONS.get(key);
		if(region == null) REGIONS.put(key, region = new ChunkRegion(key));
		return region;
	}

}
