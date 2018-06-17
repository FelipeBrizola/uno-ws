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
public class Card {

    private int number;
    private ColorCard color;
    private TypeCard type;

    public Card(ColorCard color, TypeCard type, int number) {
        this.color = color;
        this.type = type;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public ColorCard getColor() {
        return color;
    }

    public void setColor(ColorCard color) {
        this.color = color;
    }

    public TypeCard getType() {
        return type;
    }

    public void setType(TypeCard type) {
        this.type = type;
    }

}
