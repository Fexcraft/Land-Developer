package net.fexcraft.mod.landdev;

import java.io.File;

import org.apache.logging.log4j.Logger;

import net.fexcraft.mod.landdev.data.chunk.ChunkCap;
import net.fexcraft.mod.landdev.db.Database;
import net.fexcraft.mod.landdev.db.JsonFileDB;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.ChunkCapabilityUtil;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

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
    }
	
	public static final File updateSaveDirectory(World world){
		return SAVE_DIR = new File(world.getSaveHandler().getWorldDirectory(), "states/");
	}
	
	public static final File getSaveDirectory(){
		return SAVE_DIR;
	}
	
}
