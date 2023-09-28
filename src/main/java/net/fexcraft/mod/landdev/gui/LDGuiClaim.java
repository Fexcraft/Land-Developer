package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.gui.LDGuiClaimCon.ChunkData;
import net.fexcraft.mod.landdev.gui.LDGuiClaimCon.DisData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class LDGuiClaim extends GenericGui<LDGuiClaimCon> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/claim.png");
	private static ArrayList<String> info = new ArrayList<>();
	private static Button[][] ckbuttons = new Button[15][15];
	protected static BasicText title;

	public LDGuiClaim(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiClaimCon(player, x, y, z), player);
		xSize = 206;
		ySize = 220;
	}
	
	@Override
	public void init(){
		texts.put("title", title = new BasicText(guiLeft + 8, guiTop + 8, 165, 0x0e0e0e, "landdev.gui.claim.title").hoverable(true).autoscale().translate());
		for(int i = 0; i < ckbuttons.length; i++){
			for(int k = 0; k < ckbuttons[i].length; k++){
				buttons.put(i + "_" + k, ckbuttons[i][k] = new Button(i + "_" + k, guiLeft + 6 + i * 13, guiTop + 20 + k * 13, 6, 20));
			}
		}
		container.gui = this;
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		for(int i = 0; i < container.chunks.length; i++){
			for(int k = 0; k < container.chunks[i].length; k++){
				container.chunks[i][k].color.glColorApply();
				drawTexturedModalRect(guiLeft + 6 + (i * 13), guiTop + 20 + (k * 13), 6, 20, 12, 12);
				RGB.glColorReset();
			}
		}
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		info.clear();
		for(int i = 0; i < ckbuttons.length; i++){
			for(int k = 0; k < ckbuttons[i].length; k++){
				if(ckbuttons[i][k].hovered){
					ChunkData ck = container.chunks[i][k];
					info.add(Formatter.format(I18n.format("landdev.gui.claim.chunk_coord", ck.x, ck.z)));
					info.add(Formatter.format(I18n.format("landdev.gui.claim.chunk_price", ck.price == 0 ? "&c---" : Config.getWorthAsString(ck.price))));
					DisData dis = container.dists.get(ck.dis);
					if(dis == null) return;
					info.add(Formatter.format(I18n.format("landdev.gui.claim.district", dis.name)));
					info.add(Formatter.format(I18n.format("landdev.gui.claim." + (dis.county ? "county" : "municipality"), dis.cname)));
				}
			}
		}
		if(title.hovered) info.add(title.string);
		if(info.size() > 0) drawHoveringText(info, mouseX, mouseY);
	}

	public static class Button extends BasicButton {

		public Button(String name, int x, int y, int tx, int ty){
			super(name, x, y, tx, ty, 12, 12, true);
		}

		public void draw(GenericGui<?> gui, float pticks, int mouseX, int mouseY){
			if(!hovered) return;
			rgb_hover.glColorApply();
            gui.drawTexturedModalRect(x, y, tx, ty, sizex, sizey);
            RGB.glColorReset();
		}
    	
    }
	
	@Override
	public boolean buttonClicked(int mx, int my, int mb, String key, BasicButton button){
		for(int i = 0; i < ckbuttons.length; i++){
			for(int k = 0; k < ckbuttons[i].length; k++){
				if(ckbuttons[i][k].name.equals(key)){
					NBTTagCompound compound = new NBTTagCompound();
					compound.setIntArray("claim", new int[]{ i, k });
					container.send(Side.SERVER, compound);
					return true;
				}
			}
		}
		return false;
	}
	
}