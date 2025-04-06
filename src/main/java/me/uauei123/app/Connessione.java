package me.uauei123.app;

import java.io.Serializable;

public class Connessione implements Serializable {
    private final Nodo origine;
    private final Nodo destinazione;
    private final int peso;

    public Connessione(Nodo origine, Nodo destinazione, int peso) {
        this.origine = origine;
        this.destinazione = destinazione;
        this.peso = peso;
    }

    public Nodo getOrigine() {
        return origine;
    }

    public Nodo getDestinazione() {
        return destinazione;
    }

    public int getPeso() {
        return peso;
    }
}
