package net.fexcraft.mod.landdev.data.chunk;

import net.fexcraft.lib.common.math.V3D;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkKey implements Comparable<ChunkKey> {
	
	public int x, z;
	
	public ChunkKey(int x, int z){
		this.x = x;
		this.z = z;
	}
	
	public ChunkKey(String string){
		String[] split = string.contains("_") ? string.split("_") : string.split(",");
		x = Integer.parseInt(split[0].trim());
		z = Integer.parseInt(split[1].trim());
	}

	public ChunkKey(int x, int z, boolean reg){
		this(reg ? (int)Math.floor(x / 32) : x, reg ? (int)Math.floor(z / 32) : z);
	}

	public ChunkKey(V3D pos){
		this((int)pos.x >> 4, (int)pos.z >> 4);
	}

	@Override
	public String toString(){
		return x + "_" + z;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof int[]){
			int[] a = (int[])o;
			return a.length > 1 && x == a[0] && z == a[1];
		}
		else if(o instanceof ChunkKey){
			ChunkKey c = (ChunkKey)o;
			return x == c.x && z == c.z;
		}
		return super.equals(o);
	}

	@Override
	public int compareTo(ChunkKey c){
		if(c.x < x) return -1;
		if(c.x > x){
			if(c.z < z) return -1;
			if(c.z > z) return 1;
		}
		return 0;
	}

	public String comma(){
		return x + ", " + z;
	}

	public ChunkKey asRegion(){
		return new ChunkKey(x, z, true);
	}

}
