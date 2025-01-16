package net.fexcraft.mod.landdev.event;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.player.LDPlayer;

/**
 * Called when someone joins a Layer, e.g. Municipality, County
 *
 * @author Ferdinand Calo' (FEX___96)
 */
public class JoinLayerEvent extends LDEvent {

    private final Layer layer;
    private final LDPlayer player;

    public JoinLayerEvent(Layer lay, LDPlayer play){
        layer = lay;
        player = play;
    }

    public Layer layer(){
        return layer;
    }

    public LDPlayer player(){
        return player;
    }

}
