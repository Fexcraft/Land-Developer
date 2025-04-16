package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.data.Account;
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
import net.fexcraft.mod.uni.ui.UIKey;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.HashMap;
import java.util.UUID;

import static net.fexcraft.mod.landdev.data.PermAction.*;
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
	protected UUID uuid;
	protected Mode mode;
	protected int prix;

	public ChunkClaimCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		ldp = ResManager.getPlayer(ply);
		for(int i = 0; i < chunks.length; i++) for(int k = 0; k < chunks[i].length; k++) chunks[i][k] = new ChunkData();

	}

	public ContainerInterface set(UserInterface ui){
		cui = (ChunkClaimUI)ui;
		return super.set(ui);
	}

	@Override
	public void init(){
		mode = Mode.fromKey(uiid);
		uuid = player.entity.getUUID();
		if(!player.entity.isOnClient()){
			if(mode != Mode.SELL && !(mode == Mode.BUY && pos.y < 0)) district = ResManager.getDistrict(pos.y);
			if(mode == Mode.SELL) district = ldp.chunk_current.district;;
		}
		prix = pos.y;
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
				if(!ldp.adm && mode.claimtrans() && !adjacent(chunk, district)){
					sendMsg(com, "landdev.gui.claim.not_adjacent");
					return;
				}
				if(!ldp.adm && chunk.locked()){
					sendMsg(com, "landdev.gui.claim.locked");
					return;
				}
				if(mode == Mode.CLAIM){
					if(chunk.district.id > -1){
						sendMsg(com, "landdev.gui.claim.already_claimed");
						return;
					}
					if(isNotClaimable(chunk, district, com)) return;
					//
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
				}
				else if(mode == Mode.TRANSFER){
					if(chunk.district.id < 0){
						sendMsg(com, "landdev.gui.claim.not_claimed");
						return;
					}
					if(isNotTransferable(chunk, district, com)) return;
					//
					com.set("msg", "landdev.gui.claim.transferred");
					if(chunk.district.id > -1) chunk.district.chunks -= 1;
					chunk.district = district;
					chunk.district.chunks += 1;
					chunk.owner.set(district.owner.is_county ? Layers.COUNTY : Layers.MUNICIPALITY, null, district.owner.owid);
					chunk.save();
				}
				else if(mode == Mode.SELL){
					if(isNotOwner(chunk, district, com)) return;
					//
					com.set("msg", "landdev.gui.claim.price_set");
					chunk.sell.price = prix;
					chunk.save();
				}
				else if(mode == Mode.BUY){
					if(canNotBuy(chunk, district, com)) return;
					//
					Layers layer = prix < 0 ? prix == -1 ? Layers.PLAYER : prix < -2 ? Layers.REGION : Layers.COMPANY : district.owner.layer();
					if(!layer.isValidChunkOwner()) return;
					if(layer.is(Layers.MUNICIPALITY) && district.owner.is_county){
						sendMsg(com, "landdev.district.not_part_of_municipality");
						return;
					}
					Account account = prix < 0 ? prix == -1 ? ldp.account : prix < -2 ? district.region().account : null : district.owner.account();
					if(account == null) return;
					if(account.getBalance() < chunk.sell.price){
						sendMsg(com, "landdev.gui.chunk.buy.notenoughmoney");
						return;
					}
					if(!account.getBank().processAction(Bank.Action.TRANSFER, ldp.entity, account, chunk.sell.price, chunk.owner.getAccount(chunk))) return;
					com.set("msg", "landdev.gui.claim.bought");
					if(layer.is(Layers.PLAYER)){
						chunk.owner.set(layer, uuid, 0);
					}
					else if(layer.is(Layers.COMPANY)){
						//TODO
					}
					else if(layer.is(Layers.REGION)){
						chunk.owner.set(layer, null, prix);
					}
					else{
						chunk.owner.set(layer, null, district.getLayerId(layer));
					}
					chunk.sell.price = 0;
					chunk.save();
				}
				sendSync(com);
			}
		}
	}

	private void sendMsg(TagCW com, String str){
		com.set("msg", str);
		SEND_TO_CLIENT.accept(com, player);
	}

	private boolean adjacent(Chunk_ chunk, District district){
		Chunk_ o = ResManager.getChunk(chunk.key.x - 1, chunk.key.z);
		if(o.district.id == district.id) return true;
		o = ResManager.getChunk(chunk.key.x + 1, chunk.key.z);
		if(o.district.id == district.id) return true;
		o = ResManager.getChunk(chunk.key.x, chunk.key.z - 1);
		if(o.district.id == district.id) return true;
		o = ResManager.getChunk(chunk.key.x, chunk.key.z + 1);
		if(o.district.id == district.id) return true;
		return false;
	}

	private boolean sameDistrict(Chunk_ chunk, District district, TagCW com){
		if(chunk.district.id == district.id){
			com.set("msg", "landdev.gui.claim.part_of_district");
			SEND_TO_CLIENT.accept(com, player);
			return true;
		}
		return false;
	}

	private boolean isNotClaimable(Chunk_ chunk, District district, TagCW com){
		if(sameDistrict(chunk, district, com)) return true;
		if(!ldp.adm && !district.can(CHUNK_CLAIM, uuid)){
			sendMsg(com, "landdev.gui.claim.no_perm_district");
			return true;
		}
		return false;
	}

	private boolean isNotTransferable(Chunk_ chunk, District district, TagCW com){
		if(sameDistrict(chunk, district, com)) return true;
		if(chunk.district.region().id != district.region().id){
			sendMsg(com, "landdev.gui.claim.transfer.not_same_region");
			return true;
		}
		if(!ldp.adm && !chunk.district.can(MANAGE_DISTRICT, uuid)){
			sendMsg(com, "landdev.gui.claim.transfer.no_perm_district");
			return true;
		}
		return false;
	}

	private boolean isNotOwner(Chunk_ chunk, District district, TagCW com){
		if(chunk.owner.playerchunk && !chunk.owner.player.equals(uuid)){
			sendMsg(com, "landdev.gui.claim.sell.not_owner");
			return true;
		}
		if(!chunk.owner.playerchunk){
			if(!chunk.owner.owner.is(district.owner.layer())){
				sendMsg(com, "landdev.gui.claim.sell.not_owner");
				return true;
			}
			if(!ldp.adm && !chunk.district.can(MANAGE_DISTRICT, uuid)){
				sendMsg(com, "landdev.gui.claim.sell.no_perm");
				return true;
			}
		}
		return false;
	}

	private boolean canNotBuy(Chunk_ chunk, District district, TagCW com){
		if(chunk.sell.price == 0 && chunk.district.id >= 0){
			sendMsg(com, "landdev.gui.claim.buy.not_for_sale");
			return true;
		}
		if(prix < 0){//-1 private, -2 company, -3 region
			if(prix <= -2) return true;
			//TODO
		}
		else{
			if(chunk.district.county() != district.county()){
				sendMsg(com, "landdev.gui.claim.buy.other_county");
				return true;
			}
			if(chunk.district.owner.is_county){
				if(!district.owner.is_county){
					sendMsg(com, "landdev.gui.claim.buy.in_county");
					return true;
				}
			}
			else{
				if(!district.owner.is_county && chunk.district.municipality().id != district.municipality().id){
					sendMsg(com, "landdev.gui.claim.buy.other_municipality");
					return true;
				}
			}
		}
		return false;
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

	public static enum Mode {

		CLAIM, TRANSFER, SELL, BUY;

		public static Mode fromKey(UIKey key){
			switch(key.id){
				case LDKeys.ID_CHUNK_CLAIM: return CLAIM;
				case LDKeys.ID_CHUNK_TRANSFER: return TRANSFER;
				case LDKeys.ID_CHUNK_SELL: return SELL;
				case LDKeys.ID_CHUNK_BUY: return BUY;
			}
			return CLAIM;
		}

		public boolean claimtrans(){
			return this == CLAIM || this == TRANSFER;
		}
	}
	
}
