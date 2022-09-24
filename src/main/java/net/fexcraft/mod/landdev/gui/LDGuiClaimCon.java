package net.fexcraft.mod.landdev.gui;

import java.util.HashMap;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LDGuiClaimCon extends GenericContainer {

	protected ChunkData[][] chunks = new ChunkData[15][15];
	protected HashMap<Integer, DisData> dists = new HashMap<>();
	public final int x, z, dis;
	@SideOnly(Side.CLIENT)
	public LDGuiClaim gui;

	public LDGuiClaimCon(EntityPlayer player, int x, int y, int z){
		super(player);
		this.x = x;
		dis = y;
		this.z = z;
		for(int i = 0; i < chunks.length; i++) for(int k = 0; k < chunks[i].length; k++) chunks[i][k] = new ChunkData();
		if(player.world.isRemote){
			NBTTagCompound com = new NBTTagCompound();
			com.setBoolean("sync", true);
			send(Side.SERVER, com);
		}
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side.isServer()){
			if(packet.hasKey("sync")){
				NBTTagCompound compound = new NBTTagCompound();
				compound.setBoolean("ckdata", true);
				NBTTagList list = new NBTTagList();
				HashMap<Integer, District> dis = new HashMap<>();
				Chunk_ chunk = null;
				for(int i = -7; i < 8; i++){
					for(int k = -7; k < 8; k++){
						chunk = ResManager.getChunk(x + i, z + k);
						NBTTagCompound com = new NBTTagCompound();
						com.setInteger("c", chunk.district.color.getInteger());
						com.setInteger("d", chunk.district.id);
						com.setLong("p", chunk.sell.price);
						list.appendTag(com);
						if(!dis.containsKey(chunk.district.id)) dis.put(chunk.district.id, chunk.district);
					}
				}
				compound.setTag("cks", list);
				list = new NBTTagList();
				for(District d : dis.values()){
					NBTTagCompound com = new NBTTagCompound();
					com.setInteger("i", d.id);
					com.setString("n", d.name());
					com.setInteger("o", d.owner.owid);
					com.setString("m", d.owner.name());
					com.setBoolean("c", d.owner.is_county);
					list.appendTag(com);
				}
				compound.setTag("dis", list);
				send(Side.CLIENT, compound);
			}
		}
		else{
			if(packet.hasKey("ckdata")){
				Print.debug(packet);
				NBTTagList list = (NBTTagList)packet.getTag("cks");
				int i = 0, k = 0;
				for(NBTBase base : list){
					if(i >= 15) i = 0;
					if(k >= 15){
						i++;
						k = 0;
					}
					chunks[i][k] = new ChunkData((NBTTagCompound)base, i - 7 + x, k - 7 + z);
					k++;
				}
				list = (NBTTagList)packet.getTag("dis");
				NBTTagCompound com = null;
				dists.clear();
				for(NBTBase base : list){
					com = (NBTTagCompound)base;
					dists.put(com.getInteger("i"), new DisData(com));
				}
			}
			else if(packet.hasKey("msg")){
				
			}
		}
	}
	
	public static class ChunkData {

		protected RGB color = new RGB();
		protected long price;
		protected int dis, x, z;
		
		public ChunkData(NBTTagCompound com, int x, int z){
			color.packed = com.getInteger("c");
			dis = com.getInteger("d");
			price = com.getLong("p");
			this.x = x;
			this.z = z;
		}
		
		public ChunkData(){}
		
	}
	
	public static class DisData {
		
		protected int id, cid;
		protected String name, cname;
		protected boolean county;
		
		public DisData(NBTTagCompound com){
			id = com.getInteger("i");
			name = com.getString("n");
			cid = com.getInteger("o");
			cname = com.getString("m");
			county = com.getBoolean("c");
		}
		
	}
	
}