package net.fexcraft.mod.landdev.data.chunk;

import static net.fexcraft.lib.common.math.Time.getAsString;
import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.gui.GuiHandler.DISTRICT;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.Createable;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.Sellable;
import net.fexcraft.mod.landdev.data.Taxable;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class Chunk_ implements Saveable, Layer, LDGuiModule {
	
	public ChunkKey key;
	public Createable created = new Createable();
	public ChunkType type = ChunkType.NORMAL;
	public Sellable sell = new Sellable(this);
	public ChunkLink link = null;
	public AccessList access = new AccessList();
	public ChunkOwner owner = new ChunkOwner();
	public Taxable tax = new Taxable(this);
	public ChunkLabel label = new ChunkLabel();
	public District district;

	public Chunk_(World world, int x, int z){
		key = new ChunkKey(x, z);
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
		map.add("district", district.id);
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
		district = ResManager.getDistrict(map.getInteger("district", -1), true);
	}
	
	@Override
	public void gendef(){
		district = ResManager.getDistrict(-1, true);
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
	public Layers getLayer(){
		return Layers.CHUNK;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.DISTRICT;
	}

	public boolean can_manage(Player player){
		if(player.adm) return true;
		UUID uuid = player.uuid;
		if(owner.playerchunk && owner.player.equals(uuid)) return true;
		else if(owner.owner.is(Layers.DISTRICT) && district.can(PermAction.ACT_MANAGE_DISTRICT, uuid)) return true;
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
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
		com.setString("title_lang", "chunk.title");
		NBTTagList list = new NBTTagList();
		switch(container.x){
		case UI_MAIN:
			boolean canman = can_manage(container.player());// || container.player.adm;
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			if(Settings.CHUNK_LINK_LIMIT > 0){
				if(link == null){
					addToList(list, "link", ELM_GENERIC, canman ? ICON_ADD : ICON_EMPTY, true, false, null);
				}
				else if(link.linked != null){
					addToList(list, "links", ELM_GENERIC, ICON_LIST, true, false, link.linked.size());
				}
				else if(link.root_key != null){
					addToList(list, "linked", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, true, false, link.root_key.comma());
				}
			}
			addToList(list, "type", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, false, type.lang());
			addToList(list, "district", ELM_GENERIC, ICON_OPEN, true, false, district.name());
			addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			addToList(list, "owner", ELM_GENERIC, ICON_OPEN, true, false, owner.name());
			if(sell.price > 0){
				addToList(list, "price", ELM_GENERIC, canman ? ICON_EMPTY : ICON_OPEN, true, false, sell.price_formatted());
			}
			if(canman){
				addToList(list, "set_price", ELM_GENERIC, ICON_OPEN, true, false, null);
			}
			addToList(list, "tax", ELM_GENERIC, ICON_OPEN, true, false, getWorthAsString(tax.custom_tax == 0 ? district.tax() : tax.custom_tax));
			addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			addToList(list, "access_interact", ELM_GENERIC, canman ? access.interact ? ICON_ENABLED : ICON_DISABLED : ICON_EMPTY, canman, false, access.interact ? LANG_YES : LANG_NO);
			addToList(list, "access_player", ELM_GENERIC, ICON_LIST, true, false, access.players.size());
			addToList(list, "access_company", ELM_GENERIC, ICON_LIST, true, false, access.companies.size());
			break;
		case UI_LINK:
			com.setString("title_lang", "chunk.link.title");
			addToList(list, "link.info0", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "link.info1", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "link.key", ELM_GENERIC, ICON_BLANK, false, false, null);
			addToList(list, "link.field", ELM_BLANK, ICON_BLANK, false, true, null);
			addToList(list, "link.submit", ELM_BLUE, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_LINKS:
			com.setString("title_lang", "chunk.links.title");
			if(link.linked == null) break;
			addToList(list, "links.submit", ELM_BLUE, ICON_OPEN, true, false, key.comma());
			boolean first = true;
			for(int i = 0; i < link.linked.size(); i++){
				addToList(list, "links.key" + i, ELM_BLUE, radio(first), true, false, "!!!" + link.linked.get(i).comma());
				first = false;
			}
			com.setBoolean("form", true);
			break;
		case UI_LINKED:
			com.setString("title_lang", "chunk.linked.title");
			addToList(list, "linked.key", ELM_GENERIC, ICON_OPEN, true, false, "!!!" + link.root_key.comma());
			addToList(list, "linked.disconnect", ELM_RED, ICON_REM, true, false, key.comma());
			break;
		case UI_TYPE:
			com.setString("title_lang", "chunk.select_type.title");
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			addToList(list, "type.normal", ELM_BLUE, radio(type == ChunkType.NORMAL), true, false, null);
			addToList(list, "type.private", ELM_BLUE, radio(type == ChunkType.PRIVATE), true, false, null);
			addToList(list, "type.restricted", ELM_BLUE, radio(type == ChunkType.RESTRICTED), true, false, null);
			addToList(list, "type.public", ELM_BLUE, radio(type == ChunkType.PUBLIC), true, false, null);
			addToList(list, "select_type.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_OWNER:
			com.setString("title_lang", "chunk.set_owner.title");
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			addToList(list, "set_owner.warning0", ELM_RED, ICON_BLANK, false, false, null);
			addToList(list, "set_owner.warning1", ELM_RED, ICON_BLANK, false, false, null);
			addToList(list, "set_owner.warning2", ELM_RED, ICON_BLANK, false, false, null);
			addToList(list, "set_owner.warning3", ELM_RED, ICON_BLANK, false, false, null);
			addToList(list, "set_owner.district", ELM_BLUE, radio(owner.owner == Layers.DISTRICT), true, false, null);
			if(!district.owner.is_county) addToList(list, "set_owner.municipality", ELM_BLUE, radio(owner.owner == Layers.MUNICIPALITY), true, false, null);
			addToList(list, "set_owner.county", ELM_BLUE, radio(owner.owner == Layers.COUNTY), true, false, null);
			addToList(list, "set_owner.state", ELM_BLUE, radio(owner.owner == Layers.STATE), true, false, null);
			addToList(list, "set_owner.none", ELM_BLUE, radio(owner.owner == Layers.NONE), true, false, null);
			addToList(list, "set_owner.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_PRICE:
			com.setString("title_lang", "chunk.buy.title");
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			addToList(list, "buy.info", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "buy.self", ELM_BLUE, ICON_RADIOBOX_CHECKED, true, false, null);
			addToList(list, "buy.company", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.district", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			if(!district.owner.is_county) addToList(list, "buy.municipality", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.county", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.state", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.payer", ELM_GENERIC, ICON_CHECKBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_SET_PRICE:
			com.setString("title_lang", "chunk.set_price.title");
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			addToList(list, "set_price.field", ELM_BLANK, ICON_BLANK, false, true, null);
			addToList(list, "set_price.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_TAX:
			boolean bool = district.can(PermAction.ACT_SET_TAX_CHUNK_CUSTOM, container.player.uuid) || container.player.adm;
			com.setString("title_lang", "chunk.tax.title");
			addToList(list, "tax.info0", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "tax.info1", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "tax.info2", ELM_YELLOW, ICON_BLANK, false, false, null);
			addToList(list, "tax.default", ELM_GENERIC, ICON_BLANK, false, false, getWorthAsString(district.tax()));
			if(bool || tax.custom_tax > 0){
				addToList(list, "tax.custom", ELM_GENERIC, ICON_BLANK, false, false, getWorthAsString(tax.custom_tax));
			}
			addToList(list, "tax.last_amount", ELM_GENERIC, ICON_BLANK, false, false, getWorthAsString(tax.last_tax));
			addToList(list, "tax.last_time", ELM_GENERIC, ICON_BLANK, false, false, getAsString(tax.last_interval));
			if(bool){
				addToList(list, "set_tax.spacer", ELM_BLANK, ICON_BLANK, false, false, null);
				addToList(list, "set_tax.title", ELM_GENERIC, ICON_BLANK, false, false, null);
				addToList(list, "set_tax.field", ELM_BLANK, ICON_BLANK, false, true, getWorthAsString(tax.custom_tax, false));
				addToList(list, "set_tax.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
				com.setBoolean("form", true);
			}
			break;
		case UI_ACC_PLAYER:
			com.setString("title_lang", "chunk.access_player.title");
			boolean bcm = can_manage(container.player);
			if(bcm){
				addToList(list, "access_player.info", ELM_GREEN, ICON_BLANK, false, false, null);
				addToList(list, "access_player.field", ELM_BLANK, ICON_BLANK, false, true, null);
				addToList(list, "access_player.add.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
				addToList(list, "access_player.spacer", ELM_BLANK, ICON_BLANK, false, false, null);
				com.setBoolean("form", true);
			}
			if(access.players.isEmpty()){
				addToList(list, "access_player.empty", ELM_YELLOW, ICON_BLANK, false, false, null);
			}
			else{
				if(bcm) addToList(list, "access_player.rem.submit", ELM_GENERIC, ICON_REM, true, false, null);
				boolean primo = true;
				UUID[] keys = access.players.keySet().toArray(new UUID[0]);
				for(int i = 0; i < access.players.size(); i++){
					addToList(list, "access_player.id_" + keys[i], ELM_BLUE, bcm ? radio(primo) : ICON_EMPTY, true, false, "!!!" + ResManager.getPlayerName(keys[i]));
					primo = false;
				}
				if(bcm) com.setBoolean("form", true);
			}
			break;
		case UI_ACC_COMPANY:
			break;
		}
		com.setTag("elements", list);
	}

	@Override
	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index){
		boolean canman = can_manage(container.player());
		switch(index){
			case "access_interact":{
				if(!canman) return;
				access.interact = !access.interact;
				container.sendSync();
				return;
			}
			case "link": container.open(UI_LINK); return;
			case "link.submit":{
				if(!canman || Settings.CHUNK_LINK_LIMIT == 0 || link != null) return;
				ChunkKey ckk = new ChunkKey(packet.getCompoundTag("fields").getString("link.field"));
				Chunk_ ck = ResManager.getChunk(ckk);
				if(ck == null){
					container.sendMsg("link.notfound");
					return;
				}
				if(ck.link != null && ck.link.root_key != null){
					container.sendMsg("link.issub");
					return;
				}
				ChunkLink ckl = ck.link == null ? new ChunkLink(ck) : ck.link;
				if(ckl.linked == null) ckl.linked = new ArrayList<>();
				if(key.equals(ckk) || ckl.linked.contains(ckk)){
					container.sendMsg("link.islinked");
					return;
				}
				if(ckl.linked.size() > Settings.CHUNK_LINK_LIMIT){
					container.sendMsg("link.limit");
					return;
				}
				if(!ckl.validate(key)){
					container.sendMsg("link.noborder");
					return;
				}
				if(!ck.owner.issame(owner)){
					container.sendMsg("link.nosameowner");
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
				ChunkKey key = link.linked.get(Integer.parseInt(packet.getString("radiobox").replace("links.key", "")));
				player.openGui(GuiHandler.CHUNK, 0, key.x, key.z);
				return;
			}
			case "linked": container.open(UI_LINKED); return;
			case "linked.key":{
				container.open(GuiHandler.CHUNK, 0, link.root_key.x, link.root_key.z);
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
			case "district": container.open(DISTRICT, 0, district.id, 0);
			case "owner": if(canman) container.open(UI_OWNER); return;
			case "price": if(!canman) container.open(UI_PRICE); return;
			case "set_price": container.open(UI_SET_PRICE); return;
			case "tax": container.open(UI_TAX); return;
			case "access_player": container.open(UI_ACC_PLAYER); return;
			case "access_company": container.open(UI_ACC_COMPANY); return;
			//
			case "select_type.submit":{
				if(!canman) return;
				ChunkType type = ChunkType.get(packet.getString("radiobox").replace("type.", ""));
				if(type == null) return;
				if(owner.playerchunk && type == ChunkType.RESTRICTED){
					container.sendMsg("select_type.notforplayerchunks");
					return;
				}
				this.type = type;
				container.open(UI_MAIN);
				return;
			}
			case "set_price.submit":{
				if(!canman) return;
				String[] err = new String[]{ "" };
				String val = packet.getCompoundTag("fields").getString("set_price.field");
				long value = Settings.format_price(err, val);
				if(err[0].length() > 0){
					container.sendMsg(err[0], false);
				}
				else{
					sell.price = value;
					container.open(UI_MAIN);
				}
				return;
			}
			case "set_owner.submit":{
				if(!canman) return;
				Layers layer = Layers.get(packet.getString("radiobox").replace("set_owner.", ""));
				if(!layer.isValidChunkOwner2()) return;
				if(layer.is(Layers.MUNICIPALITY) && district.owner.is_county){
					container.sendMsg("landdev.district.not_part_of_municipality", false);
					return;
				}
				owner.set(layer, null, getLayerId(layer));
				sell.price = 0;
				container.open(UI_MAIN);
				return;
			}
			case "buy.submit":{
				String radio = packet.getString("radiobox");
				Layers layer = radio.endsWith(".self") ? Layers.PLAYER : Layers.get(radio.replace("buy.", ""));
				if(!layer.isValidChunkOwner()) return;
				if(layer.is(Layers.MUNICIPALITY) && district.owner.is_county){
					container.sendMsg("landdev.district.not_part_of_municipality", false);
					return;
				}
				boolean npp = packet.getCompoundTag("checkboxes").getBoolean("buy.payer");
				Account account = npp ? district.getLayerAccount(layer, container, player) : player.account;
				if(account == null) return;
				if(account.getBalance() < sell.price){
					container.sendMsg("buy.notenoughmoney");
					return;
				}
				Bank bank = DataManager.getBank(account.getBankId(), false, true);
				if(!bank.processAction(Action.TRANSFER, player.entity, account, sell.price, owner.getAccount(this))) return;
				owner.set(layer, layer.is(Layers.PLAYER) ? player.uuid : null, district.getLayerId(layer));
				sell.price = 0;
				container.open(UI_MAIN);
				return;
			}
			case "set_tax.submit":{
				if(!district.can(PermAction.ACT_SET_TAX_CHUNK_CUSTOM, container.player.uuid) && !container.player.adm) return;
				String val = packet.getCompoundTag("fields").getString("set_tax.field");
				String[] err = new String[]{ "" };
				long value = Settings.format_price(err, val);
				if(err[0].length() > 0){
					container.sendMsg(err[0], false);
				}
				else{
					tax.custom_tax = value;
					container.open(UI_TAX);
				}
				return;
			}
			case "access_player.add.submit":{
				if(!can_manage(container.player)) return;
				Player other = ResManager.getPlayer(packet.getCompoundTag("fields").getString("access_player.field"), true);
				if(other == null){
					container.sendMsg("access_player.notfound");
					return;
				}
				access.players.put(other.uuid, 0l);
				container.open(UI_ACC_PLAYER);
				return;
			}
			case "access_player.rem.submit":{
				if(!can_manage(container.player)) return;
				UUID uuid = UUID.fromString(packet.getString("radiobox").replace("access_player.id_", ""));
				if(!access.players.containsKey(uuid)){
					container.sendMsg("access_player.notfound");
					return;
				}
				access.players.remove(uuid);
				container.open(UI_ACC_PLAYER);
				return;
			}
		}
	}

	private int getLayerId(Layers layer){
		switch(layer){
			case COMPANY: return -1;
			case DISTRICT: return district.id;
			case MUNICIPALITY: return district.owner.is_county ? -1 : district.municipality().id;
			case COUNTY: return district.county().id;
			case STATE: return district.county().state.id;
			default: return -1;
		}
	}

}
