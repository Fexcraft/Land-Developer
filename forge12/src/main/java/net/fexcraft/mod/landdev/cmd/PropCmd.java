package net.fexcraft.mod.landdev.cmd;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.prop.Property;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class PropCmd extends CommandBase {

    public PropCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("prop");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("prop");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
		LDPlayer ply = ResManager.getPlayer(sender);
		Property prop = ResManager.getProperty(ply.entity.getV3I());
		if(args.length > 0){
			switch(args[0]){
				case "create":{
					if(ply.defcache != null){
						ply.entity.openUI(LDKeys.DEF_SPACE, ply.defcache.pos);
					}
					else{
						ply.entity.openUI(LDKeys.PROPERTY, LDUIModule.UI_CREATE, -1, 0);
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
			if(prop == null){
				ply.entity.send("landdev.cmd.no_property_at_pos");
				return;
			}
			ply.entity.openUI(LDKeys.PROPERTY, 0, prop.id, 0);
		}

    }

}
