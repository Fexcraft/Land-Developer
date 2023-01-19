package net.fexcraft.mod.landdev.util.broad;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class Message {
	
	public Message(String string){
		length = (value = string).length();
	}
	
	public Message(){}
	
	protected int length;
	protected String value;
	
	@Override
	public String toString(){
		return length > 0 ? length + "|" + value : length + "";
	}

}
