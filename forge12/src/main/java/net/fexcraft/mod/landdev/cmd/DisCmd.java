package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DisCmd extends CommandBase {
	
    public DisCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("dis");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("dis");
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
					ply.entity.openUI(LDKeys.DISTRICT, District.UI_CREATE, 0, 0);
					return;
				}
				default:{
					Print.chat(sender, translateCmd("unknown_argument"));
					return;
				}
			}
		}
		else{
			ply.entity.openUI(LDKeys.DISTRICT, 0, chunk.district.id, 0);
		}

    }

}
