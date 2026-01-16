package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.LDN.PKT_RECEIVER_ID;

import java.util.List;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.fexcraft.mod.uni.impl.PacketTagHandler;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.WrapperHolder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class LDCmd extends CommandBase {
	
    public LDCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("landdev");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("landdev");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	LDPlayer player = ResManager.getPlayer(sender);
    	if(args.length > 0){
    		switch(args[0]){
	    		case "fees":{
	    			Chunk_ chunk = ResManager.getChunk(player.entity);
	        		player.entity.send("landdev.cmd.fees");
	        		long sf = LDConfig.PROPERTY_CREATION_FEE;
	        		long cf = chunk.district.norms.get("new-property-fee").integer();
	        		player.entity.send("landdev.cmd.fees_property");
	        		player.entity.send("landdev.cmd.fees_prop_server", getWorthAsString(sf));
	        		player.entity.send("landdev.cmd.fees_prop_district", getWorthAsString(cf));
	        		player.entity.send("landdev.cmd.fees_prop_total", getWorthAsString(sf + cf));
	        		sf = LDConfig.MUNICIPALITY_CREATION_FEE;
	        		cf = chunk.district.county().norms.get("new-municipality-fee").integer();
	        		player.entity.send("landdev.cmd.fees_municipality");
	        		player.entity.send("landdev.cmd.fees_mun_server", getWorthAsString(sf));
	        		player.entity.send("landdev.cmd.fees_mun_county", getWorthAsString(cf));
	        		player.entity.send("landdev.cmd.fees_mun_total", getWorthAsString(sf + cf));
					sf = LDConfig.COUNTY_CREATION_FEE;
					cf = chunk.district.region().norms.get("new-county-fee").integer();
					player.entity.send("landdev.cmd.fees_county");
					player.entity.send("landdev.cmd.fees_ct_server", getWorthAsString(sf));
					player.entity.send("landdev.cmd.fees_ct_region", getWorthAsString(cf));
					player.entity.send("landdev.cmd.fees_ct_total", getWorthAsString(sf + cf));
					sf = LDConfig.REGION_CREATION_FEE;
					player.entity.send("landdev.cmd.fees_region");
					player.entity.send("landdev.cmd.fees_rg_server", getWorthAsString(sf));
					player.entity.send("landdev.cmd.fees_rg_total", getWorthAsString(sf));
	        		return;
	    		}
	    		case "admin":{
	    			if(server.isSinglePlayer() || WrapperHolder.isOp(player.entity)){
	    				player.adm = !player.adm;
		        		player.entity.send("landdev.cmd.adminmode." + player.adm);
	    			}
	    			else{
	    				player.entity.send("&cno.permission");
	    			}
	        		return;
	    		}
	    		case "reload":{
	    			Protector.load();
	        		player.entity.send("landdev.cmd.reload", "landdev-interaction.json");
	        		DiscordTransmitter.restart();
	        		player.entity.send("landdev.cmd.reload", "discord-bot-integration");
	        		player.entity.send("landdev.cmd.reload.complete");
	    			return;
	    		}
	    		case "img":{
	    			int w = args.length > 2 ? Integer.parseInt(args[2]) : 256;
	    			int h = args.length > 3 ? Integer.parseInt(args[3]) : 256;
	    			player.entity.openUI(LDKeys.IMG_VIEW, w, h, 0);
	    			TagCW com = TagCW.create();
	    			com.set("task", "img_preview_url");
	    			com.set("url", args[1]);
	    			PacketHandler.getInstance().sendTo(new PacketTagHandler.I12_PacketTag(PKT_RECEIVER_ID, com), (EntityPlayerMP)player.entity);
	    			return;
	    		}
				case "bulkmail":{
					if(!player.adm) return;
					/*Chunk_ ck = ResManager.getChunk(player.entity);
					District dis = ck.district;
					for(MailType type : MailType.values()){
						Mail mail = new Mail();
						mail.expiry = Time.getDate() + Time.DAY_MS;
						mail.from = Layers.NONE;
						mail.fromid = "SYSTEM";
						mail.receiver = dis.getLayer() + "_" + dis.lid;
						mail.unread = true;
						mail.title = "Bulk mail from /ld bulkmail";
						mail.message.add("Example Mail Text 1");
						mail.message.add("Example Mail Text 2");
						mail.message.add("Example Mail Text 3");
						mail.message.add("Example Mail Text 4");
						mail.type = type;
						dis.mail.mails.add(mail);
					}*/
					for(MailType type : MailType.values()){
						Mail mail = new Mail();
						mail.expiry = Time.getDate() + Time.DAY_MS;
						mail.from = Layers.NONE;
						mail.fromid = "SYSTEM";
						mail.receiver = Layers.PLAYER;
						mail.recid = player.uuid.toString();
						mail.unread = true;
						mail.title = "Bulk mail from /ld bulkmail";
						mail.addMessage("Example Mail Text 1");
						mail.addMessage("Example Mail Text 2");
						mail.addMessage("Example Mail Text 3");
						mail.addMessage("Example Mail Text 4");
						mail.type = type;
						mail.staff = type == MailType.INVITE;
						player.mail.mails.add(mail);
					}
					return;
				}
				case "force-tax":{
					if(!player.adm) return;
					TaxSystem.INSTANCE.collect(Time.getDate(), true);
					return;
				}
				case "polyclaim":{
					if(!player.adm) return;
					switch(args[1]){
						case "district":{
							int did = Integer.parseInt(args[2]);
							PolyClaim.setDis(player, did);
							break;
						}
						case "select":{
							PolyClaim.selCnk(player, ResManager.getChunk(player.entity.getPos()));
							break;
						}
						case "status":{
							PolyClaim.status(player);
							break;
						}
						case "clear":{
							PolyClaim.clear(player);
							break;
						}
						case "start":{
							PolyClaim.process(player);
							break;
						}
					}
				}
    			case "help":
    			default:{
					player.entity.send("\u00A70[\u00A7bLD\u00A70]\u00A76>>\u00A72===========");
					player.entity.send("/ld (UI)");
					player.entity.send("/ld help");
					player.entity.send("/ld admin");
					player.entity.send("/ld fees");
					player.entity.send("/ld reload");
					player.entity.send("/ld force-tax");
					player.entity.send("PolyClaim (Admin)");
					player.entity.send("/ld polyclaim district <dis-id>");
					player.entity.send("/ld polyclaim select");
					player.entity.send("/ld polyclaim status");
					player.entity.send("/ld polyclaim clear");
					player.entity.send("/ld polyclaim start");
    				return;
    			}
    		}
    	}
		player.entity.openUI(LDKeys.MAIN, new V3I(0, (int)player.entity.getPos().x >> 4, (int)player.entity.getPos().z >> 4));
    }

}
