package net.fexcraft.mod.landdev.data.district;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank.Action;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.AppearModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.ui.modules.NormModule;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;

import java.util.UUID;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.MAILBOX;
import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;

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
	public DistrictType type = DistrictType.getDefault();
	public Manageable manage = new Manageable(false, DISTRICT_ACTIONS);
	public Norms norms = new Norms();
	public DistrictOwner owner = new DistrictOwner();
	public ExternalData external = new ExternalData(this);
	public boolean disbanded;
	public boolean locked;
	public long tax_collected;
	public long chunks;
	
	public District(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		norms.add(new StringNorm("name", "Unnamed District"));
		norms.add(new BoolNorm("explosions", false));
		norms.add(new IntegerNorm("chunk-tax", 1000));
		norms.add(new IntegerNorm("new-property-fee", 100000));
		norms.add(new BoolNorm("municipality-can-form", false));
		norms.add(new BoolNorm("county-can-form", false));
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
		external.save(this, map);
		map.add("chunks", chunks);
		if(disbanded) map.add("disbanded", true);
		if(locked) map.add("locked", true);
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
		external.load(this, map);
		chunks = map.getLong("chunks", 0);
		disbanded = map.getBoolean("disbanded", false);
		locked = map.getBoolean("locked", false);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set("Wilderness");
			owner.owid = -1;
			owner.is_county = true;
			owner.county = ResManager.getCounty(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set("Spawn District");
			owner.owid = 0;
			owner.is_county = false;
			owner.municipality = ResManager.getMunicipality(0, true);
			color.set(0xff9900);
		}
		external.gendef(this);
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

	public Region region(){
		return owner.is_county ? owner.county.region : owner.municipality.county.region;
	}

	@Override
	public boolean can(PermAction act, UUID uuid){
		if(locked) return false;
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
		if(layer == Layers.REGION) return county().region.id;
		return -1;
	}

	public Account getLayerAccount(Layers layer, BaseCon container){
		if(layer.is(Layers.PLAYER)) return container.ldp.account;
		boolean dis = layer.is(Layers.DISTRICT);
		if((dis && !owner.is_county) || layer.is(Layers.MUNICIPALITY)){
			if(!owner.municipality.manage.can(container.ldp.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) container.ldp.entity.send("landdev.cmd.account.noperm.municipality");
				else container.msg("landdev.cmd.account.noperm.municipality", false);
				return null;
			}
			return owner.municipality.account;
		}
		if((dis && owner.is_county) || layer.is(Layers.COUNTY)){
			if(!county().manage.can(container.ldp.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) container.ldp.entity.send("landdev.cmd.account.noperm.county");
				else container.msg("landdev.cmd.account.noperm.county", false);
				return null;
			}
			return county().account;
		}
		if(layer.is(Layers.REGION)){
			if(!region().manage.can(container.ldp.uuid, FINANCES_USE, FINANCES_MANAGE)){
				if(container == null) container.ldp.entity.send("landdev.cmd.account.noperm.region");
				else container.msg("landdev.cmd.account.noperm.region", false);
				return null;
			}
			return region().account;
		}
		return null;
	}

	public static final int UI_TYPE = 1;
	public static final int UI_PRICE = 2;
	public static final int UI_MANAGER = 3;
	public static final int UI_SET_PRICE = 4;
	public static final int UI_NORMS = 5;
	public static final int UI_NORM_EDIT = 6;
	public static final int UI_APPREARANCE = 7;
	public static final int UI_MERGE = 8;
	public static final int UI_DISBAND = 9;

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("district.title");
		boolean canman = (!locked && can(MANAGE_DISTRICT, container.ldp.uuid)) || container.ldp.adm;
		boolean canoman = (!locked && owner.manageable().can(MANAGE_DISTRICT, container.ldp.uuid)) || container.ldp.adm;
		switch(container.pos.x){
			case UI_MAIN:
				if(disbanded){
					resp.addRow("disbanded", ELM_RED, id);
				}
				if(locked){
					resp.addButton("locked", ELM_RED, container.ldp.adm ? ENABLED : EMPTY);
				}
				resp.addRow("id", ELM_GENERIC, id);
				if(canman){
					resp.addButton("name", ELM_GENERIC, OPEN, name());
					resp.addButton("type", ELM_GENERIC, OPEN, type.name());
				}
				else{
					resp.addRow("name", ELM_GENERIC, EMPTY, name());
					resp.addRow("type", ELM_GENERIC, EMPTY, type.name());
				}
				resp.addButton("owner", ELM_GENERIC, OPEN, owner.name());
				if(canoman || manage.hasManager()){
					resp.addRow("manager", ELM_GENERIC, canoman ? OPEN : EMPTY, canoman, manage.getManagerName());
				}
				resp.addBlank();
				if(sell.price > 0){
					resp.addButton("price", ELM_GENERIC, OPEN, sell.price_formatted());
				}
				if(canman){
					resp.addButton("set_price", ELM_GENERIC, OPEN);
				}
				if(sell.price > 0) resp.addBlank();
				resp.addRow("chunk_tax", ELM_GENERIC, getWorthAsString(tax()));
				if(id >= 0) resp.addRow("chunks", ELM_GENERIC, chunks);
				if(canman){
					resp.addButton("mailbox", ELM_GENERIC, OPEN, mail.unread());
				}
				resp.addButton("norms", ELM_GREEN, OPEN);
				resp.addButton("appearance", ELM_YELLOW, OPEN);
				resp.addBlank();
				resp.addButton("merge", ELM_YELLOW, OPEN);
				resp.addButton("disband", ELM_RED, OPEN);
				if(!locked && container.ldp.adm){
					resp.addButton("lock", ELM_RED, DISABLED);
				}
				break;
			case UI_TYPE:
				resp.setTitle("district.type.title");
				for(DistrictType dtp : DistrictType.TYPES.values()){
					resp.addRadio("type." + dtp.id(), ELM_BLUE, dtp == type, resp.val(dtp.name()));
				}
				resp.addButton("type.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
				break;
			case UI_MANAGER:
				resp.setTitle("district.manager.title");
				resp.addRow("manager.current", ELM_GENERIC, BLANK, manage.getManagerName());
				resp.addField("manager.field", manage.getManagerName());
				resp.addButton("manager.submit", ELM_GENERIC, manage.hasManager() ? OPEN : ADD);
				if(manage.hasManager()) resp.addButton("manager.remove", ELM_GENERIC, REM);
				resp.setFormular();
				break;
			case UI_PRICE:
				resp.setTitle("district.buy.title");
				resp.addRow("id", ELM_GENERIC, BLANK, id);
				resp.addRow("buy.info", ELM_YELLOW, BLANK, null);
				if(!owner.is_county)  resp.addButton("buy.this_county", ELM_BLUE, RADIO_UNCHECKED);
				if(container.ldp.municipality.id >= 0 && !owner.is_county && owner.municipality.id != container.ldp.municipality.id){
					resp.addButton("buy.my_municipality", ELM_BLUE, RADIO_UNCHECKED);
				}
				resp.addButton("buy.my_county", county().id == container.ldp.county.id ? ELM_RED : ELM_BLUE, RADIO_UNCHECKED);
				resp.addButton("buy.payer", ELM_GENERIC, CHECK_UNCHECKED);
				resp.addButton("buy.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
				break;
			case UI_SET_PRICE:
				resp.setTitle("district.set_price.title");
				resp.addRow("id", ELM_GENERIC, BLANK, id);
				resp.addField("set_price.field");
				resp.addButton("set_price.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
				break;
			case UI_APPREARANCE:
				AppearModule.resp(container, resp, "district", icon, color, canman);
				break;
			case UI_NORMS:
				NormModule.respNormList(norms, container, resp, "district", canman);
				break;
			case UI_NORM_EDIT:{
				NormModule.respNormEdit(norms, container, resp, "district", canman);
				break;
			}
			case UI_MERGE:{
				resp.setTitle("district.merge.title");
				resp.addRow("merge.wip", ELM_GENERIC);
				break;
			}
			case UI_DISBAND:{
				resp.setTitle("district.disband.title");
				resp.addRow("disband.warning0", ELM_RED);
				resp.addButton("disband.warning1", ELM_YELLOW, OPEN);
				resp.addBlank();
				resp.addRow("disband.info", ELM_GENERIC);
				resp.addField("disband.name");
				resp.addButton("disband.submit", ELM_YELLOW, canoman ? OPEN : EMPTY);
				resp.setFormular();
				break;
			}
			case UI_CREATE:{
				resp.setTitle("district.create.title");
				resp.addRow("create.name", ELM_GENERIC);
				resp.addField("create.name_field");
				resp.addBlank();
				resp.addRow("create.owner", ELM_YELLOW);
				resp.addRadio("create.owner_county", ELM_BLUE, true);
				if(container.ldp.municipality.manage.can(CREATE_DISTRICT, container.ldp.uuid)){
					resp.addRadio("create.owner_municipality", ELM_BLUE, false);
				}
				resp.addCheck("create.owner_funded", ELM_GREEN, true);
				resp.addBlank();
				resp.addButton("create.submit", ELM_BLUE, OPEN);
				resp.setFormular();
				resp.setNoBack();
				break;
			}
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		boolean canman = (!locked && can(MANAGE_DISTRICT, container.ldp.uuid)) || container.ldp.adm;
		boolean canoman = (!locked && owner.manageable().can(MANAGE_DISTRICT, container.ldp.uuid)) || container.ldp.adm;
		switch(req.event()){
			case "name":{
				container.open(UI_NORM_EDIT, id, norms.index(norms.get("name")));
				break;
			}
			case "type": container.open(UI_TYPE); break;
			case "owner":{
				container.open(owner.is_county ? LDKeys.COUNTY : LDKeys.MUNICIPALITY, 0, owner.owid, 0);
				break;
			}
			case "manager": if(canoman) container.open(UI_MANAGER); break;
			case "price": container.open(UI_PRICE); break;
			case "set_price": if(canman) container.open(UI_SET_PRICE); break;
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); break;
			case "norms": container.open(UI_NORMS); break;
			case "appearance": container.open(UI_APPREARANCE); break;
			case "merge": container.open(UI_MERGE); break;
			case "disband": container.open(UI_DISBAND); break;
			case "lock":
			case "locked":{
				if(!container.ldp.adm) break;
				locked = !locked;
				save();
				container.open(UI_MAIN);
				break;
			}
			//
			case "type.submit":{
				if(!canman) break;
				DistrictType type = DistrictType.TYPES.get(req.getRadio("type."));
				if(type == null) break;
				this.type = type;
				container.open(UI_MAIN);
				break;
			}
			case "manager.submit":{
				if(!canoman) break;
				LDPlayer man = req.getPlayerField("manager.field");
				if(man == null){
					container.msg("landdev.cmd.uuid_player_not_found", false);
					break;
				}
				if(owner.manageable().isStaff(man.uuid)){
					manage.setManager(man.uuid);
					container.open(0);
				}
				else{
					container.msg("landdev.cmd.player_not_staff", false);
				}
				break;
			}
			case "manager.remove":{
				if(!canoman) break;
				manage.setNoManager();
				container.open(0);
				break;
			}
			case "buy.submit":{
				String radio = req.getRadio();
				boolean tct = radio.equals("buy.this_county");
				boolean mct = radio.equals("buy.my_county");
				boolean mmu = radio.equals("buy.my_municipality");
				boolean rep = req.getCheck("buy.payer");
				if(!tct && !mct && !mmu){
					container.msg("buy.nobuyer");
					break;
				}
				if(tct || mct){
					if(mct && county().id == container.ldp.county.id){
						container.msg("buy.alreadypartofcounty");
						break;
					}
					else if(tct && owner.is_county){
						container.msg("buy.alreadypartofcounty");
						break;
					}
					County ct = mct ? container.ldp.county : county();
					if(rep && !ct.manage.can(FINANCES_USE, container.ldp.uuid)){
						container.msg("buy.no_county_perm");
						break;
					}
					Account account = rep ? ct.account : container.ldp.account;
					if(account.getBalance() < sell.price){
						container.msg("buy.notenoughmoney");
						break;
					}
					if(!account.getBank().processAction(Action.TRANSFER, container.ldp.entity, account, sell.price, ct.account)) break;
					owner.set(ct);
					sell.price = 0;
					container.open(UI_MAIN);
				}
				else{
					if(!owner.is_county && municipality().id == container.ldp.municipality.id){
						container.msg("buy.alreadypartofmunicipality");
						break;
					}
					Municipality mun = container.ldp.municipality;
					if(rep && !mun.manage.can(FINANCES_USE, container.ldp.uuid)){
						container.msg("buy.no_municipality_perm");
						break;
					}
					Account account = rep ? mun.account : container.ldp.account;
					if(account.getBalance() < sell.price){
						container.msg("buy.notenoughmoney");
						break;
					}
					if(!account.getBank().processAction(Action.TRANSFER, container.ldp.entity, account, sell.price, mun.account)) break;
					owner.set(mun);
					sell.price = 0;
					container.open(UI_MAIN);
				}
				break;
			}
			case "set_price.submit":{
				if(!canoman) break;
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
				break;
			}
			case "norm_submit":{
				if(!canman) break;
				NormModule.processNorm(norms, container, req, UI_NORM_EDIT);
				break;
			}
			case "norm_bool":{
				if(!canman) break;
				NormModule.processBool(norms, container, req, UI_NORM_EDIT);
				break;
			}
			case "disband.warning1":{
				container.open(UI_MERGE);
				break;
			}
			case "disband.submit":{
				if(id < 0) break;
				if(owner.is_county){
					if(county().districts.size() < 2){
						container.msg("disband.last_county");
						break;
					}
				}
				else{
					if(municipality().districts.size() < 2){
						container.msg("disband.last_municipality");
						break;
					}
				}
				if(!canoman){
					container.msg("disband.no_perm");
					break;
				}
				String name = req.getField("disband.name");
				if(!name.equals(name())){
					container.msg("disband.wrong_name");
					break;
				}
				disband();
				container.open(UI_MAIN);
				break;
			}
			case "create.submit":{
				Chunk_ chunk = ResManager.getChunk(container.ldp.entity);
				LDPlayer player = container.ldp;
				long sum = LDConfig.DISTRICT_CREATION_FEE;
				boolean forct = req.getRadio("create.owner_").equals("county");
				boolean opay = req.getCheck("create.owner_funded");
				String name = req.getField("create.name_field");
				Account account = null;
				if(chunk.district.id > -1 && chunk.district.chunks < 2){
					container.msg("create.exists");
					break;
				}
				if(forct){
					if(chunk.district.county().id < 0){
						container.msg("create.county_invalid");
						break;
					}
					if(!chunk.district.county().manage.can(CREATE_DISTRICT, player.uuid)){
						container.msg("create.no_perm");
						break;
					}
					if(opay){
						if(!chunk.district.county().manage.can(FINANCES_USE, player.uuid)){
							container.msg("create.no_fund_perm");
							break;
						}
						account = chunk.district.county().account;
					}
				}
				else{
					if(player.municipality.id < 0) break;
					if(player.municipality.county.id != chunk.district.county().id){
						container.msg("create.wrong_county");
						break;
					}
					if(!player.municipality.manage.can(CREATE_DISTRICT, player.uuid)){
						container.msg("create.no_perm");
						break;
					}
					if(opay){
						if(!player.municipality.manage.can(FINANCES_USE, player.uuid)){
							container.msg("create.no_fund_perm");
							break;
						}
						account = player.municipality.account;
					}
				}
				if(account == null) account = player.account;
				if(account.getBalance() < sum){
					container.msg("create.not_enough_money");
					break;
				}
				int newid = ResManager.getNewIdFor(saveTable());
				if(newid < 0){
					player.entity.send("DB ERROR, INVALID NEW ID '" + newid + "'!");
					break;
				}
				if(!account.getBank().processAction(Action.TRANSFER, player.entity, account, sum, SERVER_ACCOUNT)){
					break;
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
				player.entity.send("landdev.gui.district.create.complete");
				Announcer.announce(Announcer.Target.GLOBAL, 0, "announce.district.created", name, newid);
				break;
			}
			case "appearance.submit":{
				if(!canman) break;
				if(AppearModule.req(container, req, icon, color)) container.open(UI_MAIN);
				break;
			}
		}
		if(NormModule.isNormReq(norms, container, req, UI_NORM_EDIT, id)) return;
		external.on_interact(container, req);
	}

	public void disband(){
		disbanded = true;
		District wil = ResManager.getDistrict(-1);
		for(Chunk_ chunk : ResManager.CHUNKS.values()){
			if(chunk.district.id != id) continue;
			chunk.district = wil;
			if(chunk.owner.layer() == owner.layer() && chunk.owner.owid == owner.owid){
				chunk.owner.set(Layers.NONE, null, 0);
			}
			chunk.save();
		}
		if(owner.is_county){
			county().districts.remove((Integer)id);
			county().save();
		}
		else{
			municipality().districts.remove((Integer)id);
			municipality().save();
		}
		owner.set(ResManager.getCounty(-1, true));
		save();
	}

	public void addTaxStat(long tax){
		tax_collected += tax;
		owner.addTaxStat(tax);
	}

}
