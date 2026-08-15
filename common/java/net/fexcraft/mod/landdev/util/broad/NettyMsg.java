package net.fexcraft.mod.landdev.util.broad;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class NettyMsg {

	public NettyMsg(){}
	
	public NettyMsg(String string){
		length = (value = string).length();
	}
	
	protected int length;
	protected String value;
	
	@Override
	public String toString(){
		return length > 0 ? length + "|" + value : length + "";
	}

}
