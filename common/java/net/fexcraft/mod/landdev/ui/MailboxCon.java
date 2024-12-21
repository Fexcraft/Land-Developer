package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.modules.MailModule;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UserInterface;

import static net.fexcraft.mod.landdev.ui.LDKeys.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailboxCon extends ContainerInterface {

	protected MailData mailbox;
	public MailboxUI mui;
	private LDPlayer ldp;

	public MailboxCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		if(ply.entity.isOnClient()) return;
		ldp = ResManager.getPlayer(ply);
		mailbox = MailModule.getMailbox(ldp, pos.x, pos.y);
	}

	public ContainerInterface set(UserInterface ui){
		mui = (MailboxUI)ui;
		return super.set(ui);
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(client){
			if(com.has("mails")){
				JsonMap map = JsonHandler.parse(com.getString("mails"), true).asMap();
				mailbox = new MailData(Layers.values()[pos.x], "client");
				mailbox.load(map);
			}
		}
		else{
			if(com.getBoolean("sync")){
				sendSync();
			}
			else if(com.has("read")){
				mailbox.mails.get(com.getInteger("read")).unread = false;
				ldp.entity.openUI(KEY_MAIL, pos.x, pos.y, com.getInteger("read"));
			}
			else if(com.has("delete")){
				if(!MailModule.canDelete(ldp, pos.x, pos.y)) return;
				mailbox.mails.remove(com.getInteger("delete"));
				sendSync();
			}
		}
	}

	private void sendSync(){
		JsonMap map = new JsonMap();
		mailbox.save(map);
		TagCW sync = TagCW.create();
		sync.set("mails", JsonHandler.toString(map, JsonHandler.PrintOption.FLAT));
		SEND_TO_CLIENT.accept(sync, player);
	}

}
