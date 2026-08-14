package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.world.WrapperHolder;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class InternalTransmitter implements Transmitter {

	@Override
	public void transmit(Channel channel, String sender, String message, String tint, Object[] args){
		if(channel == Channel.CHAT && !LDConfig.CHAT_OVERRIDE) return;
		TagCW com = TagCW.create();
		com.set("task", "chat_message");
		TagCW msg = TagCW.create();
		TagLW lis = TagLW.create();
		msg.set("c", channel.toString());
		if(tint != null) msg.set("t", tint);
		msg.set("s", sender);
		msg.set("m", message);
		if(args.length > 0 && args[0].equals("img")){
			msg.set("i", true);
			lis.add(args[1].toString());
			lis.add(args[2].toString());
			lis.add(args[3].toString());
		}
		else{
			for(Object arg : args) lis.add(arg.toString());
		}
		msg.set("a", lis);
		com.set("msg", msg);
		switch(channel.sub){
			case ALL:
				switch(channel.type){
					case CHAT:
					case SERVER:
						LandDev.sendToAll(com);
						break;
					case REGION:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInRegion(channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(channel, com);
						break;
					case COUNTY:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInCounty(channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(channel, com);
						break;
					case MUNICIPALITY:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInMunicipality(channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						toMembers(channel, com);
						break;
					case DISTRICT:
						for(UniEntity player : WrapperHolder.getPlayers()){
							LDPlayer ply = ResManager.getPlayer(player);
							if(ply.isCurrentlyInDistrict(channel.layer.lid())) LandDev.sendTo(com, ply);
						}
						break;
					case COMPANY:
						toMembers(channel, com);
						break;
				}
				break;
			case MEMBER:
				toMembers(channel, com);
				break;
			case STAFF:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(channel.isStaffOrManager(ply)) LandDev.sendTo(com, ply);
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
