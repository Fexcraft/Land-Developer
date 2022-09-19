package net.fexcraft.mod.landdev.util;

import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class TranslationUtil {

	public static String translate(String key){
		return I18n.translateToLocal("landdev" + key);
	}

}
