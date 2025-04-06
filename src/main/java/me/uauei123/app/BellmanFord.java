package me.uauei123.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BellmanFord {
    
    public static void risolvi(Grafo grafo, Nodo nodoIniziale) {
        // distanza di ogni nodo dal nodo iniziale
        Map<Nodo, Integer> distanze = new HashMap<>();
        // predecessore di ogni nodo nel cammino piu breve
        Map<Nodo, Nodo> predecessori = new HashMap<>();
        
        // tutti i nodi nel grafo
        List<Nodo> nodi = new ArrayList<>(grafo.getGrafo().keySet());
        
        // tutte le connessioni del grafo
        List<Connessione> connessioni = new ArrayList<>();
        for (Map.Entry<Nodo, List<Connessione>> entry : grafo.getGrafo().entrySet()) {
            connessioni.addAll(entry.getValue());
        }
        
        // inizializza tutti i nodi ad infinito tranne il primo che e' 0
        for (Nodo nodo : nodi) {
            if (nodo.equals(nodoIniziale)) {
                distanze.put(nodo, 0);
            } else {
                distanze.put(nodo, Integer.MAX_VALUE);
            }
            // nessun precedente all'inizio
            predecessori.put(nodo, null);
        }
        
        // si ripete per numero di nodi - 1 volte
        for (int i = 1; i < nodi.size(); i++) {
            for (Connessione conn : connessioni) {
                Nodo origine = conn.getOrigine();
                Nodo destinazione = conn.getDestinazione();
                int peso = conn.getPeso();
                
                // se la distanza del nodo di origine non e' infinita
                // e il cammino trovato e' piu corto
                if (distanze.get(origine) != Integer.MAX_VALUE && 
                    distanze.get(origine) + peso < distanze.getOrDefault(destinazione, Integer.MAX_VALUE)) {
                    distanze.put(destinazione, distanze.get(origine) + peso); // aggiorna distanza
                    predecessori.put(destinazione, origine); // aggiorna precedente
                }
            }
        }
        
        // controlla la presenza di cicli negativi
        for (Connessione conn : connessioni) {
            Nodo origine = conn.getOrigine();
            Nodo destinazione = conn.getDestinazione();
            int peso = conn.getPeso();
            
            // se una distanza puo essere ancora ridotta, c'e' un ciclo
            if (distanze.get(origine) != Integer.MAX_VALUE && 
                distanze.get(origine) + peso < distanze.getOrDefault(destinazione, Integer.MAX_VALUE)) {
                throw new RuntimeException("Il grafo contiene un ciclo di peso negativo");
            }
        }
        
        // apri la tabella dei risultati grafica
        GUI.tabellaBellmanFord(nodoIniziale, nodi, distanze, predecessori);
    }
}