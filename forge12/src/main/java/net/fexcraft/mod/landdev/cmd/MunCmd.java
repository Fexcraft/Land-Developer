package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;

import java.util.List;

import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;

public class MunCmd extends CommandBase {
	
    public MunCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("mun");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("mun");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	LDPlayer ply = ResManager.getPlayer((EntityPlayer)sender);
		Chunk_ chunk = ResManager.getChunkP(sender.getCommandSenderEntity());
    	if(args.length > 0){
    		switch(args[0]){
	    		case "create":{
	    			County county = chunk.district.county();
	    			boolean cn = county.norms.get("new-municipalities").bool();
	    			boolean pp = ply.hasPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id);
	    			if(!cn && !pp){
		    			ply.entity.send("landdev.cmd.mun.no_new_municipalities");
		    			ply.entity.send("landdev.cmd.mun.no_create_permit");
	    			}
	    			else{
	    				ply.entity.openUI(LDKeys.MUNICIPALITY, Municipality.UI_CREATE, 0, 0);
	    			}
	    			return;
	    		}
				case "center":{
					if(chunk.district.municipality() == null){
						ply.entity.send("landdev.cmd.mun.not_in_a_municipality");
						return;
					}
					Municipality mun = chunk.district.municipality();
					if(!mun.manage.can(PermAction.MANAGE_MUNICIPALITY, ply.uuid) && !ply.adm){
						ply.entity.send("no perm");
						return;
					}
					int min = Math.max(LDConfig.MIN_MUN_DIS, mun.county.norms.get("min-municipality-distance").integer());
					if(min < LDConfig.MIN_MUN_DIS) min = LDConfig.MIN_MUN_DIS;
					Pair<Integer, Double> dis = ResManager.disToNearestMun(chunk.key, mun.id);
					if(dis.getLeft() >= 0 && dis.getRight() < min){
						ply.entity.send("landdev.cmd.mun.center_too_close", ResManager.getMunicipality(dis.getLeft(), true).name(), dis.getLeft());
					}
					else{
						ResManager.MUN_CENTERS.put(mun.id, chunk.key);
						ply.entity.openUI(LDKeys.MUNICIPALITY, 0, mun.id, 0);
					}
					return;
				}
	    		default:{
	    			ply.entity.send("landdev.cmd.unknown_argument");
	    			return;
	    		}
    		}
    	}
    	else{
    		if(chunk.district.municipality() == null){
    			ply.entity.send("landdev.cmd.mun.not_in_a_municipality");
    			return;
    		}
        	ply.entity.openUI(LDKeys.MUNICIPALITY, 0, chunk.district.municipality().id, 0);
    	}
    }

}
