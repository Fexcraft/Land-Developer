package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class LDGuiMailbox extends GenericGui<LDGuiMailboxCon> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/mail.png");
	private static ArrayList<String> info = new ArrayList<>();
	protected static BasicText title;

	public LDGuiMailbox(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiMailboxCon(player, x, y, z), player);
		xSize = 245;
		ySize = 164;
	}
	
	@Override
	public void init(){
		texts.put("title", title = new BasicText(guiLeft + 8, guiTop + 8, 165, 0x0e0e0e, "landdev.gui.mailbox.title").hoverable(true).autoscale().translate());
		//
		container.gui = this;
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		for(int i = 0; i < 10; i++){

		}
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		info.clear();
		//
		if(title.hovered) info.add(title.string);
		if(info.size() > 0) drawHoveringText(info, mouseX, mouseY);
	}
	
}