package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.landdev.data.MailType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class LDGuiMailbox extends GenericGui<LDGuiMailboxCon> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/mail.png");
	private static ArrayList<String> info = new ArrayList<>();
	protected static BasicText title;
	protected static BasicText[] titles = new BasicText[10];
	protected static BasicButton prev;
	protected static BasicButton next;
	protected static BasicButton[] read = new BasicButton[10];
	protected static BasicButton[] del = new BasicButton[10];
	private int page;

	public LDGuiMailbox(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiMailboxCon(player, x, y, z), player);
		xSize = 245;
		ySize = 164;
	}
	
	@Override
	public void init(){
		texts.put("title", title = new BasicText(guiLeft + 8, guiTop + 8, 165, 0x0e0e0e, "landdev.gui.mailbox.title").hoverable(true).autoscale().translate(page));
		buttons.put("prev", prev = new BasicButton("prev", guiLeft + 185, guiTop + 163, 185, 163, 11, 11, true){
			@Override
			public boolean onclick(int mouseX, int mouseY, int mouseButton){
				page--;
				if(page < 0) page = 0;
				title.string = "landdev.gui.mailbox.title";
				title.translate(page);
				return true;
			}
		});
		buttons.put("next", next = new BasicButton("prev", guiLeft + 198, guiTop + 163, 198, 163, 11, 11, true){
			@Override
			public boolean onclick(int mouseX, int mouseY, int mouseButton){
				page++;
				title.string = "landdev.gui.mailbox.title";
				title.translate(page);
				return true;
			}
		});
		for(int i = 0; i < 10; i++){
			int j = i;
			buttons.put("read" + i, read[i] = new BasicButton("read", guiLeft + 214, guiTop + 20 + i * 14, 214, 164, 12, 12, true){
				@Override
				public boolean onclick(int mouseX, int mouseY, int mouseButton){
					NBTTagCompound com = new NBTTagCompound();
					com.setInteger("read", j + page * 10);
					container.send(Side.SERVER, com);
					return true;
				}
			});
			buttons.put("del" + i, del[i] = new BasicButton("del", guiLeft + 228, guiTop + 20 + i * 14, 228, 164, 12, 12, true){
				@Override
				public boolean onclick(int mouseX, int mouseY, int mouseButton){
					NBTTagCompound com = new NBTTagCompound();
					com.setInteger("delete", j + page * 10);
					container.send(Side.SERVER, com);
					return true;
				}
			});
			texts.put("title" + i, titles[i] = new BasicText(guiLeft + 26, guiTop + 22 + i * 14, 184, 0x0e0e0e, "...").hoverable(true).autoscale());
		}
		container.gui = this;
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sync", true);
		container.send(Side.SERVER, com);
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(container.mailbox == null) return;
		for(int i = 0; i < 10; i++){
			boolean bool = i + page * 10 < container.mailbox.mails.size();
			read[i].visible = del[i].visible = bool;
			titles[i].string = bool ? container.mailbox.mails.get(i).title : "";
		}
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		if(container.mailbox == null) return;
		drawTexturedModalRect(guiLeft + 182, guiTop + 164, 182, 164, 30, 13);
		for(int i = 0; i < 10; i++){
			int j = i + page * 10;
			if(j >= container.mailbox.mails.size()) break;
			MailType type = container.mailbox.mails.get(j).type;
			if(type == MailType.INVITE && container.mailbox.mails.get(j).expired()) type = MailType.EXPIRED;
			boolean unread = container.mailbox.mails.get(j).unread;
			drawTexturedModalRect(guiLeft + 6, guiTop + 21 + (i * 14), unread ? type.u_unread : type.u_read, unread ? type.v_unread : type.v_read, 16, 10);
		}
	}
	
	@Override
	protected void drawlast(float pticks, int mx, int my){
		if(container.mailbox == null) return;
		info.clear();
		if(title.hovered) info.add(title.string);
		if(prev.hovered) info.add(I18n.format("landdev.gui.mailbox.prev"));
		if(next.hovered) info.add(I18n.format("landdev.gui.mailbox.next"));
		for(int i = 0; i < 10; i++){
			if(read[i].visible && read[i].hovered(mx, my)) info.add(I18n.format("landdev.gui.mailbox.read"));
			if(del[i].visible && del[i].hovered(mx, my)) info.add(I18n.format("landdev.gui.mailbox.delete"));
			if(titles[i].hovered(mx, my)) info.add(titles[i].string);
		}
		if(info.size() > 0) drawHoveringText(info, mx, my);
	}
	
}