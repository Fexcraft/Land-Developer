package net.fexcraft.mod.landdev.util;

import java.util.List;
import java.util.Set;

import net.fexcraft.mod.landdev.LandDev;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft mc_instance){
		//
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories(){
		return null;
	}
	
	public static class ConfigGui extends GuiConfig {

		public ConfigGui(GuiScreen parent){
			super(parent, getList(), LandDev.MODID, false, false, "Mod Settings");
			titleLine2 = Settings.getConfig().getConfigFile().getAbsolutePath();
		}
		
		public static List<IConfigElement> getList(){
			return Settings.getList();
		}
		
	}

	@Override
	public boolean hasConfigGui(){
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen){
		return new ConfigGui(parentScreen);
	}
	
}