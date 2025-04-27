package net.fexcraft.mod.landdev;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LandDev implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("landdev");

	@Override
	public void onInitialize(){
		LDN.preinit(FabricLoader.getInstance().getConfigDir().toFile());
		LDN.init(this);
		LDN.postinit();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> LDN.onServerStarting());
		ServerLifecycleEvents.SERVER_STARTED.register(server -> LDN.onServerStarted());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> LDN.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LDN.onServerStop());
	}

	public static void sendLocationPacket(EntityW entity, TagCW com){

	}

}