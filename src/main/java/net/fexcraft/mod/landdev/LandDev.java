package net.fexcraft.mod.landdev;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.PacketHandler.PacketHandlerType;
import net.fexcraft.mod.landdev.cmd.CkCmd;
import net.fexcraft.mod.landdev.cmd.DebugCmd;
import net.fexcraft.mod.landdev.cmd.DisCmd;
import net.fexcraft.mod.landdev.cmd.LDCmd;
import net.fexcraft.mod.landdev.cmd.MunCmd;
import net.fexcraft.mod.landdev.data.chunk.ChunkCap;
import net.fexcraft.mod.landdev.db.Database;
import net.fexcraft.mod.landdev.db.JsonFileDB;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ChunkCapabilityUtil;
import net.fexcraft.mod.landdev.util.PacketReceiver;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = LandDev.MODID, name = LandDev.NAME, version = LandDev.VERSION,
	dependencies = "required-after:fcl", guiFactory = "net.fexcraft.mod.landdev.util.GuiFactory",
	acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class LandDev {
	
    public static final String MODID = "landdev";
    public static final String NAME = "LandDev";
    public static final String VERSION = "1.0";
	@Mod.Instance(MODID)
	public static LandDev INSTANCE;
	public static Database DB = new JsonFileDB();
	public static File SAVE_DIR;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
    	Settings.initialize(event);
        logger = event.getModLog();
		CapabilityManager.INSTANCE.register(ChunkCap.class, new ChunkCapabilityUtil.Storage(), new ChunkCapabilityUtil.Callable());
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        if(event.getSide().isClient()){
        	PacketHandler.registerListener(PacketHandlerType.NBT, Side.CLIENT, new PacketReceiver());
        	MinecraftForge.EVENT_BUS.register(new LocationUpdate());
        }
    }

    @EventHandler
    public void init(FMLPostInitializationEvent event){
        Protector.load();
    }
    
    @Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		AliasLoader.load();
		event.registerServerCommand(new DebugCmd());
		event.registerServerCommand(new LDCmd());
		event.registerServerCommand(new CkCmd());
		event.registerServerCommand(new DisCmd());
		event.registerServerCommand(new MunCmd());
		DiscordTransmitter.restart();
	}
    
    @Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		Broadcaster.send(BroadcastChannel.SERVER, null, TranslationUtil.translate("server.stopping"), null, true);
	}
    
    @Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event){
		Runtime.getRuntime().addShutdownHook(new Thread(() -> { DiscordTransmitter.exit(); }));
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
