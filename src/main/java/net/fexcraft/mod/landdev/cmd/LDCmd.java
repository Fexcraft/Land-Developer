package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;
import static net.fexcraft.mod.landdev.LandDev.CLIENT_RECEIVER_ID;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.List;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.*;
import net.fexcraft.mod.landdev.util.broad.DiscordTransmitter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
	        		Print.chat(sender, TranslationUtil.translateCmd("fees"));
	        		long sf = LDConfig.MUNICIPALITY_CREATION_FEE;
	        		long cf = chunk.district.county().norms.get("new-municipality-fee").integer();
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_municipality"));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_mun_server", getWorthAsString(sf)));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_mun_county", getWorthAsString(cf)));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_mun_total", getWorthAsString(sf + cf)));
					sf = LDConfig.COUNTY_CREATION_FEE;
					cf = chunk.district.state().norms.get("new-county-fee").integer();
					Print.chat(sender, TranslationUtil.translateCmd("fees_county"));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_server", getWorthAsString(sf)));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_state", getWorthAsString(cf)));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_total", getWorthAsString(sf + cf)));
	        		return;
	    		}
	    		case "admin":{
	    			if(server.isSinglePlayer() || Static.isOp((EntityPlayer)player.entity.direct())){
	    				player.adm = !player.adm;
		        		Print.chat(sender, TranslationUtil.translateCmd("adminmode." + player.adm));
	    			}
	    			else{
	    				Print.chat(sender, "&cno.permission");
	    			}
	        		return;
	    		}
	    		case "reload":{
	    			Protector.load();
	        		Print.chat(sender, TranslationUtil.translateCmd("reload", "landdev-interaction.json"));
	        		DiscordTransmitter.restart();
	        		Print.chat(sender, TranslationUtil.translateCmd("reload", "discord-bot-integration"));
	        		Print.chat(sender, TranslationUtil.translateCmd("reload.complete"));
	    			return;
	    		}
	    		case "img":{
	    			int w = args.length > 2 ? Integer.parseInt(args[2]) : 256;
	    			int h = args.length > 3 ? Integer.parseInt(args[3]) : 256;
	    			player.openGui(LDKeys.IMG_PREVIEW, w, h, 0);
	    			NBTTagCompound com = new NBTTagCompound();
	    			com.setString("target_listener", CLIENT_RECEIVER_ID);
	    			com.setString("task", "img_preview_url");
	    			com.setString("url", args[1]);
	    			PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(com), (EntityPlayerMP)player.entity);
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
						mail.message.add("Example Mail Text 1");
						mail.message.add("Example Mail Text 2");
						mail.message.add("Example Mail Text 3");
						mail.message.add("Example Mail Text 4");
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
    			case "help":
    			default:{
	        		Print.chat(sender, "&0[&bLD&0]&6>>&2===========");
	        		Print.chat(sender, "/ld (UI)");
	        		Print.chat(sender, "/ld help");
	        		Print.chat(sender, "/ld admin");
	        		Print.chat(sender, "/ld fees");
	        		Print.chat(sender, "/ld reload");
	        		Print.chat(sender, "/ld force-tax");
    				return;
    			}
    		}
    	}
		player.entity.openUI(LDKeys.KEY_MAIN, new V3I(0, (int)player.entity.getPos().x >> 4, (int)player.entity.getPos().z >> 4));
    }

}
