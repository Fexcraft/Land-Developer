package net.fexcraft.mod.landdev.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.db.JsonNBTConverter;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkRegion {

	public static ConcurrentHashMap<ChunkKey, ChunkRegion> REGIONS = new ConcurrentHashMap<>();
	public ConcurrentHashMap<ChunkKey, Chunk_> chunks = new ConcurrentHashMap<>();
	private NBTTagCompound compound;
	private ChunkKey key;
	private File file;
	public long last_access;

	public ChunkRegion(ChunkKey rkey){
		key = rkey;
		file = new File(LandDev.SAVE_DIR + "/chunks/" + key.toString() + ".nbt");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try{
			compound = CompressedStreamTools.read(file);
		}
		catch(IOException e){
			if(file.exists()) e.printStackTrace();
		}
		if(compound == null) compound = new NBTTagCompound();
	}

	public static void save(Chunk_ ck){
		ChunkRegion region = REGIONS.get(ck.region);
		if(region == null) REGIONS.put(ck.region, region = new ChunkRegion(ck.region));
		region.chunks.put(ck.key, ck);
		JsonMap map = new JsonMap();
		ck.save(map);
		region.compound.setTag(ck.key.toString(), JsonNBTConverter.map(new NBTTagCompound(), map));
		region.last_access = Time.getDate();
	}

	public static Chunk_ load(Chunk chunk){
		Chunk_ ck = get(chunk);
		ChunkRegion region = REGIONS.get(ck.region);
		if(region == null) REGIONS.put(ck.region, region = new ChunkRegion(ck.region));
		region.chunks.put(ck.key, ck);
		ck.load(JsonNBTConverter.demap(region.compound.getCompoundTag(ck.key.toString())));
		ResManager.CHUNKS.put(ck.key, ck);
		return ck;
	}

	private static Chunk_ get(Chunk chunk){
		ChunkKey key = new ChunkKey(chunk.x, chunk.z);
		ChunkRegion region = REGIONS.get(key.asRegion());
		return region == null || !region.chunks.containsKey(key) ? new Chunk_(chunk) : region.chunks.get(key);
	}

	public static void saveAll(){
		for(ChunkRegion value : REGIONS.values()){
			value.save();
		}
	}

	public static void unload(Chunk_ ck){
		ChunkRegion region = REGIONS.get(ck);
		if(region != null){
			region.chunks.remove(ck.key);
			region.last_access = Time.getDate();
		}
	}

	private void save(){
		try{
			CompressedStreamTools.write(compound, file);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}
