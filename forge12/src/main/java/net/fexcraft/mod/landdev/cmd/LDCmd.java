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
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
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
					cf = chunk.district.region().norms.get("new-county-fee").integer();
					Print.chat(sender, TranslationUtil.translateCmd("fees_county"));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_server", getWorthAsString(sf)));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_region", getWorthAsString(cf)));
					Print.chat(sender, TranslationUtil.translateCmd("fees_ct_total", getWorthAsString(sf + cf)));
					sf = LDConfig.REGION_CREATION_FEE;
					Print.chat(sender, TranslationUtil.translateCmd("fees_region"));
					Print.chat(sender, TranslationUtil.translateCmd("fees_rg_server", getWorthAsString(sf)));
					Print.chat(sender, TranslationUtil.translateCmd("fees_rg_total", getWorthAsString(sf)));
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
	    			player.entity.openUI(LDKeys.IMG_VIEW, w, h, 0);
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
				case "polyclaim":{
					if(!player.adm) return;
					switch(args[1]){
						case "district":{
							int did = Integer.parseInt(args[2]);
							PolyClaim.setDis(player.uuid, did);
							District dis = ResManager.getDistrict(did);
							player.entity.send("landdev.cmd.polyclaim.district", dis.name(), dis.id);
							break;
						}
						case "select":{
							int am = PolyClaim.selCnk(player.uuid, ResManager.getChunk(player.entity.getPos()));
							player.entity.send("landdev.cmd.polyclaim.selected", am);
							break;
						}
						case "status":{
							player.entity.send("[LD] === === ===");
							player.entity.send("landdev.cmd.polyclaim.status.title");
							PolyClaim.PolyClaimObj obj = PolyClaim.get(player.uuid);
							District dis = ResManager.getDistrict(obj.district);
							if(dis.id < 0){
								player.entity.send("landdev.cmd.polyclaim.status.district", "AUTO", "-1/" + player.chunk_current.district.id);
							}
							else{
								player.entity.send("landdev.cmd.polyclaim.status.district", dis.name(), dis.id);
							}
							player.entity.send("landdev.cmd.polyclaim.status.chunks");
							for(ChunkKey key : obj.chunks){
								player.entity.send("- " + key.comma());
							}
							player.entity.send("landdev.cmd.polyclaim.status.mode", obj.chunks.size() < 2 ? "PASS" : obj.chunks.size() == 2 ? "QUAD" : "POLYGON");
							break;
						}
						case "clear":{
							PolyClaim.clear(player.uuid);
							player.entity.send("landdev.cmd.polyclaim.cleared");
							break;
						}
						case "start":{
							player.entity.send("landdev.cmd.polyclaim.starting");
							int[] res = PolyClaim.process(player.uuid, player.chunk_current.district);
							player.entity.send("landdev.cmd.polyclaim.finished", res[0], res[1]);
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
