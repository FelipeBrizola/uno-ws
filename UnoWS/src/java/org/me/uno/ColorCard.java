/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.uno;

/**
 *
 * @author felipebrizola
 */
public enum ColorCard {

    Az(0), Am(1), Vd(2), Vm(3);

    private final int value;

    ColorCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
    
}
