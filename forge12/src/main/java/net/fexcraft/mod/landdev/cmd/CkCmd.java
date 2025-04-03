package net.fexcraft.mod.landdev.cmd;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.world.EntityW;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CkCmd extends CommandBase {
	
    public CkCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("ck");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("ck");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	EntityPlayer player = (EntityPlayer)sender;
		EntityW ent = UniEntity.getEntity(player);
		Chunk_ chunk = ResManager.getChunkP(player);
    	if(args.length > 0){
			if(args[0].equals("claim")){
				int dis = args.length > 1 ? Integer.parseInt(args[1]) : chunk.district.id;
				ent.openUI(LDKeys.CHUNK_CLAIM, chunk.key.x, dis, chunk.key.z);
			}
			else if(args[0].equals("transfer")){
				int dis = args.length > 1 ? Integer.parseInt(args[1]) : chunk.district.id;
				ent.openUI(LDKeys.CHUNK_TRANSFER, chunk.key.x, dis, chunk.key.z);
			}
			else if(args[0].equals("sell")){
				int dis = args.length > 1 ? Integer.parseInt(args[1]) : chunk.district.id;
				ent.openUI(LDKeys.CHUNK_SELL, chunk.key.x, dis, chunk.key.z);
			}
			else if(args[0].equals("buy")){
				int dis = chunk.district.id;
				if(args.length > 1){
					switch(args[1]){
						case "self":
						case "player":
							dis = -1;
							break;
						case "company":
						case "com":
							dis = -2;
							break;
						case "region":
						case "reg":
							dis = -3;
							break;
					}
					if(dis >= 0) dis =Integer.parseInt(args[0]);
				}
				ent.openUI(LDKeys.CHUNK_BUY, chunk.key.x, dis, chunk.key.z);
			}
			else if(args[0].equals("map")){
				String marker = null;
				Chunk_ ck = null;
				int r = 9, rm = 4;
				for(int i = 0; i < r; i++){
					String str = "&0|";
					for(int j = 0; j < r; j++){
						int x = (chunk.key.x - rm) + j;
						int z = (chunk.key.z - rm) + i;
						marker = x == chunk.key.x && z == chunk.key.z ? "+" : "#";
						ck = ResManager.getChunk(x, z);
						str += (ck == null ? "&4" : ck.district.id >= 0 ? "&9" : "&2") + marker;
					}
					Print.chat(sender, str + "&0|");
				}
				Print.chat(sender, TranslationUtil.translateCmd("chunk.mapdesc"));
			}
    	}
    	else{
			ent.openUI(LDKeys.CHUNK, 0, chunk.key.x, chunk.key.z);
    	}
    }

}
