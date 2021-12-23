package net.fexcraft.mod.landdev;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LandDev.MODID, name = LandDev.NAME, version = LandDev.VERSION)
public class LandDev {
	
    public static final String MODID = "landdev";
    public static final String NAME = "LandDev";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        logger.info("init");
    }
}
