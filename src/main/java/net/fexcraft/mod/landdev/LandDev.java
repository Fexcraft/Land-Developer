package net.fexcraft.mod.landdev;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.PacketHandler.PacketHandlerType;
import net.fexcraft.mod.landdev.cmd.*;
import net.fexcraft.mod.landdev.data.chunk.ChunkRegion;
import net.fexcraft.mod.landdev.data.chunk.cap.ChunkCap;
import net.fexcraft.mod.landdev.data.chunk.cap.ChunkCapCallable;
import net.fexcraft.mod.landdev.data.chunk.cap.ChunkCapStorage;
import net.fexcraft.mod.landdev.db.Database;
import net.fexcraft.mod.landdev.db.JsonFileDB;
import net.fexcraft.mod.landdev.events.FsmmEventHooks;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

@Mod(modid = LandDev.MODID, name = LandDev.NAME, version = LandDev.VERSION,
	dependencies = "required-after:fcl", guiFactory = "net.fexcraft.mod.landdev.util.GuiFactory",
	acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class LandDev {
	
    public static final String MODID = "landdev";
    public static final String NAME = "LandDev";
    public static final String VERSION = "1.1.16";
	@Mod.Instance(MODID)
	public static LandDev INSTANCE;
	public static Database DB = new JsonFileDB();
	public static File SAVE_DIR;
	public static Timer TAX_TIMER;
	public static Timer GENERIC_TIMER;

	public static final String CLIENT_RECEIVER_ID = "landdev:util";
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	Settings.initialize(event);
        logger = event.getModLog();
		CapabilityManager.INSTANCE.register(ChunkCap.class, new ChunkCapStorage(), new ChunkCapCallable());
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.NBT, Side.CLIENT, new PacketReceiver());
        	MinecraftForge.EVENT_BUS.register(new LocationUpdate());
        }
		FsmmEventHooks.init();
    }

    @EventHandler
    public void init(FMLPostInitializationEvent event){
        Protector.load();
		DiscordTransmitter.restart();
    }
    
    @Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		AliasLoader.load();
		event.registerServerCommand(new DebugCmd());
		event.registerServerCommand(new LDCmd());
		event.registerServerCommand(new CkCmd());
		event.registerServerCommand(new DisCmd());
		event.registerServerCommand(new MunCmd());
		event.registerServerCommand(new CntCmd());
		DiscordTransmitter.restart();
	}

    @Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli();
		setupTaxTimer(mid);
		setupGenericTimer(mid);
	}

	private void setupTaxTimer(long mid){
		long date = Time.getDate();
		while((mid += Settings.TAX_INTERVAL) < date);
		if(TAX_TIMER == null && Settings.TAX_ENABLED){
			(TAX_TIMER = new Timer()).schedule(new TaxSystem().load(), new Date(mid), Settings.TAX_INTERVAL);
		}
	}

	private void setupGenericTimer(long mid){
		if(GENERIC_TIMER != null) return;
		long date = Time.getDate();
		long offset = Time.MIN_MS * 6;
		while((mid += offset) < date);
		GENERIC_TIMER = new Timer();
		GENERIC_TIMER.schedule(new TimerTask(){
			@Override
			public void run(){
				ChunkRegion.saveRegions();
			}
		}, new Date(mid), offset);
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, TranslationUtil.translate("server.stopping"));
		if(TAX_TIMER != null) TAX_TIMER.cancel();
		if(GENERIC_TIMER != null) GENERIC_TIMER.cancel();
	}
    
    @Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event){
		DiscordTransmitter.exit();
	}
	
	public static final File updateSaveDirectory(World world){
		return SAVE_DIR = new File(world.getSaveHandler().getWorldDirectory(), "landdev/");
	}
	
	public static final File getSaveDirectory(){
		return SAVE_DIR;
	}
	
	public static void log(Object obj){
		logger.log(Level.INFO, obj);
	}
	
}
