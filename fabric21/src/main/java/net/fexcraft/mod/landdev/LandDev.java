package net.fexcraft.mod.landdev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.fcl.util.PacketTag21;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.isOp;
import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_COUNTY;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.util.InteractHandler.control;
import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LandDev implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("landdev");
	public static final Object VERSION = "1.5.0";
	public static File SAVE_DIR;

	@Override
	public void onInitialize(){
		LDN.preinit(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toFile());
		LDN.init(this);
		LDN.postinit();

		UniFCL.regTagPacketListener("landdev", false, (com, player) -> {});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> LDN.onServerStarting());
		ServerLifecycleEvents.SERVER_STARTED.register(server -> LDN.onServerStarted());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> LDN.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LDN.onServerStop());

		/*ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
			if(level != FCL.SERVER.get().overworld()) return;
			loadResManager();
			UniChunk.get(chunk);
		});*/
		ServerChunkEvents.CHUNK_UNLOAD.register((level, lvlck) -> {
			if(level != FCL.SERVER.get().overworld()) return;
			Chunk_ chunk = ResManager.getChunk(lvlck.getPos().x, lvlck.getPos().z);
			if(chunk != null) ResManager.remChunk(lvlck.getPos().x, lvlck.getPos().z);
		});
		ServerWorldEvents.LOAD.register((server, world) -> {
			loadResManager();
		});
		ServerWorldEvents.UNLOAD.register(((server, world) -> {
			if(world != server.overworld()) return;
			LandDev.log("Unloading LandDev World Data...");
			ResManager.saveAll();
			ResManager.clear();
			LandDev.log("Unloaded LandDev World Data.");
		}));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			LDPlayer player = ResManager.getPlayer(handler.player.getGameProfile().getId(), true);
			player.entity = UniEntity.getEntity(handler.player);
			player.offline = false;
			player.login = Time.getDate();
			player.chunk_last = ResManager.getChunkP(handler.player);
			TaxSystem.taxPlayer(player, null, false);
			Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, LDConfig.SERVLANG_JOINED.formatted(player.name_raw()));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			LDPlayer player = ResManager.getPlayer(handler.player.getGameProfile().getId(), false);
			if(player != null){
				Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, LDConfig.SERVLANG_LEFT.formatted(player.name_raw()));
				player.save();
				player.last_login = player.login;
				player.last_logout = Time.getDate();
				player.login = 0;
				player.offline = true;
				player.entity = null;
			}
		});
		ServerPlayerEvents.COPY_FROM.register((old, nw, al) -> {
			LDPlayer player = ResManager.getPlayer(nw.getGameProfile().getId(), false);
			if(player != null) player.entity = UniEntity.getEntity(nw);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((old, nw, al) -> {
			LDPlayer player = ResManager.getPlayer(nw.getGameProfile().getId(), false);
			if(player != null) player.entity = UniEntity.getEntity(nw);
		});
		//TODO chat
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time;
			boolean moved, label;
			for(ServerPlayer servplay : server.getPlayerList().getPlayers()){
				if(servplay.level() != server.overworld()) return;
				LDPlayer player = ResManager.getPlayer(servplay);
				if(player == null) return;
				if((time = Time.getDate()) > player.last_pos_update){
					player.last_pos_update = time;
					player.chunk_last = player.chunk_current;
					player.chunk_current = ResManager.getChunkP(servplay);
					if(player.chunk_current == null) return;
					if(player.chunk_last == null) player.chunk_last = player.chunk_current;
					moved = player.chunk_current.district.id != player.chunk_last.district.id;
					label = player.chunk_current.label.present && player.chunk_current != player.chunk_last;
					if(moved || label) player.sendLocationUpdate(moved, label, 0);
				}
			}
		});
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, ent) -> {
			if(level != FCL.SERVER.get().overworld()) return false;
			if(!control(pos.getX(), pos.getY(), pos.getZ(), player, false)){
				UniEntity.getEntity(player).bar("interact.break.noperm");
				return false;
			}
			return true;
		});
		/*UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if(level != FCL.SERVER.get().overworld()) return InteractionResult.PASS;
			if(hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
			BlockState state = level.getBlockState(hit.getBlockPos());
			boolean check = state.getBlock() instanceof SignBlock == false && Protector.INSTANCE.isProtected(state);
			if(check && !control(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ(), player, true)){
				UniEntity.getEntity(player).bar("interact.interact.noperm");
				return InteractionResult.FAIL;
			}
			return InteractionResult.SUCCESS;
		});*/
		CommandRegistrationCallback.EVENT.register((dis, ctx, sel) -> regCmd(dis));
	}

	private void loadResManager(){
		if(ResManager.INSTANCE.LOADED) return;
		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
		SAVE_DIR = new File(FCL.SERVER.get().getServerDirectory().toAbsolutePath().toFile(), "landdev/");
		ResManager.INSTANCE.load();
	}

	private void regCmd(CommandDispatcher<CommandSourceStack> dispatcher){
		dispatcher.register(literal("ld")
			.then(literal("admin").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				if(cmd.getSource().getServer().isSingleplayer() || isOp(cmd.getSource())){
					player.adm = !player.adm;
					player.entity.send("landdev.cmd.adminmode." + player.adm);
				}
				else{
					player.entity.send("\u00A7cno.permission");
				}
				return 0;
			}))
			.then(literal("uuid").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal(cmd.getSource().getPlayerOrException().getGameProfile().getId().toString()));
				return 0;
			}))
			.then(literal("reload").executes(cmd -> {
				Protector.load();
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				cmd.getSource().sendSystemMessage(Component.translatable("landdev.cmd.reload", "landdev-interaction.json"));
				DiscordTransmitter.restart();
				cmd.getSource().sendSystemMessage(Component.translatable("landdev.cmd.reload", "discord-bot-integration"));
				cmd.getSource().sendSystemMessage(Component.translatable("landdev.cmd.reload.complete"));
				return 0;
			}))
			.then(literal("force-tax").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				if(!player.adm) return -1;
				TaxSystem.INSTANCE.collect(Time.getDate(), true);
				return 0;
			}))
			.then(literal("fees").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.send("landdev.cmd.fees");
				long sf = LDConfig.MUNICIPALITY_CREATION_FEE;
				long cf = chunk.district.county().norms.get("new-municipality-fee").integer();
				player.entity.send("landdev.cmd.fees_municipality");
				player.entity.send("landdev.cmd.fees_mun_server", getWorthAsString(sf));
				player.entity.send("landdev.cmd.fees_mun_county", getWorthAsString(cf));
				player.entity.send("landdev.cmd.fees_mun_total", getWorthAsString(sf + cf));
				sf = LDConfig.COUNTY_CREATION_FEE;
				cf = chunk.district.region().norms.get("new-county-fee").integer();
				player.entity.send("landdev.cmd.fees_county");
				player.entity.send("landdev.cmd.fees_ct_server", getWorthAsString(sf));
				player.entity.send("landdev.cmd.fees_ct_region", getWorthAsString(cf));
				player.entity.send("landdev.cmd.fees_ct_total", getWorthAsString(sf + cf));
				sf = LDConfig.REGION_CREATION_FEE;
				player.entity.send("landdev.cmd.fees_region");
				player.entity.send("landdev.cmd.fees_rg_server", getWorthAsString(sf));
				player.entity.send("landdev.cmd.fees_rg_total", getWorthAsString(sf));
				return 0;
			}))
			.then(literal("help").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				player.entity.send("\u00A70[\u00A7bLD\u00A70]\u00A76>>\u00A72===========");
				player.entity.send("/ld (UI)");
				player.entity.send("/ld help");
				player.entity.send("/ld admin");
				player.entity.send("/ld fees");
				player.entity.send("/ld reload");
				player.entity.send("/ld force-tax");
				player.entity.send("PolyClaim (Admin)");
				player.entity.send("/ld polyclaim district <dis-id>");
				player.entity.send("/ld polyclaim select");
				player.entity.send("/ld polyclaim status");
				player.entity.send("/ld polyclaim clear");
				player.entity.send("/ld polyclaim start");
				return 0;
			}))
			.then(literal("polyclaim")
				.then(literal("district").then(argument("district", IntegerArgumentType.integer(0)).executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm) return 0;
					int did = IntegerArgumentType.getInteger(cmd, "district");
					PolyClaim.setDis(player, did);
					return 0;
				})))
				.then(literal("select").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm) return 0;
					PolyClaim.selCnk(player, ResManager.getChunk(player.entity.getPos()));
					return 0;
				}))
				.then(literal("start").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm) return 0;
					PolyClaim.process(player);
					return 0;
				}))
				.then(literal("status").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm) return 0;
					PolyClaim.status(player);
					return 0;
				}))
				.then(literal("clear").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm) return 0;
					PolyClaim.clear(player);
					return 0;
				}))
			)
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					player.entity.openUI(LDKeys.MAIN, new V3I(0, (int)player.entity.getPos().x >> 4, (int)player.entity.getPos().z >> 4));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			})
		);
		dispatcher.register(literal("ck")
			.then(literal("claim").then(argument("district", IntegerArgumentType.integer(-2)).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.CHUNK_CLAIM, new V3I(chunk.key.x, cmd.getArgument("district", Integer.class), chunk.key.z));
				return 0;
			})).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.CHUNK_CLAIM, new V3I(chunk.key.x, chunk.district.id, chunk.key.z));
				return 0;
			}))
			.then(literal("transfer").then(argument("district", IntegerArgumentType.integer(-2)).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.CHUNK_TRANSFER, new V3I(chunk.key.x, cmd.getArgument("district", Integer.class), chunk.key.z));
				return 0;
			})).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.CHUNK_CLAIM, new V3I(chunk.key.x, chunk.district.id, chunk.key.z));
				return 0;
			}))
			.then(literal("sell").then(argument("price", IntegerArgumentType.integer(-2)).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.CHUNK_CLAIM, new V3I(chunk.key.x, cmd.getArgument("price", Integer.class), chunk.key.z));
				return 0;
			})))
			.then(literal("buy").then(argument("for", StringArgumentType.greedyString()).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				String arg = cmd.getArgument("for", String.class);
				int dis = chunk.district.id;
				switch(arg){
					case "self":
					case "player":
						dis = -1;
						break;
					case "company":
					case "com":
						dis = -2;
						break;
					case "region":
					case "reg":
						dis = -3;
						break;
					case "here":
						dis = chunk.district.id;
						break;
				}
				if(arg.startsWith("municipality:")){
					dis = Integer.parseInt(arg.replace("municipality:", ""));
				}
				if(arg.startsWith("county:")){
					dis = Integer.parseInt(arg.replace("county:", ""));
				}
				player.entity.openUI(LDKeys.CHUNK_TRANSFER, new V3I(chunk.key.x, dis, chunk.key.z));
				return 0;
			})))
			.then(literal("map").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				String marker = null;
				Chunk_ ck = null;
				int r = 9, rm = 4;
				for(int i = 0; i < r; i++){
					String str = "\u00A70|";
					for(int j = 0; j < r; j++){
						int x = (chunk.key.x - rm) + j;
						int z = (chunk.key.z - rm) + i;
						marker = x == chunk.key.x && z == chunk.key.z ? "+" : "#";
						ck = ResManager.getChunk(x, z);
						str += (ck == null ? "\u00A74" : ck.district.id >= 0 ? "\u00A79" : "\u00A72") + marker;
					}
					player.entity.send(str + "\u00A70|");
				}
				player.entity.send("landdev.cmd.chunk.mapdesc");
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.CHUNK, new V3I(0, chunk.key.x, chunk.key.z));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			})
		);
		dispatcher.register(literal("dis")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				player.entity.openUI(LDKeys.DISTRICT, District.UI_CREATE, 0, 0);
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.DISTRICT, new V3I(0, chunk.district.id, 0));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			})
		);
		dispatcher.register(literal("mun")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				County county = chunk.district.county();
				boolean cn = county.norms.get("new-municipalities").bool();
				boolean pp = player.hasPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id);
				if(!cn && !pp){
					player.entity.send("landdev.cmd.mun.no_new_municipalities");
					player.entity.send("landdev.cmd.mun.no_create_permit");
				}
				else{
					player.entity.openUI(LDKeys.MUNICIPALITY, Municipality.UI_CREATE, 0, 0);
				}
				return 0;
			}))
			.then(literal("center").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				if(chunk.district.municipality() == null){
					player.entity.send("landdev.cmd.mun.not_in_a_municipality");
					return 0;
				}
				Municipality mun = chunk.district.municipality();
				if(!mun.manage.can(PermAction.MANAGE_MUNICIPALITY, player.uuid) && !player.adm){
					player.entity.send("no perm");
					return 0;
				}
				int min = Math.max(LDConfig.MIN_MUN_DIS, mun.county.norms.get("min-municipality-distance").integer());
				if(min < LDConfig.MIN_MUN_DIS) min = LDConfig.MIN_MUN_DIS;
				Pair<Integer, Double> dis = ResManager.disToNearestMun(chunk.key, mun.id);
				if(dis.getLeft() >= 0 && dis.getRight() < min){
					player.entity.send("landdev.cmd.mun.center_too_close", ResManager.getMunicipality(dis.getLeft(), true).name(), dis.getLeft());
				}
				else{
					ResManager.MUN_CENTERS.put(dis.getLeft(), chunk.key);
					player.entity.openUI(LDKeys.MUNICIPALITY, 0, mun.id, 0);
				}
				return 0;
			}))
			.executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				if(chunk.district.municipality() == null){
					player.entity.send("landdev.cmd.mun.not_in_a_municipality");
					return 0;
				}
				player.entity.openUI(LDKeys.MUNICIPALITY, new V3I(0, chunk.district.municipality().id, 0));
				return 0;
			})
		);
		dispatcher.register(literal("ct")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				Region region = chunk.district.region();
				boolean cn = region.norms.get("new-counties").bool();
				boolean pp = player.hasPermit(CREATE_COUNTY, region.getLayer(), region.id);
				if(!cn && !pp){
					player.entity.send("landdev.cmd.ct.no_new_county");
					player.entity.send("landdev.cmd.ct.no_create_permit");
				}
				else{
					player.entity.openUI(LDKeys.COUNTY, County.UI_CREATE, 0, 0);
				}
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.COUNTY, new V3I(0, chunk.district.county().id, 0));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			})
		);
		dispatcher.register(literal("reg")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				if(!LDConfig.NEW_REGIONS && !player.adm){
					player.entity.send("landdev.cmd.rg.no_new_region");
					player.entity.send("landdev.cmd.rg.no_create_permit");
				}
				else{
					player.entity.openUI(LDKeys.REGION, County.UI_CREATE, 0, 0);
				}
				return 0;
			}))
			.executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.REGION, new V3I(0, chunk.district.region().id, 0));
				return 0;
			})
		);
	}

	public static void log(String str){
		LOGGER.info(str);
	}

	public static void sendLocationPacket(EntityW entity, TagCW com){
		ServerPlayNetworking.getSender((ServerPlayer)entity.direct()).sendPacket(new PacketTag21("landdev", com));
	}

	public static void sendToAll(TagCW com){
		for(ServerPlayer player : FCL.SERVER.get().getPlayerList().getPlayers()){
			ServerPlayNetworking.getSender(player).sendPacket(new PacketTag21("landdev", com));
		}
	}

}