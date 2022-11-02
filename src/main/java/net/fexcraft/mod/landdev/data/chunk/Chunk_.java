package net.fexcraft.mod.landdev.data.chunk;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.gui.GuiHandler.DISTRICT;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Createable;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.Sellable;
import net.fexcraft.mod.landdev.data.Taxable;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.util.ResManager;
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

	public boolean can_manage(Player player, boolean notplayer){
		if(player.adm) return true;
		UUID uuid = player.uuid;
		if(!notplayer && owner.playerchunk && owner.player.equals(uuid)) return true;
		if(owner.owner.is(Layers.DISTRICT) && (district.manage.isManager(uuid) || district.owner.manageable().isManager(uuid))) return true;
		if(owner.owner.is(Layers.MUNICIPALITY) && district.owner.manageable().isManager(uuid)) return true;
		//TODO
		return false;
	}

	@Override
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
		com.setString("title_lang", "chunk.title");
		NBTTagList list = new NBTTagList();
		if(container.x == 0){
			boolean canman = can_manage(container.player(), false);
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			if(link == null){
				addToList(list, "link", ELM_GENERIC, canman ? ICON_ADD : ICON_EMPTY, true, false, null);
			}
			else if(link.linked != null){
				addToList(list, "links", ELM_GENERIC, ICON_LIST, true, false, link.linked.size());
			}
			else if(link.root_key != null){
				addToList(list, "linked", ELM_GENERIC, ICON_REM, true, false, link.root_key.comma());
			}
			addToList(list, "type", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, false, type.lang());
			addToList(list, "district", ELM_GENERIC, ICON_OPEN, true, false, district.name());
			addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			addToList(list, "owner", ELM_GENERIC, ICON_OPEN, true, false, owner.name());
			if(sell.price > 0){
				addToList(list, "price", ELM_GENERIC, ICON_OPEN, true, false, sell.price_formatted());
			}
			else if(canman){
				addToList(list, "set_price", ELM_GENERIC, ICON_OPEN, true, false, null);
			}
			addToList(list, "tax", ELM_GENERIC, ICON_ADD, true, false, getWorthAsString(tax.custom_tax == 0 ? district.tax() : tax.custom_tax));
			addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
			addToList(list, "access_interact", ELM_GENERIC, canman ? access.interact ? ICON_ENABLED : ICON_DISABLED : ICON_EMPTY, canman, false, access.interact ? LANG_YES : LANG_NO);
			addToList(list, "access_player", ELM_GENERIC, ICON_LIST, true, false, access.players.size());
			addToList(list, "access_company", ELM_GENERIC, ICON_LIST, true, false, access.companies.size());
		}
		else if(container.x == 1){
			//
		}
		else if(container.x == 2){
			//
		}
		else if(container.x == 3){
			//
		}
		else if(container.x == 4){
			com.setString("title_lang", "chunk.select_type.title");
			addToList(list, "key", ELM_GENERIC, ICON_BLANK, false, false, key.comma());
			addToList(list, "type.normal", ELM_BLUE, radio(type == ChunkType.NORMAL), true, false, null);
			addToList(list, "type.private", ELM_BLUE, radio(type == ChunkType.PRIVATE), true, false, null);
			addToList(list, "type.restricted", ELM_BLUE, radio(type == ChunkType.RESTRICTED), true, false, null);
			addToList(list, "type.public", ELM_BLUE, radio(type == ChunkType.PUBLIC), true, false, null);
			addToList(list, "select_type.submit", ELM_GENERIC, ICON_OPEN, true, false, null);
			com.setBoolean("form", true);
		}
		com.setTag("elements", list);
	}

	@Override
	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index){
		boolean canman = can_manage(container.player(), false);
		switch(index){
			case "access_interact":{
				if(!canman) return;
				access.interact = !access.interact;
				container.sendSync();
				return;
			}
			case "link": container.open(1); return;
			case "linka": container.open(2); return;
			case "linked": container.open(3); return;
			case "type": if(canman) container.open(4); return;
			case "district": container.open(DISTRICT, 0, district.id, 0);
			case "owner":{
				//TODO 5 / open player profile or layer main ui
				return;
			}
			case "price":{
				//TODO 6 / open ui with choice what layer to buy chunk for
				return;
			}
			case "set_price": container.open(7); return;
			case "tax": if(can_manage(player, true)) container.open(8); return;
			case "access_player": container.open(9); return;
			case "access_company": container.open(10); return;
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
				container.open(0);
				return;
			}
		}
	}

}
