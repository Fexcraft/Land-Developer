package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.checkbox;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.radio;

import net.fexcraft.mod.landdev.gui.LDGuiElementType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ModuleResponse {

	private NBTTagCompound compound;
	private NBTTagList list;
	private boolean form;

	public ModuleResponse(){
		compound = new NBTTagCompound();
		list = new NBTTagList();
	}

	public NBTTagCompound getCompound(){
		return compound;
	}

	public void setFormular(){
		form = true;
	}

	public void setNoBack(){
		compound.setBoolean("noback", true);
	}

	public NBTTagCompound build(){
		compound.setTag("elements", list);
		if(form) compound.setBoolean("form", true);
		return compound;
	}

	public void setTitle(String title){
		compound.setString("title_lang", title);
	}

	public void addRow(String id, LDGuiElementType style){
		addEntry(list, id, style, ICON_BLANK, false, false, null);
	}

	public void addRow(String id, LDGuiElementType style, Object value){
		addEntry(list, id, style, ICON_BLANK, false, false, value);
	}

	public void addRow(String id, LDGuiElementType style, LDGuiElementType icon){
		addEntry(list, id, style, icon, false, false, null);
	}

	public void addRow(String id, LDGuiElementType style, LDGuiElementType icon, Object value){
		addEntry(list, id, style, icon, false, false, value);
	}

	public void addRow(String id, LDGuiElementType style, LDGuiElementType icon, boolean button, Object value){
		addEntry(list, id, style, icon, button, false, value);
	}

	public void addButton(String id, LDGuiElementType style, LDGuiElementType icon){
		addEntry(list, id, style, icon, true, false, null);
	}

	public void addButton(String id, LDGuiElementType style, LDGuiElementType icon, Object value){
		addEntry(list, id, style, icon, true, false, value);
	}

	public void addField(String id){
		addEntry(list, id, ELM_BLANK, ELM_BLANK, false, true, null);
	}

	public void addField(String id, Object value){
		addEntry(list, id, ELM_BLANK, ELM_BLANK, false, true, value);
	}

	public void addRadio(String id, LDGuiElementType style, boolean checked){
		addEntry(list, id, style, radio(checked), true, false, null);
	}

	public void addRadio(String id, LDGuiElementType style, boolean checked, Object value){
		addEntry(list, id, style, radio(checked), true, false, value);
	}

	public void addCheck(String id, LDGuiElementType style, boolean checked){
		addEntry(list, id, style, checkbox(checked), true, false, null);
	}

	public void addCheck(String id, LDGuiElementType style, boolean checked, Object value){
		addEntry(list, id, style, checkbox(checked), true, false, value);
	}

	public void addBlank(){
		addEntry(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
	}

	//

	public void addEntry(NBTTagList root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value){
		addEntry(root, index, elm, icon, button, field, value, false);
	}

	public void addEntry(NBTTagList root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value, boolean valonly){
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString(index));
		list.appendTag(new NBTTagString(elm.name()));
		list.appendTag(new NBTTagString(icon.name()));
		list.appendTag(new NBTTagString((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? "1" : "0")));
		if(value != null) list.appendTag(new NBTTagString(valonly ? val(value.toString()) : value.toString()));
		root.appendTag(list);
	}

	public String val(String string){
		return LDGuiModule.VALONLY + string;
	}

}
