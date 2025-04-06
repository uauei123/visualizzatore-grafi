package me.uauei123.app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Dijkstra {

    public static List<List<Nodo>> risolvi(Grafo grafo, Nodo inizio, Nodo fine) {
        // distanza di ogni nodo da inizio
        Map<Nodo, Integer> distanze = new HashMap<>();
        // lista di predecessori di ogni nodo (per ricostruire i percorsi)
        Map<Nodo, List<Nodo>> predecessori = new HashMap<>();
        
        // inizializza distanze a infinito e predecessori vuoti
        for (Nodo n : grafo.getGrafo().keySet()) {
            distanze.put(n, Integer.MAX_VALUE);
            predecessori.put(n, new ArrayList<>());
        }
        
        // la distanza dal nodo iniziale e' 0
        distanze.put(inizio, 0);
        
        // coda ordinata in base alla distanza, prima quello con distanza minore per esplorare i nodi
        PriorityQueue<Nodo> coda = new PriorityQueue<>(Comparator.comparingInt(distanze::get));
        coda.add(inizio); // aggiunge solo il nodo iniziiale
        
        // esploriamo finche la coda non e' vuota (finche tutto e' esplorato)
        while (!coda.isEmpty()) {
            Nodo attuale = coda.poll(); // prendiamo il nodo con la distanza minore
            int distanzaAttuale = distanze.get(attuale);
            
            // se e' il nodo finale, abbiamo finito
            if (attuale.equals(fine)) break;
            
            // esploriamo le connessioni uscenti da questo nodo
            for (Connessione conn : grafo.getConnessioni(attuale)) {
                Nodo dest = conn.getDestinazione();
                int nuovaDistanza = distanzaAttuale + conn.getPeso();
                
                // se abbiamo trovato un percorso piu corto verso dest
                // aggiorniamo la distanza
                if (nuovaDistanza < distanze.get(dest)) {
                    distanze.put(dest, nuovaDistanza); 
                    predecessori.get(dest).clear(); // pulisce i predecessori
                    predecessori.get(dest).add(attuale); // aggiunge il nodo attuale come predecessore
                    coda.add(dest); // aggiunge il nodo alla coda per essere esplorato
                } 
                else if (nuovaDistanza == distanze.get(dest)) {
                    // Trovato un percorso alternativo con pari costo
                    if (!predecessori.get(dest).contains(attuale)) {
                        predecessori.get(dest).add(attuale);
                    }
                }
            }
        }
        
        // ricostruzione percorsi minimi, partendo dalla fine
        List<List<Nodo>> percorsiMinimi = new ArrayList<>();
        
        if (distanze.get(fine) == Integer.MAX_VALUE) {
            return percorsiMinimi; // nessun percorso trovato
        }
        
        // usa una coda di percorsi parziali per l'esplorazione
        Queue<List<Nodo>> percorsiParziali = new LinkedList<>();
        List<Nodo> percorsoIniziale = new ArrayList<>();
        percorsoIniziale.add(fine);
        percorsiParziali.add(percorsoIniziale);
        
        // esplora tutti i possibili percorsi minimi
        // ricostruendoli tramite i precedenti
        while (!percorsiParziali.isEmpty()) {
            List<Nodo> percorso = percorsiParziali.poll();
            Nodo ultimo = percorso.get(0);
            
            // se arriva al nodo iniziale
            if (ultimo.equals(inizio)) {
                // percorso completo trovato
                percorsiMinimi.add(new ArrayList<>(percorso)); // aggiungi ai percorsi completi
                continue;
            }
            
            // esplora tutti i possibili precedenti dell'ultimo nodo percorso
            for (Nodo predecessore : predecessori.get(ultimo)) {
                if (!percorso.contains(predecessore)) {
                    List<Nodo> nuovoPercorso = new ArrayList<>(percorso);
                    nuovoPercorso.add(0, predecessore); // aggiunge il precedente all'inizio del percorso
                    percorsiParziali.add(nuovoPercorso); // aggiunge il nuovo percorso parziale alla coda
                }
            }
        }
        
        return percorsiMinimi;
    }
}