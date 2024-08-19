package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.landdev.util.Settings;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class GeneralEvents {

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.getModID().equals("landdev")) Settings.refresh(false);
	}
	
}
