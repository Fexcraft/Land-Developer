package net.fexcraft.mod.landdev.gui;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.mod.landdev.LandDev;
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
		if(player.world.isRemote) return;
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
			if(packet.getBoolean("sync")){
				sendSync();
				return;
			}
			else if(packet.hasKey("read")){
				mailbox.mails.get(packet.getInteger("read")).unread = false;
				player.openGui(LandDev.INSTANCE, GuiHandler.MAIL, player.world, x, y, packet.getInteger("read"));
				return;
			}
			else if(packet.hasKey("delete")){
				mailbox.mails.remove(packet.getInteger("delete"));
				sendSync();
				return;
			}
		}
		else{
			if(packet.hasKey("mails")){
				JsonMap map = JsonHandler.parse(packet.getString("mails"), true).asMap();
				mailbox = new MailData(Layers.values()[x], "client");
				mailbox.load(map);
				return;
			}
		}
	}

	private void sendSync(){
		JsonMap map = new JsonMap();
		mailbox.save(map);
		NBTTagCompound sync = new NBTTagCompound();
		sync.setString("mails", JsonHandler.toString(map, PrintOption.FLAT));
		send(Side.CLIENT, sync);
	}

}