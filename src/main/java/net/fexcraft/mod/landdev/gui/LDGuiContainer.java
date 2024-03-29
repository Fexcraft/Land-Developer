package net.fexcraft.mod.landdev.gui;

import static net.fexcraft.mod.landdev.gui.GuiHandler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui.BasicButton;
import net.fexcraft.lib.mc.gui.GenericGui.BasicText;
import net.fexcraft.lib.mc.render.ExternalTextureHelper;
import net.fexcraft.mod.landdev.data.ColorData;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.modules.MailModule;
import net.fexcraft.mod.landdev.gui.modules.MainModule;
import net.fexcraft.mod.landdev.gui.modules.Missing;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LDGuiContainer extends GenericContainer {

	protected String prefix;
	public final int type, x, y, z;
	@SideOnly(Side.CLIENT)
	public LDGuiBase gui;
	public final Player player;
	private boolean form;
	private boolean nosubmit;
	protected HashMap<String, Boolean> checkboxes = new HashMap<>();
	protected ArrayList<String> radioboxes = new ArrayList<>();
	protected LinkedHashMap<String, String> sfields = new LinkedHashMap<>();
	protected String radiobox;
	protected int backto;

	public LDGuiContainer(EntityPlayer player, int id, int x, int y, int z){
		super(player);
		switch(type = id){
			case MAIN: prefix = "main"; break;
			case MAIL: prefix = "mail"; break;
			case CHUNK: prefix = "chunk"; break;
			case DISTRICT: prefix = "district"; break;
			case MUNICIPALITY: prefix = "municipality"; break;
			case COUNTY: prefix = "county"; break;
		}
		this.player = ResManager.getPlayer(player);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side == Side.CLIENT) client_packet(packet, player);
		else server_packet(packet, player);
	}

	private void server_packet(NBTTagCompound packet, EntityPlayer player){
		//Player ply = ResManager.getPlayer(player);
		if(packet.getBoolean("sync")){
			sendSync();
			return;
		}
		if(packet.hasKey("interact")){
			ModuleRequest req = new ModuleRequest(packet);
			switch(type){
				case MAIN:{
					MainModule.INST.on_interact(this, req);
					break;
				}
				case MAIL:{
					MailModule.INST.on_interact(this, req);
					break;
				}
				case CHUNK:{
					Chunk_ chunk = ResManager.getChunk(y, z);
					chunk.on_interact(this, req);
					break;
				}
				case DISTRICT:{
					District dis = ResManager.getDistrict(y);
					if(dis != null){
						dis.on_interact(this, req);
						break;
					}
					break;
				}
				case MUNICIPALITY:{
					if(x < 0){
						ResManager.getMunicipality(-1, true).on_interact(this, req);
						break;
					}
					Municipality mun = ResManager.getMunicipality(y, y > -2);
					if(mun != null){
						mun.on_interact(this, req);
						break;
					}
					break;
				}
				case COUNTY:{
					if(x < 0){
						ResManager.getCounty(-1, true).on_interact(this, req);
						break;
					}
					County ct = ResManager.getCounty(y, y > -1);
					if(ct != null){
						ct.on_interact(this, req);
						break;
					}
					break;
				}
				default: Missing.INST.on_interact(this, req); break;
			}
		}
		if(packet.hasKey("go_back")){
			if(x != 0) open(backto);
			else{
				Chunk_ chunk = ResManager.getChunk(player);
				open(MAIN, 0, chunk.key.x, chunk.key.z);
			}
		}
	}

	public void sendSync(){
		Chunk_ chunk = ResManager.getChunk(player.entity);
		ModuleResponse resp = new ModuleResponse();
		IconHolder holder = null;
		ColorData color = null;
		switch(type){
			case MAIN:{
				MainModule.INST.sync_packet(this, resp);
				break;
			}
			case MAIL:{
				MailModule.INST.sync_packet(this, resp);
				break;
			}
			case CHUNK:{
				chunk.sync_packet(this, resp);
				break;
			}
			case DISTRICT:{
				District dis = ResManager.getDistrict(y);
				if(dis != null){
					dis.sync_packet(this, resp);
					holder = dis.icon;
					color = dis.color;
					break;
				}
				break;
			}
			case MUNICIPALITY:{
				if(x < 0){
					ResManager.getMunicipality(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				Municipality mun = ResManager.getMunicipality(y, y > -2);
				if(mun != null){
					mun.sync_packet(this, resp);
					holder = mun.icon;
					color = mun.color;
					break;
				}
				break;
			}
			case COUNTY:{
				if(x < 0){
					ResManager.getCounty(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				County ct = ResManager.getCounty(y, y > -1);
				if(ct != null){
					ct.sync_packet(this, resp);
					holder = ct.icon;
					color = ct.color;
					break;
				}
				break;
			}
			default: Missing.INST.sync_packet(this, resp); break;
		}
		if(holder != null){
			resp.getCompound().setString("gui_icon", holder.getnn());
			resp.getCompound().setInteger("gui_color", color.getInteger());
		}
		send(Side.CLIENT, resp.build());
	}

	private void client_packet(NBTTagCompound packet, EntityPlayer player){
		if(packet.hasKey("msg")){
			gui.setMsg(gui.notification.string = Formatter.format(I18n.format(packet.getString("msg"))));
			return;
		}
		else if(!packet.hasKey("elements")) return;
		NBTTagList list = (NBTTagList)packet.getTag("elements");
		sfields.clear();
		gui.clear();
		int size = 0;
		for(NBTBase base : list) if(((NBTTagList)base).getStringTagAt(3).charAt(2) != '2') size++;
		gui.addscroll = size > 12;
		gui.sizeOf(gui.addscroll ? 12 : size);
		if(gui.addscroll){
			LDGuiElementType type = LDGuiElementType.SCROLL_UP;
			gui.add(new BasicButton("scroll_up", gui.getGuiLeft() + type.x - 5, gui.getGuiTop() + type.y, type.x, type.y, type.w, type.h, true){
				@Override
				public boolean onclick(int x, int y, int m){
					gui.scroll(-1);
					return true;
				}
			});
			type = LDGuiElementType.SCROLL_DOWN;
			gui.add(new BasicButton("scroll_down", gui.getGuiLeft() + type.x - 5, gui.getGuiTop() + type.y, type.x, type.y, type.w, type.h, true){
				@Override
				public boolean onclick(int x, int y, int m){
					gui.scroll(1);
					return true;
				}
			});
		}
		gui.title = new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + 8, 196, 0x0e0e0e, "landdev.gui." + packet.getString("title_lang")).hoverable(true).autoscale().translate();
		gui.add("title", gui.title);
		if(type != MAIN && !packet.hasKey("noback")){
			gui.backbutton = new BasicButton("back", gui.getGuiLeft() - 10, gui.getGuiTop() + 5, 243, 3, 11, 11, true){
				@Override
				public boolean onclick(int x, int y, int m){
					NBTTagCompound com = new NBTTagCompound();
					com.setBoolean("go_back", true);
					gui.container().send(Side.SERVER, com);
					return true;
				}
			};
			gui.add(gui.backbutton);
			backto = packet.hasKey("backto") ? packet.getInteger("backto") : 0;
		}
		else gui.backbutton = null;
		gui.elements().clear();
		if(packet.hasKey("title")) gui.title.string = String.format(gui.title.string, packet.getString("title"));
		for(NBTBase base : list){
			NBTTagList lis = (NBTTagList)base;
			String index = lis.getStringTagAt(0);
			LDGuiElementType elm = LDGuiElementType.valueOf(lis.getStringTagAt(1));
			LDGuiElementType icon = LDGuiElementType.valueOf(lis.getStringTagAt(2));
			String bools = lis.getStringTagAt(3);
			String val = lis.tagCount() > 4 ? lis.getStringTagAt(4) : null;
			if(bools.charAt(2) == '2'){
				sfields.put(index, val);
			}
			else gui.addElm(index, elm, icon, bools.charAt(0) == '1', bools.charAt(1) == '1', bools.charAt(2) == '1', val);
			if(icon.is_checkbox()){
				checkboxes.put(index, icon.checkbox());
			}
			if(icon.is_radiobox()){
				radioboxes.add(index);
				if(icon.radiobox()) radiobox = index;
			}
		}
		form = packet.getBoolean("form");
		nosubmit = packet.getBoolean("nosubmit");
		if(gui.showicon = (packet.hasKey("gui_icon") && gui.elements().size() > 6)){
			gui.iconurl = ExternalTextureHelper.get(packet.getString("gui_icon"));
			gui.color.packed = packet.getInteger("gui_color");
		}
		gui.scroll(0);
		gui.addMsgElms();
	}

	public boolean form(){
		return form;
	}

	public boolean nosubmit(){
		return nosubmit;
	}

	public void sendMsg(String string, boolean addprefix){
		NBTTagCompound com = new NBTTagCompound();
		com.setString("msg", addprefix ? "landdev.gui." + prefix + "." + string : string);
		send(Side.CLIENT, com);
	}

	public void sendMsg(String string){
		sendMsg(string, true);
	}

	public void open(int x){
		player.openGui(type, x, y, z);
	}

	public void open(int x, int y, int z){
		player.openGui(type, x, y, z);
	}

	public void open(int type, int x, int y, int z){
		player.openGui(type, x, y, z);
	}

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return !(player == null);
    }
	
}