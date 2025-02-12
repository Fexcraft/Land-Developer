package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.fsmm.data.AccountPermission;
import net.fexcraft.mod.fsmm.event.ATMEvent;
import net.fexcraft.mod.fsmm.event.FsmmEvent;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.state.State;
import org.apache.commons.lang3.math.NumberUtils;

import static net.fexcraft.mod.landdev.LDN.DB;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class FsmmEventHooks {

	public static void init(){
		FsmmEvent.addListener(ATMEvent.GatherAccounts.class, event -> {
			LDPlayer player = ResManager.getPlayer(event.getPlayer());
			if(player.adm){
				event.getAccountsList().add(new AccountPermission(ResManager.SERVER_ACCOUNT, true, true, true, true));
				Chunk_ ck = ResManager.getChunk(event.getPlayer());
				if(ck.district.state().id > -1){
					event.getAccountsList().add(new AccountPermission(ck.district.state().account, true, true, true, true));
				}
				if(ck.district.county().id > -1){
					event.getAccountsList().add(new AccountPermission(ck.district.county().account, true, true, true, true));
				}
				if(!ck.district.owner.is_county && ck.district.municipality().id > -1){
					event.getAccountsList().add(new AccountPermission(ck.district.municipality().account, true, true, true, true));
				}
				event.getAccountsList().add(new AccountPermission(ResManager.getState(-1, true).account, true, true, true, true));
				event.getAccountsList().add(new AccountPermission(ResManager.getCounty(-1, true).account, true, true, true, true));
				event.getAccountsList().add(new AccountPermission(ResManager.getMunicipality(-1, true).account, true, true, true, true));
			}
			boolean use = player.county.manage.can(PermAction.FINANCES_USE, player.uuid);
			boolean man = player.county.manage.can(PermAction.MANAGE_COUNTY, player.uuid);
			if(use || man){
				event.getAccountsList().add(new AccountPermission(player.county.account, use || man, true, man, man));
			}
			use = player.municipality.manage.can(PermAction.FINANCES_USE, player.uuid);
			man = player.municipality.manage.can(PermAction.MANAGE_MUNICIPALITY, player.uuid);
			if(use || man){
				event.getAccountsList().add(new AccountPermission(player.municipality.account, use || man, true, man, man));
			}
		});
		FsmmEvent.addListener(ATMEvent.SearchAccounts.class, event -> {
			if(!event.getSearchedType().equals("state")
				&& !event.getSearchedType().equals("municipality")
				&& !event.getSearchedType().equals("county")){
				return;
			}
			LDPlayer player = ResManager.getPlayer(event.getPlayer());
			boolean man = player.adm;
			boolean use = player.adm;
			switch(event.getSearchedType()){
				case "state":{
					if(NumberUtils.isCreatable(event.getSearchedId())){
						int id = Integer.parseInt(event.getSearchedId());
						if(ResManager.STATES.containsKey(id)){
							State state = ResManager.getState(id, true);
							if(!man) man = state.manage.can(PermAction.FINANCES_MANAGE, player.uuid);
							if(!use) use = state.manage.can(PermAction.FINANCES_USE, player.uuid);
							event.getAccountsMap().put("state:" + id, new AccountPermission(state.account, use || man, man, man, man));
						}
						else if(DB.exists("states", event.getSearchedId())){
							event.getAccountsMap().put("state:" + id, new AccountPermission("state:" + id));
						}
					}
					else{
						//TODO name cache
					}
					return;
				}
				case "county":{
					if(NumberUtils.isCreatable(event.getSearchedId())){
						int id = Integer.parseInt(event.getSearchedId());
						if(ResManager.COUNTIES.containsKey(id)){
							County cou = ResManager.getCounty(id, true);
							if(!man) man = cou.manage.can(PermAction.FINANCES_MANAGE, player.uuid);
							if(!use) use = cou.manage.can(PermAction.FINANCES_USE, player.uuid);
							event.getAccountsMap().put("county:" + id, new AccountPermission(cou.account, use || man, man, man, man));
						}
						else if(DB.exists("counties", event.getSearchedId())){
							event.getAccountsMap().put("county:" + id, new AccountPermission("county:" + id));
						}
					}
					else{
						//TODO name cache
					}
					return;
				}
				case "municipality":{
					if(NumberUtils.isCreatable(event.getSearchedId())){
						int id = Integer.parseInt(event.getSearchedId());
						if(ResManager.MUNICIPALITIES.containsKey(id)){
							Municipality mun = ResManager.getMunicipality(id, true);
							if(!man) man = mun.manage.can(PermAction.FINANCES_MANAGE, player.uuid);
							if(!use) use = mun.manage.can(PermAction.FINANCES_USE, player.uuid);
							event.getAccountsMap().put("municipality:" + id, new AccountPermission(mun.account, use || man, man, man, man));
						}
						else if(DB.exists("municipalities", event.getSearchedId())){
							event.getAccountsMap().put("municipality:" + id, new AccountPermission("municipality:" + id));
						}
					}
					else{
						//TODO name cache
					}
					return;
				}
			}
		});
	}

}
