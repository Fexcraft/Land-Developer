package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.ui.LDUIButton.BLANK;
import static net.fexcraft.mod.landdev.ui.LDUIButton.checkbox;
import static net.fexcraft.mod.landdev.ui.LDUIButton.radio;
import static net.fexcraft.mod.landdev.ui.LDUIRow.ELM_BLANK;

import net.fexcraft.mod.landdev.ui.LDUIButton;
import net.fexcraft.mod.landdev.ui.LDUIRow;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;

import java.util.ArrayList;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ModuleResponse {

	private ArrayList<TagLW> elmlist = new ArrayList<>();
	private TagCW compound;
	private int in_index = 0;
	private boolean form;
	private boolean nosubmit;

	public ModuleResponse(){
		compound = TagCW.create();
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
		TagLW list = TagLW.create();
		for(TagLW elm : elmlist) list.add(elm);
		compound.set("elements", list);
		if(form) compound.set("form", true);
		if(nosubmit) compound.set("nosubmit", true);
		return compound;
	}

	public void setTitle(String title){
		compound.set("title_lang", title);
	}

	public void addRow(String id, LDUIRow style){
		addEntry(id, style, BLANK, false, false, null);
	}

	public void addRow(String id, LDUIRow style, Object value){
		addEntry(id, style, BLANK, false, false, value);
	}

	public void addRow(String id, LDUIRow style, LDUIButton icon){
		addEntry(id, style, icon, false, false, null);
	}

	public void addRow(String id, LDUIRow style, LDUIButton icon, Object value){
		addEntry(id, style, icon, false, false, value);
	}

	public void addRow(String id, LDUIRow style, LDUIButton icon, boolean button, Object value){
		addEntry(id, style, icon, button, false, value);
	}

	public void addButton(String id, LDUIRow style, LDUIButton icon){
		addEntry(id, style, icon, true, false, null);
	}

	public void addButton(String id, LDUIRow style, LDUIButton icon, Object value){
		addEntry(id, style, icon, true, false, value);
	}

	public void addField(String id){
		addEntry(id, ELM_BLANK, BLANK, false, true, null);
	}

	public void addField(String id, Object value){
		addEntry(id, ELM_BLANK, BLANK, false, true, value);
	}

	/** Also sets the response into a Formular */
	public void addHiddenField(String id, Object value){
		addEntry(id, ELM_BLANK, BLANK, false, true, value, false, true);
		setFormular();
	}

	public void addRadio(String id, LDUIRow style, boolean checked){
		addEntry(id, style, radio(checked), true, false, null);
	}

	public void addRadio(String id, LDUIRow style, boolean checked, Object value){
		addEntry(id, style, radio(checked), true, false, value);
	}

	public void addCheck(String id, LDUIRow style, boolean checked){
		addEntry(id, style, checkbox(checked), true, false, null);
	}

	public void addCheck(String id, LDUIRow style, boolean checked, Object value){
		addEntry(id, style, checkbox(checked), true, false, value);
	}

	public void addBlank(){
		addEntry("spacer", ELM_BLANK, BLANK, false, false, null);
	}

	//

	public void addEntry(String index, LDUIRow elm, LDUIButton icon, boolean button, boolean field, Object value){
		addEntry(index, elm, icon, button, field, value, false);
	}

	public void addEntry(String index, LDUIRow elm, LDUIButton icon, boolean button, boolean field, Object value, boolean valonly){
		addEntry(index, elm, icon, button, field, value, valonly, false);
	}

	public void addEntry(String index, LDUIRow elm, LDUIButton icon, boolean button, boolean field, Object value, boolean valonly, boolean hidefield){
		TagLW list = TagLW.create();
		list.add(index);
		list.add(elm.name());
		list.add(icon.name());
		list.add((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? hidefield ? "2" : "1" : "0"));
		if(value != null) list.add(valonly ? val(value.toString()) : value.toString());
		elmlist.add(in_index++, list);
	}

	public String val(String string){
		return LDUIModule.VALONLY + string;
	}

	public void remEntry(String index){
		int remidx = -1;
		for(int idx = 0; idx < elmlist.size(); idx++){
			TagLW list = elmlist.get(idx);
			if(list.getString(0).equals(index)){
				remidx = idx;
				break;
			}
		}
		if(remidx > -1) elmlist.remove(remidx);
	}

	public void setInsertAfter(String index){
		in_index = indexOfEntry(index) + 1;
	}

	public void setInsertBefore(String index){
		in_index = indexOfEntry(index) - 1;
		if(in_index < 0) in_index = 0;
	}

	public void setInsertIndex(int index){
		in_index = index;
	}

	private int indexOfEntry(String index){
		int idx = -1;
		for(int i = 0; i < elmlist.size(); i++){
			TagLW entry = elmlist.get(i);
			if(entry.getString(0).equals(index)){
				idx = i;
				break;
			}
		}
		return idx;
	}

}
