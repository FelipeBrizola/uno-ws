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

    Az(0), Vd(2), Vm(3), Am(1);

    private final int value;

    ColorCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public ColorCard getValue(int key) {
        switch (key) {
            case 0:
                return Az;
            case 1:
                return Am;
            case 2:
                return Vd;

            case 3:
                return Vm;

            default:
                break;
        }
        return null;
    }
}
