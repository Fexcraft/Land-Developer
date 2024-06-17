package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.event.ATMEvent;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.math.NumberUtils;

@Mod.EventBusSubscriber
public class GeneralEvents {

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.getModID().equals("landdev")) Settings.refresh(false);
	}
	
}
