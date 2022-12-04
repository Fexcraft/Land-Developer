package net.fexcraft.mod.landdev.data.district;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_CLAIM;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_MANAGE_DISTRICT;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_MANAGE_FINANCES;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_SET_TAX_CHUNK;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_USE_FINANCES;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class District implements Saveable, Layer, PermInteractive, LDGuiModule {
	
	public static PermActions actions = new PermActions(ACT_CLAIM);
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public DistrictType type = DistrictType.getDefault();;
	public Manageable manage = new Manageable(false, actions);
	public Norms norms = new Norms();
	public DistrictOwner owner = new DistrictOwner();
	public long chunks;
	
	public District(int id){
		this.id = id;
		norms.add(new StringNorm("name", translate("district.norm.name")));
		norms.add(new BoolNorm("explosions", false));
		norms.add(new IntegerNorm("chunk-tax", 1000));
		norms.add(new BoolNorm("municipality-can-form", false));
		norms.add(new BoolNorm("municipality-can-claim", false));
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		created.save(map);
		sell.save(map);
		icon.save(map);
		color.save(map);
		neighbors.save(map);
		mail.save(map);
		type.save(map);
		manage.save(map);
		norms.save(map);
		owner.save(map);
		map.add("chunks", chunks);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		sell.load(map);
		icon.load(map);
		color.load(map);
		neighbors.load(map);
		mail.load(map);
		type = DistrictType.get(map);
		manage.load(map);
		norms.load(map);
		owner.load(map);
		chunks = map.getLong("chunks", 0);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("district.wilderness.name"));
			owner.owid = -1;
			owner.is_county = true;
			owner.county = ResManager.getCounty(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("district.spawnzone.name"));
			owner.owid = 0;
			owner.is_county = false;
			owner.municipality = ResManager.getMunicipality(0, true);
			color.set(0xff9900);
		}
		else return;
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "districts";
	}

	@Override
	public Layers getLayer(){
		return Layers.DISTRICT;
	}

	@Override
	public Layers getParentLayer(){
		return owner.is_county ? Layers.COUNTY : Layers.MUNICIPALITY;
	}

	public String name(){
		return norms.get("name").string();
	}

	public long tax(){
		return norms.get("chunk-tax").integer();
	}

	public State state(){
		return owner.is_county ? owner.county.state : owner.municipality.county.state;
	}

	@Override
	public boolean can(PermAction act, UUID uuid){
		boolean man = manage.isManager(uuid);
		if(act == ACT_CLAIM){
			return man || owner.manageable().can(act, uuid);
		}
		if(act == ACT_MANAGE_DISTRICT){
			return man || owner.manageable().can(act, uuid);
		}
		if(act == ACT_SET_TAX_CHUNK){
			return man || owner.manageable().can(act, uuid);
		}
		return false;
	}

	@Override
	public boolean can(UUID uuid, PermAction... acts){
		for(PermAction act : acts) if(can(act, uuid)) return true;
		return false;
	}

	public County county(){
		return owner.is_county ? owner.county : owner.municipality.county;
	}

	public Account account(){
		return owner.is_county ? owner.county.account : owner.municipality.account;
	}

	public Municipality municipality(){
		return owner.is_county ? null : owner.municipality;
	}

	public int getLayerId(Layers layer){
		if(layer == Layers.DISTRICT) return id;
		if(layer == Layers.MUNICIPALITY) return owner.is_county ? -1 : owner.municipality.id;
		if(layer == Layers.COUNTY) return owner.county_id();
		if(layer == Layers.STATE) return county().state.id;
		return -1;
	}

	public Account getLayerAccount(Layers layer, LDGuiContainer container, Player player){
		if(layer.is(Layers.PLAYER)) return player.account;
		boolean dis = layer.is(Layers.DISTRICT);
		if((dis && !owner.is_county) || layer.is(Layers.MUNICIPALITY)){
			if(!owner.municipality.manage.can(player.uuid, ACT_USE_FINANCES, ACT_MANAGE_FINANCES)){
				if(container == null) Print.chat(player.entity, TranslationUtil.translateCmd("account.noperm.municipality"));
				else container.sendMsg("landdev.cmd.account.noperm.municipality", false);
				return null;
			}
			return owner.municipality.account;
		}
		if((dis && owner.is_county) || layer.is(Layers.COUNTY)){
			if(!county().manage.can(player.uuid, ACT_USE_FINANCES, ACT_MANAGE_FINANCES)){
				if(container == null) Print.chat(player.entity, TranslationUtil.translateCmd("account.noperm.county"));
				else container.sendMsg("landdev.cmd.account.noperm.county", false);
				return null;
			}
			return county().account;
		}
		if(layer.is(Layers.STATE)){
			if(!state().manage.can(player.uuid, ACT_USE_FINANCES, ACT_MANAGE_FINANCES)){
				if(container == null) Print.chat(player.entity, TranslationUtil.translateCmd("account.noperm.state"));
				else container.sendMsg("landdev.cmd.account.noperm.state", false);
				return null;
			}
			return state().account;
		}
		return null;
	}
	
	public static final int
		UI_NAME = 1,
		UI_TYPE = 2,
		UI_OWNER = 3,
		UI_PRICE = 4,
		UI_MANAGER = 5,
		UI_SET_PRICE = 6,
		UI_CHUNK_TAX = 7,
		UI_MAILBOX = 8,
		UI_NORMS = 9,
		UI_APPREARANCE = 10
		;

	@Override
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
		com.setString("title_lang", "district.title");
		NBTTagList list = new NBTTagList();
		boolean canman = can(ACT_MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(ACT_MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		switch(container.x){
		case UI_MAIN:
			addToList(list, "id", ELM_GENERIC, ICON_BLANK, false, false, id);
			addToList(list, "name", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, false, name());
			addToList(list, "type", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, false, type.name());
			addToList(list, "owner", ELM_GENERIC, ICON_OPEN, true, false, owner.name());
			if(canoman || manage.hasManager()) addToList(list, "manager", ELM_GENERIC, canoman ? ICON_OPEN : ICON_EMPTY, canoman, false, manage.getManagerName());
			addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			if(sell.price > 0){
				addToList(list, "price", ELM_GENERIC, ICON_OPEN, true, false, sell.price_formatted());
			}
			if(canman){
				addToList(list, "set_price", ELM_GENERIC, ICON_OPEN, true, false, null);
			}
			if(sell.price > 0){
				addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			}
			addToList(list, "chunk_tax", ELM_GENERIC, canman ? ICON_ADD : ICON_EMPTY, canman, false, getWorthAsString(tax()));
			addToList(list, "chunks", ELM_GENERIC, ICON_BLANK, false, false, chunks);
			addToList(list, "mailbox", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, false, mail.asString());
			addToList(list, "norms", ELM_GREEN, ICON_OPEN, true, false, null);
			addToList(list, "appearance", ELM_YELLOW, ICON_OPEN, true, false, null);
			break;
		case UI_NAME:
			com.setString("title_lang", "district.name.title");
			addToList(list, "id", ELM_GENERIC, ICON_BLANK, false, false, id);
			addToList(list, "name.current", ELM_GENERIC, ICON_BLANK, false, false, name());
			addToList(list, "name.field", ELM_BLANK, ICON_BLANK, false, true, name());
			addToList(list, "name.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_TYPE:
			com.setString("title_lang", "district.type.title");
			for(DistrictType dtp : DistrictType.TYPES.values()){
				addToList(list, "type." + dtp.id(), ELM_BLUE, radio(dtp == type), true, false, dtp.name(), true);
			}
			addToList(list, "type.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_MANAGER:
			com.setString("title_lang", "district.manager.title");
			addToList(list, "manager.current", ELM_GENERIC, ICON_BLANK, false, false, manage.getManagerName());
			addToList(list, "manager.field", ELM_BLANK, ICON_BLANK, false, true, manage.getManagerName());
			addToList(list, "manager.submit", ELM_GENERIC, manage.hasManager() ? ICON_OPEN : ICON_ADD, true, false, null);
			addToList(list, "manager.remove", ELM_GENERIC, ICON_REM, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_PRICE:
			com.setString("title_lang", "district.buy.title");
			addToList(list, "id", ELM_GENERIC, ICON_BLANK, false, false, id);
			addToList(list, "buy.info", ELM_YELLOW, ICON_BLANK, false, false, null);
			if(owner.is_county) addToList(list, "buy.this_municipality", ELM_BLUE, ICON_RADIOBOX_CHECKED, true, false, null);
			else addToList(list, "buy.this_county", ELM_BLUE, ICON_RADIOBOX_CHECKED, true, false, null);
			addToList(list, "buy.other_municipality", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.other_county", ELM_BLUE, ICON_RADIOBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.payer", ELM_GENERIC, ICON_CHECKBOX_UNCHECKED, true, false, null);
			addToList(list, "buy.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_SET_PRICE:
			com.setString("title_lang", "district.set_price.title");
			addToList(list, "id", ELM_GENERIC, ICON_BLANK, false, false, id);
			addToList(list, "set_price.field", ELM_BLANK, ICON_BLANK, false, true, null);
			addToList(list, "set_price.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
			break;
		case UI_APPREARANCE:
			addToList(list, "icon", ELM_BLANK, ICON_OPEN, true, true, icon.getn());
			addToList(list, "color", ELM_BLANK, ICON_OPEN, true, true, color.getString());
			com.setBoolean("form", true);
			break;
		}
		com.setTag("elements", list);
	}

	@Override
	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index){
		boolean canman = can(ACT_MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(ACT_MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		switch(index){
			case "name": container.open(UI_NAME); return;
			case "type": container.open(UI_TYPE); return;
			case "owner":{
				if(canoman) container.open(UI_OWNER);
				else container.open(owner.is_county ? GuiHandler.COUNTY : GuiHandler.MUNICIPALITY, 0, owner.owid, 0);
				return;
			}
			case "manager": if(canoman) container.open(UI_MANAGER); return;
			case "price": container.open(UI_PRICE); return;
			case "set_price": if(canman) container.open(UI_SET_PRICE); return;
			case "chunk_tax": if(canman) container.open(UI_CHUNK_TAX); return;
			case "mailbox": if(canman) container.open(UI_MAILBOX); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
			case "icon": return;
			case "color": return;
			case "name.submit":{
				if(!canman) return;
    			String name = packet.getCompoundTag("fields").getString("name.field");
    			if(!validateName(container, name)) return;
    			norms.get("name").set(name);
    			container.open(UI_MAIN);
				return;
			}
			case "type.submit":{
				if(!canman) return;
				DistrictType type = DistrictType.TYPES.get(packet.getString("radiobox").replace("type.", ""));
				if(type == null) return;
				this.type = type;
				container.open(UI_MAIN);
				return;
			}
			case "manager.submit":{
				if(!canoman) return;
				UUID uuid = null;
				try{
					uuid = UUID.fromString(packet.getCompoundTag("fields").getString("manager.field"));
				}
				catch(Exception e){
					if(Static.dev()) e.printStackTrace();
					uuid = ResManager.getUUIDof(packet.getCompoundTag("fields").getString("manager.field"));
				}
				if(uuid == null){
					container.sendMsg("landdev.cmd.uuid_player_not_found", false);
					return;
				}
				if(owner.manageable().isStaff(uuid)){
					manage.setManager(uuid);
					container.open(0);
				}
				else{
					container.sendMsg("landdev.cmd.player_not_staff", false);
				}
				return;
			}
			case "manager.remove":{
				if(!canoman) return;
				manage.setNoManager();
				container.open(0);
				return;
			}
			case "buy.submit":{
				
				return;
			}
			case "set_price.submit":{
				if(!canoman) return;
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
			default:{
				container.sendMsg("work-in-progress", false);
				return;
			}
		}
	}
}
