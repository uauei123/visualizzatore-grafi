package me.uauei123.app;

import java.io.*;

public class Serializza {

    public static void salvaGrafo(Grafo grafo, File file) throws IOException {
         try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
             out.writeObject(grafo); // Prova a scrivere in binario l'oggetto Grafo nel file
         }
    }
    
    public static Grafo caricaGrafo(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Grafo grafo = (Grafo) in.readObject(); // Prova a leggere un oggetto Grafo dal file
            
            return grafo;
        }
    }
}
