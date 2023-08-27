package net.fexcraft.mod.landdev.data.district;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.DISTRICT_ACTIONS;
import static net.fexcraft.mod.landdev.data.PermAction.DISTRICT_MANAGE;
import static net.fexcraft.mod.landdev.data.PermAction.FINANCES_MANAGE;
import static net.fexcraft.mod.landdev.data.PermAction.FINANCES_USE;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MAILBOX;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.Map.Entry;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.Norm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.LDGuiElementType;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.TranslationUtil;

public class District implements Saveable, Layer, PermInteractive, LDGuiModule {

	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public DistrictType type = DistrictType.getDefault();;
	public Manageable manage = new Manageable(false, DISTRICT_ACTIONS);
	public Norms norms = new Norms();
	public DistrictOwner owner = new DistrictOwner();
	public long chunks;
	
	public District(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
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
		for(PermAction action : manage.actions()){
			if(action != act) continue;
			if(man || owner.manageable().can(act, uuid)) return true;
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

	public Account getLayerAccount(Layers layer, LDGuiContainer container){
		if(layer.is(Layers.PLAYER)) return container.player.account;
		boolean dis = layer.is(Layers.DISTRICT);
		if((dis && !owner.is_county) || layer.is(Layers.MUNICIPALITY)){
			if(!owner.municipality.manage.can(container.player.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) Print.chat(container.player.entity, TranslationUtil.translateCmd("account.noperm.municipality"));
				else container.sendMsg("landdev.cmd.account.noperm.municipality", false);
				return null;
			}
			return owner.municipality.account;
		}
		if((dis && owner.is_county) || layer.is(Layers.COUNTY)){
			if(!county().manage.can(container.player.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) Print.chat(container.player.entity, TranslationUtil.translateCmd("account.noperm.county"));
				else container.sendMsg("landdev.cmd.account.noperm.county", false);
				return null;
			}
			return county().account;
		}
		if(layer.is(Layers.STATE)){
			if(!state().manage.can(container.player.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) Print.chat(container.player.entity, TranslationUtil.translateCmd("account.noperm.state"));
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
		UI_NORMS = 7,
		UI_NORM_EDIT = 8,
		UI_APPREARANCE = 9
		;

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("district.title");
		boolean canman = can(DISTRICT_MANAGE, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(DISTRICT_MANAGE, container.player.uuid) || container.player.adm;
		switch(container.x){
			case UI_MAIN:
				resp.addRow("id", ELM_GENERIC, id);
				if(canman){
					resp.addButton("name", ELM_GENERIC, ICON_OPEN, name());
					resp.addButton("type", ELM_GENERIC, ICON_OPEN, type.name());
				}
				else{
					resp.addRow("name", ELM_GENERIC, ICON_EMPTY, name());
					resp.addRow("type", ELM_GENERIC, ICON_EMPTY, type.name());
				}
				resp.addButton("owner", ELM_GENERIC, ICON_OPEN, owner.name());
				if(canoman || manage.hasManager()){
					resp.addRow("manager", ELM_GENERIC, canoman ? ICON_OPEN : ICON_EMPTY, canoman, manage.getManagerName());
				}
				resp.addBlank();
				if(sell.price > 0){
					resp.addButton("price", ELM_GENERIC, ICON_OPEN, sell.price_formatted());
				}
				if(canman){
					resp.addButton("set_price", ELM_GENERIC, ICON_OPEN);
				}
				if(sell.price > 0) resp.addBlank();
				resp.addRow("chunk_tax", ELM_GENERIC, getWorthAsString(tax()));
				if(id >= 0) resp.addRow("chunks", ELM_GENERIC, chunks);
				if(canman){
					resp.addButton("mailbox", ELM_GENERIC, ICON_OPEN, mail.unread());
				}
				resp.addButton("norms", ELM_GREEN, ICON_OPEN);
				resp.addButton("appearance", ELM_YELLOW, ICON_OPEN);
				break;
			case UI_NAME:
				resp.setTitle("district.name.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("name.current", ELM_GENERIC, name());
				resp.addField("name.field", name());
				resp.addButton("name.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				break;
			case UI_TYPE:
				resp.setTitle("district.type.title");
				for(DistrictType dtp : DistrictType.TYPES.values()){
					resp.addRadio("type." + dtp.id(), ELM_BLUE, dtp == type, resp.val(dtp.name()));
				}
				resp.addButton("type.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				break;
			case UI_MANAGER:
				resp.setTitle("district.manager.title");
				resp.addRow("manager.current", ELM_GENERIC, ICON_BLANK, manage.getManagerName());
				resp.addField("manager.field", manage.getManagerName());
				resp.addButton("manager.submit", ELM_GENERIC, manage.hasManager() ? ICON_OPEN : ICON_ADD);
				if(manage.hasManager()) resp.addButton("manager.remove", ELM_GENERIC, ICON_REM);
				resp.setFormular();
				break;
			case UI_PRICE:
				resp.setTitle("district.buy.title");
				resp.addRow("id", ELM_GENERIC, ICON_BLANK, id);
				resp.addRow("buy.info", ELM_YELLOW, ICON_BLANK, null);
				if(!owner.is_county)  resp.addButton("buy.this_county", ELM_BLUE, ICON_RADIOBOX_UNCHECKED);
				if(container.player.municipality.id >= 0 && !owner.is_county && owner.municipality.id != container.player.municipality.id){
					resp.addButton("buy.my_municipality", ELM_BLUE, ICON_RADIOBOX_UNCHECKED);
				}
				resp.addButton("buy.my_county", county().id == container.player.county.id ? ELM_RED : ELM_BLUE, ICON_RADIOBOX_UNCHECKED);
				resp.addButton("buy.payer", ELM_GENERIC, ICON_CHECKBOX_UNCHECKED);
				resp.addButton("buy.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				break;
			case UI_SET_PRICE:
				resp.setTitle("district.set_price.title");
				resp.addRow("id", ELM_GENERIC, ICON_BLANK, id);
				resp.addField("set_price.field");
				resp.addButton("set_price.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				break;
			case UI_APPREARANCE:
				resp.setTitle("district.appearance.title");
				resp.addRow("appearance.icon", ELM_GENERIC);
				resp.addField("appearance.icon_field", icon.getn());
				resp.addRow("appearance.color", ELM_GENERIC);
				resp.addField("appearance.color_field", color.getString());
				if(canman){
					resp.addRow("appearance.submit", ELM_GENERIC, ICON_OPEN);
					resp.setFormular();
				}
				break;
			case UI_NORMS:
				resp.setTitle("district.norms");
				LDGuiElementType icon = canman ? ICON_OPEN : ICON_EMPTY;
				for(Entry<String, Norm> entry : norms.norms.entrySet()){
					if(entry.getValue().type.isBool()){
						resp.addRow("norm." + entry.getKey(), ELM_GENERIC, icon, canman, entry.getValue().bool() ? LANG_YES : LANG_NO);
					}
					else{
						resp.addRow("norm." + entry.getKey(), ELM_GENERIC, icon, canman, entry.getValue().string());
					}
				}
				break;
			case UI_NORM_EDIT:{
				resp.setTitle("district.norm_editor");
				Norm norm = norms.get(container.z);
				if(norm == null) return;
				resp.addRow("norm_id", ELM_GREEN, norm.id);
				resp.addRow("norm_value", ELM_GENERIC, norm.string());
				if(!canman) break;
				if(norm.type.isBool()){
					resp.addButton("norm_bool", ELM_BLUE, norm.bool() ? ICON_ENABLED : ICON_DISABLED);
				}
				else{
					resp.addField("norm_field", norm.string());
					resp.addButton("norm_submit", ELM_BLUE, ICON_OPEN);
					resp.setFormular();
				}
				break;
			}
		}
	}

	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		boolean canman = can(DISTRICT_MANAGE, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(DISTRICT_MANAGE, container.player.uuid) || container.player.adm;
		switch(req.event()){
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
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
			case "icon": return;
			case "color": return;
			case "name.submit":{
				if(!canman) return;
    			String name = req.getField("name.field");
    			if(!validateName(container, name)) return;
    			norms.get("name").set(name);
    			container.open(UI_MAIN);
				return;
			}
			case "type.submit":{
				if(!canman) return;
				DistrictType type = DistrictType.TYPES.get(req.getRadio("type."));
				if(type == null) return;
				this.type = type;
				container.open(UI_MAIN);
				return;
			}
			case "manager.submit":{
				if(!canoman) return;
				Player man = req.getPlayerField("manager.field");
				if(man == null){
					container.sendMsg("landdev.cmd.uuid_player_not_found", false);
					return;
				}
				if(owner.manageable().isStaff(man.uuid)){
					manage.setManager(man.uuid);
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
				String radio = req.getRadio();
				boolean tct = radio.equals("buy.this_county");
				boolean mct = radio.equals("buy.my_county");
				boolean mmu = radio.equals("buy.my_municipality");
				boolean rep = req.getCheck("buy.payer");
				if(!tct && !mct && !mmu){
					container.sendMsg("buy.nobuyer");
					return;
				}
				if(tct || mct){
					if(mct && county().id == container.player.county.id){
						container.sendMsg("buy.alreadypartofcounty");
						return;
					}
					else if(tct && owner.is_county){
						container.sendMsg("buy.alreadypartofcounty");
						return;
					}
					County ct = mct ? container.player.county : county();
					if(rep && !ct.manage.can(FINANCES_USE, container.player.uuid)){
						container.sendMsg("buy.no_county_perm");
						return;
					}
					Account account = rep ? ct.account : container.player.account;
					if(account.getBalance() < sell.price){
						container.sendMsg("buy.notenoughmoney");
						return;
					}
					Bank bank = DataManager.getBank(account.getBankId(), false, true);
					if(!bank.processAction(Action.TRANSFER, container.player.entity, account, sell.price, ct.account)) return;
					owner.set(ct);
					sell.price = 0;
					container.open(UI_MAIN);
				}
				else{
					if(!owner.is_county && municipality().id == container.player.municipality.id){
						container.sendMsg("buy.alreadypartofmunicipality");
						return;
					}
					Municipality mun = container.player.municipality;
					if(rep && !mun.manage.can(FINANCES_USE, container.player.uuid)){
						container.sendMsg("buy.no_municipality_perm");
						return;
					}
					Account account = rep ? mun.account : container.player.account;
					if(account.getBalance() < sell.price){
						container.sendMsg("buy.notenoughmoney");
						return;
					}
					Bank bank = DataManager.getBank(account.getBankId(), false, true);
					if(!bank.processAction(Action.TRANSFER, container.player.entity, account, sell.price, mun.account)) return;
					owner.set(mun);
					sell.price = 0;
					container.open(UI_MAIN);
				}
				return;
			}
			case "set_price.submit":{
				if(!canoman) return;
				String[] err = new String[]{ "" };
				String val = req.getField("set_price.field");
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
			case "norm_submit":{
				if(!canman) return;
				Norm norm = norms.get(container.z);
				if(norm == null) return;
				if(norm.type.isInteger()){
					try{
						norm.set(Integer.parseInt(req.getField("norm_field")));
					}
					catch(Exception e){
						container.sendMsg("Error: " + e.getMessage());
					}
				}
				else if(norm.type.isDecimal()){
					try{
						norm.set(Float.parseFloat(req.getField("norm_field")));
					}
					catch(Exception e){
						container.sendMsg("Error: " + e.getMessage());
					}
				}
				else{
					norm.set(req.getField("norm_field"));
				}
				container.open(UI_NORM_EDIT);
				return;
			}
			case "norm_bool":{
				if(!canman) return;
				Norm norm = norms.get(container.z);
				if(norm == null) return;
				if(!norm.type.isBool()) return;
				norm.toggle();
				container.open(UI_NORM_EDIT);
				return;
			}
		}
		if(req.event().startsWith("norm.")){
			Norm norm = norms.get(req.event().substring(5));
			if(norm == null) return;
			container.open(UI_NORM_EDIT, id, norms.index(norm));
			return;
		}
	}

}
