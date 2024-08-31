package net.fexcraft.mod.landdev.util;

public class TranslationUtil {

	public static String translate(String str, Object... args){
		return "landdev." + str;
	}

	public static String translateCmd(String str, Object... args){
		return "landdev.cmd." + str;
	}

}
