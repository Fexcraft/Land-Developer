package net.fexcraft.mod.landdev;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkApp;
import net.fexcraft.mod.landdev.data.chunk.ChunkRegion;
import net.fexcraft.mod.landdev.db.Database;
import net.fexcraft.mod.landdev.db.JsonFileDB;
import net.fexcraft.mod.landdev.events.FsmmEventHooks;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.BaseUI;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.UniReg;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIKey;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDN {

	public static final String MODID = "landdev";
	public static Database DB = new JsonFileDB();
	public static LDConfig CONFIG;
	public static Timer TAX_TIMER;
	public static Timer GENERIC_TIMER;

	public static void preinit(File confdir){
		CONFIG = new LDConfig(new File(confdir, "landdev.json"));
		UniChunk.register(new ChunkApp(null));
		
	}

	public static void init(LandDev inst){
		FsmmEventHooks.init();
		UniReg.registerMod(MODID, inst);
		for(Map.Entry<UIKey, Class<? extends BaseCon>> entry : LDKeys.CONS.entrySet()){
			UniReg.registerUI(entry.getKey(), BaseUI.class);
			UniReg.registerMenu(entry.getKey(), "assets/landdev/uis/base", entry.getValue());
		}
		UniReg.registerUI(LDKeys.KEY_MAILBOX, UserInterface.class);
		UniReg.registerMenu(LDKeys.KEY_MAILBOX, "assets/landdev/uis/mailbox", ContainerInterface.class);
	}

	public static void postinit(){
		Protector.load();
		DiscordTransmitter.restart();
	}

	public static void onServerStarting(){
		DiscordTransmitter.restart();
	}

	public static void onServerStarted(){
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli();
		setupTaxTimer(mid);
		setupGenericTimer(mid);
	}

	private static void setupTaxTimer(long mid){
		long date = Time.getDate();
		while((mid += LDConfig.TAX_INTERVAL) < date);
		if(TAX_TIMER == null && LDConfig.TAX_ENABLED){
			(TAX_TIMER = new Timer()).schedule(new TaxSystem().load(), new Date(mid), LDConfig.TAX_INTERVAL);
		}
	}

	private static void setupGenericTimer(long mid){
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

	public static void onServerStopping(){
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, TranslationUtil.translate("server.stopping"));
		if(TAX_TIMER != null) TAX_TIMER.cancel();
		if(GENERIC_TIMER != null) GENERIC_TIMER.cancel();
	}

	public static void onServerStop(){
		DiscordTransmitter.exit();
	}

}
