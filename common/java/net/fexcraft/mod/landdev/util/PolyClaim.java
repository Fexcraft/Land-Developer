package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

	public static int[] process(UUID uuid, District dis){
		int xn = 0, xx = 0, zn = 0, zx = 0;
		PolyClaimObj obj = get(uuid);
		if(obj.chunks.size() < 2) return new int[]{ 0, 0 };
		if(obj.district >= 0) dis = ResManager.getDistrict(obj.district);
		if(dis.id < 0) return new int[]{ -1, -1 };
		for(ChunkKey key : obj.chunks){
			if(key.x < xn) xn = key.x;
			if(key.x > xx) xx = key.x;
			if(key.z < zn) zn = key.z;
			if(key.z > zx) zx = key.z;
		}
		int w = xx - xn + 1, h = zx - zn + 1;
		int t = 0, p = 0;
		if(obj.chunks.size() == 2){
			t = w * h;
			for(int ox = 0; ox < w; ox++){
				for(int oz = 0; oz < h; oz++){
					p += claimIfPossible(xn + ox, zn + oz, uuid, dis);
				}
			}
			clear(uuid);
			return new int[]{ p, t };
		}
		else{
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			for(int x = 0; x < w; x++) for(int z = 0; z < h; z++) img.setRGB(x, z, Color.BLACK.getRGB());
			Graphics g = img.createGraphics();
			g.setColor(Color.WHITE);
			int[] x = new int[obj.chunks.size()], z = new int[obj.chunks.size()];
			ChunkKey key;
			for(int i = 0; i < obj.chunks.size(); i++){
				key = obj.chunks.get(i);
				x[i] = key.x - xn;
				z[i] = key.z - zn;
			}
			g.drawPolygon(x, z, obj.chunks.size());
			g.fillPolygon(x, z, obj.chunks.size());
			g.dispose();
			try{
				File pcf = new File(LandDev.SAVE_DIR, "polyclaim/");
				if(!pcf.exists()) pcf.mkdirs();
				ImageIO.write(img, "PNG", new File(pcf, Time.getAsString(Time.getDate(), true) + ".png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
			int c;
			for(int xo = 0; xo < w; xo++){
				for(int zo = 0; zo < h; zo++){
					c = img.getRGB(xo, zo);
					if(c == Color.BLACK.getRGB()) continue;
					p += claimIfPossible(xn + xo, zn + zo, uuid, dis);
					t++;
				}
			}
			clear(uuid);
			return new int[]{ p, t };
		}
	}

	private static int claimIfPossible(int x, int z, UUID uuid, District dis){
		Chunk_ ck = ResManager.getChunk(x, z);
		if(ck.district.id < 0){
			ck.created.setClaimer(uuid);
			ck.owner.set(dis.owner.layer(), null, dis.owner.owid);
			ck.sell.price = 0;
			ck.type = ChunkType.NORMAL;
			ck.district = dis;
			dis.chunks++;
			ck.save();
			return 1;
		}
		return 0;
	}

	public static class PolyClaimObj {

		public ArrayList<ChunkKey> chunks = new ArrayList<>();
		public int district = -1;

	}

}
