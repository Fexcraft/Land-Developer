package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.Manageable.Staff;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailModule implements LDUIModule {
	
	public static MailModule INST = new MailModule();

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("mail.title");
		resp.setNoBack();
		MailData mailbox = getMailbox(container.ldp, container.pos.x, container.pos.y);
		if(mailbox == null){
			resp.addRow("boxnotfound", ELM_GENERIC);
			return;
		}
		Mail mail = container.pos.z >= mailbox.mails.size() ? null : mailbox.mails.get(container.pos.z);
		if(mail == null){
			resp.addRow("notfound", ELM_GENERIC);
			return;
		}
		resp.addRow("type", ELM_BLUE, mail.type.name().toLowerCase());
		resp.addRow("from", ELM_BLUE, mail.from.name().toLowerCase() + " > " + mail.fromid);
		//resp.addRow("to", ELM_BLUE, mail.receiver);
		resp.addRow("at", ELM_BLUE, Time.getAsString(mail.sent));
		resp.addBlank();
		resp.addRow("title", ELM_GREEN, resp.val(mail.title));
		for(int i = 0; i < mail.message.size(); i++){
			resp.addRow("msg" + i, ELM_GENERIC, resp.val(mail.message.get(i)));
		}
		if(mail.invite()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("invite.accept", ELM_GREEN, ADD);
				resp.addButton("invite.reject", ELM_RED, REM);
			}
		}
		if(mail.request()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("request.accept", ELM_GREEN, ADD);
				resp.addButton("request.reject", ELM_RED, REM);
				resp.addButton("request.timeout", ELM_YELLOW, REM, LDConfig.REQUEST_TIMEOUT_DAYS);
			}
		}
		resp.addButton("goback", ELM_GENERIC, LIST);
	}

	public void on_interact(BaseCon container, ModuleRequest req){
		MailData mailbox = getMailbox(container.ldp, container.pos.x, container.pos.y);
		if(mailbox == null) return;
		LDPlayer player;
		Mail mail = container.pos.z >= mailbox.mails.size() ? null : mailbox.mails.get(container.pos.z);
		switch(req.event()){
			case "invite.accept":{
				if(mail.type != MailType.INVITE) return;
				switch(mail.from){
					case MUNICIPALITY:{
						if(mail.receiver.is(Layers.PLAYER)){
							player = ResManager.getPlayer(mail.recUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.fromInt(), true);
							if(mail.staff){
								if(!mun.citizens.isCitizen(player.uuid)){
									player.entity.send(translate("mail.municipality.staff.notmember"));
									return;
								}
								mun.manage.staff.add(new Staff(player.uuid, MUNICIPALITY_STAFF));
								mun.save();
								String pln = ResManager.getPlayerName(player.uuid);
								for(Staff stf : mun.manage.staff){
									LDPlayer stp = ResManager.getPlayer(stf.uuid, true);
									mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, mun.id, Layers.PLAYER, stp.uuid).expireInDays(7);
									mail.setTitle(mun.name()).addMessage(translate("mail.municipality.staff.added", pln));
									stp.addMailAndSave(mail);
								}
								mail.expire();
							}
							else{
								if(player.isMunicipalityManager()){
									player.entity.send(translate("mail.municipality.citizen.ismanager"));
									return;
								}
								if(player.isCountyManager() && mun.county.id != player.county.id){
									player.entity.send(translate("mail.county.citizen.ismanager"));
									return;
								}
								player.setCitizenOf(mun);
								mail.expire();
								//TODO announce
							}
						}
						else if(mail.receiver.is(Layers.MUNICIPALITY)){
							//invites into a county
						}
						goback(container);
						return;
					}
					case COUNTY:{
						if(mail.receiver.is(Layers.PLAYER)){
							player = ResManager.getPlayer(mail.recUUID(), true);
							County ct = ResManager.getCounty(mail.fromInt(), true);
							if(mail.staff){
								if(!ct.citizens.isCitizen(player.uuid)){
									player.entity.send(translate("mail.county.staff.notmember"));
									return;
								}
								ct.manage.staff.add(new Staff(player.uuid, COUNTY_STAFF));
								ct.save();
								String pln = ResManager.getPlayerName(player.uuid);
								for(Staff stf : ct.manage.staff){
									LDPlayer stp = ResManager.getPlayer(stf.uuid, true);
									mail = new Mail(MailType.SYSTEM, Layers.COUNTY, ct.id, Layers.PLAYER, stp.uuid).expireInDays(7);
									mail.setTitle(ct.name()).addMessage(translate("mail.county.staff.added", pln));
									stp.addMailAndSave(mail);
								}
								mail.expire();
							}
							else{
								if(player.isMunicipalityManager() && ct.id != player.municipality.county.id){
									player.entity.send(translate("mail.municipality.citizen.ismanager"));
									return;
								}
								if(player.isCountyManager() && ct.id != player.county.id){
									player.entity.send(translate("mail.county.citizen.ismanager"));
									return;
								}
								player.setCitizenOf(ct);
								mail.expire();
								//TODO announce
							}
						}
						else if(mail.receiver.is(Layers.COUNTY)){
							//invites into a region
						}
						goback(container);
						return;
					}
					case REGION:{
						if(mail.receiver.is(Layers.PLAYER)){
							player = ResManager.getPlayer(mail.recUUID(), true);
							Region rg = ResManager.getRegion(mail.fromInt(), true);
							if(mail.staff){
								if(!rg.isCitizen(player.uuid)){
									player.entity.send(translate("mail.region.staff.notmember"));
									return;
								}
								rg.manage.staff.add(new Staff(player.uuid, COUNTY_STAFF));
								rg.save();
								String pln = ResManager.getPlayerName(player.uuid);
								for(Staff stf : rg.manage.staff){
									LDPlayer stp = ResManager.getPlayer(stf.uuid, true);
									mail = new Mail(MailType.SYSTEM, Layers.REGION, rg.id, Layers.PLAYER, stp.uuid).expireInDays(7);
									mail.setTitle(rg.name()).addMessage(translate("mail.region.staff.added", pln));
									stp.addMailAndSave(mail);
								}
								mail.expire();
							}
						}
						else if(mail.receiver.is(Layers.REGION)){
							//
						}
						return;
					}
					case COMPANY:{
						//
						return;
					}
				}
				return;
			}
			case "invite.reject":{
				if(mail.type != MailType.INVITE) return;
				mail.expire();
				goback(container);
				return;
			}
			case "request.accept":{
				if(mail.type != MailType.REQUEST) return;
				switch(mail.from){
					case PLAYER:{
						if(mail.receiver.is(Layers.MUNICIPALITY)){
							player = ResManager.getPlayer(mail.fromUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.recInt(), true);
							if(player.isMunicipalityManager()){
								player.entity.send(translate("mail.municipality.request.ismanager"));
								return;
							}
							if(player.isCountyManager() && mun.county.id != player.county.id){
								player.entity.send(translate("mail.county.request.ismanager"));
								return;
							}
							player.setCitizenOf(mun);
							mail.expire();
							//TODO announce
						}
						else if(mail.receiver.is(Layers.COUNTY)){
							//
						}
						else if(mail.receiver.is(Layers.COMPANY)){
							//
						}
						goback(container);
						return;
					}
				}
				return;
			}
			case "request.reject":
			case "request.timeout":{
				if(mail.type != MailType.REQUEST) return;
				int mul = req.event().endsWith("timeout") ? LDConfig.REQUEST_TIMEOUT_DAYS : 1;
				switch(mail.from){
					case PLAYER:{
						if(mail.receiver.is(Layers.MUNICIPALITY)){
							player = ResManager.getPlayer(mail.fromUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.recInt(), true);
							mun.requests.timeouts.put(player.uuid, Time.getDate() + Time.DAY_MS * mul);
							mail.expire();
							goback(container);
						}
						return;
					}
				}
				return;
			}
			case "goback":{
				goback(container);
				return;
			}
		}
	}

	private void goback(BaseCon container){
		container.open(LDKeys.MAILBOX, container.pos.x, container.pos.y, container.pos.z);
	}

	public static MailData getMailbox(LDPlayer player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return player.mail;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y);
				if(player.adm || dis.can(MAIL_READ, player.uuid)){
					return dis.mail;
				}
				break;
			}
			case MUNICIPALITY:{
				Municipality mun = ResManager.getMunicipality(y, false);
				if(player.adm || mun.manage.can(MAIL_READ, player.uuid)){
					return mun.mail;
				}
				break;
			}
			case COUNTY:{
				County con = ResManager.getCounty(y, false);
				if(player.adm || con.manage.can(MAIL_READ, player.uuid)){
					return con.mail;
				}
				break;
			}
			case REGION:{
				Region st = ResManager.getRegion(y, false);
				if(player.adm || st.manage.can(MAIL_READ, player.uuid)){
					return st.mail;
				}
				break;
			}
		}
		return null;
	}

	public static boolean canDelete(LDPlayer player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return true;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y);
				if(player.adm || dis.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case MUNICIPALITY:{
				Municipality mun = ResManager.getMunicipality(y, false);
				if(player.adm || mun.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case COUNTY:{
				County con = ResManager.getCounty(y, false);
				if(player.adm || con.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case REGION:{
				Region st = ResManager.getRegion(y, false);
				if(player.adm || st.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
		}
		return false;
	}

}
