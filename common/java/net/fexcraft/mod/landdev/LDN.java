package net.fexcraft.mod.landdev;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkApp;
import net.fexcraft.mod.landdev.data.chunk.ChunkRegion;
import net.fexcraft.mod.landdev.db.Database;
import net.fexcraft.mod.landdev.db.JsonFileDB;
import net.fexcraft.mod.landdev.util.FsmmEventHooks;
import net.fexcraft.mod.landdev.ui.*;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.UniReg;
import net.fexcraft.mod.uni.ui.UIKey;

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
			UniReg.registerMenu(entry.getKey(), "landdev:uis/base", entry.getValue());
		}
		UniReg.registerUI(LDKeys.MAILBOX, MailboxUI.class);
		UniReg.registerMenu(LDKeys.MAILBOX, "landdev:uis/mailbox", MailboxCon.class);
		UniReg.registerUI(LDKeys.CHUNK_CLAIM, ChunkClaimUI.class);
		UniReg.registerMenu(LDKeys.CHUNK_CLAIM, "landdev:uis/chunk_claim", ChunkClaimCon.class);
		UniReg.registerUI(LDKeys.CHUNK_TRANSFER, ChunkClaimUI.class);
		UniReg.registerMenu(LDKeys.CHUNK_TRANSFER, "landdev:uis/chunk_claim", ChunkClaimCon.class);
		UniReg.registerUI(LDKeys.CHUNK_SELL, ChunkClaimUI.class);
		UniReg.registerMenu(LDKeys.CHUNK_SELL, "landdev:uis/chunk_claim", ChunkClaimCon.class);
		UniReg.registerUI(LDKeys.CHUNK_BUY, ChunkClaimUI.class);
		UniReg.registerMenu(LDKeys.CHUNK_BUY, "landdev:uis/chunk_claim", ChunkClaimCon.class);
		UniReg.registerUI(LDKeys.CHUNK_LOCK, ChunkClaimUI.class);
		UniReg.registerMenu(LDKeys.CHUNK_LOCK, "landdev:uis/chunk_claim", ChunkClaimCon.class);
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
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, String.format(LDConfig.SERVLANG_STARTED, LandDev.VERSION));
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
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, LDConfig.SERVLANG_STOPPING);
		if(TAX_TIMER != null) TAX_TIMER.cancel();
		if(GENERIC_TIMER != null) GENERIC_TIMER.cancel();
	}

	public static void onServerStop(){
		DiscordTransmitter.exit();
	}

}
