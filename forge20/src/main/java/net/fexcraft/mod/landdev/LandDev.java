package net.fexcraft.mod.landdev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fcl.util.ClientPacketPlayer;
import net.fexcraft.mod.fcl.util.UIPacket;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.File;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.isOp;
import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_COUNTY;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;
import static net.fexcraft.mod.uni.ui.ContainerInterface.transformat;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(LandDev.MODID)
public class LandDev {

	public static final String MODID = "landdev";
	public static final String VERSION = "1.4.2";
	private static final Logger LOGGER = LogUtils.getLogger();
	public static File SAVE_DIR = new File("./landdev/");
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation("landdev", "channel"))
		.clientAcceptedVersions(pro -> true)
		.serverAcceptedVersions(pro -> true)
		.networkProtocolVersion(() -> VERSION)
		.simpleChannel();

	public LandDev(){
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		modbus.addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.register(this);
		LDN.preinit(FMLPaths.CONFIGDIR.get().toFile());
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		LDN.init(this);
		LDN.postinit();
		CHANNEL.registerMessage(1, UIPacket.class, (packet, buffer) -> buffer.writeNbt(packet.com()), buffer -> new UIPacket(buffer.readNbt()), (packet, context) -> {
			context.get().enqueueWork(() -> {
				if(context.get().getDirection().getOriginationSide().isClient()){
					ServerPlayer player = context.get().getSender();
					onServerPacket(player, packet.com());
				}
				else{
					ClientModEvents.onClientPacket(ClientPacketPlayer.get(), packet.com());
				}
			});
			context.get().setPacketHandled(true);
		});
	}

	private static void onServerPacket(ServerPlayer player, CompoundTag com){

	}

	public static void log(Object s){
		LOGGER.info(s + "");
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LDN.onServerStarting();
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event){
		LDN.onServerStarted();
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event){
		LDN.onServerStopping();
	}

	@SubscribeEvent
	public void onServerStopped(ServerStoppedEvent event){
		LDN.onServerStop();
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event){
			//
		}

		private static void onClientPacket(Player player, CompoundTag com){
			switch(com.getString("task")){
				case "location_update":{
					int time = com.contains("time") ? com.getInt("time") : 10;
					LocationUpdate.clear(Time.getDate() + (time * 1000));
					LocationUpdate.loadIcons((ListTag)com.get("icons"));
					LocationUpdate.loadLines((ListTag)com.get("lines"));
					return;
				}
				case "chat_message":{
					ListTag list = (ListTag)com.get("msg");
					String c = list.size() > 3 ? list.getString(3) : "\u00A7a";
					Component text = null;
					switch(list.getString(0)){
						case "chat_img":
							text = Component.literal(list.getString(2));
							//TODO
							break;
						case "chat":
						default:
							text = Component.literal(transformat(LDConfig.CHAT_OVERRIDE_LANG, c, list.getString(1), list.getString(2)));
							break;
					}
					Minecraft.getInstance().gui.getChat().addMessage(text);
					return;
				}
			}
		}

	}

	@SubscribeEvent
	public void onCmdReg(RegisterCommandsEvent event){
		//AliasLoader.load();
		regCmd(event.getDispatcher());
	}

	public static void sendLocationPacket(EntityW entity, TagCW com){
		CHANNEL.send(PacketDistributor.PLAYER.with(entity::local), new UIPacket(com.local()));
	}

	public static void sendToAll(TagCW com){
		try{
			CHANNEL.send(PacketDistributor.ALL.noArg(), new UIPacket(com.local()));
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	private void regCmd(CommandDispatcher<CommandSourceStack> dispatcher){
		dispatcher.register(literal("ld")
			.then(literal("admin").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				if(cmd.getSource().getServer().isSingleplayer() || isOp(cmd.getSource())){
					player.adm = !player.adm;
					player.entity.send(TranslationUtil.translateCmd("adminmode." + player.adm));
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
				player.entity.send(TranslationUtil.translateCmd("fees"));
				long sf = LDConfig.MUNICIPALITY_CREATION_FEE;
				long cf = chunk.district.county().norms.get("new-municipality-fee").integer();
				player.entity.send(TranslationUtil.translateCmd("fees_municipality"));
				player.entity.send(TranslationUtil.translateCmd("fees_mun_server"), getWorthAsString(sf));
				player.entity.send(TranslationUtil.translateCmd("fees_mun_county"), getWorthAsString(cf));
				player.entity.send(TranslationUtil.translateCmd("fees_mun_total"), getWorthAsString(sf + cf));
				sf = LDConfig.COUNTY_CREATION_FEE;
				cf = chunk.district.region().norms.get("new-county-fee").integer();
				player.entity.send(TranslationUtil.translateCmd("fees_county"));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_server"), getWorthAsString(sf));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_region"), getWorthAsString(cf));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_total"), getWorthAsString(sf + cf));
				sf = LDConfig.REGION_CREATION_FEE;
				player.entity.send(TranslationUtil.translateCmd("fees_region"));
				player.entity.send(TranslationUtil.translateCmd("fees_rg_server"), getWorthAsString(sf));
				player.entity.send(TranslationUtil.translateCmd("fees_rg_total"), getWorthAsString(sf));
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
					if(!player.adm || !LDConfig.SAVE_CHUNKS_IN_REGIONS) return 0;
					int did = IntegerArgumentType.getInteger(cmd, "district");
					PolyClaim.setDis(player.uuid, did);
					District dis = ResManager.getDistrict(did);
					player.entity.send("landdev.cmd.polyclaim.district", dis.name(), dis.id);
					return 0;
				})))
				.then(literal("select").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm || !LDConfig.SAVE_CHUNKS_IN_REGIONS) return 0;
					int am = PolyClaim.selCnk(player.uuid, ResManager.getChunk(player.entity.getPos()));
					player.entity.send("landdev.cmd.polyclaim.selected", am);
					return 0;
				}))
				.then(literal("start").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm || !LDConfig.SAVE_CHUNKS_IN_REGIONS) return 0;
					player.entity.send("landdev.cmd.polyclaim.starting");
					int[] res = PolyClaim.process(player.uuid);
					player.entity.send("landdev.cmd.polyclaim.finished", res[0], res[1]);
					PolyClaim.clear(player.uuid);
					return 0;
				}))
				.then(literal("status").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm || !LDConfig.SAVE_CHUNKS_IN_REGIONS) return 0;
					player.entity.send("[LD] === === ===");
					player.entity.send("landdev.cmd.polyclaim.status.title");
					PolyClaim.PolyClaimObj obj = PolyClaim.get(player.uuid);
					District dis = ResManager.getDistrict(obj.district);
					if(dis.id < 0){
						player.entity.send("landdev.cmd.polyclaim.status.district", "AUTO", -1);
					}
					else{
						player.entity.send("landdev.cmd.polyclaim.status.district", dis.name(), dis.id);
					}
					player.entity.send("landdev.cmd.polyclaim.status.chunks");
					for(ChunkKey key : obj.chunks){
						player.entity.send("- " + key.comma());
					}
					player.entity.send("landdev.cmd.polyclaim.status.mode", obj.chunks.size() < 2 ? "PASS" : obj.chunks.size() == 2 ? "QUAD" : "POLYGON");
					return 0;
				}))
				.then(literal("clear").executes(cmd -> {
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					if(!player.adm || !LDConfig.SAVE_CHUNKS_IN_REGIONS) return 0;
					PolyClaim.clear(player.uuid);
					player.entity.send("landdev.cmd.polyclaim.cleared");
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
				player.entity.send(TranslationUtil.translateCmd("chunk.mapdesc"));
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
					player.entity.send(translateCmd("mun.no_new_municipalities"));
					player.entity.send(translateCmd("mun.no_create_permit"));
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
					player.entity.send(translateCmd("mun.not_in_a_municipality"));
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
					player.entity.send(translateCmd("mun.center_too_close", ResManager.getMunicipality(dis.getLeft(), true).name(), dis.getLeft()));
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
					player.entity.send(translateCmd("mun.not_in_a_municipality"));
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
					player.entity.send(translateCmd("ct.no_new_county"));
					player.entity.send(translateCmd("ct.no_create_permit"));
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
					player.entity.send(translateCmd("rg.no_new_region"));
					player.entity.send(translateCmd("rg.no_create_permit"));
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

}
