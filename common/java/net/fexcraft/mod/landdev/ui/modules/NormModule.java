package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIElmType.*;
import static net.fexcraft.mod.landdev.ui.LDUIElmType.ELM_BLUE;
import static net.fexcraft.mod.landdev.ui.LDUIModule.LANG_NO;
import static net.fexcraft.mod.landdev.ui.LDUIModule.LANG_YES;

import java.util.Map.Entry;

import net.fexcraft.mod.landdev.data.Norms;
import net.fexcraft.mod.landdev.data.norm.Norm;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIButton;

/**
 * Standardised Norm List/Editor across all Layers
 * @author Ferdindand Calo'
 */
public class NormModule {

	public static void processNorm(Norms norms, BaseCon container, ModuleRequest req, int uiid){
		Norm norm = norms.get(container.pos.z);
		if(norm == null) return;
		if(norm.type.isInteger()){
			try{
				norm.set(Integer.parseInt(req.getField("norm_field")));
			}
			catch(Exception e){
				container.msg("Error: " + e.getMessage());
			}
		}
		else if(norm.type.isDecimal()){
			try{
				norm.set(Float.parseFloat(req.getField("norm_field")));
			}
			catch(Exception e){
				container.msg("Error: " + e.getMessage());
			}
		}
		else{
			norm.set(req.getField("norm_field"));
		}
		container.open(uiid);
	}

	public static void processBool(Norms norms, BaseCon container, ModuleRequest req, int uiid){
		Norm norm = norms.get(container.pos.z);
		if(norm == null) return;
		if(!norm.type.isBool()) return;
		norm.toggle();
		container.open(uiid);
	}

	public static boolean isNormReq(Norms norms, BaseCon container, ModuleRequest req, int uiid, int lid){
		if(!req.event().startsWith("norm.")) return false;
		Norm norm = norms.get(req.event().substring(5));
		if(norm == null) return false;
		container.open(uiid, lid, norms.index(norm));
		return true;
	}

	public static void respNormList(Norms norms, BaseCon container, ModuleResponse resp, String prefix, boolean canman){
		resp.setTitle(prefix + ".norms");
		LDUIButton icon = canman ? OPEN : EMPTY;
		for(Entry<String, Norm> entry : norms.norms.entrySet()){
			if(entry.getValue().type.isBool()){
				resp.addRow("norm." + entry.getKey(), ELM_GENERIC, icon, canman, entry.getValue().bool() ? LANG_YES : LANG_NO);
			}
			else{
				resp.addRow("norm." + entry.getKey(), ELM_GENERIC, icon, canman, entry.getValue().string());
			}
		}
	}

	public static void respNormEdit(Norms norms, BaseCon container, ModuleResponse resp, String prefix, boolean canman){
		resp.setTitle(prefix + ".norm_editor");
		Norm norm = norms.get(container.pos.z);
		if(norm == null) return;
		resp.addRow("norm_id", ELM_GREEN, norm.id);
		resp.addRow("norm_value", ELM_GENERIC, norm.string());
		if(!canman) return;
		if(norm.type.isBool()){
			resp.addButton("norm_bool", ELM_BLUE, norm.bool() ? ENABLED : DISABLED);
		}
		else{
			resp.addField("norm_field", norm.string());
			resp.addButton("norm_submit", ELM_BLUE, OPEN);
			resp.setFormular();
		}
	}

}
