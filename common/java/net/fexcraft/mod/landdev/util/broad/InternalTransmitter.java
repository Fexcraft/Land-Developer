package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.WrapperHolder;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class InternalTransmitter implements Transmitter {

	@Override
	public void transmit(Broadcaster.Message msg){
		if(msg.channel == Channel.CHAT && !LDConfig.CHAT_OVERRIDE) return;
		TagCW com = TagCW.create();
		com.set("task", "chat_message");
		com.set("msg", msg.toTag());
		switch(msg.channel.sub){
			case ALL:
				switch(msg.channel.type){
					case CHAT:
					case SERVER:
						LandDev.sendToAll(com);
						break;
					case REGION:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInRegion(msg.channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(msg.channel, com);
						break;
					case COUNTY:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInCounty(msg.channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(msg.channel, com);
						break;
					case MUNICIPALITY:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInMunicipality(msg.channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(msg.channel, com);
						break;
					case DISTRICT:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInDistrict(msg.channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						break;
					case COMPANY:
						toMembers(msg.channel, com);
						break;
				}
				break;
			case MEMBER:
				toMembers(msg.channel, com);
				break;
			case STAFF:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(msg.channel.isStaffOrManager(ply)) LandDev.sendTo(com, ply);
				}
				break;
		}
	}

	private void toMembers(Channel channel, TagCW com){
		for(UniEntity player : WrapperHolder.getPlayers()){
			LDPlayer ply = ResManager.getPlayer(player);
			if(channel.isMember(ply)) LandDev.sendTo(com, ply);
		}
	}

	@Override
	public boolean internal(){
		return true;
	}

	@Override
	public TransmitterType type(){
		return TransmitterType.INTERNAL;
	}
	
}
