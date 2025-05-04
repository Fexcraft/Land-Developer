package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.landdev.data.ColorData;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.ui.modules.MailModule;
import net.fexcraft.mod.landdev.ui.modules.MainModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.IDLManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIKey;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static net.fexcraft.mod.landdev.ui.LDKeys.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.ID_COUNTY;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BaseCon extends ContainerInterface {

	protected Chunk_ chunk;
	public LDPlayer ldp;
	protected UIKey type;
	protected String prefix;
	protected int backto;
	protected BaseUI bui;
	//
	protected LinkedHashMap<String, String> sfields = new LinkedHashMap<>();
	protected HashMap<String, Boolean> checkboxes = new HashMap<>();
	protected ArrayList<String> radioboxes = new ArrayList<>();
	protected String radiobox;
	public boolean nosubmit;
	public boolean form;

	public BaseCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		if(!ply.entity.isOnClient()){
			ldp = ResManager.getPlayer(ply);
			chunk = ldp.chunk_current;
		}
		type = MAIN;
		prefix = "main";
	}

	public ContainerInterface set(UserInterface ui){
		bui = (BaseUI)ui;
		return super.set(ui);
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(client){
			if(com.has("msg")){
				bui.msg(com.getString("msg"));
				return;
			}
			if(!com.has("elements")) return;
			process_client_packet(com);
			return;
		}
		if(com.has("sync")){
			sendResp();
		}
		if(com.has("interact")){
			ModuleRequest req = new ModuleRequest(com);
			switch(type.id){
				case ID_MAIN:{
					MainModule.INST.on_interact(this, req);
					break;
				}
				case ID_MAIL:{
					MailModule.INST.on_interact(this, req);
					break;
				}
				case ID_PLAYER:{
					ldp.on_interact(this, req);
					break;
				}
				case ID_CHUNK:{
					Chunk_ chunk = ResManager.getChunk(pos.y, pos.z);
					chunk.on_interact(this, req);
					break;
				}
				case ID_DISTRICT:{
					District dis = ResManager.getDistrict(pos.y);
					if(dis != null){
						dis.on_interact(this, req);
						break;
					}
					break;
				}
				case ID_MUNICIPALITY:{
					if(pos.x < 0){
						ResManager.getMunicipality(-1, true).on_interact(this, req);
						break;
					}
					Municipality mun = ResManager.getMunicipality(pos.y, pos.y > -2);
					if(mun != null){
						mun.on_interact(this, req);
						break;
					}
					break;
				}
				case ID_COUNTY:{
					if(pos.x < 0){
						ResManager.getCounty(-1, true).on_interact(this, req);
						break;
					}
					County ct = ResManager.getCounty(pos.y, pos.y > -1);
					if(ct != null){
						ct.on_interact(this, req);
						break;
					}
					break;
				}
				case ID_REGION:{
					if(pos.x < 0){
						ResManager.getRegion(-1, true).on_interact(this, req);
						break;
					}
					Region rg = ResManager.getRegion(pos.y, pos.y > -1);
					if(rg != null){
						rg.on_interact(this, req);
						break;
					}
					break;
				}
				default: LDUIModule.Missing.INST.on_interact(this, req); break;
			}
		}
		if(com.has("go_back")){
			if(pos.x != 0) open(backto);
			else{
				Chunk_ chunk = ResManager.getChunk(player.entity);
				open(MAIN, 0, chunk.key.x, chunk.key.z);
			}
		}
	}

	private void process_client_packet(TagCW com){
		TagLW list = com.getList("elements");
		sfields.clear();
		bui.clear();
		int size = 0;
		for(int i = 0; i < list.size(); i++){
			TagLW li = list.getList(i);
			if(li.getString(3).charAt(2) != '2') size++;
		}
		bui.tabs.get("scroll").visible(bui.addscroll = size > 12);
		bui.texts.get("title").value("landdev.gui." + com.getString("title_lang"));
		if(com.has("title")) bui.texts.get("title").translate(com.getString("title"));
		else bui.texts.get("title").translate();
		//
		if(type != MAIN && !com.has("noback")){
			backto = com.has("backto") ? com.getInteger("backto") : 0;
			bui.tabs.get("back").visible(true);
		}
		else bui.tabs.get("back").visible(false);
		//
		bui.texttips.clear();
		bui.elements.clear();
		for(int li = 0; li < list.size(); li++){
			TagLW lis = list.getList(li);
			String index = lis.getString(0);
			LDUIRow elm = LDUIRow.valueOf(lis.getString(1));
			LDUIButton icon = LDUIButton.valueOf(lis.getString(2));
			String bools = lis.getString(3);
			String val = lis.size() > 4 ? lis.getString(4) : null;
			if(bools.charAt(2) == '2'){
				sfields.put(index, val);
			}
			else bui.addElm(index, elm, icon, bools.charAt(0) == '1', bools.charAt(1) == '1', bools.charAt(2) == '1', val);
			if(icon.isCheck()){
				checkboxes.put(index, icon.check());
			}
			if(icon.isRadio()){
				radioboxes.add(index);
				if(icon.radio()) radiobox = index;
			}
		}
		bui.tabs.get("bottom").y = (bui.elements.size() > 12 ? 12 : bui.elements.size()) * 14 + 19;
		form = com.getBoolean("form");
		nosubmit = com.getBoolean("nosubmit");
		if((com.has("gui_icon") && bui.elements.size() > 6)){
			bui.buttons.get("color").ecolor.packed = com.getInteger("gui_color");
			String icon = com.getString("gui_icon");
			if(icon.startsWith("server:")){
				bui.imgres = UniFCL.requestServerFile(null, icon);
			}
			else if(icon.startsWith("http") || icon.contains("://")){
				bui.imgres = bui.drawer.loadExternal(icon);
			}
			else bui.imgres = IDLManager.getIDLCached(icon);
			bui.tabs.get("icon").visible(true);
		}
		else bui.tabs.get("icon").visible(false);
		bui.scroll(0);
	}

	public void sendResp(){
		ModuleResponse resp = new ModuleResponse();
		IconHolder holder = null;
		ColorData color = null;
		switch(type.id){
			case ID_MAIN:{
				MainModule.INST.sync_packet(this, resp);
				break;
			}
			case ID_MAIL:{
				MailModule.INST.sync_packet(this, resp);
				break;
			}
			case ID_PLAYER:{
				ldp.sync_packet(this, resp);
				break;
			}
			case ID_CHUNK:{
				chunk.sync_packet(this, resp);
				break;
			}
			case ID_DISTRICT:{
				District dis = ResManager.getDistrict(pos.y);
				if(dis != null){
					dis.sync_packet(this, resp);
					holder = dis.icon;
					color = dis.color;
					break;
				}
				break;
			}
			case ID_MUNICIPALITY:{
				if(pos.x < 0){
					ResManager.getMunicipality(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				Municipality mun = ResManager.getMunicipality(pos.y, pos.y > -2);
				if(mun != null){
					mun.sync_packet(this, resp);
					holder = mun.icon;
					color = mun.color;
					break;
				}
				break;
			}
			case ID_COUNTY:{
				if(pos.x < 0){
					ResManager.getCounty(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				County ct = ResManager.getCounty(pos.y, pos.y > -1);
				if(ct != null){
					ct.sync_packet(this, resp);
					holder = ct.icon;
					color = ct.color;
					break;
				}
				break;
			}
			case ID_REGION:{
				if(pos.x < 0){
					ResManager.getRegion(-1, true).sync_packet(this, resp);
					holder = chunk.district.region().icon;
					color = chunk.district.region().color;
					break;
				}
				Region rg = ResManager.getRegion(pos.y, pos.y > -1);
				if(rg != null){
					rg.sync_packet(this, resp);
					holder = rg.icon;
					color = rg.color;
					break;
				}
				break;
			}
			default: LDUIModule.Missing.INST.sync_packet(this, resp); break;
		}
		if(holder != null){
			resp.getCompound().set("gui_icon", holder.getnn());
			resp.getCompound().set("gui_color", color.getInteger());
		}
		SEND_TO_CLIENT.accept(resp.build(), player);
	}

	public void open(int x){
		player.entity.openUI(type, x, pos.y, pos.z);
	}

	public void open(int x, int y, int z){
		player.entity.openUI(type, x, y, z);
	}

	public void open(UIKey type, int x, int y, int z){
		player.entity.openUI(type, x, y, z);
	}

	public void msg(String string, boolean addprefix){
		TagCW com = TagCW.create();
		com.set("msg", addprefix ? "landdev.gui." + prefix + "." + string : string);
		SEND_TO_CLIENT.accept(com, player);
	}

	public void msg(String string){
		msg(string, true);
	}

	//

	public static class PropBaseCon extends BaseCon {

		public PropBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = PROPERTY;
			prefix = "property";
		}

	}

	public static class ChunkBaseCon extends BaseCon {

		public ChunkBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = CHUNK;
			prefix = "chunk";
		}

	}

	public static class DisBaseCon extends BaseCon {

		public DisBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = DISTRICT;
			prefix = "district";
		}

	}

	public static class MunBaseCon extends BaseCon {

		public MunBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = MUNICIPALITY;
			prefix = "municipality";
		}

	}

	public static class CouBaseCon extends BaseCon {

		public CouBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = COUNTY;
			prefix = "county";
		}

	}

	public static class RegBaseCon extends BaseCon {

		public RegBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = REGION;
			prefix = "region";
		}

	}

	public static class PlayerBaseCon extends BaseCon {

		public PlayerBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = PLAYER;
			prefix = "player";
		}

	}

	public static class PollBaseCon extends BaseCon {

		public PollBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = POLL;
			prefix = "poll";
		}

	}

	public static class MailBaseCon extends BaseCon {

		public MailBaseCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
			type = MAIL;
			prefix = "mail";
		}

	}

}
