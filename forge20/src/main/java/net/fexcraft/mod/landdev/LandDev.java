package net.fexcraft.mod.landdev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fcl.util.ClientPacketPlayer;
import net.fexcraft.mod.fcl.util.UIPacketF;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
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
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
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
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.isOp;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(LandDev.MODID)
public class LandDev {

	public static final String MODID = "landdev";
	public static final String VERSION = "1.2.0";
	private static final Logger LOGGER = LogUtils.getLogger();
	public static File SAVE_DIR;
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

	public static TagCW read(File file) throws IOException {
		return TagCW.wrap(NbtIo.read(file));
	}

	public static void write(TagCW compound, File file) throws IOException {
		NbtIo.write(compound.local(), file);
	}

	public static final File updateSaveDirectory(Level world){
		return SAVE_DIR = new File(world.getServer().getServerDirectory(), "landdev/");
	}

	public static void log(String s){
		LOGGER.info(s);
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LDN.onServerStarting();
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartedEvent event){
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
				case "location":{
					int time = com.contains("time") ? com.getInt("time") : 10;
					LocationUpdate.clear(Time.getDate() + (time * 1000));
					LocationUpdate.loadIcons((ListTag)com.get("icons"));
					LocationUpdate.loadLines((ListTag)com.get("lines"));
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
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> entity.local()), new UIPacketF(com.local()));
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
			.executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				player.entity.openUI(LDKeys.KEY_MAIN, new V3I(0, (int)player.entity.getPos().x >> 4, (int)player.entity.getPos().z >> 4));
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
				//
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
				//
				return 0;
			}))
			.executes(cmd -> {
				LDPlayer player = ResManager.getPlayer(cmd.getSource().getPlayer());
				Chunk_ chunk = ResManager.getChunk(player.entity);
				player.entity.openUI(LDKeys.KEY_COUNTY, new V3I(0, chunk.district.county().id, 0));
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
