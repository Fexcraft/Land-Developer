package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class LDGuiBase extends GenericGui<LDGuiContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/guibase.png");
	private static ArrayList<String> info = new ArrayList<>();
	private ArrayList<LDGuiElement> elements = new ArrayList<>();
	protected BasicText title;

	public LDGuiBase(int id, EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiContainer(player, id, x, y, z), player);
		deftexrect = false;
		sizeOf(0);
	}
	
	@Override
	public void init(){
		texts.put("title", title = new BasicText(guiLeft + 8, guiTop + 8, 196, 0x0e0e0e, "landdev.gui.loading.title").hoverable(true).autoscale().translate());
		container.gui = this;
		reqsync();
	}

	private void reqsync(){
		NBTTagCompound com = new NBTTagCompound();
		com.setBoolean("sync", true);
		container.send(Side.SERVER, com);
	}

	protected void addElm(String id, LDGuiElementType elm, LDGuiElementType icon, int idx, boolean text, boolean button, boolean field, String val){
		if(!field) elements().add(new LDGuiElement(id + "_elm", elm).pos(idx, 0).text(this, text ? id : null, val));
		else elements().add(new LDGuiElement(id + "_elm", elm).pos(idx, 0).field(this, val, icon == LDGuiElementType.ICON_BLANK));
		elements().add(new LDGuiElement(id, icon).pos(idx, 1).button(this, button));
	}

	protected void sizeOf(int elms){
		ySize = 25 + (elms * 14);
		xSize = 224;
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		drawElement(LDGuiElementType.TOP, 0);
		for(LDGuiElement elm : elements()){
			drawElement(elm.type, elm.pos);
		}
		drawElement(LDGuiElementType.BOTTOM, ySize - 6);
	}
	
	private void drawElement(LDGuiElementType elm, int y){
		drawTexturedModalRect(guiLeft + elm.x, guiTop + y, elm.x, elm.y, elm.w, elm.h);
	}

	protected void add(String id, BasicText text){
		texts.put(id, text);
	}

	protected void add(BasicButton button){
		buttons.put(button.name, button);
	}

	protected void add(String index, TextField field){
		fields.put(index, field);
	}

	protected String prefix(){
		return container.prefix;
	}

	protected LDGuiContainer container(){
		return container;
	}

	public boolean hasField(String id){
		return fields.containsKey(id);
	}

	public String getField(String id){
		return fields.get(id).getText();
	}

	public void setField(String id, String val){
		fields.get(id).setText(val);
	}

	public void clear(){
		buttons.clear();
		fields.clear();
		texts.clear();
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		info.clear();
		for(BasicText text : texts.values()){
			if(text.hovered) info.add(text.string);
		}
		if(container.checkboxes.size() > 0){
			BasicButton button = null;
			for(LDGuiElement elm : elements()){
				if(!elm.type.is_checkbox()) continue;
				if((button = buttons.get(elm.index)) == null || !button.hovered) continue;
				info.add(Formatter.format(I18n.format(elm.type.translation())));
			}
		}
		if(info.size() > 0) drawHoveringText(info, mouseX, mouseY);
	}

	public ArrayList<LDGuiElement> elements(){
		return elements;
	}

	public FontRenderer fontrenderer(){
		return fontRenderer;
	}
	
}