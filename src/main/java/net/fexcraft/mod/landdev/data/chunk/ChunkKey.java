package net.fexcraft.mod.landdev.data.chunk;

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
		else return false;
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
	
}
