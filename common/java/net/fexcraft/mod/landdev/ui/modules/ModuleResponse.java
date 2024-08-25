package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.checkbox;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.radio;

import net.fexcraft.mod.landdev.gui.LDGuiElementType;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ModuleResponse {

	private TagCW compound;
	private TagLW list;
	private boolean form;
	private boolean nosubmit;

	public ModuleResponse(){
		compound = TagCW.create();
		list = TagLW.create();
	}

	public TagCW getCompound(){
		return compound;
	}

	public void setFormular(){
		form = true;
	}

	public void setNoSubmit(){
		nosubmit = true;
	}

	public void setNoBack(){
		compound.set("noback", true);
	}

	public TagCW build(){
		compound.set("elements", list);
		if(form) compound.set("form", true);
		if(nosubmit) compound.set("nosubmit", true);
		return compound;
	}

	public void setTitle(String title){
		compound.set("title_lang", title);
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

	/** Also sets the response into a Formular */
	public void addHiddenField(String id, Object value){
		addEntry(list, id, ELM_BLANK, ELM_BLANK, false, true, value, false, true);
		setFormular();
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

	public void addEntry(TagLW root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value){
		addEntry(root, index, elm, icon, button, field, value, false);
	}

	public void addEntry(TagLW root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value, boolean valonly){
		addEntry(root, index, elm, icon, button, field, value, valonly, false);
	}

	public void addEntry(TagLW root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value, boolean valonly, boolean hidefield){
		TagLW list = TagLW.create();
		list.add(index);
		list.add(elm.name());
		list.add(icon.name());
		list.add((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? hidefield ? "2" : "1" : "0"));
		if(value != null) list.add(valonly ? val(value.toString()) : value.toString());
		root.add(list);
	}

	public String val(String string){
		return LDUIModule.VALONLY + string;
	}

}
