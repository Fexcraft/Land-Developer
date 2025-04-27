package net.fexcraft.mod.landdev;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fcl.util.TagPacket;
import net.fexcraft.mod.fcl.util.UIPacket;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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


		FCL.addListener("landdev", false, (com, player) -> {

		});
	}

	public static void sendLocationPacket(EntityW entity, TagCW com){
		ServerPlayNetworking.getSender((ServerPlayer)entity.direct()).sendPacket(new TagPacket("landdev", com));
	}

	public static void sendToAll(TagCW com){
		for(ServerPlayer player : FCL.SERVER.get().getPlayerList().getPlayers()){
			ServerPlayNetworking.getSender(player).sendPacket(new TagPacket("landdev", com));
		}
	}

}