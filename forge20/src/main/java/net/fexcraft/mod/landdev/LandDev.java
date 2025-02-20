package net.fexcraft.mod.landdev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fcl.util.ClientPacketPlayer;
import net.fexcraft.mod.fcl.util.UIPacketF;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.State;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.fexcraft.lib.common.utils.Formatter.format;
import static net.fexcraft.mod.fsmm.local.FsmmCmd.isOp;
import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_COUNTY;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;
import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;
import static net.fexcraft.mod.uni.ui.ContainerInterface.transformat;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(LandDev.MODID)
public class LandDev {

	public static final String MODID = "landdev";
	public static final String VERSION = "1.2.1";
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
		CHANNEL.registerMessage(1, UIPacketF.class, (packet, buffer) -> buffer.writeNbt(packet.com()), buffer -> new UIPacketF(buffer.readNbt()), (packet, context) -> {
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
					String c = list.size() > 3 ? list.getString(3) : "&a";
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
		CHANNEL.send(PacketDistributor.PLAYER.with(entity::local), new UIPacketF(com.local()));
	}

	public static void sendToAll(CompoundTag com){
		try{
			CHANNEL.send(PacketDistributor.ALL.noArg(), new UIPacketF(com));
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
					player.entity.send("&cno.permission");
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
				cf = chunk.district.state().norms.get("new-county-fee").integer();
				player.entity.send(TranslationUtil.translateCmd("fees_county"));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_server"), getWorthAsString(sf));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_state"), getWorthAsString(cf));
				player.entity.send(TranslationUtil.translateCmd("fees_ct_total"), getWorthAsString(sf + cf));
				return 0;
			}))
			.then(literal("help").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				player.entity.send("&0[&bLD&0]&6>>&2===========");
				player.entity.send("/ld (UI)");
				player.entity.send("/ld help");
				player.entity.send("/ld admin");
				player.entity.send("/ld fees");
				player.entity.send("/ld reload");
				player.entity.send("/ld force-tax");
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					player.entity.openUI(LDKeys.KEY_MAIN, new V3I(0, (int)player.entity.getPos().x >> 4, (int)player.entity.getPos().z >> 4));
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
				player.entity.openUI(LDKeys.KEY_CLAIM, new V3I(chunk.key.x, cmd.getArgument("district", Integer.class), chunk.key.z));
				return 0;
			})).executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.KEY_CLAIM, new V3I(chunk.key.x, chunk.district.id, chunk.key.z));
				return 0;
			}))
			.then(literal("map").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				String marker = null;
				Chunk_ ck = null;
				int r = 9, rm = 4;
				for(int i = 0; i < r; i++){
					String str = "&0|";
					for(int j = 0; j < r; j++){
						int x = (chunk.key.x - rm) + j;
						int z = (chunk.key.z - rm) + i;
						marker = x == chunk.key.x && z == chunk.key.z ? "+" : "#";
						ck = ResManager.getChunk(x, z);
						str += (ck == null ? "&4" : ck.district.id >= 0 ? "&9" : "&2") + marker;
					}
					player.entity.send(str + "&0|");
				}
				player.entity.send(TranslationUtil.translateCmd("chunk.mapdesc"));
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.KEY_CHUNK, new V3I(0, chunk.key.x, chunk.key.z));
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
				player.entity.openUI(LDKeys.KEY_DISTRICT, District.UI_CREATE, 0, 0);
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.KEY_DISTRICT, new V3I(0, chunk.district.id, 0));
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
					player.entity.openUI(LDKeys.KEY_MUNICIPALITY, Municipality.UI_CREATE, 0, 0);
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
				player.entity.openUI(LDKeys.KEY_MUNICIPALITY, new V3I(0, chunk.district.municipality().id, 0));
				return 0;
			})
		);
		dispatcher.register(literal("ct")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				State state = chunk.district.state();
				boolean cn = state.norms.get("new-counties").bool();
				boolean pp = player.hasPermit(CREATE_COUNTY, state.getLayer(), state.id);
				if(!cn && !pp){
					player.entity.send(translateCmd("ct.no_new_county"));
					player.entity.send(translateCmd("ct.no_create_permit"));
				}
				else{
					player.entity.openUI(LDKeys.KEY_COUNTY, County.UI_CREATE, 0, 0);
				}
				return 0;
			}))
			.executes(cmd -> {
				try{
					LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
					Chunk_ chunk = ResManager.getChunk(player.entity);
					player.entity.openUI(LDKeys.KEY_COUNTY, new V3I(0, chunk.district.county().id, 0));
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			})
		);
		dispatcher.register(literal("st")
			.then(literal("create").executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				//
				return 0;
			}))
			.executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.KEY_STATE, new V3I(0, chunk.district.state().id, 0));
				return 0;
			})
		);
	}

}
