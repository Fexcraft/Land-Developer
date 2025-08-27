package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BaseUI extends UserInterface {

	protected static ConcurrentHashMap<UIText, List<String>> texttips = new ConcurrentHashMap<>();
	protected ArrayList<BaseElm> elements = new ArrayList<>();
	protected List<String> hint;
	protected IDL imgres;
	protected BaseCon bon;
	protected boolean addscroll;
	protected int scroll;

	public BaseUI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		bon = (BaseCon)container;
	}

	@Override
	public void init(){
		sync();
	}

	private void sync(){
		TagCW com = TagCW.create();
		com.set("sync", true);
		container.SEND_TO_SERVER.accept(com);
	}

	public void msg(String msg){
		texts.get("note").value(msg);
		texts.get("note").translate();
		tabs.get("note").visible(true);
	}

	public void clear(){
		texts.keySet().removeIf(key -> key.startsWith("gen_"));
		buttons.keySet().removeIf(key -> key.startsWith("gen_"));
		fields.keySet().removeIf(key -> key.startsWith("gen_"));
		tabs.keySet().removeIf(key -> key.startsWith("gen_"));
	}

	public void scroll(int d){
		scroll += d;
		if(scroll < 0) scroll = 0;
		int s = elements.size();
		int l = s < 12 ? 0 : s - 12;
		if(scroll > l) scroll = l;
		for(int i = 0; i < s; i++){
			int k = i - scroll;
			elements.get(i).repos(k, k >= 0 && k < 12);
		}
		UITab tab = tabs.remove("scroll");
		tabs.put("scroll", tab);
		tab = tabs.remove("icon");
		tabs.put("icon", tab);
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		buttons.get("flash").ty = Time.getSecond() % 2 == 1 ? 23 : 5;
	}

	@Override
	public void drawbackground(float ticks, int mx, int my){
		if(tabs.get("icon").visible() && imgres != null){
			drawer.bind(imgres);
			drawer.drawFull(tabs.get("icon").x + 3 + gLeft, tabs.get("icon").y + 3 + gTop, 28, 28);
		}
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		if(id.startsWith("gen_")){
			for(BaseElm elm : elements){
				if(elm.id.equals(id)){
					elm.run.run();
					return true;
				}
			}
		}
		switch(id){
			case "scroll_up":{
				scroll(-1);
				return true;
			}
			case "scroll_down":{
				scroll(1);
				return true;
			}
			case "back":{
				TagCW com = TagCW.create();
				com.set("go_back", true);
				container.SEND_TO_SERVER.accept(com);
				return true;
			}
			case "note_ok":{
				tabs.get("note").visible(false);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		scroll(am > 0 ? 1 : -1);
		return true;
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		for(UIText text : texts.values()){
			if(text.hovered()){
				hint = texttips.get(text);
				if(hint == null) list.add(text.value());
				else list.addAll(hint);
			}
		}
		if(bon.checkboxes.size() > 0 || bon.radioboxes.size() > 0){
			for(BaseElm elm : elements){
				if(!elm.icon.isCheck() && !elm.icon.isRadio()) continue;
				if(elm.button == null || !elm.button.hovered()) continue;
				list.add(drawer.translate(elm.icon.translation()));
			}
		}
		if(list.size() == 1 && list.get(0).length() == 0) list.clear();
	}

	protected void addElm(String id, LDUIRow elm, LDUIButton icon, boolean text, boolean button, boolean field, Object val){
		BaseElm belm = new BaseElm(this, "gen_" + id, elm, icon, button);
		if(field) belm.addField(this, val, icon == LDUIButton.BLANK);
		else belm.addText(this, text ? id : null, val);
		elements.add(belm);
	}

	public static class BaseElm {

		protected LDUIRow row;
		protected LDUIButton icon;
		protected UITab tab;
		protected UIButton button;
		protected UIField field;
		protected UIText text;
		protected Runnable run;
		private String id;

		public BaseElm(BaseUI base, String id, LDUIRow row, LDUIButton icon, boolean ab){
			this.icon = icon;
			this.row = row;
			this.id = id;
			try{
				tab = UIElement.create(UITab.IMPLEMENTATION, base, base.container.ui_map.getMap("tabs").getMap(row.tabid));
				while(base.tabs.containsKey(id)) id += ".";
				base.tabs.put(id, tab);
				tab.visible(true);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			addButton(base);
			if(!ab){
				button.enabled(false);
				button.ecolor.packed = button.dcolor.packed = button.hcolor.packed = RGB.WHITE.packed;
			}
		}

		public void repos(int nidx, boolean vis){
			tab.y = (nidx * 14) + 19;
			if(text != null){
				text.y = tab.y + 3;
				text.visible(vis);
			}
			if(button != null){
				button.y = tab.y + 1;
				button.visible(vis);
			}
			if(field != null){
				field.y = tab.y + 1;
				field.visible(vis);
			}
			tab.visible(vis);
		}

		public void addText(BaseUI base, String str, Object val){
			if(str == null && val == null) return;
			try{
				text = UIElement.create(UIText.IMPLEMENTATION, base, base.container.ui_map.getMap("texts").getMap("temp"));
				text.color.packed = row.lighttext() ? 0xdedede : 0x5d5d5d;
				text.hover.packed = row.lighttext() ? 0x574a0d : 0xcfb117;
				text.value("landdev.gui." + base.bon.prefix + "." + str);
				String tip = "landdev.gui." + base.bon.prefix + "." + str + ".hint";
				String hin = ContainerInterface.TRANSLATOR.apply(tip);
				if(!hin.equals(tip)){
					texttips.put(text, Arrays.asList(hin.split("\\\\n")));
				}
				if(val == null) text.translate();
				else if(val instanceof String){
					String valstr = val.toString();
					if(valstr.startsWith(LDUIModule.VALONLY)) text.value(valstr.substring(3));
					else{
						String old = text.value();
						text.transval(valstr);
						text.transval(old, text.value());
					}
				}
				else{
					String[] arr = (String[])val;
					if(arr.length == 0){
						text.transval(arr[0]);
					}
					else{
						text.transval(arr[0], Arrays.copyOfRange(arr, 1, arr.length));
					}
				}
				text.scale = -1;
				tab.texts.put(id, text);
				base.texts.put(id, text);
				text.visible(true);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		public void addField(BaseUI base, Object val, boolean wide){
			try{
				field = UIElement.create(UIField.IMPLEMENTATION, base, new JsonMap());
				field.width = (field.background = wide) ? 212 : 198;
				field.height = 12;
				field.x = 6;
				base.fields.put(id, field);
				tab.fields.put(id, field);
				base.root.initField(field);
				field.maxlength(256);
				if(val != null) field.text(val.toString());
				field.visible(true);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		private void addButton(BaseUI base){
			try{
				button = UIElement.create(UIButton.IMPLEMENTATION, base, base.container.ui_map.getMap("buttons").getMap(icon.id));
				base.buttons.put(id, button);
				tab.buttons.put(id, button);
				String idx = id.substring(4);
				run = () -> {
					if(icon.isCheck()){
						boolean bool = !base.bon.checkboxes.get(idx);
						base.bon.checkboxes.put(idx, bool);
						icon = LDUIButton.checkbox(bool);
						JsonArray uv = base.container.ui_map.getMap("buttons").getMap(icon.id).getArray("uv");
						button.tx = uv.get(0).integer_value();
						button.ty = uv.get(1).integer_value();
						return;
					}
					if(icon.isRadio()){
						JsonArray uv;
						for(BaseElm elm : base.elements){
							if(!elm.icon.isRadio()) continue;
							elm.icon = LDUIButton.radio(false);
							uv = base.container.ui_map.getMap("buttons").getMap(icon.id).getArray("uv");
							elm.button.tx = uv.get(0).integer_value();
							elm.button.ty = uv.get(1).integer_value();
						}
						base.bon.radiobox = idx;
						icon = LDUIButton.radio(true);
						uv = base.container.ui_map.getMap("buttons").getMap(icon.id).getArray("uv");
						button.tx = uv.get(0).integer_value();
						button.ty = uv.get(1).integer_value();
						return;
					}
					if(base.bon.form){
						if(!idx.contains("submit") && !base.bon.nosubmit) return;
						TagCW com = TagCW.create();
						com.set("submit", true);
						com.set("interact", idx);
						TagCW cbs = TagCW.create();
						base.bon.checkboxes.forEach((key, val) -> cbs.set(key, val));
						com.set("checkboxes", cbs);
						if(base.bon.radiobox != null) com.set("radiobox", base.bon.radiobox);
						TagCW fields = TagCW.create();
						base.fields.forEach((key, val) -> fields.set(key.substring(4), val.text()));
						base.bon.sfields.forEach((key, val) -> fields.set(key, val));
						com.set("fields", fields);
						base.container.SEND_TO_SERVER.accept(com);
						return;
					}
					TagCW com = TagCW.create();
					com.set("interact", idx);
					if(base.fields.containsKey(idx)) com.set("field", base.fields.get(idx).text());
					base.container.SEND_TO_SERVER.accept(com);
					return;
				};
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

	}

}
