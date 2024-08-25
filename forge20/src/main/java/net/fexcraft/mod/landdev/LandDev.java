package net.fexcraft.mod.landdev;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(LandDev.MODID)
public class LandDev {

	public static final String MODID = "landdev";
	private static final Logger LOGGER = LogUtils.getLogger();

	public LandDev(){
		IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
		modbus.addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.register(this);
		LDN.preinit(FMLPaths.CONFIGDIR.get().toFile());
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		LDN.init();
		LDN.postinit();
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LDN.onServerStart();
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

}
