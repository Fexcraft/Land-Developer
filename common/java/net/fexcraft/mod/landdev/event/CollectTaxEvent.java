package net.fexcraft.mod.landdev.event;

import net.fexcraft.mod.landdev.data.Layer;

/**
 * Called when taxes are collected on a Layer, e.g. Municipality, County
 *
 * @author Moose1002
 */
public class CollectTaxEvent extends LDEvent {

    private final Layer layer;

    public CollectTaxEvent(Layer lay){
        layer = lay;
    }

    public Layer layer(){
        return layer;
    }

}
