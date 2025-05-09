package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.ui.LDUIButton.OPEN;
import static net.fexcraft.mod.landdev.ui.LDUIRow.ELM_BLUE;
import static net.fexcraft.mod.landdev.ui.LDUIRow.ELM_GENERIC;

import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.landdev.data.ColorData;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.ui.BaseCon;

/**
 * Standarized Appearance Editor across all Layers
 * @author Ferdindand Calo'
 */
public class AppearModule {

	public static void resp(BaseCon container, ModuleResponse resp, String prefix, IconHolder icon, ColorData color, boolean canman){
		resp.setTitle(prefix + ".appearance.title");
		resp.addRow("appearance.icon", ELM_GENERIC);
		resp.addField("appearance.icon_field", icon.getn());
		resp.addRow("appearance.color", ELM_GENERIC);
		resp.addField("appearance.color_field", color.getString());
		if(canman){
			resp.addButton("appearance.submit", ELM_BLUE, OPEN);
			resp.setFormular();
		}
	}

	public static boolean req(BaseCon container, ModuleRequest req, IconHolder icon, ColorData color){
		String tex = req.getField("appearance.icon_field");
		if(!UniFCL.URL_TEXTURES && (tex.startsWith("http") || tex.contains("://"))){
			container.msg("landdev.gui.no_url_tex", false);
			if(!container.ldp.adm) return false;
		}
		icon.set(tex);
		try{
			color.set(req.getField("appearance.color_field"));
		}
		catch(Exception e){
			container.player.entity.send("&cerror parsing color");
			e.printStackTrace();
		}
		return true;
	}

}
