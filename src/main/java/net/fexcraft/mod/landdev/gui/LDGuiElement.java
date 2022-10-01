package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericGui.BasicButton;
import net.fexcraft.lib.mc.gui.GenericGui.BasicText;
import net.fexcraft.lib.mc.gui.GenericGui.TextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class LDGuiElement {
	
	public String index;
	public LDGuiElementType type;
	protected int pos, off;
	protected boolean visible;
	//
	private BasicText text;
	private BasicButton button;
	private TextField field;
	
	public LDGuiElement(String index, LDGuiElementType type){
		this.index = index;
		this.type = type;
	}

	public LDGuiElement text(LDGuiBase gui, String text, String val){
		if(text != null || val != null){
			this.text = new BasicText(gui.getGuiLeft() + 8, 0, 196, 0xcdcdcd, "landdev.gui." + (gui.prefix()) + "." + text);
			gui.add(index, (val == null ? this.text.translate() : this.text.translate(I18n.format(val))).hoverable(true).autoscale());
		}
		return this;
	}

	public LDGuiElement button(LDGuiBase gui, boolean button){
		off = 1;
		if(!button) return this;
		gui.add(this.button = new BasicButton(index, gui.getGuiLeft() + 208, 0, type.x, type.y, type.w, type.h, true){
			
			@Override
			public boolean onclick(int x, int y, int b){
				if(type.is_checkbox()){
					boolean bool = !gui.container().checkboxes.get(index);
					gui.container().checkboxes.put(index, bool);
					type = LDGuiElementType.checkbox(bool);
					tx = type.x;
					ty = type.y;
					return true;
				}
				if(gui.container().form()){
					if(!index.contains("submit")) return true;
					NBTTagCompound com = new NBTTagCompound();
					com.setBoolean("submit", true);
					com.setString("interact", index);
					NBTTagCompound cbs = new NBTTagCompound();
					gui.container().checkboxes.forEach((key, val) -> cbs.setBoolean(key, val));
					com.setTag("checkboxes", cbs);
					NBTTagCompound fields = new NBTTagCompound();
					gui.fields().forEach((key, val) -> fields.setString(key, val.getText()));
					com.setTag("fields", fields);
					gui.container().send(Side.SERVER, com);
					return true;
				}
				NBTTagCompound com = new NBTTagCompound();
				com.setString("interact", index);
				if(gui.hasField(index)) com.setString("field", gui.getField(index));
				gui.container().send(Side.SERVER, com);
				return true;
			}
			
		});
		return this;
	}

	public LDGuiElement field(LDGuiBase gui, String val, boolean wide){
		gui.add(index, field = new TextField(pos, gui.fontrenderer(), gui.getGuiLeft() + 7, 0, wide ? 212 : 198, 10).setMaxLength(256));
		if(val != null) gui.setField(index, val);
		return this;
	}

	public void repos(int top, int nidx, boolean vis){
		pos = top + (nidx * 14) + 19 + off;
		if(text != null){
			text.y = pos + 3;
			text.visible = vis;
		}
		if(button != null){
			button.y = pos;
			button.visible = vis;
		}
		if(field != null){
			field.y = pos + 2;
			field.setVisible(vis);
		}
		visible = vis;
	}

}
