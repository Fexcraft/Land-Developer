package net.fexcraft.mod.landdev.util;

import net.minecraftforge.fml.common.ICrashCallable;

public class CrashHook implements ICrashCallable {

	@Override
	public String call() throws Exception {
		return "//TODO States Mod crash log message";
	}

	@Override
	public String getLabel(){
		return "States Crash Hook";
	}

}
