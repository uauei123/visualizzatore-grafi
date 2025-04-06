package me.uauei123.app;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Piano extends JPanel {
    public static final int GRANDEZZA_NODO = 40;

    private Grafo grafo;
    private int numeroNodi = 0;

    private double fattoreZoom = 1.0; // zoom, 1.0 = 100% zoom
    private double vistaX = 0; // posizione rispetto il centro del piano (di quanto si e' spostata la vista)
    private double vistaY = 0; // posizione rispetto il centro del piano       

    private Modalita modalitaAttuale;

    private Nodo nodoSelezionato = null;
    
    private Point ultimoSpostamento = null;
    
    private boolean spostandoVista = false;
    private boolean trascinandoNodo = false;
    
    private List<List<Nodo>> percorsiMinimi = new ArrayList<>();
    private int percorso = 0;

    public Piano(Grafo grafo) {
        this.grafo = grafo;

        setBackground(Color.WHITE);
        setBorder(new LineBorder(Color.GRAY));
        setPreferredSize(new Dimension(3000, 3000));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Quando si clicca il mouse, gestisci l'evento secondo la modalita
                if(modalitaAttuale != null) {
                    Point2D puntoPiano = coordinatePiano(e.getX(), e.getY());
                    int pianoX = (int) puntoPiano.getX();
                    int pianoY = (int) puntoPiano.getY();


                    switch(modalitaAttuale) {
                        case AGGIUNGI_NODO -> aggiungiNodo(pianoX, pianoY);
                        case COLLEGA_NODI -> gestisciCollegamento(pianoX, pianoY);
                        case SELEZIONA -> selezionaNodo(pianoX, pianoY);
                        case RIMUOVI_NODO -> rimuoviNodo(pianoX, pianoY);
                        case RIMUOVI_CONNESSIONE -> rimuoviConnessione(pianoX, pianoY);
                        case ESEGUI_DIJKSTRA -> eseguiDijkstra(pianoX, pianoY);
                        case ESEGUI_BELLMANFORD -> eseguiBellmanFord(pianoX, pianoY);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point2D puntoPiano = coordinatePiano(e.getX(), e.getY());
                int pianoX = (int) puntoPiano.getX();
                int pianoY = (int) puntoPiano.getY();

                if(modalitaAttuale == Modalita.SELEZIONA) {
                    // Se e' in un nodo, trascina il nodo, senno sposta la vista
                    Nodo nodo = grafo.trovaNodo(pianoX, pianoY);
                    if(nodo != null) {
                        nodoSelezionato = nodo;
                        trascinandoNodo = true;
                        ultimoSpostamento = new Point(pianoX, pianoY);
                        repaint();
                    } else {
                        spostandoVista = true;
                        ultimoSpostamento = e.getPoint();
                    }
                }
            }

            
            @Override
            public void mouseReleased(MouseEvent e) {
                trascinandoNodo = false;
                spostandoVista = false;
                ultimoSpostamento = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(spostandoVista) {
                    // Calcola quanto il mouse e' stato spostato rispetto a prima,
                    // aggiungi lo spostamento alla vista
                    Point punto = e.getPoint();
                    
                    double deltaX = (punto.x - ultimoSpostamento.x) / fattoreZoom;
                    double deltaY = (punto.y - ultimoSpostamento.y) / fattoreZoom;
                    
                    vistaX += deltaX;
                    vistaY += deltaY;
                    
                    ultimoSpostamento = punto;
                    
                    repaint();
                } else if(trascinandoNodo && nodoSelezionato != null && modalitaAttuale == Modalita.SELEZIONA) {
                    // Sposta il nodo nel nuovo punto aggiungendo lo spostamento rispetto al mouse
                    Point2D puntoPiano = coordinatePiano(e.getX(), e.getY());
                    Point puntoAttuale = new Point((int) puntoPiano.getX(), (int) puntoPiano.getY());
                    
                    int deltaX = puntoAttuale.x - ultimoSpostamento.x;
                    int deltaY = puntoAttuale.y - ultimoSpostamento.y;
                    
                    nodoSelezionato.setX(nodoSelezionato.getX() + deltaX);
                    nodoSelezionato.setY(nodoSelezionato.getY() + deltaY);
                    
                    ultimoSpostamento =  puntoAttuale;
                    
                    GUI.cambiaStato("Nodo spostato a: (" + nodoSelezionato.getX() + ", " + nodoSelezionato.getY() + ")");
                    
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            // Zoom rispetto alle rotazioni (tacche) della rotellina del mouse. 
            // Cambia il fattore zoom. Max 500%, Min 20%
            int rotazioni = e.getWheelRotation();
            double fattoreZoomPrima = fattoreZoom;

            if(rotazioni < 0) {
                fattoreZoom *= 1.1;
                if(fattoreZoom > 5.0) fattoreZoom = 5.0;
            } else {
                fattoreZoom /= 1.1;
                if(fattoreZoom < 0.2) fattoreZoom = 0.2;
            }

            // differenza tra dove si trovava prima rispetto allo zoom e dove si trova ora
            vistaX = e.getX() / fattoreZoomPrima - (e.getX() / fattoreZoom);
            vistaY = e.getY() / fattoreZoomPrima - (e.getY() / fattoreZoom);

            GUI.cambiaStato("Zoom: " + String.format("%.2f", fattoreZoom * 100) + "%");

            repaint();
        });
        
        // Aggiungi keybindings per andare avanti e indietro per i percorsi
        InputMap input = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap azioni = getActionMap();
        
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "prossimoPercorso");
        azioni.put("prossimoPercorso", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!percorsiMinimi.isEmpty()) {
                    // se si preme la freccia a destra, vai avanti nei percorsi
                    percorso = (percorso + 1) % percorsiMinimi.size();
                    GUI.cambiaStato("Percorso " + (percorso + 1) + " di " + percorsiMinimi.size() + " - Costo: " + grafo.calcolaCosto(percorsiMinimi.get(percorso)));
                    repaint();
                }
            }
        });
        
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "percorsoPrecedente");
        azioni.put("percorsoPrecedente", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!percorsiMinimi.isEmpty()) {
                     // se si preme la freccia a sinistra, vai indietro nei percorsi
                    percorso = (percorso - 1 + percorsiMinimi.size()) % percorsiMinimi.size();
                    GUI.cambiaStato("Percorso " + (percorso + 1) + " di " + percorsiMinimi.size() + " - Costo: " + grafo.calcolaCosto(percorsiMinimi.get(percorso)));
                    repaint();
                }
            }
        });
    }

    // trasforma le coordinate dello schermo in coordinate del piano,
    // rispetto alla vista e allo zoom
    private Point2D coordinatePiano(int schermoX, int schermoY) {
        double pianoX = schermoX / fattoreZoom - vistaX;
        double pianoY = schermoY / fattoreZoom - vistaY;

        return new Point2D.Double(pianoX, pianoY);
    }

    private void aggiungiNodo(int x, int y) {
        // Se non c'e' gia un nodo
        if(grafo.trovaNodo(x, y) == null) {
            // aggiungi un nuovo nodo al grafo
            Nodo nodo = new Nodo(x, y, numeroNodi);

            numeroNodi++;

            grafo.aggiungiNodo(nodo);

            repaint();

            GUI.cambiaStato("Nodo aggiunto alle coordinate (" + x + ", " + y + ")");
        }
    }

    private void gestisciCollegamento(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);

        if(nodo != null) {
            if(nodoSelezionato == null) {
                // Selezionato il primo nodo
                nodoSelezionato = nodo;
                GUI.cambiaStato("Primo nodo selezionato. Ora seleziona il secondo nodo.");
            } else {
                if(nodoSelezionato != nodo) {
                    // Chiedi il peso e aggiungi la nuova connessione
                    String pesoStringa = JOptionPane.showInputDialog(this, "Inserisci il peso della connessione:", "Peso", JOptionPane.QUESTION_MESSAGE);

                    try {
                        if(pesoStringa != null && !pesoStringa.trim().isEmpty()) {
                            int peso = Integer.parseInt(pesoStringa.trim());

                            grafo.aggiungiConnessione(nodoSelezionato, nodo, peso);

                            repaint();

                            GUI.cambiaStato("Connessione aggiunta con peso " + peso);
                        }
                    } catch(NumberFormatException e) {
                        // Numero invalido (probabilmente non e' un numero)
                        JOptionPane.showMessageDialog(this, "Inserisci un numero valido per il peso", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                nodoSelezionato = null;

                GUI.cambiaStato("Seleziona il primo nodo per creare una nuova connessione");
            }

            repaint();
        }
    }

    private void selezionaNodo(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);

        // Se ha cliccato un nodo, selezionalo
        if (nodo != null) {
            nodoSelezionato = nodo;
            GUI.cambiaStato("Nodo selezionato: (" + nodo.getX() + ", " + nodo.getY() + ")");
            repaint();
        } else {
            nodoSelezionato = null;
            repaint();
        }
    }

    private void rimuoviNodo(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);
        
        if(nodo != null) {
            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Sei sicuro di voler rimuovere questo nodo? Verranno eliminate anche tutte le connessioni associate.",
                    "Conferma rimozione",
                    JOptionPane.YES_NO_OPTION
            );
            
            // Se vuole rimuoverlo
            if (conferma == JOptionPane.YES_OPTION) {
                // toglilo dal grafo
                grafo.rimuoviNodo(nodo);
                numeroNodi--;
                
                // Cambia il numero di tutti i nodi maggiori di quello rimosso
                // cosi che un nuovo nodo avra' l'ultimo numero
                for(Nodo n : grafo.getGrafo().keySet()) {
                    if(n.getNumero() > nodo.getNumero()) {
                        n.setNumero(n.getNumero() - 1);
                    }
                }
                
                repaint();
                GUI.cambiaStato("Nodo rimosso e connessioni associate eliminate");
            }
        }
    }

    private void rimuoviConnessione(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);
        
        if(nodo != null) {
            if(nodoSelezionato == null) {
                // primo nodo selezionato
                nodoSelezionato = nodo;
                GUI.cambiaStato("Primo nodo selezionato. Ora seleziona il nodo destinazione della connessione da rimuovere.");
                repaint();
            } else {
                if(nodoSelezionato != nodo) {
                    // Prova a rimuovere la connessione se esiste
                    if(grafo.rimuoviConnessione(nodoSelezionato, nodo)) {
                        GUI.cambiaStato("Connessione rimossa correttamente");
                    } else {
                        GUI.cambiaStato("Nessuna connessione trovata tra i nodi selezionati");
                    }
                } else {
                    // ha selezionato lo stesso nodo
                    GUI.cambiaStato("Devi selezionare un nodo diverso come destinazione");
                }

                nodoSelezionato = null;
                repaint();
            }
        }
    }
    
    private void eseguiDijkstra(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);
        
        if(nodo != null) {
            if(nodoSelezionato == null) {
                // ha selezionato il nodo di inizio
                nodoSelezionato = nodo;
                GUI.cambiaStato("Nodo iniziale selezionato. Ora seleziona il nodo finale.");
                repaint();
            } else {
                if(nodoSelezionato != nodo) {
                    
                    // Controlla se c'e' una connessione con un peso negativo
                    boolean pesoNegativo = false;
                    
                    for(Map.Entry<Nodo, List<Connessione>> e : grafo.getGrafo().entrySet()) {
                        for(Connessione c : e.getValue()) {
                            if(c.getPeso() < 0) {
                                pesoNegativo = true;
                                break;
                            }
                        }
                        if(pesoNegativo) break;
                    }
                    
                    if(pesoNegativo) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Il grafo contiene archi con pesi negativi.\n" +
                            "L'algoritmo di Dijkstra non può essere utilizzato in questo caso.\n" +
                            "Si consiglia di utilizzare l'algoritmo di Bellman-Ford.",
                            "Impossibile eseguire Dijkstra",
                            JOptionPane.WARNING_MESSAGE
                        );
                        GUI.cambiaStato("Impossibile eseguire Dijkstra: rilevati archi con pesi negativi");
                    } else {
                        // Calcola i percorsi, e mostra il risultato
                        percorsiMinimi = Dijkstra.risolvi(grafo, nodoSelezionato, nodo);
                        percorso = 0;
                        
                        if(!percorsiMinimi.isEmpty()) {
                            if(percorsiMinimi.size() == 1) {
                                GUI.cambiaStato("Trovato 1 percorso. Costo: " + grafo.calcolaCosto(percorsiMinimi.get(0)));
                            } else {
                                GUI.cambiaStato("Trovati " + percorsiMinimi.size() + " percorsi. Costo: " + grafo.calcolaCosto(percorsiMinimi.get(0)) + " - Usa le freccie per cambiare percorso.");
                            }
                        } else {
                            GUI.cambiaStato("Nessun percorso trovato");
                        }
                    }
                } else {
                    GUI.cambiaStato("Devi selezionare un nodo diverso da quello iniziale");
                }
                
                nodoSelezionato = null;
                repaint();
            }
        }
    }
    
    private void eseguiBellmanFord(int x, int y) {
        Nodo nodo = grafo.trovaNodo(x, y);
        
        if(nodo != null) {
            try {
                BellmanFord.risolvi(grafo, nodo);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Il grafo contiene cicli negativi.\n" +
                    "L'algoritmo di Bellman-Ford non può essere utilizzato in questo caso.\n",
                    "Impossibile eseguire Bellman-Ford",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }
    
    private void disegnaGriglia(Graphics2D g2d) {
        g2d.setColor(new Color(230, 230, 230));
        // Spessore della linea della griglia.
        // piu lo zoom e' grande, piu sara' sottile
        g2d.setStroke(new BasicStroke(0.5f / (float) fattoreZoom));
        
        // partiamo dall'inizio della vista
        double inizioX = -vistaX;
        double inizioY = -vistaY;
        
        // finisce alla fine dello schermo
        double fineX = inizioX + getWidth() / fattoreZoom;
        double fineY = inizioY + getHeight() / fattoreZoom;
        
        final int GRANDEZZA_GRIGLIA = 50; // spazio tra una riga e l'altra
        
        // calcoliamo l'inizio della griglia 
        int inizioGrigliaX = (int) (Math.floor(inizioX / GRANDEZZA_GRIGLIA) * GRANDEZZA_GRIGLIA);
        int inizioGrigliaY = (int) (Math.floor(inizioX / GRANDEZZA_GRIGLIA) * GRANDEZZA_GRIGLIA);
        
        // linee verticali
        for(int x = inizioGrigliaX; x <= fineX; x += GRANDEZZA_GRIGLIA) {
            g2d.drawLine(x, (int) inizioY, x, (int) fineY);
        }
        
        // linee orizzontali
        for(int y = inizioGrigliaY; y <= fineY; y += GRANDEZZA_GRIGLIA) {
            g2d.drawLine((int) inizioX, y, (int) fineX, y);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Antialiasing (rende i pixel piu "morbidi")
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // tiene la trasformazione attuale della grafica cosi da poter essere ripristinata
        AffineTransform trasformazionePrima = g2d.getTransform();
        
        // trasla il piano nella posizione della vista (tenendo conto dello zoom)
        g2d.translate(vistaX * fattoreZoom, vistaY * fattoreZoom);
        // scala tutto del fattore dello zoom
        g2d.scale(fattoreZoom, fattoreZoom);
        
        disegnaGriglia(g2d);
        
        // imposta la larghezza della linea
        g2d.setStroke(new BasicStroke(2 / (float) fattoreZoom));
        
        // per ogni nodo con le sue connessioni nel grafo
        for(Map.Entry<Nodo, List<Connessione>> entry : grafo.getGrafo().entrySet()) {
            // per ogni connessione del nodo
            for(Connessione c : entry.getValue()) {
                Nodo origine = c.getOrigine();
                Nodo destinazione = c.getDestinazione();
                
                // se fa parte del percorso di dijkstra, colore rosso, senno nero
                boolean connessionePercorso = false;
                if (!percorsiMinimi.isEmpty()) {
                    List<Nodo> percorsoAttuale = percorsiMinimi.get(percorso);
                    for (int i = 0; i < percorsoAttuale.size() - 1; i++) {
                        Nodo inizio = percorsoAttuale.get(i);
                        Nodo fine = percorsoAttuale.get(i + 1);
                        if (origine.equals(inizio) && destinazione.equals(fine)) {
                            connessionePercorso = true;
                            break;
                        }
                    }
                }
                
                if (connessionePercorso) {
                    g2d.setColor(new Color(255, 50, 50));
                } else {
                    g2d.setColor(new Color(64, 63, 76));
                }
                
                // trova l'angolo
                double deltaX = destinazione.getX() - origine.getX();
                double deltaY = destinazione.getY() - origine.getY();
                double angolo = Math.atan2(deltaY, deltaX);
                
                int raggio = GRANDEZZA_NODO / 2;
                
                // trova punto dell'altro cerchio
                int x2 = (int) (destinazione.getX() - raggio * Math.cos(angolo));
                int y2 = (int) (destinazione.getY() - raggio * Math.sin(angolo));
                
                // disegna la connessione
                g2d.drawLine(origine.getX(), origine.getY(), x2, y2);
                
                
                
                // grandezza della freccia tenendo conto dello zoom (limitata ad un minimo di 5)
                int grandezza_freccia = (int) (10 / fattoreZoom);
                if(grandezza_freccia < 5) grandezza_freccia = 5;
                
                // punti del triangolo (punta della freccia)
                int[] puntiX = new int[3];
                int[] puntiY = new int[3];
                
                // Il punto del nodo e' uno dei vertici
                puntiX[0] = x2;
                puntiY[0] = y2;
                
                // trova la perpendicolare al punto del nodo
                double perpendicolare = angolo + Math.PI / 2;
                // trova il punto centrale della freccia
                double mezzo = grandezza_freccia / 2;
                
                // trova punti spostandoti partendo dalla perpendicolare della grandezza della freccia
                // una volta a destra e una volta a sinistra
                puntiX[1] = (int) (x2 - grandezza_freccia * Math.cos(angolo) + mezzo * Math.cos(perpendicolare));
                puntiY[1] = (int) (y2 - grandezza_freccia * Math.sin(angolo) + mezzo * Math.sin(perpendicolare));
                
                puntiX[2] = (int) (x2 - grandezza_freccia * Math.cos(angolo) - mezzo * Math.cos(perpendicolare));
                puntiY[2] = (int) (y2 - grandezza_freccia * Math.sin(angolo) - mezzo * Math.sin(perpendicolare));
                
                // disegna il triangolo (punta della freccia)
                g2d.fillPolygon(puntiX, puntiY, 3);
                
                
                
                String pesoStringa = String.valueOf(c.getPeso());
                
                // fattore di lontananza dal centro della linea
                final double fattorePosizione = 0.7;
                
                //trova la posizione del peso
                int pesoX = (int) (origine.getX() + deltaX * fattorePosizione);
                int pesoY = (int) (origine.getY() + deltaY * fattorePosizione);
                
                // distanza fissa dal centro e angolo del testo sempre 180 gradi
                final int offsetDistanza = 15;
                double offsetAngolo = perpendicolare - Math.PI / 4;
                
                pesoX += (int) (offsetDistanza * Math.cos(offsetAngolo));
                pesoY += (int) (offsetDistanza * Math.sin(offsetAngolo));
                
                // scala il font con lo zoom
                Font fontOriginale = g2d.getFont();
                Font fontScalato = fontOriginale.deriveFont((float) (fontOriginale.getSize() / fattoreZoom));
                
                g2d.setFont(fontScalato);
                
                FontMetrics fm = g2d.getFontMetrics();
                
                // calcola la dimensione del testo
                int larghezzaTesto = fm.stringWidth(pesoStringa);
                int altezzaTesto = fm.getHeight();
            
                // aggiungi un rettangolo semitrasparente per leggere meglio il testo
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(pesoX - larghezzaTesto / 2 - 4, pesoY - altezzaTesto / 2, larghezzaTesto + 8, altezzaTesto, 8, 8);
                
                // disegna il testo
                g2d.setColor(new Color(50, 50, 50));
                g2d.drawString(pesoStringa, pesoX - larghezzaTesto / 2, pesoY + altezzaTesto / 4);
                
                // ripristina il font originale
                g2d.setFont(fontOriginale);
            }
        }
        
        // per ogni nodo
        for(Nodo nodo : grafo.getGrafo().keySet()) {
            Point p = new Point(nodo.getX(), nodo.getY());
            
            // se e' il nodo selezionato in modalita seleziona
            if(modalitaAttuale == Modalita.COLLEGA_NODI && nodo == nodoSelezionato) {
                g2d.setColor(new Color(126, 166, 224));
            } else if(modalitaAttuale == Modalita.RIMUOVI_CONNESSIONE && nodo == nodoSelezionato) {
                // se e' il nodo selezionato in modalita rimuovi connessione
                g2d.setColor(new Color(255, 150, 150));
            } else if(modalitaAttuale == Modalita.ESEGUI_DIJKSTRA && nodo == nodoSelezionato) {
                // se e' il nodo di inizio di dijkstra
                g2d.setColor(new Color(255, 190, 125));
            } else {
                g2d.setColor(new Color(70, 130, 180));
            }
            
            // disegna il cerchio
            g2d.fillOval(p.x - GRANDEZZA_NODO / 2, p.y - GRANDEZZA_NODO / 2, GRANDEZZA_NODO, GRANDEZZA_NODO);
            
            // fai un outline al cerchio
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawOval(p.x - GRANDEZZA_NODO / 2, p.y - GRANDEZZA_NODO / 2, GRANDEZZA_NODO, GRANDEZZA_NODO);
        
            // scala il font
            Font fontOriginale = g2d.getFont();
            Font fontScalato = fontOriginale.deriveFont((float) (fontOriginale.getSize() / fattoreZoom));
            
            g2d.setFont(fontScalato);
            
            // scrivi il numero del nodo
            g2d.setColor(Color.WHITE);
            String nomeNodo = String.valueOf(nodo.getNumero());
            FontMetrics fm = g2d.getFontMetrics();
            int larghezzaTesto = fm.stringWidth(nomeNodo);
            g2d.drawString(nomeNodo, p.x - larghezzaTesto / 2, p.y + fm.getAscent() / 2);
            
            g2d.setFont(fontOriginale);
        }
        // ripristina la trasformazione della grafica originale
        g2d.setTransform(trasformazionePrima);
    }
    
    public void disegnaGrafo(Grafo grafo) {
        // cambia la mappa interna del Grafo
        // NON CAMBIARE DIRETTAMENTE GRAFO!!! 
        // L'IDENTITYHASHMAP AVRA' LE REFERENCE SBAGLIATE!!!
        // ho speso 8 ore a fixare questo bug
        this.grafo.setGrafo(grafo.getGrafo());
        
        this.numeroNodi = grafo.getGrafo().size();
        this.nodoSelezionato = null;
        this.spostandoVista = false;
        this.trascinandoNodo = false;
        this.ultimoSpostamento = null;
        
        repaint();
    }

    public Modalita getModalita() {
        return modalitaAttuale;
    }

    public void setModalita(Modalita modalita) {
        this.modalitaAttuale = modalita;
        percorsiMinimi.clear();
        percorso = 0;
        nodoSelezionato = null;
    }
}
