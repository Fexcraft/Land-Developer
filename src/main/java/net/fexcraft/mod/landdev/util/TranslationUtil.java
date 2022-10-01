package net.fexcraft.mod.landdev.util;

import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class TranslationUtil {

	public static String translate(String key){
		return I18n.translateToLocal("landdev." + key);
	}

	public static String translate(String key, Object... args){
		return I18n.translateToLocalFormatted("landdev." + key, args);
	}

	public static String translateCmd(String key){
		return I18n.translateToLocal("landdev.cmd." + key);
	}

	public static String translateCmd(String key, Object... args){
		return I18n.translateToLocalFormatted("landdev.cmd." + key, args);
	}

}
