package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter;
import net.fexcraft.mod.uni.world.WrapperHolder;

import java.io.File;
import java.util.*;

import static net.fexcraft.mod.landdev.LandDev.SAVE_DIR;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

public class TaxSystem extends TimerTask {

	private static String CHATNAME = "TaxSystem";
	private static String PREFIX = "&b";
	public static TaxSystem INSTANCE;
	private static long last;

	@Override
	public void run(){
		load();
		long date = Time.getDate();
		if(tooSoon(last, date)){
			broad("Tax interval will be skipped. Previous Interval was not long ago.");
			broad("LAST: " + Time.getAsString(last) + " + INTERVAL:" + (LDConfig.TAX_INTERVAL / Time.HOUR_MS) + "h > NOW: " + Time.getAsString(date));
			save();
			return;
		}
		collect(date, false);
		save();
	}

	private static boolean tooSoon(long last, long date){
		return last + LDConfig.TAX_INTERVAL - 100 > date;
	}

	public void collect(long date, boolean ignore){
		broad("Starting regular Tax collection...");
		for(District dis : ResManager.DISTRICTS.values()){
			Mail mail = new Mail(MailType.SYSTEM, Layers.DISTRICT, dis.id);
			mail.addMessage(translate("tax.district.collected"));
			mail.addMessage(Config.getWorthAsString(dis.tax_collected));
			mail.setTitle(translate("tax.summary.mail"));
			mail.expireInDays(3);
			dis.mail.add(mail);
			dis.tax_collected = 0;
		}
		for(Municipality mun : ResManager.MUNICIPALITIES.values()){
			float muntax = mun.county.norms.get("municipality-tax-percent").decimal();
			if(muntax > 20) muntax = 20;
			if(muntax < 0) muntax = 0;
			long tax = (long)((mun.tax_collected / 100) * muntax);
			if(mun.account.getBalance() < tax){
				Mail mail = new Mail(MailType.SYSTEM, Layers.COUNTY, mun.county.id);
				mail.addMessage(translate("tax.county.mun_missing_funds0"));
				mail.addMessage(mun.id + " | " + mun.name());
				mail.addMessage(translate("tax.county.mun_missing_funds1"));
				mail.addMessage(Config.getWorthAsString(mun.account.getBalance()));
				mail.addMessage(translate("tax.county.mun_missing_funds2"));
				mail.addMessage(Config.getWorthAsString(tax));
				mail.setTitle(translate("tax.warning.mail"));
				mail.expireInDays(3);
				mun.county.mail.add(mail);
				tax = mun.account.getBalance() > 0 ? mun.account.getBalance() : 0;
			}
			mun.account.getBank().processAction(Bank.Action.TRANSFER, null, mun.account, tax, mun.county.account);
			mun.county.tax_collected += tax;
			//
			Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, mun.id);
			mail.addMessage(translate("tax.municipality.collected"));
			mail.addMessage(Config.getWorthAsString(mun.tax_collected));
			mail.addMessage(translate("tax.municipality.transferred"));
			mail.addMessage(Config.getWorthAsString(tax));
			mail.setTitle(translate("tax.summary.mail"));
			mail.expireInDays(3);
			mun.mail.add(mail);
			//
			mun.tax_collected = 0;
		}
		for(County ct : ResManager.COUNTIES.values()){
			float cttax = ct.region.norms.get("county-tax-percent").decimal();
			if(cttax > 20) cttax = 20;
			if(cttax < 0) cttax = 0;
			long tax = (long)((ct.tax_collected / 100) * cttax);
			ct.account.getBank().processAction(Bank.Action.TRANSFER, null, ct.account, tax, ct.region.account);
			ct.region.tax_collected += tax;
			if(ct.account.getBalance() < tax){
				Mail mail = new Mail(MailType.SYSTEM, Layers.REGION, ct.region.id);
				mail.addMessage(translate("tax.region.county_missing_funds0"));
				mail.addMessage(ct.id + " | " + ct.name());
				mail.addMessage(translate("tax.region.county_missing_funds1"));
				mail.addMessage(Config.getWorthAsString(ct.account.getBalance()));
				mail.addMessage(translate("tax.region.county_missing_funds2"));
				mail.addMessage(Config.getWorthAsString(tax));
				mail.setTitle(translate("tax.warning.mail"));
				mail.expireInDays(3);
				ct.region.mail.add(mail);
				tax = ct.account.getBalance() > 0 ? ct.account.getBalance() : 0;
			}
			//
			Mail mail = new Mail(MailType.SYSTEM, Layers.COUNTY, ct.id);
			mail.addMessage(translate("tax.county.collected"));
			mail.addMessage(Config.getWorthAsString(ct.tax_collected));
			mail.addMessage(translate("tax.county.transferred"));
			mail.addMessage(Config.getWorthAsString(tax));
			mail.setTitle(translate("tax.summary.mail"));
			mail.expireInDays(3);
			ct.mail.add(mail);
			//
			ct.tax_collected = 0;
		}
		for(Region st : ResManager.REGIONS.values()){
			Mail mail = new Mail(MailType.SYSTEM, Layers.REGION, st.id);
			mail.addMessage(translate("tax.county.collected"));
			mail.addMessage(Config.getWorthAsString(st.tax_collected));
			mail.setTitle(translate("tax.summary.mail"));
			mail.expireInDays(3);
			st.mail.add(mail);
			st.tax_collected = 0;
		}
		HashMap<ChunkKey, Chunk_> map = new HashMap(ResManager.CHUNKS);
		long cktax = 0;
		long pytax = 0;
		for(Chunk_ chunk : map.values()){
			cktax += taxChunk(chunk, date, ignore);
		}
		broad(Config.getWorthAsString(cktax) + " loaded Chunk tax collected.");
		if(LDConfig.TAX_OFFLINE){
			File folder = new File(SAVE_DIR, "players/");
			if(folder.exists()){
				for(File file : folder.listFiles()){
					UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
					LDPlayer player = ResManager.getPlayer(uuid, true);
					pytax += taxPlayer(player, date, ignore);
					ResManager.unloadIfOffline(player);
				}
			}
		}
		else{
			List<UUID> players = WrapperHolder.getOnlinePlayerIDs();
			for(UUID player : players){
				pytax += taxPlayer(ResManager.getPlayer(player, true), date, ignore);
			}
		}
		broad(Config.getWorthAsString(pytax) + " player tax collected.");
		broad(Config.getWorthAsString(cktax + pytax) + " tax collected in total.");
	}

	public static long taxPlayer(LDPlayer player, Long date, boolean ignore){
		if(date == null) date = last == 0 ? Time.getDate() : last;
		if(tooSoon(player.last_tax, date) && !ignore) return 0;
		long tax = 0;
		Account account = player.account;
		Account receiver = null;
		Layer in = null;
		boolean kick;
		if(player.municipality.id >= 0){
			tax = player.municipality.norms.get("citizen-tax").integer();
			receiver = player.municipality.account;
			kick = player.municipality.norms.get("kick-bankrupt").bool();
			in = player.municipality;
		}
		else if(player.county.id < 0) return 0;
		else{
			tax = player.county.norms.get("citizen-tax").integer();
			receiver = player.county.account;
			kick = player.county.norms.get("kick-bankrupt").bool();
			in = player.county;
		}
		if(tax > 0){
			Bank bank = player.account.getBank();
			if(account.getBalance() < tax){
				if(account.getBalance() <= 0){
					Mail mail = new Mail(MailType.SYSTEM, in.getLayer(), in.lid(), Layers.PLAYER, player.uuid).expireInDays(7);
					mail.setTitle(translate("tax.unpaid_notice.mail"));
					mail.addMessage(translate("tax.no_funds"));
					if(kick && !player.isInManagement(in.getLayer())){
						mail.addMessage(translate("tax.no_funds.kicked"));
						if(in.is(Layers.COUNTY)){
							Mail ml = new Mail(MailType.SYSTEM, in.getLayer(), in.lid());
							ml.setTitle(translate("tax.player_kicked"));
							ml.addMessage(translate("tax.player_kicked.info"));
							ml.addMessage(player.name());
							ml.addMessage(player.uuid.toString());
							player.county.mail.add(ml);
							player.leaveCounty();
						}
						else{
							Mail ml = new Mail(MailType.SYSTEM, in.getLayer(), in.lid());
							ml.setTitle(translate("tax.player_kicked"));
							ml.addMessage(translate("tax.player_kicked.info"));
							ml.addMessage(player.name());
							ml.addMessage(player.uuid.toString());
							player.municipality.mail.add(ml);
							player.leaveMunicipality();
						}
					}
					player.addMailAndSave(mail);
					return 0;
				}
				else{
					tax = account.getBalance();
					Mail mail = new Mail(MailType.SYSTEM, in.getLayer(), in.lid(), Layers.PLAYER, player.uuid).expireInDays(7);
					mail.setTitle(translate("tax.unpaid_notice.mail"));
					mail.addMessage(translate("tax.missing_funds"));
					if(kick && !player.isInManagement(in.getLayer())){
						mail.addMessage(translate("tax.missing_funds.kick"));
					}
					player.addMailAndSave(mail);
				}
			}
			bank.processAction(Bank.Action.TRANSFER, null, account, tax, receiver);
			if(player.municipality.id >= 0) player.municipality.tax_collected += tax;
			else player.county.tax_collected += tax;
		}
		player.last_tax = date;
		player.save();
		return tax;
	}

	public static long taxChunk(Chunk_ chunk, Long date, boolean ignore){
		if(date == null) date = last == 0 ? Time.getDate() : last;
		if(tooSoon(chunk.tax.last_tax, date) && !ignore) return 0;
		if(chunk.link != null && chunk.link.root_key != null){
			Chunk_ ck = ResManager.getChunk(chunk.link.root_key);
			return ck == null ? 0 : taxChunk(ck, date, ignore);
		}
		if(chunk.district.id < -1) return 0;
		if(!chunk.type.taxable()) return 0;
		if(!chunk.owner.taxable()) return 0;
		long tax = chunk.tax.custom_tax > 0 ? chunk.tax.custom_tax : chunk.district.tax();
		if(tax > 0){
			Account account = chunk.owner.getAccount(chunk);
			Account receiver = chunk.district.account();
			Bank bank = account.getBank();
			if(account.getBalance() < tax){
				Layers ckow = chunk.owner.layer();
				String ckoid = chunk.owner.playerchunk ? chunk.owner.player.toString() : chunk.owner.owid + "";
				if(account.getBalance() <= 0){
					Mail mail = new Mail(MailType.SYSTEM, chunk).expireInDays(7);
					mail.setTitle(translate("tax.unpaid_notice.mail"));
					mail.addMessage(translate("tax.no_funds"));
					if(chunk.district.norms.get("unclaim-bankrupt").bool()){
						mail.addMessage(translate("tax.no_funds.unclaimed"));
						chunk.owner.set(Layers.DISTRICT, null, chunk.district.id);
					}
					mail.addMessage("Chunk: " + chunk.key.toString());
					chunk.sendToOwner(mail);
					return 0;
				}
				else{
					Mail mail = new Mail(MailType.SYSTEM, chunk).expireInDays(7);
					mail.setTitle(translate("tax.unpaid_notice.mail"));
					mail.addMessage(translate("tax.missing_funds"));
					if(chunk.district.norms.get("unclaim-bankrupt").bool()){
						mail.addMessage(translate("tax.missing_funds.unclaim"));
					}
					mail.addMessage("Chunk: " + chunk.key.toString());
					chunk.sendToOwner(mail);
					tax = account.getBalance();
				}
			}
			bank.processAction(Bank.Action.TRANSFER, null, account, tax, receiver);
			chunk.district.addTaxStat(tax);
		}
		chunk.tax.last_tax = date;
		chunk.save();
		return tax;
	}

	private void broad(String str){
		Broadcaster.send(TargetTransmitter.INTERNAL_ONLY, BroadcastChannel.ANNOUNCE, "&b" + CHATNAME, str, PREFIX);
		Broadcaster.send(TargetTransmitter.NO_INTERNAL, BroadcastChannel.ANNOUNCE, CHATNAME, str);
	}

	public TaxSystem load(){
		File file = new File(SAVE_DIR, "tax.json");
		JsonMap map = JsonHandler.parse(file);
		last = map.empty() ? 0 : map.getLong("last-interval", Time.getDate() - 200);
		INSTANCE = this;
		if(!file.exists()) save();
		return this;
	}

	public void save(){
		JsonMap map = new JsonMap();
		map.add("last-interval", last);
		JsonHandler.print(new File(SAVE_DIR, "tax.json"), map, JsonHandler.PrintOption.FLAT_SPACED);
	}
	
	public static void stop(){
		INSTANCE.cancel();
	}

}
