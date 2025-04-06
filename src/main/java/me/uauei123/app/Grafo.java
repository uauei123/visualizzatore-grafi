package me.uauei123.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

// Implementa Serializable per usare ObjectOutputStream
public class Grafo implements Serializable {
    private Map<Nodo, List<Connessione>> grafo;
    
    public Grafo() {
        // Crea una IdentityHashMap
        // Una HashMap che invece che essere basata su hashCode
        // (e quindi chiavi mutabili, in questo caso x e y dei nodi)
        // si basa sull'hash della reference alla chiave
        grafo = new IdentityHashMap<>(); 
    }

    public void setGrafo(Map<Nodo, List<Connessione>> grafo) {
        this.grafo = grafo;
    }
    
    public void aggiungiNodo(Nodo nodo) {
        if(nodo == null)
            throw new NullPointerException("nodo e' null.");
        
        // Se non c'e', aggiungi un nodo e una nuova ArrayList vuota per le connesioni
        grafo.putIfAbsent(nodo, new ArrayList<>());
    }
    
    public void aggiungiConnessione(Nodo origine, Nodo destinazione, int peso) {
        if(origine == null || destinazione == null)
            throw new NullPointerException("destinazione o origine e' null.");
        
        // Se non c'e', aggiungi un nodo e una nuova ArrayList vuota per le connesioni
        grafo.putIfAbsent(origine, new ArrayList<>());
        // Aggiungi una nuova connessione all'arraylist di origine
        grafo.get(origine).add(new Connessione(origine, destinazione, peso));
    }
    
    public List<Connessione> getConnessioni(Nodo nodo) {
        if(nodo == null)
            throw new NullPointerException("nodo e' null.");
        
        // Ritorna la lista di nodo o se non c'e' una vuota
        return grafo.getOrDefault(nodo, new ArrayList<>());
    }
    
    public Map<Nodo, List<Connessione>> getGrafo() {
        return grafo;
    }
    
    public void rimuoviNodo(Nodo nodo) {  
        if (nodo == null)
            throw new NullPointerException("nodo è null.");
        
        // rimuovi il nodo
        grafo.remove(nodo);

        // rimuovi qualsiasi connessione del nodo (parte da o finisce a)
        for (List<Connessione> connessioni : grafo.values()) {
            connessioni.removeIf(conn -> conn.getDestinazione().equals(nodo));
        }
    }

    
    public boolean rimuoviConnessione(Nodo origine, Nodo destinazione) {
        if (origine == null || destinazione == null)
            throw new NullPointerException("origine o destinazione è null.");

        List<Connessione> connessioni = grafo.get(origine);
        if (connessioni != null) {
            // rimuovi la connessione che ha come destinazione il nodo
            boolean rimossa = connessioni.removeIf(conn -> conn.getDestinazione().equals(destinazione));
            return rimossa;
        }
        
        return false;
    }

    public Nodo trovaNodo(int x, int y) {
        // Per tutti i nodi nel grafo
        for (Nodo nodo : grafo.keySet()) {
            // calcola la distanza tra il punto x y e il centro del nodo
            double distanza = Math.sqrt(Math.pow(x - nodo.getX(), 2) + Math.pow(y - nodo.getY(), 2));

            // se il punto cade nel nodo
            if (distanza <=  Piano.GRANDEZZA_NODO / 2) {
                return nodo;
            }
        }
        // nessun nodo trovato 
        return null;
    }
    
    public int calcolaCosto(List<Nodo> percorso) {
        int costoTotale = 0;
        
        if (percorso.size() < 2) return costoTotale;
        
        for (int i = 0; i < percorso.size() - 1; i++) {
            Nodo nodoAttuale = percorso.get(i);
            Nodo nodoProssimo = percorso.get(i + 1);
            
            // Trova la connessione tra il nodo e il prossimo e aggiungi il costo
            for (Connessione c : grafo.get(nodoAttuale)) {
                if (c.getDestinazione().equals(nodoProssimo)) {
                    costoTotale += c.getPeso();
                    break;
                }
            }
        }
        
        return costoTotale;
    }
}
