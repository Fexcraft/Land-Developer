package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;
import java.util.TreeMap;

import net.fexcraft.lib.common.math.Time;
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
	private static final ResourceLocation NOTIFICATION = new ResourceLocation("landdev:textures/gui/notification.png");
	private static ArrayList<String> info = new ArrayList<>();
	private ArrayList<LDGuiElement> elements = new ArrayList<>();
	protected BasicText title, notification;
	protected BasicButton notificationbutton;
	protected boolean addscroll, notify;
	protected int scroll;

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

	protected void addElm(String id, LDGuiElementType elm, LDGuiElementType icon, boolean text, boolean button, boolean field, String val){
		if(!field) elements().add(new LDGuiElement(id + "_elm", elm).text(this, text ? id : null, val));
		else elements().add(new LDGuiElement(id, elm).field(this, val, icon == LDGuiElementType.ICON_BLANK));
		elements().add(new LDGuiElement(id, icon).button(this, button));
	}

	protected void sizeOf(int elms){
		ySize = 25 + (elms * 14);
		xSize = 224;
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		drawElement(LDGuiElementType.TOP, guiTop);
		for(LDGuiElement elm : elements()){
			if(elm.visible) drawElement(elm.type, elm.pos);
		}
		if(addscroll){
			LDGuiElementType elm = LDGuiElementType.SCROLLBAR;
			drawTexturedModalRect(guiLeft + elm.x - 5, guiTop + 17, elm.x, elm.y, elm.w, elm.h);
		}
		drawElement(LDGuiElementType.BOTTOM, guiTop + ySize - 6);
		if(notify){
			mc.renderEngine.bindTexture(NOTIFICATION);
			drawTexturedModalRect(guiLeft - 16, guiTop - 24, 0, 0, 256, 22);
			if(Time.getSecond() % 2 == 1) drawTexturedModalRect(guiLeft - 10, guiTop - 19, 6, 23, 6, 12);
		}
	}
	
	private void drawElement(LDGuiElementType elm, int y){
		drawTexturedModalRect(guiLeft + elm.x, y, elm.x, elm.y, elm.w, elm.h);
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

	public TreeMap<String, TextField> fields(){
		return fields;
	}

	public void scroll(int dir){
		scroll += dir;
		if(scroll < 0) scroll = 0;
		int s = elements.size() / 2;
		int l = s < 12 ? 0 : s - 12;
		if(scroll > l) scroll = l;
		for(int i = 0; i < s; i++){
			int k = i - scroll, m = i * 2;
			elements.get(m).repos(guiTop, k, k >= 0 && k < 12);
			elements.get(m + 1).repos(guiTop, k, k >= 0 && k < 12);
		}
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		if(!addscroll) return;
		scroll(am > 0 ? 1 : -1);
	}

	public void addMsgElms(){
		buttons.put("notification", notificationbutton = new BasicButton("note", guiLeft - 16 + 237, guiTop - 24 + 5, 237, 5, 13, 12, true){
			@Override
			public boolean onclick(int x, int y, int b){
				return !(notificationbutton.visible = notification.visible = notify = false);
			}
		});
		texts.put("notification", notification = new BasicText(guiLeft, guiTop - 24 + 7, 217, null, "").hoverable(true).autoscale());
		buttons.get("notification").visible = texts.get("notification").visible = notify = false;
	}

	public void setMsg(String string){
		notification.string = string;
		notification.visible = notificationbutton.visible = notify = true;
	}
	
}