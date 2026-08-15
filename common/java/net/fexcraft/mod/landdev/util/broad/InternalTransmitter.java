package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.fexcraft.mod.landdev.util.broad.Channel.SubChannelType;
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
		if(msg.channel.type.ordinal() < 2){
			LandDev.sendToAll(com);
		}
		else{
			for(UniEntity player : WrapperHolder.getPlayers()){
				LDPlayer ply = ResManager.getPlayer(player);
				if(shouldSend(msg.channel, ply)) LandDev.sendTo(com, ply);
			}
		}
	}

	private boolean shouldSend(Channel channel, LDPlayer ply){
		if(channel.sub == SubChannelType.ALL || channel.sub == SubChannelType.INCHUNK){
			switch(channel.type){
				case REGION: if(ply.isCurrentlyInRegion(channel.layer.lid())) return true; break;
				case COUNTY: if(ply.isCurrentlyInCounty(channel.layer.lid())) return true; break;
				case MUNICIPALITY: if(ply.isCurrentlyInMunicipality(channel.layer.lid())) return true; break;
				case DISTRICT: if(ply.isCurrentlyInDistrict(channel.layer.lid())) return true; break;
				case COMPANY: break;
			}
		}
		if(channel.sub == SubChannelType.ALL || channel.sub == SubChannelType.MEMBER){
			if(channel.isMember(ply)) return true;
		}
		if(channel.sub == SubChannelType.STAFF){
			if(channel.isStaffOrManager(ply)) return true;
		}
		return false;
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
