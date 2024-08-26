package net.fexcraft.mod.landdev.data.district;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.MAILBOX;
import static net.fexcraft.mod.landdev.ui.LDUIElmType.*;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.UUID;

import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank.Action;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.ui.modules.AppearModule;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.ui.modules.NormModule;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.TranslationUtil;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class District implements Saveable, Layer, PermInteractive, LDUIModule {

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
	public ExternalData external = new ExternalData(this);
	public long tax_collected;
	public long chunks;
	
	public District(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		norms.add(new StringNorm("name", translate("district.norm.name")));
		norms.add(new BoolNorm("explosions", false));
		norms.add(new IntegerNorm("chunk-tax", 1000));
		norms.add(new BoolNorm("municipality-can-form", false));
		norms.add(new BoolNorm("municipality-can-claim", false));
		norms.add(new BoolNorm("unclaim-bankrupt", false));
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
		map.add("tax_collected", tax_collected);
		external.save(map);
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
		tax_collected = map.getLong("tax_collected", 0);
		external.load(map);
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
		external.gendef();
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

	@Override
	public int lid(){
		return id;
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
			if(owner.citizen().can(act, uuid)) return true;
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
				if(container == null) container.player.entity.send(TranslationUtil.translateCmd("account.noperm.municipality"));
				else container.sendMsg("landdev.cmd.account.noperm.municipality", false);
				return null;
			}
			return owner.municipality.account;
		}
		if((dis && owner.is_county) || layer.is(Layers.COUNTY)){
			if(!county().manage.can(container.player.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) container.player.entity.send(TranslationUtil.translateCmd("account.noperm.county"));
				else container.sendMsg("landdev.cmd.account.noperm.county", false);
				return null;
			}
			return county().account;
		}
		if(layer.is(Layers.STATE)){
			if(!state().manage.can(container.player.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) container.player.entity.send(TranslationUtil.translateCmd("account.noperm.state"));
				else container.sendMsg("landdev.cmd.account.noperm.state", false);
				return null;
			}
			return state().account;
		}
		return null;
	}

	public static final int UI_CREATE = -1;
	public static final int UI_TYPE = 1;
	public static final int UI_PRICE = 2;
	public static final int UI_MANAGER = 3;
	public static final int UI_SET_PRICE = 4;
	public static final int UI_NORMS = 5;
	public static final int UI_NORM_EDIT = 6;
	public static final int UI_APPREARANCE = 7;

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("district.title");
		boolean canman = can(MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
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
				return;
			case UI_TYPE:
				resp.setTitle("district.type.title");
				for(DistrictType dtp : DistrictType.TYPES.values()){
					resp.addRadio("type." + dtp.id(), ELM_BLUE, dtp == type, resp.val(dtp.name()));
				}
				resp.addButton("type.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			case UI_MANAGER:
				resp.setTitle("district.manager.title");
				resp.addRow("manager.current", ELM_GENERIC, ICON_BLANK, manage.getManagerName());
				resp.addField("manager.field", manage.getManagerName());
				resp.addButton("manager.submit", ELM_GENERIC, manage.hasManager() ? ICON_OPEN : ICON_ADD);
				if(manage.hasManager()) resp.addButton("manager.remove", ELM_GENERIC, ICON_REM);
				resp.setFormular();
				return;
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
				return;
			case UI_SET_PRICE:
				resp.setTitle("district.set_price.title");
				resp.addRow("id", ELM_GENERIC, ICON_BLANK, id);
				resp.addField("set_price.field");
				resp.addButton("set_price.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			case UI_APPREARANCE:
				AppearModule.resp(container, resp, "district", icon, color, canman);
				return;
			case UI_NORMS:
				NormModule.respNormList(norms, container, resp, "district", canman);
				return;
			case UI_NORM_EDIT:{
				NormModule.respNormEdit(norms, container, resp, "district", canman);
				return;
			}
			case UI_CREATE:{
				resp.setTitle("district.create.title");
				resp.addRow("create.name", ELM_GENERIC);
				resp.addField("create.name_field");
				resp.addBlank();
				resp.addRow("create.owner", ELM_YELLOW);
				resp.addRadio("create.owner_county", ELM_BLUE, true);
				if(container.player.municipality.manage.can(CREATE_DISTRICT, container.player.uuid)){
					resp.addRadio("create.owner_municipality", ELM_BLUE, false);
				}
				resp.addCheck("create.owner_funded", ELM_GREEN, true);
				resp.addBlank();
				resp.addButton("create.submit", ELM_BLUE, ICON_OPEN);
				resp.setFormular();
				resp.setNoBack();
				return;
			}
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		boolean canman = can(MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		boolean canoman = owner.manageable().can(MANAGE_DISTRICT, container.player.uuid) || container.player.adm;
		switch(req.event()){
			case "name":{
				container.open(UI_NORM_EDIT, id, norms.index(norms.get("name")));
				return;
			}
			case "type": container.open(UI_TYPE); return;
			case "owner":{
				container.open(owner.is_county ? LDKeys.COUNTY : LDKeys.MUNICIPALITY, 0, owner.owid, 0);
				return;
			}
			case "manager": if(canoman) container.open(UI_MANAGER); return;
			case "price": container.open(UI_PRICE); return;
			case "set_price": if(canman) container.open(UI_SET_PRICE); return;
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
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
					if(!account.getBank().processAction(Action.TRANSFER, container.player.entity, account, sell.price, ct.account)) return;
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
					if(!account.getBank().processAction(Action.TRANSFER, container.player.entity, account, sell.price, mun.account)) return;
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
				long value = LDConfig.format_price(err, val);
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
				NormModule.processNorm(norms, container, req, UI_NORM_EDIT);
				return;
			}
			case "norm_bool":{
				if(!canman) return;
				NormModule.processBool(norms, container, req, UI_NORM_EDIT);
				return;
			}
			case "create.submit":{
				Chunk_ chunk = ResManager.getChunk(container.player.entity);
				Player player = container.player;
				long sum = LDConfig.DISTRICT_CREATION_FEE;
				boolean forct = req.getRadio("create.owner_").equals("county");
				boolean opay = req.getCheck("create.owner_funded");
				String name = req.getField("create.name_field");
				Account account = null;
				if(chunk.district.id > -1){
					container.sendMsg("create.exists");
					return;
				}
				if(forct){
					if(chunk.district.county().id < 0){
						container.sendMsg("create.county_invalid");
						return;
					}
					if(!chunk.district.county().manage.can(CREATE_DISTRICT, player.uuid)){
						container.sendMsg("create.no_perm");
						return;
					}
					if(opay){
						if(!chunk.district.county().manage.can(FINANCES_USE, player.uuid)){
							container.sendMsg("create.no_fund_perm");
							return;
						}
						account = chunk.district.county().account;
					}
				}
				else{
					if(player.municipality.id < 0) return;
					if(player.municipality.county.id != chunk.district.county().id){
						container.sendMsg("create.wrong_county");
						return;
					}
					if(!player.municipality.manage.can(CREATE_DISTRICT, player.uuid)){
						container.sendMsg("create.no_perm");
						return;
					}
					if(opay){
						if(!player.municipality.manage.can(FINANCES_USE, player.uuid)){
							container.sendMsg("create.no_fund_perm");
							return;
						}
						account = player.municipality.account;
					}
				}
				if(account == null) account = player.account;
				if(account.getBalance() < sum){
					container.sendMsg("create.not_enough_money");
					return;
				}
				int newid = ResManager.getNewIdFor(saveTable());
				if(newid < 0){
					player.entity.send("DB ERROR, INVALID NEW ID '" + newid + "'!");
					return;
				}
				if(!account.getBank().processAction(Action.TRANSFER, player.entity, account, sum, SERVER_ACCOUNT)){
					return;
				}
				District dis = new District(newid);
				ResManager.DISTRICTS.put(dis.id, dis);
				if(forct) chunk.district.county().districts.add(dis.id);
				else player.municipality.districts.add(dis.id);
				if(forct) dis.owner.set(chunk.district.county());
				else dis.owner.set(player.municipality);
				if(name.length() > 0) dis.norms.get("name").set(name);
				dis.save();
				chunk.district = dis;
				chunk.save();
				ResManager.bulkSave(dis.owner.is_county? dis.owner.county : dis.owner.municipality, dis, chunk, player);
				player.entity.closeUI();
				player.entity.send(translate("gui.district.create.complete"));
				Announcer.announce(Announcer.Target.GLOBAL, 0, "announce.district.created", name, newid);
				return;
			}
			case "appearance.submit":{
				if(!canman) return;
				AppearModule.req(container, req, icon, color);
				container.open(UI_MAIN);
				return;
			}
		}
		if(NormModule.isNormReq(norms, container, req, UI_NORM_EDIT, id)) return;
		external.on_interact(container, req);
	}

	public void addTaxStat(long tax){
		tax_collected += tax;
		owner.addTaxStat(tax);
	}

}
