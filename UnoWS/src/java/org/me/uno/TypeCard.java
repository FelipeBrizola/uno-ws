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
public enum TypeCard {

    //PULA, INVERTE, +2, coringa, coring+4;
    Pu(1), In(2), M2(3), Cg(4), C4(5);

    private final int value;

    TypeCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    
}
