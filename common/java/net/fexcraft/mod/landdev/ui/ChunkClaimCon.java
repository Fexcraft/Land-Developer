package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.HashMap;

import static net.fexcraft.mod.landdev.data.PermAction.CHUNK_CLAIM;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkClaimCon extends ContainerInterface {

	protected ChunkData[][] chunks = new ChunkData[15][15];
	protected HashMap<Integer, DisData> dists = new HashMap<>();
	protected ChunkClaimUI cui;
	protected District district;
	protected LDPlayer ldp;

	public ChunkClaimCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		ldp = ResManager.getPlayer(ply);
		ldp = ResManager.getPlayer(player);
		for(int i = 0; i < chunks.length; i++) for(int k = 0; k < chunks[i].length; k++) chunks[i][k] = new ChunkData();
		if(!ply.entity.isOnClient()){
			district = ResManager.getDistrict(pos.y);
		}
	}

	public ContainerInterface set(UserInterface ui){
		cui = (ChunkClaimUI)ui;
		return super.set(ui);
	}

	@Override
	public void packet(TagCW pkt, boolean client){
		if(client){
			if(pkt.has("ckdata")){
				TagLW list = pkt.getList("cks");
				for(int i = 0; i < 15; i++){
					for(int k = 0; k < 15; k++){
						chunks[i][k] = new ChunkData(list.getCompound(k + i * 15), i - 7 + pos.x, k - 7 + pos.z);
					}
				}
				dists.clear();
				list = pkt.getList("dis");
				list.forEach(tag -> dists.put(tag.getInteger("i"), new DisData(tag)));
			}
			if(pkt.has("msg")){
				cui.texts.get("title").value(pkt.getString("msg"));
				cui.texts.get("title").translate();
			}
		}
		else{
			if(pkt.has("sync")){
				sendSync(TagCW.create());
			}
			else if(pkt.has("claim")){
				int[] key = pkt.getIntArray("claim");
				Chunk_ chunk = ResManager.getChunk(key[0] - 7 + pos.x, key[1] - 7 + pos.z);
				if(chunk == null) return;
				TagCW com = TagCW.create();
				if(!ldp.adm && !district.can(CHUNK_CLAIM, player.entity.getUUID())){
					com.set("msg", "landdev.gui.claim.no_perm_district");
					SEND_TO_CLIENT.accept(com, player);
					SEND_TO_CLIENT.accept(com, player);
					return;
				}
				if(chunk.district.id == district.id){
					com.set("msg", "landdev.gui.claim.part_of_district");
					SEND_TO_CLIENT.accept(com, player);
					return;
				}
				if(chunk.district.id >= 0){
					if(chunk.district.owner.is_county){
						if(district.owner.is_county){
							com.set("msg", "landdev.gui.claim.county_chunk");
							SEND_TO_CLIENT.accept(com, player);
							return;
						}
						else if(chunk.district.county().id != district.county().id){
							com.set("msg", "landdev.gui.claim.county_other");
							SEND_TO_CLIENT.accept(com, player);
							return;
						}
					}
					else{
						if(district.owner.is_county){
							com.set("msg", "landdev.gui.claim.municipality_chunk");
							SEND_TO_CLIENT.accept(com, player);
							return;
						}
						else if(chunk.district.municipality().id != district.municipality().id){
							com.set("msg", "landdev.gui.claim.municipality_other");
							SEND_TO_CLIENT.accept(com, player);
							return;
						}
					}
					if(!district.owner.is_county && !chunk.district.norms.get("municipality-can-claim").bool()){
						com.set("msg", "landdev.gui.claim.no_municipality_claim");
						SEND_TO_CLIENT.accept(com, player);
						return;
					}
				}
				else if(chunk.sell.price == 0 && chunk.district.id >= 0){
					com.set("msg", "landdev.gui.claim.not_for_sale");
					SEND_TO_CLIENT.accept(com, player);
					return;
				}
				long price = chunk.sell.price > 0 ? chunk.sell.price : LDConfig.DEFAULT_CHUNK_PRICE;
				if(price > 0){
					if(!district.owner.account().getBank().processAction(Bank.Action.TRANSFER, player.entity, district.account(), price, SERVER_ACCOUNT)) return;
				}
				com.set("msg", "landdev.gui.claim.pass");
				if(chunk.district.id < 0) chunk.created.setClaimer(player.entity.getUUID());
				if(chunk.district.id > -1) chunk.district.chunks -= 1;
				chunk.district = district;
				chunk.district.chunks += 1;
				chunk.sell.price = 0;
				if(!chunk.owner.layer().isPlayerBased()){
					chunk.owner.set(district.owner.is_county ? Layers.COUNTY : Layers.MUNICIPALITY, null, district.owner.owid);
				}
				chunk.type = ChunkType.NORMAL;
				chunk.save();
				sendSync(com);
			}
		}
	}

	private void sendSync(TagCW compound){
		compound.set("ckdata", true);
		TagLW list = TagLW.create();
		HashMap<Integer, District> dis = new HashMap<>();
		Chunk_ chunk = null;
		int di = -1;
		for(int i = -7; i < 8; i++){
			for(int k = -7; k < 8; k++){
				chunk = ResManager.getChunk(pos.x + i, pos.z + k);
				TagCW com = TagCW.create();
				if(chunk == null){
					com.set("d", di = -10);
					com.set("c", 0xffffff);
					com.set("p", 0);
				}
				else{
					com.set("d", di = chunk.district.id);
					com.set("c", chunk.district.color.getInteger());
					com.set("p", chunk.sell.price == 0 && chunk.district.id == -1 ? LDConfig.DEFAULT_CHUNK_PRICE : chunk.sell.price);
				}
				list.add(com);
				if(!dis.containsKey(di) && di != -10) dis.put(di, ResManager.getDistrict(di));
			}
		}
		compound.set("cks", list);
		list = TagLW.create();
		for(District d : dis.values()){
			TagCW com = TagCW.create();
			com.set("i", d.id);
			com.set("n", d.name());
			com.set("o", d.owner.owid);
			com.set("m", d.owner.name());
			com.set("c", d.owner.is_county);
			list.add(com);
		}
		if(dis.containsKey(-10)){
			TagCW com = TagCW.create();
			com.set("i", -10);
			com.set("n", "not loaded");
			com.set("o", -10);
			com.set("m", "Void");
			com.set("c", true);
			list.add(com);
		}
		compound.set("dis", list);
		SEND_TO_CLIENT.accept(compound, player);
	}

	public static class ChunkData {

		protected RGB color = new RGB();
		protected long price;
		protected int dis, x, z;

		public ChunkData(TagCW com, int x, int z){
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

		public DisData(TagCW com){
			id = com.getInteger("i");
			name = com.getString("n");
			cid = com.getInteger("o");
			cname = com.getString("m");
			county = com.getBoolean("c");
		}

	}
	
}
