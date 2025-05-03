package net.fexcraft.mod.landdev;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.landdev.cmd.*;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.util.GuiHandler;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = LDN.MODID, name = LandDev.NAME, version = LandDev.VERSION,
	dependencies = "required-after:fcl", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class LandDev {

    public static final String NAME = "LandDev";
    public static final String VERSION = "1.x.x";
	@Mod.Instance(LDN.MODID)
	public static LandDev INSTANCE;
	public static File SAVE_DIR;

	public static final String PKT_RECEIVER_ID = "landdev:util";
    private static Logger logger;

	@EventHandler
    public void preInit(FMLPreInitializationEvent event){
		LDN.preinit(event.getModConfigurationDirectory());
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        if(event.getSide().isClient()){
			UniFCL.regTagPacketListener(PKT_RECEIVER_ID, true, new PacketReceiver());
        	MinecraftForge.EVENT_BUS.register(new LocationUpdate());
        }
		LDN.init(this);
    }

    @EventHandler
    public void init(FMLPostInitializationEvent event){
		LDN.postinit();
    }
    
    @Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		LDN.onServerStarting();
		AliasLoader.load();
		event.registerServerCommand(new DebugCmd());
		event.registerServerCommand(new LDCmd());
		event.registerServerCommand(new CkCmd());
		event.registerServerCommand(new DisCmd());
		event.registerServerCommand(new MunCmd());
		event.registerServerCommand(new CntCmd());
		event.registerServerCommand(new RgCmd());
	}

    @Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		LDN.onServerStarted();
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		LDN.onServerStopping();
	}
    
    @Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event){
		LDN.onServerStop();
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

	public static void sendLocationPacket(EntityW entity, TagCW com){
		com.set("target_listener", PKT_RECEIVER_ID);
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(com.local()), entity.local());
	}

	public static void sendToAll(TagCW com){
		com.set("target_listener", PKT_RECEIVER_ID);
		if(ResManager.INSTANCE.LOADED) PacketHandler.getInstance().sendToAll(new PacketNBTTagCompound(com.local()));
	}
	
}
