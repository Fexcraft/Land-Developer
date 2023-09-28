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
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.relauncher.Side;

public class LDGuiClaim extends GenericGui<LDGuiClaimCon> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/claim.png");
	private static ArrayList<String> info = new ArrayList<>();
	private static Button[][] ckbuttons = new Button[15][15];
	private static BasicButton mm, gm;
	private static Integer lx, lz;
	protected static BasicText title;
	private boolean gridview = true;
	private boolean deltex;

	public LDGuiClaim(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiClaimCon(player, x, y, z), player);
		xSize = 206;
		ySize = 220;
		ChunkPos pos = new ChunkPos(player.getPosition());
		if(lx != null && (pos.x != lx || pos.z != lz)) deltex = true;
		lx = pos.x;
		lz = pos.z;
	}
	
	@Override
	public void init(){
		texts.put("title", title = new BasicText(guiLeft + 8, guiTop + 8, 165, 0x0e0e0e, "landdev.gui.claim.title").hoverable(true).autoscale().translate());
		for(int i = 0; i < ckbuttons.length; i++){
			for(int k = 0; k < ckbuttons[i].length; k++){
				buttons.put(i + "_" + k, ckbuttons[i][k] = new Button(i + "_" + k, guiLeft + 6 + i * 13, guiTop + 20 + k * 13, 6, 20));
			}
		}
		buttons.put("mapmode", mm = new BasicButton("mm", guiLeft + 203, guiTop + 20, 203, 20, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				if(deltex){
					mc.renderEngine.deleteTexture(ClaimMapTexture.temptexid);
					deltex = false;
				}
				gridview = false;
				return true;
			}
		});
		buttons.put("gridmode", gm = new BasicButton("gm", guiLeft + 203, guiTop + 33, 203, 33, 12, 12, true){
			@Override
			public boolean onclick(int mx, int my, int mb){
				gridview = true;
				return true;
			}
		});
		container.gui = this;
	}

	@Override
	public void drawbackground(float ticks, int mx, int my){
		drawTexturedModalRect(guiLeft + 206, guiTop + 14, 206, 14, 15, 37);
		if(!gridview){
			ClaimMapTexture.bind(mc, lx, lz);
			for(int i = 0; i < container.chunks.length; i++){
				for(int k = 0; k < container.chunks[i].length; k++){
					drawTexturedModalRect(guiLeft + 6 + (i * 13), guiTop + 20 + (k * 13), i * 16, k * 16, 12, 12);
				}
			}
		}
		else{
			for(int i = 0; i < container.chunks.length; i++){
				for(int k = 0; k < container.chunks[i].length; k++){
					container.chunks[i][k].color.glColorApply();
					drawTexturedModalRect(guiLeft + 6 + (i * 13), guiTop + 20 + (k * 13), 6, 20, 12, 12);
					RGB.glColorReset();
				}
			}
		}
		mc.renderEngine.bindTexture(texloc);
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		info.clear();
		for(int i = 0; i < ckbuttons.length; i++){
			for(int k = 0; k < ckbuttons[i].length; k++){
				if(ckbuttons[i][k].hovered){
					ChunkData ck = container.chunks[i][k];
					info.add(Formatter.format(I18n.format("landdev.gui.claim.chunk_coord", ck.x, ck.z)));
					if(ck.price > 0) info.add(Formatter.format(I18n.format("landdev.gui.claim.chunk_price", Config.getWorthAsString(ck.price))));
					DisData dis = container.dists.get(ck.dis);
					if(dis == null) return;
					info.add(Formatter.format(I18n.format("landdev.gui.claim.district", dis.name)));
					info.add(Formatter.format(I18n.format("landdev.gui.claim." + (dis.county ? "county" : "municipality"), dis.cname)));
				}
			}
		}
		if(title.hovered) info.add(title.string);
		if(gm.hovered) info.add(I18n.format("landdev.gui.claim.gridmode"));
		if(mm.hovered) info.add(I18n.format("landdev.gui.claim.mapmode"));
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