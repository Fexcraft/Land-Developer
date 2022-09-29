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
	protected int pos;
	
	public LDGuiElement(String index, LDGuiElementType type){
		this.index = index;
		this.type = type;
	}

	public LDGuiElement pos(int num, int off){
		pos = num * 14 + 19 + off;
		return this;
	}

	public LDGuiElement text(LDGuiBase gui, String text, String val){
		if(text != null || val != null){
			BasicText telm = new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + pos + 3, 196, 0xcdcdcd, "landdev.gui." + (gui.prefix()) + "." + text);
			gui.add(index, (val == null ? telm.translate() : telm.translate(I18n.format(val))).hoverable(true).autoscale());
		}
		return this;
	}

	public LDGuiElement button(LDGuiBase gui, boolean button){
		if(!button) return this;
		gui.add(new BasicButton(index, gui.getGuiLeft() + 208, gui.getGuiTop() + pos, type.x, type.y, type.w, type.h, true){
			
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
				if(gui.container().form() && !index.contains("submit")) return true;
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
		gui.add(index, new TextField(pos, gui.fontrenderer(), gui.getGuiLeft() + 7, gui.getGuiTop() + pos + 2, wide ? 212 : 198, 10));
		if(val != null) gui.setField(index, val);
		return this;
	}

}
