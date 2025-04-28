package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PolyClaim {

	public static ConcurrentHashMap<UUID, PolyClaimObj> CACHE = new ConcurrentHashMap<>();

	public static void setDis(UUID uuid, int district){
		PolyClaimObj obj = get(uuid);
		obj.district = district;
	}

	public static int selCnk(UUID uuid, Chunk_ chunk){
		PolyClaimObj obj = get(uuid);
		obj.chunks.add(chunk.key);
		return obj.chunks.size();
	}

	public static PolyClaimObj get(UUID uuid){
		return CACHE.computeIfAbsent(uuid, key -> new PolyClaimObj());
	}

	public static void clear(UUID uuid){
		CACHE.remove(uuid);
	}

	public static int[] process(UUID uuid){

		return new int[]{ 0, 0 };
	}

	public static class PolyClaimObj {

		public ArrayList<ChunkKey> chunks = new ArrayList<>();
		public int district = -1;

	}

}
