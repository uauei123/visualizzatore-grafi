package me.uauei123.app;

import java.io.Serializable;
import java.util.Objects;

public class Nodo implements Serializable {
    
    private int x;
    private int y;
    
    private int numero;
        
    public Nodo(int x, int y, int numero) {
        this.x = x;
        this.y = y;        
        
        this.numero = numero;
    }
    
    public int getNumero() {
        return numero;
    }
    
    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nodo nodo = (Nodo) o;
        return x == nodo.x && y == nodo.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
