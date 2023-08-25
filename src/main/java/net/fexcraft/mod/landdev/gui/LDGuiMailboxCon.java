package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LDGuiMailboxCon extends GenericContainer {

	protected MailData mailbox;
	public final int x, y, z;
	@SideOnly(Side.CLIENT)
	public LDGuiMailbox gui;

	public LDGuiMailboxCon(EntityPlayer player, int x, int y, int z){
		super(player);
		this.x = x;
		this.y = y;
		this.z = z;
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: mailbox = ResManager.getPlayer(player).mail; break;
			case DISTRICT: mailbox = ResManager.getDistrict(y, false).mail; break;
			case MUNICIPALITY: mailbox = ResManager.getMunicipality(y, false).mail; break;
			case COUNTY: mailbox = ResManager.getCounty(y, false).mail; break;
			case STATE: mailbox = ResManager.getState(y, false).mail; break;
		}
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side.isServer()){
			//
		}
		else{
			//
		}
	}
	
}