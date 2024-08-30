package net.fexcraft.mod.landdev;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.uni.tag.TagCW;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
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
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.fexcraft.mod.fsmm.local.FsmmCmd.isOp;
import static net.minecraft.commands.Commands.literal;

@Mod(LandDev.MODID)
public class LandDev {

	public static final String MODID = "landdev";
	public static final String VERSION = "1.2.0";
	private static final Logger LOGGER = LogUtils.getLogger();
	public static File SAVE_DIR;

	public LandDev(){
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		modbus.addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.register(this);
		LDN.preinit(FMLPaths.CONFIGDIR.get().toFile());
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		LDN.init(this);
		LDN.postinit();
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

	}

	@SubscribeEvent
	public void onCmdReg(RegisterCommandsEvent event){
		AliasLoader.load();
		regCmd(event.getDispatcher());
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
	}

}
