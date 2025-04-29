package net.fexcraft.mod.landdev.data.chunk;

import static net.fexcraft.lib.common.math.Time.getAsString;
import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.CHUNK_CUSTOMTAX;
import static net.fexcraft.mod.landdev.data.PermAction.MANAGE_DISTRICT;
import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank.Action;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.uni.UniChunk;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Chunk_ implements Saveable, Layer, LDUIModule {
	
	public final ChunkKey key;
	public final ChunkKey region;
	public Createable created = new Createable(true);
	public ChunkType type = ChunkType.NORMAL;
	public Sellable sell = new Sellable(this);
	public ChunkLink link = null;
	public AccessList access = new AccessList();
	public ChunkOwner owner = new ChunkOwner();
	public Taxable tax = new Taxable(this);
	public ChunkLabel label = new ChunkLabel();
	public ExternalData external = new ExternalData(this);
	public District district;
	public long loaded;
	public UniChunk uck;

	public Chunk_(UniChunk ck){
		key = new ChunkKey(ck.chunk.getX(), ck.chunk.getZ());
		region = key.asRegion();
		uck = ck;
	}

	public Chunk_(ChunkKey ckkey){
		key = ckkey;
		region = key.asRegion();
		loaded = Time.getDate();
	}

	@Override
	public void save(JsonMap map){
		map.add("id", key.toString());
		created.save(map);
		owner.save(map);
		map.add("type", type.l1());
		sell.save(map);
		if(link != null) link.save(map);
		access.save(map);
		tax.save(map);
		label.save(map);
		if(district != null) map.add("district", district.id);
		external.save(map);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		owner.load(map);
		type = ChunkType.l1(map.getString("type", "N"));
		sell.load(map);
		if(map.has("linked")){
			link = new ChunkLink(this);
			link.load(map);
		}
		access.load(map);
		tax.load(map);
		label.load(map);
		district = ResManager.getDistrict(map.getInteger("district", -1));
		if(district.disbanded) district = ResManager.getDistrict(-1);
		external.load(map);
		TaxSystem.taxChunk(this, null, false);
	}
	
	@Override
	public void gendef(){
		district = ResManager.getDistrict(-1);
		external.gendef();
	}
	
	@Override
	public String saveId(){
		return key.toString();
	}
	
	@Override
	public String saveTable(){
		return "chunks";
	}

	@Override
	public void save(){
		ChunkRegion.save(this);
		if(uck != null) uck.chunk.markChanged();
	}

	@Override
	public Layers getLayer(){
		return Layers.CHUNK;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.DISTRICT;
	}

	public boolean can_manage(LDPlayer player){
		if(player.adm) return true;
		UUID uuid = player.uuid;
		if(owner.playerchunk && owner.player.equals(uuid)) return true;
		else if(owner.owner.is(Layers.DISTRICT) && district.can(MANAGE_DISTRICT, uuid)) return true;
		else if(owner.owner.is(Layers.MUNICIPALITY) && district.owner.manageable().isManager(uuid)) return true;
		//TODO
		return false;
	}
	
	public static final int
		UI_LINK = 1,
		UI_LINKS = 2,
		UI_LINKED = 3,
		UI_TYPE = 4,
		UI_OWNER = 5,
		UI_PRICE = 6,
		UI_SET_PRICE = 7,
		UI_TAX = 8,
		UI_ACC_PLAYER = 9,
		UI_ACC_COMPANY = 10
		;

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("chunk.title");
		switch(container.pos.x){
		case UI_MAIN:
			boolean canman = can_manage(container.ldp);// || container.ldp.adm;
			resp.addRow("key", ELM_GENERIC, key.comma());
			if(LDConfig.CHUNK_LINK_LIMIT > 0){
				if(link == null){
					if(canman) resp.addButton("link", ELM_GENERIC, ADD);
					else resp.addRow("link", ELM_GENERIC, EMPTY);
				}
				else if(link.linked != null){
					resp.addButton("links", ELM_GENERIC, LIST, link.linked.size());
				}
				else if(link.root_key != null){
					resp.addButton("linked", ELM_GENERIC, canman ? OPEN : EMPTY, link.root_key.comma());
				}
			}
			resp.addRow("type", ELM_GENERIC, canman ? OPEN : EMPTY, canman, type.lang());
			resp.addButton("district", ELM_GENERIC, OPEN, district.name());
			resp.addBlank();
			resp.addButton("owner", ELM_GENERIC, OPEN, owner.name());
			if(sell.price > 0){
				resp.addRow("price", ELM_GENERIC, canman ? EMPTY : OPEN, !canman, sell.price_formatted());
			}
			if(canman){
				resp.addButton("set_price", ELM_GENERIC, OPEN);
			}
			resp.addButton("tax", ELM_GENERIC, OPEN, getWorthAsString(tax.custom_tax == 0 ? district.tax() : tax.custom_tax));
			resp.addBlank();
			resp.addRow("access_interact", ELM_GENERIC, canman ? access.interact ? ENABLED : DISABLED : EMPTY, canman, access.interact ? LANG_YES : LANG_NO);
			resp.addButton("access_player", ELM_GENERIC, LIST, access.players.size());
			resp.addButton("access_company", ELM_GENERIC, LIST, access.companies.size());
			return;
		case UI_LINK:
			resp.setTitle("chunk.link.title");
			resp.addRow("link.info0", ELM_YELLOW, BLANK);
			resp.addRow("link.info1", ELM_YELLOW, BLANK);
			resp.addRow("link.key", ELM_GENERIC, BLANK);
			resp.addField("link.field");
			resp.addButton("link.submit", ELM_BLUE, OPEN);
			resp.setFormular();
			return;
		case UI_LINKS:
			resp.setTitle("chunk.links.title");
			if(link.linked == null) return;
			resp.addButton("links.submit", ELM_BLUE, OPEN, key.comma());
			boolean first = true;
			for(int i = 0; i < link.linked.size(); i++){
				resp.addButton("links.key" + i, ELM_BLUE, radio(first), "!!!" + link.linked.get(i).comma());
				first = false;
			}
			resp.setFormular();
			return;
		case UI_LINKED:
			resp.setTitle("chunk.linked.title");
			resp.addButton("linked.key", ELM_GENERIC, OPEN, "!!!" + link.root_key.comma());
			resp.addButton("linked.disconnect", ELM_RED, REM, key.comma());
			return;
		case UI_TYPE:
			resp.setTitle("chunk.select_type.title");
			resp.addRow("key", ELM_GENERIC, BLANK, key.comma());
			resp.addRadio("type.normal", ELM_BLUE, type == ChunkType.NORMAL);
			resp.addRadio("type.private", ELM_BLUE, type == ChunkType.PRIVATE);
			resp.addRadio("type.restricted", ELM_BLUE, type == ChunkType.RESTRICTED);
			resp.addRadio("type.public", ELM_BLUE, type == ChunkType.PUBLIC);
			if(container.ldp.adm){
				resp.addRadio("type.locked", ELM_YELLOW, type == ChunkType.LOCKED);
			}
			resp.addButton("select_type.submit", ELM_GENERIC, OPEN);
			resp.setFormular();
			return;
		case UI_OWNER:
			resp.setTitle("chunk.set_owner.title");
			resp.addRow("key", ELM_GENERIC, BLANK, key.comma());
			resp.addRow("set_owner.warning0", ELM_RED);
			resp.addRow("set_owner.warning1", ELM_RED);
			resp.addRow("set_owner.warning2", ELM_RED);
			resp.addRow("set_owner.warning3", ELM_RED);
			resp.addRadio("set_owner.district", ELM_BLUE, owner.owner == Layers.DISTRICT);
			if(!district.owner.is_county) resp.addRow("set_owner.municipality", ELM_BLUE, owner.owner == Layers.MUNICIPALITY);
			resp.addRadio("set_owner.county", ELM_BLUE, owner.owner == Layers.COUNTY);
			resp.addRadio("set_owner.region", ELM_BLUE, owner.owner == Layers.REGION);
			resp.addRadio("set_owner.none", ELM_BLUE, owner.owner == Layers.NONE);
			resp.addButton("set_owner.submit", ELM_GENERIC, OPEN);
			resp.setFormular();
			return;
		case UI_PRICE:
			resp.setTitle("chunk.buy.title");
			resp.addRow("key", ELM_GENERIC, BLANK, key.comma());
			resp.addRow("buy.info", ELM_YELLOW, BLANK);
			resp.addButton("buy.self", ELM_BLUE, RADIO_CHECKED);
			resp.addButton("buy.company", ELM_BLUE, RADIO_UNCHECKED);
			resp.addButton("buy.district", ELM_BLUE, RADIO_UNCHECKED);
			if(!district.owner.is_county) resp.addButton("buy.municipality", ELM_BLUE, RADIO_UNCHECKED);
			resp.addButton("buy.county", ELM_BLUE, RADIO_UNCHECKED);
			resp.addButton("buy.region", ELM_BLUE, RADIO_UNCHECKED);
			resp.addButton("buy.payer", ELM_GENERIC, CHECK_UNCHECKED);
			resp.addButton("buy.submit", ELM_GENERIC, OPEN);
			resp.setFormular();
			return;
		case UI_SET_PRICE:
			resp.setTitle("chunk.set_price.title");
			resp.addRow("key", ELM_GENERIC, BLANK, key.comma());
			resp.addField("set_price.field");
			resp.addButton("set_price.submit", ELM_GENERIC, OPEN);
			resp.setFormular();
			return;
		case UI_TAX:
			boolean bool = district.can(CHUNK_CUSTOMTAX, container.ldp.uuid) || container.ldp.adm;
			resp.setTitle("chunk.tax.title");
			resp.addRow("tax.info0", ELM_YELLOW, BLANK);
			resp.addRow("tax.info1", ELM_YELLOW, BLANK);
			resp.addRow("tax.info2", ELM_YELLOW, BLANK);
			resp.addRow("tax.default", ELM_GENERIC, BLANK, getWorthAsString(district.tax()));
			if(bool || tax.custom_tax > 0){
				resp.addRow("tax.custom", ELM_GENERIC, BLANK, getWorthAsString(tax.custom_tax));
			}
			resp.addRow("tax.last_amount", ELM_GENERIC, BLANK, getWorthAsString(tax.last_tax));
			resp.addRow("tax.last_time", ELM_GENERIC, BLANK, getAsString(tax.last_interval));
			if(bool){
				resp.addBlank();
				resp.addRow("set_tax.title", ELM_GENERIC, BLANK);
				resp.addField("set_tax.field", getWorthAsString(tax.custom_tax, false));
				resp.addButton("set_tax.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
			}
			return;
		case UI_ACC_PLAYER:
			resp.setTitle("chunk.access_player.title");
			boolean bcm = can_manage(container.ldp);
			if(bcm){
				resp.addRow("access_player.info", ELM_GREEN, BLANK);
				resp.addField("access_player.field");
				resp.addButton("access_player.add.submit", ELM_GENERIC, OPEN);
				resp.addBlank();
				resp.setFormular();
			}
			if(access.players.isEmpty()){
				resp.addRow("access_player.empty", ELM_YELLOW, BLANK);
			}
			else{
				if(bcm) resp.addButton("access_player.rem.submit", ELM_GENERIC, REM);
				boolean primo = true;
				UUID[] keys = access.players.keySet().toArray(new UUID[0]);
				for(int i = 0; i < access.players.size(); i++){
					resp.addButton("access_player.id_" + keys[i], ELM_BLUE, bcm ? radio(primo) : EMPTY, "!!!" + ResManager.getPlayerName(keys[i]));
					primo = false;
				}
				if(bcm) resp.setFormular();
			}
			return;
		case UI_ACC_COMPANY:
			return;
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		boolean canman = can_manage(container.ldp);
		switch(req.event()){
			case "access_interact":{
				if(!canman) return;
				access.interact = !access.interact;
				container.sendResp();
				return;
			}
			case "link": container.open(UI_LINK); return;
			case "link.submit":{
				if(!canman || LDConfig.CHUNK_LINK_LIMIT == 0 || link != null) return;
				ChunkKey ckk = new ChunkKey(req.getField("link.field"));
				Chunk_ ck = ResManager.getChunk(ckk);
				if(ck == null){
					container.msg("link.notfound");
					return;
				}
				if(ck.link != null && ck.link.root_key != null){
					container.msg("link.issub");
					return;
				}
				ChunkLink ckl = ck.link == null ? new ChunkLink(ck) : ck.link;
				if(ckl.linked == null) ckl.linked = new ArrayList<>();
				if(key.equals(ckk) || ckl.linked.contains(ckk)){
					container.msg("link.islinked");
					return;
				}
				if(ckl.linked.size() > LDConfig.CHUNK_LINK_LIMIT){
					container.msg("link.limit");
					return;
				}
				if(!ckl.validate(key)){
					container.msg("link.noborder");
					return;
				}
				if(!ck.owner.issame(owner)){
					container.msg("link.nosameowner");
					return;
				}
				(ck.link = ckl).linked.add(key);
				link = new ChunkLink(this);
				link.root_key = ck.key;
				ck.save();
				save();
				container.open(UI_MAIN);
				return;
			}
			case "links": container.open(UI_LINKS); return;
			case "links.submit":{
				if(!canman) return;
				ChunkKey key = link.linked.get(req.getRadioInt("links.key"));
				container.open(LDKeys.CHUNK, 0, key.x, key.z);
				return;
			}
			case "linked": container.open(UI_LINKED); return;
			case "linked.key":{
				container.open(LDKeys.CHUNK, 0, link.root_key.x, link.root_key.z);
				return;
			}
			case "linked.disconnect":{
				if(!canman) return;
				Chunk_ lt = ResManager.getChunk(link.root_key);
				lt.link.linked.remove(key);
				if(lt.link.linked.isEmpty()) lt.link = null;
				link = null;
				lt.save();
				save();
				container.open(UI_MAIN);
				return;
			}
			case "type": if(canman) container.open(UI_TYPE); return;
			case "district": container.open(LDKeys.DISTRICT, 0, district.id, 0);
			case "owner": if(canman) container.open(UI_OWNER); return;
			case "price": if(!canman) container.open(UI_PRICE); return;
			case "set_price": container.open(UI_SET_PRICE); return;
			case "tax": container.open(UI_TAX); return;
			case "access_player": container.open(UI_ACC_PLAYER); return;
			case "access_company": container.open(UI_ACC_COMPANY); return;
			//
			case "select_type.submit":{
				if(!canman) return;
				ChunkType type = ChunkType.get(req.getRadio("type."));
				if(type == null) return;
				if(!container.ldp.adm && (type == ChunkType.LOCKED || this.type == ChunkType.LOCKED)) return;
				if(owner.playerchunk && (type == ChunkType.RESTRICTED || type == ChunkType.LOCKED)){
					container.msg("select_type.notforplayerchunks");
					return;
				}
				this.type = type;
				container.open(UI_MAIN);
				return;
			}
			case "set_price.submit":{
				if(!canman) return;
				String[] err = new String[]{ "" };
				String val = req.getField("set_price.field");
				long value = LDConfig.format_price(err, val);
				if(err[0].length() > 0){
					container.msg(err[0], false);
				}
				else{
					sell.price = value;
					container.open(UI_MAIN);
				}
				return;
			}
			case "set_owner.submit":{
				if(!canman) return;
				Layers layer = Layers.get(req.getRadio("set_owner."));
				if(!layer.isValidChunkOwner2()) return;
				if(layer.is(Layers.MUNICIPALITY) && district.owner.is_county){
					container.msg("landdev.district.not_part_of_municipality", false);
					return;
				}
				owner.set(layer, null, getLayerId(layer));
				sell.price = 0;
				container.open(UI_MAIN);
				return;
			}
			case "buy.submit":{
				String radio = req.getRadio();
				Layers layer = radio.endsWith(".self") ? Layers.PLAYER : Layers.get(radio.replace("buy.", ""));
				if(!layer.isValidChunkOwner()) return;
				if(layer.is(Layers.MUNICIPALITY) && district.owner.is_county){
					container.msg("landdev.district.not_part_of_municipality", false);
					return;
				}
				boolean npp = req.getCheck("buy.payer");
				Account account = npp ? district.getLayerAccount(layer, container) : container.ldp.account;
				if(account == null) return;
				if(account.getBalance() < sell.price){
					container.msg("buy.notenoughmoney");
					return;
				}
				if(!account.getBank().processAction(Action.TRANSFER, container.ldp.entity, account, sell.price, owner.getAccount(this))) return;
				owner.set(layer, layer.is(Layers.PLAYER) ? container.ldp.uuid : null, district.getLayerId(layer));
				sell.price = 0;
				container.open(UI_MAIN);
				return;
			}
			case "set_tax.submit":{
				if(!district.can(CHUNK_CUSTOMTAX, container.ldp.uuid) && !container.ldp.adm) return;
				String val = req.getField("set_tax.field");
				String[] err = new String[]{ "" };
				long value = LDConfig.format_price(err, val);
				if(err[0].length() > 0){
					container.msg(err[0], false);
				}
				else{
					tax.custom_tax = value;
					container.open(UI_TAX);
				}
				return;
			}
			case "access_player.add.submit":{
				if(!can_manage(container.ldp)) return;
				LDPlayer other = req.getPlayerField("access_player.field", true);
				if(other == null){
					container.msg("access_player.notfound");
					return;
				}
				access.players.put(other.uuid, 0l);
				container.open(UI_ACC_PLAYER);
				return;
			}
			case "access_player.rem.submit":{
				if(!can_manage(container.ldp)) return;
				UUID uuid = UUID.fromString(req.getRadio("access_player.id_"));
				if(!access.players.containsKey(uuid)){
					container.msg("access_player.notfound");
					return;
				}
				access.players.remove(uuid);
				container.open(UI_ACC_PLAYER);
				return;
			}
		}
		external.on_interact(container, req);
	}

	private int getLayerId(Layers layer){
		switch(layer){
			case COMPANY: return -1;
			case DISTRICT: return district.id;
			case MUNICIPALITY: return district.owner.is_county ? -1 : district.municipality().id;
			case COUNTY: return district.county().id;
			case REGION: return district.county().region.id;
			default: return -1;
		}
	}

	public void sendToOwner(Mail mail){
		if(owner.playerchunk){
			LDPlayer player = ResManager.getPlayer(owner.player, true);
			player.addMailAndSave(mail);
			return;
		}
		switch(owner.layer()){
			case COMPANY:
				//TODO
				break;
			case DISTRICT:
				district.mail.add(mail);
				district.save();
				break;
			case MUNICIPALITY:
				if(district.owner.is_county) return;
				district.municipality().mail.add(mail);
				district.municipality().save();
				break;
			case COUNTY:
				if(!district.owner.is_county) return;
				district.county().mail.add(mail);
				district.county().save();
				break;
			case REGION:
				district.region().mail.add(mail);
				district.region().save();
				break;
			case NONE: return;
		}
	}

	public boolean locked(){
		return type == ChunkType.LOCKED;
	}

}
