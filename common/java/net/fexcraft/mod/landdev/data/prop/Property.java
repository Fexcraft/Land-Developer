package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.ChunkLabel;
import net.fexcraft.mod.landdev.data.chunk.ChunkOwner;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.player.SpaceDefinitionCache;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Property implements Saveable, Layer, LDUIModule {

	public final int id;
	public Createable created = new Createable(true);
	public ChunkOwner owner = new ChunkOwner();
	public Sellable sell = new Sellable(this);
	public ExternalData external = new ExternalData(this);
	public ChunksIn chunks_in = new ChunksIn();
	public ChunkLabel label = new ChunkLabel();
	public PropRent rent = new PropRent();
	public V3I start = new V3I();
	public V3I end = new V3I();

	public Property(int ir){
		id = ir;
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		map.add("start", new JsonArray.Flat(start.x, start.y, start.z));
		map.add("end", new JsonArray.Flat(end.x, end.y, end.z));
		created.save(map);
		owner.save(map);
		sell.save(map);
		chunks_in.save(map);
		label.save(map);
		rent.save(map);
		external.save(this, map);
	}

	@Override
	public void load(JsonMap map){
		JsonArray arr = map.getArray("start");
		start.set(arr.get(0).integer_value(), arr.get(1).integer_value(), arr.get(2).integer_value());
		arr = map.getArray("end");
		end.set(arr.get(0).integer_value(), arr.get(1).integer_value(), arr.get(2).integer_value());
		created.load(map);
		owner.load(map);
		sell.load(map);
		chunks_in.load(map);
		label.load(map);
		rent.load(map);
		external.load(this, map);
	}

	@Override
	public String saveId(){
		return id + "";
	}

	@Override
	public String saveTable(){
		return "props";
	}

	@Override
	public void gendef(){
		external.gendef(this);
	}

	@Override
	public Layers getLayer(){
		return Layers.PROPERTY;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.CHUNK;
	}

	public static final int
		UI_LABEL = 1,
		UI_BUY = 2,
		UI_PRICE = 3,
		UI_RENT = 4,
		UI_RENT_DURATION = 5,
		UI_RENT_AMOUNT = 6
		;

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("property.title");
		boolean canman = owner.isPropMan(container.ldp) || container.ldp.adm;
		boolean renter = rent.renter.isPropMan(container.ldp) || container.ldp.adm;
		switch(container.pos.x){
			case UI_MAIN:{
				resp.addRow("id", ELM_GENERIC, id);
				if(label.present){
					resp.addRow("label", ELM_GENERIC, label.label);
				}
				if(!owner.unowned){
					resp.addRow("owner", ELM_GENERIC, owner.name());
				}
				if(sell.price > 0){
					resp.addButton("price", ELM_GREEN, OPEN, Config.getWorthAsString(sell.price));
				}
				if(canman){
					resp.addButton("set_label", ELM_GENERIC, OPEN);
					resp.addButton("set_price", ELM_GENERIC, OPEN);
				}
				resp.addBlank();
				if(rent.rentable || !rent.renter.unowned){
					if(rent.renter.unowned){
						resp.addButton("rent", ELM_GREEN, OPEN);
					}
					else{
						resp.addRow("renter", ELM_GENERIC, rent.renter.name());
						resp.addRow("until", ELM_GENERIC, Time.getAsString(rent.until));
						if(renter){
							if(rent.renewable){
								resp.addButton("renew", ELM_GENERIC, rent.autorenew ? ENABLED : DISABLED);
							}
							resp.addButton("end_rent", ELM_RED, REM);
						}
					}
					resp.addRow("amount", ELM_GENERIC, Config.getWorthAsString(rent.amount));
					resp.addRow("duration", ELM_GENERIC, rent.duration_string());
				}
				if(canman){
					resp.addBlank();
					resp.addButton("rentable", ELM_GENERIC, rent.rentable ? ENABLED : DISABLED);
					resp.addButton("renewable", ELM_GENERIC, rent.renewable ? ENABLED : DISABLED);
					resp.addButton("set_amount", ELM_GENERIC, OPEN);
					resp.addButton("set_duration", ELM_GENERIC, OPEN, Config.getWorthAsString(rent.amount));
				}
				break;
			}
			case UI_LABEL:{
				if(!canman) break;
				resp.setTitle("property.set_label.title");
				resp.addField("label", label.present ? label.label : "");
				resp.addButton("set_label.submit", ELM_BLUE, OPEN);
				resp.setFormular();
				break;
			}
			case UI_BUY:
				resp.setTitle("property.buy.title");
				resp.addRow("buy.for", ELM_GENERIC);
				resp.addRadio("buy.self", ELM_BLUE, true);
				resp.addRadio("buy.company", ELM_BLUE, false);
				resp.addButton("buy.submit", ELM_YELLOW, OPEN);
				resp.setFormular();
				break;
			case UI_PRICE:
				if(!canman) break;
				resp.setTitle("property.set_price.title");
				resp.addField("price", sell.price);
				resp.addButton("set_price.submit", ELM_BLUE, OPEN);
				resp.setFormular();
				break;
			case UI_CREATE:{
				Chunk_ ck = container.ldp.chunk_current;
				resp.setTitle("property.create.title");
				resp.addBlank();
				resp.addRow("create.info", ELM_YELLOW);
				Layers ow = ck.owner.layer();
				if(ow.is(Layers.NONE)) ow = ck.district.getParentLayer();
				resp.addRow("create.owner_type", ELM_BLUE, ow);
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
		boolean canman = owner.isPropMan(container.ldp) || container.ldp.adm;
		boolean renter = rent.renter.isPropMan(container.ldp) || container.ldp.adm;
		LDPlayer player = container.ldp;
		switch(req.event()){
			case "set_label": container.open(UI_LABEL); break;
			case "price": container.open(UI_BUY); break;
			case "set_price": container.open(UI_PRICE); break;
			case "rent": container.open(UI_RENT); break;
			case "renew":{
				if(renter) rent.autorenew = !rent.autorenew;
				container.open(UI_MAIN);
				save();
				break;
			}
			case "rentable":{
				if(canman) rent.rentable = !rent.rentable;
				container.open(UI_MAIN);
				save();
				break;
			}
			case "renewable":{
				if(canman) rent.renewable = !rent.renewable;
				container.open(UI_MAIN);
				save();
				break;
			}
			case "amount": container.open(UI_RENT_AMOUNT); break;
			case "set_duration": container.open(UI_RENT_DURATION); break;
			case "set_label.submit":{
				if(!canman) break;
				label.label = req.getField("label");
				label.present = label.label.length() > 0;
				container.open(UI_MAIN);
				save();
				break;
			}
			case "set_price.submit":{
				if(!canman) break;
				sell.price = req.getFieldInt("price");
				container.open(UI_MAIN);
				save();
				break;
			}
			case "buy.submit":{
				boolean ply = req.getRadio().equals("buy.self");
				if(!ply){
					//TODO
				}
				else{
					Account oacc = owner.getAccount(player.chunk_current);
					if(!oacc.getBank().processAction(Bank.Action.TRANSFER, player.entity, player.account, sell.price, oacc)){
						return;
					}
					sell.price = 0;
					owner.set(Layers.PLAYER, player.uuid, 0);
					container.open(UI_MAIN);
					save();
				}
				break;
			}
			case "create.submit":{
				player.defcache = new SpaceDefinitionCache(player.entity.getV3I());
				player.entity.openUI(LDKeys.DEF_SPACE, player.defcache.pos);
				break;
			}
		}
		external.on_interact(container, req);
	}

	public boolean isInside(V3I pos){
		if(pos.x >= start.x && pos.x <= end.x){
			if(pos.y >= start.y && pos.y <= end.y){
				if(pos.z >= start.z && pos.z <= end.z){
					return true;
				}
			}
		}
		return false;
	}

	public boolean intersects(V3I os, V3I oe){
		return !(end.x <= os.x || start.x >= oe.x || end.y <= os.y || start.y >= oe.y || end.z <= os.z || start.z >= oe.z);
	}

}
