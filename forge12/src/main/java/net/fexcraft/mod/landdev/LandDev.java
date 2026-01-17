package net.fexcraft.mod.landdev;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.render.ExternalTextureHelper;
import net.fexcraft.mod.landdev.cmd.*;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.gui.LDGuiImgPreview;
import net.fexcraft.mod.landdev.util.GuiHandler;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.impl.PacketTagHandler.I12_PacketTag;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static net.fexcraft.lib.common.utils.Formatter.format;
import static net.fexcraft.mod.landdev.LDN.PKT_RECEIVER_ID;

@Mod(modid = LDN.MODID, name = LandDev.NAME, version = LandDev.VERSION,
	dependencies = "required-after:fcl", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class LandDev {

    public static final String NAME = "LandDev";
    public static final String VERSION = "1.x.x";
	@Mod.Instance(LDN.MODID)
	public static LandDev INSTANCE;
	public static File SAVE_DIR = new File("./landdev/");

    private static Logger logger;

	@EventHandler
    public void preInit(FMLPreInitializationEvent event){
		LDN.preinit(event.getModConfigurationDirectory());
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		LDN.init(this);
        if(event.getSide().isClient()){
			LDN.client_init(this);
			CTagListener.TASKS.put("location_update", (packet, player) -> {
				int time = packet.has("time") ? packet.getInteger("time") : 10;
				LocationUpdate.clear(Time.getDate() + (time * 1000));
				LocationUpdate.loadIcons(packet.getList("icons").local());
				LocationUpdate.loadLines(packet.getList("lines").local());
			});
			CTagListener.TASKS.put("chat_message", (packet, player) -> {
				NBTTagList list = packet.getList("msg").local();
				String c = list.tagCount() > 3 ? list.getStringTagAt(3) : "&a";
				ITextComponent text = null;
				switch(list.getStringTagAt(0)){
					case "chat_img":
						text = new TextComponentString(format(list.getStringTagAt(1)));
						TextComponentString text1 = new TextComponentString(format(" &a[ &6View &a]"));
						text1.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/landdev img " + list.getStringTagAt(2) + " " + c + " " + list.getStringTagAt(4)));
						text1.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(format(list.getStringTagAt(1)))));
						text.appendSibling(text1);
						TextComponentString text2 = new TextComponentString(format(" &e[ &6Open &e]"));
						text2.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, list.getStringTagAt(2)));
						text2.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(format(list.getStringTagAt(1)))));
						text.appendSibling(text2);
						break;
					case "chat":
					default:
						text = new TextComponentString(format(LDConfig.CHAT_OVERRIDE_LANG, c, list.getStringTagAt(1), list.getStringTagAt(2)));
						//text = new TextComponentString(list.toString());
						break;
				}
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(text);
			});
			CTagListener.TASKS.put("img_preview_url", (packet, player) -> {
				LDGuiImgPreview.IMG_URL = ExternalTextureHelper.get(packet.getString("url"));
			});
        	MinecraftForge.EVENT_BUS.register(new LocationUpdate());
        }
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
		event.registerServerCommand(new PropCmd());
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
	
	public static final File getSaveDirectory(){
		return SAVE_DIR;
	}
	
	public static void log(Object obj){
		logger.log(Level.INFO, obj);
	}

	public static void debug(String s){
		if(EnvInfo.DEV) logger.log(Level.INFO, s);
	}

	public static void sendLocationPacket(EntityW entity, TagCW com){
		PacketHandler.getInstance().sendTo(new I12_PacketTag(PKT_RECEIVER_ID, com), entity.local());
	}

	public static void sendToAll(TagCW com){
		if(ResManager.INSTANCE.LOADED){
			PacketHandler.getInstance().sendToAll(new I12_PacketTag(PKT_RECEIVER_ID, com));
		}
	}

	public static void sendTo(TagCW com, LDPlayer player){
		if(ResManager.INSTANCE.LOADED){
			PacketHandler.getInstance().sendTo(new I12_PacketTag(PKT_RECEIVER_ID, com), player.entity.local());
		}
	}
	
}
