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

    Cg(1), C4(2), Pu(3), In(4), M2(5);

    private final int value;

    TypeCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    // JOKER, JOKER_4, SKIP, REVERSE, MORE_2;
}
