package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLUE;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.ColorData;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;

/**
 * Standarized Appearance Editor across all Layers
 * @author Ferdindand Calo'
 */
public class AppearModule {

	public static void resp(LDGuiContainer container, ModuleResponse resp, String prefix, IconHolder icon, ColorData color, boolean canman){
		resp.setTitle(prefix + ".appearance.title");
		resp.addRow("appearance.icon", ELM_GENERIC);
		resp.addField("appearance.icon_field", icon.getn());
		resp.addRow("appearance.color", ELM_GENERIC);
		resp.addField("appearance.color_field", color.getString());
		if(canman){
			resp.addButton("appearance.submit", ELM_BLUE, ICON_OPEN);
			resp.setFormular();
		}
	}

	public static void req(LDGuiContainer container, ModuleRequest req, IconHolder icon, ColorData color){
		icon.set(req.getField("appearance.icon_field"));
		try{
			color.set(req.getField("appearance.color_field"));
		}
		catch(Exception e){
			Print.chat(container.player.entity, "&cerror parsing color");
			e.printStackTrace();
		}
	}

}
