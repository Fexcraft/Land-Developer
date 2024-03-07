package net.fexcraft.mod.landdev.util;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter;

import java.io.File;
import java.util.HashMap;
import java.util.TimerTask;

import static net.fexcraft.mod.landdev.LandDev.SAVE_DIR;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

public class TaxSystem extends TimerTask {

	private static String CHATNAME = "TaxSystem";
	private static String PREFIX = "&b";
	public static TaxSystem INSTANCE;
	private long last;

	@Override
	public void run(){
		load();
		long date = Time.getDate();
		if(tooSoon(last, date)){
			broad("Tax interval will be skipped. Previous Interval was not long ago.");
			broad("LAST: " + Time.getAsString(last) + " + INTERVAL:" + (Settings.TAX_INTERVAL / Time.HOUR_MS) + "h > NOW: " + Time.getAsString(date));
			return;
		}
		collect(date, false);
	}

	private static boolean tooSoon(long last, long date){
		return last + Settings.TAX_INTERVAL - 100 > date;
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
			float cttax = ct.state.norms.get("county-tax-percent").decimal();
			if(cttax > 20) cttax = 20;
			if(cttax < 0) cttax = 0;
			long tax = (long)((ct.tax_collected / 100) * cttax);
			ct.account.getBank().processAction(Bank.Action.TRANSFER, null, ct.account, tax, ct.state.account);
			ct.state.tax_collected += tax;
			if(ct.account.getBalance() < tax){
				Mail mail = new Mail(MailType.SYSTEM, Layers.STATE, ct.state.id);
				mail.addMessage(translate("tax.state.county_missing_funds0"));
				mail.addMessage(ct.id + " | " + ct.name());
				mail.addMessage(translate("tax.state.county_missing_funds1"));
				mail.addMessage(Config.getWorthAsString(ct.account.getBalance()));
				mail.addMessage(translate("tax.state.county_missing_funds2"));
				mail.addMessage(Config.getWorthAsString(tax));
				mail.setTitle(translate("tax.warning.mail"));
				mail.expireInDays(3);
				ct.state.mail.add(mail);
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
		for(State st : ResManager.STATES.values()){
			Mail mail = new Mail(MailType.SYSTEM, Layers.STATE, st.id);
			mail.addMessage(translate("tax.county.collected"));
			mail.addMessage(Config.getWorthAsString(st.tax_collected));
			mail.setTitle(translate("tax.summary.mail"));
			mail.expireInDays(3);
			st.mail.add(mail);
			st.tax_collected = 0;
		}
		HashMap<ChunkKey, Chunk_> map = new HashMap(ResManager.CHUNKS);
		long sum = 0;
		for(Chunk_ chunk : map.values()){
			sum += taxChunk(chunk, date, ignore);
		}
		long total = sum;
		broad(Config.getWorthAsString(sum) + " loaded Chunk tax collected.");
		if(Settings.TAX_OFFLINE){

		}
		else{

		}
		broad(Config.getWorthAsString(total) + " tax collected in total.");
	}

	public static long taxChunk(Chunk_ chunk, long date, boolean ignore){
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
					mail.setTitle("Tax Unpaid Notice");
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
					mail.setTitle("Tax Unpaid Notice");
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
		JsonMap map = JsonHandler.parse(new File(SAVE_DIR, "tax.json"));
		last = map.getLong("last-interval", Time.getDate() - 200);
		INSTANCE = this;
		return this;
	}
}
