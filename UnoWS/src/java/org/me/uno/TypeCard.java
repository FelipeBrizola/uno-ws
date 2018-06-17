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

    JOKER(1), JOKER_4(2), SKIP(3), REVERSE(4), MORE_2(5);

    private final int value;

    TypeCard(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    // JOKER, JOKER_4, SKIP, REVERSE, MORE_2;
}
