package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailModule implements LDGuiModule {
	
	public static MailModule INST = new MailModule();

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("mail.title");
		resp.setNoBack();
		MailData mailbox = getMailbox(container.player, container.x, container.y);
		if(mailbox == null){
			resp.addRow("boxnotfound", ELM_GENERIC);
			return;
		}
		Mail mail = container.z >= mailbox.mails.size() ? null : mailbox.mails.get(container.z);
		if(mail == null){
			resp.addRow("notfound", ELM_GENERIC);
			return;
		}
		resp.addRow("title", ELM_GREEN, resp.val(mail.title));
		resp.addRow("type", ELM_BLUE, mail.type.name().toLowerCase());
		resp.addRow("from", ELM_BLUE, mail.from + ":" + mail.fromid);
		resp.addRow("to", ELM_BLUE, mail.receiver);
		resp.addRow("at", ELM_BLUE, Time.getAsString(mail.sent));
		for(int i = 0; i < mail.message.size(); i++){
			resp.addRow("msg" + i, ELM_GENERIC, resp.val(mail.message.get(i)));
		}
		if(mail.invite()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("invite.accept", ELM_GREEN, ICON_ADD);
				resp.addButton("invite.reject", ELM_RED, ICON_REM);
			}
		}
		if(mail.request()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("request.accept", ELM_GREEN, ICON_ADD);
				resp.addButton("request.reject", ELM_RED, ICON_REM);
				resp.addButton("request.timeout", ELM_YELLOW, ICON_REM, Settings.REQUEST_TIMEOUT_DAYS);
			}
		}
	}

	public void on_interact(LDGuiContainer container, ModuleRequest req){
		//
	}

	public static MailData getMailbox(Player player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return player.mail;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y, false);
				if(player.adm || dis.can(PermAction.ACT_MANAGE_MAIL, player.uuid)){
					return dis.mail;
				}
				break;
			}
			case MUNICIPALITY:{
				Municipality mun = ResManager.getMunicipality(y, false);
				if(player.adm || mun.manage.can(PermAction.ACT_MANAGE_MAIL, player.uuid)){
					return mun.mail;
				}
				break;
			}
			case COUNTY:{
				County con = ResManager.getCounty(y, false);
				if(player.adm || con.manage.can(PermAction.ACT_MANAGE_MAIL, player.uuid)){
					return con.mail;
				}
				break;
			}
			case STATE:{
				State st = ResManager.getState(y, false);
				if(player.adm || st.manage.can(PermAction.ACT_MANAGE_MAIL, player.uuid)){
					return st.mail;
				}
				break;
			}
		}
		return null;
	}

}
