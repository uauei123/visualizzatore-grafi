package me.uauei123.app;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class GUI {
    
    private JFrame frame;
    private Piano piano;
    private Grafo grafo;
    private static JLabel stato;
    
    public GUI() {
        grafo = new Grafo();
    }
 
    public void inizializzaInterfaccia() {
        // Imposta tema al tema di default del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        // Crea la finestra e aggiungi un layout
        frame = new JFrame("Visualizzatore Grafo  |  Dijkstra e Bellman-Ford");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        // Aggiungi icone (verra' presa quella piu appropriata)
        ImageIcon icona32 = new ImageIcon(getClass().getResource("/icona32.png"));
        ImageIcon icona64 = new ImageIcon(getClass().getResource("/icona64.png"));
        ImageIcon icona96 = new ImageIcon(getClass().getResource("/icona96.png"));

        frame.setIconImages(Arrays.asList(
                icona32.getImage(),
                icona64.getImage(),
                icona96.getImage()
        ));
        
        // Crea il pannello per i pulsanti
        JPanel pannelloPulsanti = creaPannelloPulsanti();
        frame.add(pannelloPulsanti, BorderLayout.NORTH);
        
        // Crea un nuovo piano
        piano = new Piano(grafo);
        frame.add(piano, BorderLayout.CENTER);
        
        // Crea barra di stato
        stato = new JLabel("Pronto. Seleziona un'operazione dal menu in alto.");
        stato.setBorder(new EmptyBorder(5, 10, 5, 10));
        frame.add(stato, BorderLayout.SOUTH);
        
        // Crea il menu
        JMenuBar menu = creaMenu();
        frame.setJMenuBar(menu);
        
        // Rendi la finestra visibile e centrala
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Richiedi il focus per il piano
        SwingUtilities.invokeLater(() -> {
           piano.requestFocusInWindow();
        });
    }
    
    private JButton creaPulsante(String testo) {
        // Crea un pulsante
        JButton pulsante = new JButton(testo);
       
        pulsante.setFocusPainted(true);
        pulsante.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        pulsante.setMargin(new Insets(8, 15, 8, 15));
       
        return pulsante;
    }
    
    private JPanel creaPannelloPulsanti() {
        // Crea un nuovo pannello
        // Usa il FlowLayout (da destra verso sinistra)
        JPanel pannello = new JPanel();
        pannello.setLayout(new FlowLayout(FlowLayout.LEFT));
        pannello.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Crea i pulsanti e aggiungi i callback
        JButton aggiungiNodo = creaPulsante("Aggiungi Nodo");
        aggiungiNodo.addActionListener(e -> {
            piano.setModalita(Modalita.AGGIUNGI_NODO);
            cambiaStato("Clicca sul pannello per aggiungere un nodo");
        });
        
        JButton collegaNodi = creaPulsante("Collega Nodi");
        collegaNodi.addActionListener(e -> {
            piano.setModalita(Modalita.COLLEGA_NODI);
            cambiaStato("Seleziona il primo nodo per creare una connessione");
        });
        
        JButton seleziona = creaPulsante("Seleziona");
        seleziona.addActionListener(e -> {
            piano.setModalita(Modalita.SELEZIONA);
            cambiaStato("Modalità selezione attivata");
        });
        
        JButton rimuoviNodo = creaPulsante("Rimuovi Nodo");
        rimuoviNodo.addActionListener(e -> {
            piano.setModalita(Modalita.RIMUOVI_NODO);
            cambiaStato("Clicca su un nodo per rimuoverlo");
        });
        
        JButton rimuoviConn = creaPulsante("Rimuovi Connessione");
        rimuoviConn.addActionListener(e -> {
            piano.setModalita(Modalita.RIMUOVI_CONNESSIONE);
            cambiaStato("Seleziona il nodo di origine della connessione da rimuovere");
        });
        
        // Aggiungi i pulsanti al pannello
        pannello.add(aggiungiNodo);
        pannello.add(collegaNodi);
        pannello.add(seleziona);
        pannello.add(rimuoviNodo);
        pannello.add(rimuoviConn);
        
        return pannello;
    }
    
    private JMenuBar creaMenu() {
        // Crea barra menu
        JMenuBar menu = new JMenuBar();
        
        // Crea i vari menu e i pulsanti
        JMenu file = new JMenu("File");
        JMenuItem nuovo = new JMenuItem("Nuovo Grafo");
        JMenuItem salva = new JMenuItem("Salva Grafo...");
        JMenuItem carica = new JMenuItem("Carica Grafo...");
        JMenuItem esci = new JMenuItem("Esci");
        
        // Quando si preme nuovo, si crea un nuovo grafo e si sostituisce
        nuovo.addActionListener(e -> {
           grafo = new Grafo();
           piano.disegnaGrafo(grafo);
        });
        
        salva.addActionListener(e -> salvaGrafo());
        carica.addActionListener(e -> caricaGrafo());
        esci.addActionListener(e -> System.exit(0));
        
        file.add(nuovo);
        file.add(carica);
        file.add(salva);
        file.addSeparator();
        file.add(esci);
        
        JMenu aiuto = new JMenu("Aiuto");
        JMenuItem info = new JMenuItem("Info");
        JMenuItem documentazione = new JMenuItem("Documentazione");
        
        info.addActionListener(e -> {
           JOptionPane.showMessageDialog(frame,
                   "Applicazione per la visualizzazione di grafi\n" +
                   "Algoritmo di Dijkstra e Bellman-Ford per i cammini minimi.",
                   "Informazioni", JOptionPane.INFORMATION_MESSAGE); 
        });
        
        documentazione.addActionListener(e -> {
            try {
                // Prova ad aprire la pagina github del progetto
                Desktop.getDesktop().browse(new URI("https://github.com/uauei123/visualizzatore-grafi"));
            } catch (URISyntaxException | IOException ex) {
                ex.printStackTrace();
            }
        });
        
        aiuto.add(info);
        aiuto.add(documentazione);
        
        JMenu esegui = new JMenu("Esegui");
        JMenuItem dijkstra = new JMenuItem("Dijkstra");
        JMenuItem bellmanford = new JMenuItem("Bellman-Ford");
        
        dijkstra.addActionListener(e -> {
            piano.setModalita(Modalita.ESEGUI_DIJKSTRA);
            cambiaStato("Seleziona il nodo iniziale.");
        });
        
        bellmanford.addActionListener(e -> {
            piano.setModalita(Modalita.ESEGUI_BELLMANFORD);
            cambiaStato("Seleziona il nodo iniziale.");
        });
        
        esegui.add(dijkstra);
        esegui.add(bellmanford);
        
        menu.add(file);
        menu.add(esegui);
        menu.add(aiuto);
        
        return menu;
    }
    
    private void salvaGrafo() {
        // Apri selettore del file
        JFileChooser scegliFile = new JFileChooser();
        scegliFile.setDialogTitle("Salva Grafo");
        
        if(scegliFile.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = scegliFile.getSelectedFile();
            
            // Leggi il file
            if (!file.getName().toLowerCase().endsWith(".graph")) {
                file = new File(file.getAbsolutePath() + ".graph");
            }
            
            try {
                // Prova a serializzare il grafo
                Serializza.salvaGrafo(grafo, file);
                GUI.cambiaStato("Grafo salvato correttamente in: " + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                    "Errore durante il salvataggio del grafo",
                    "Errore di Salvataggio", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void caricaGrafo() {
        // Apri selettore del file
        JFileChooser scegliFile = new JFileChooser();
        scegliFile.setDialogTitle("Carica Grafo");
        
        if(scegliFile.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = scegliFile.getSelectedFile();
            
            try {
                // Prova a caricare il grafo deserializzato
                piano.disegnaGrafo(Serializza.caricaGrafo(file));
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(frame,
                    "Errore durante il caricamento del grafo",
                    "Errore di Caricamento", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    public static void cambiaStato(String messaggio) {
        // Cambia il messaggio della barra di stato
        stato.setText(messaggio);
    }
    
    public static void tabellaBellmanFord(Nodo nodoIniziale, List<Nodo> nodi, Map<Nodo, Integer> distanze, Map<Nodo, Nodo> predecessori) {
        // Esegui nell'EDT
        SwingUtilities.invokeLater(() -> {
            // Nuova finsetra senza proprietario, modale
            JDialog dialogo = new JDialog((JFrame) null, "Risultati Bellman-Ford", true);
            dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Rilascia le risorse alla chiusura
            dialogo.setLayout(new BorderLayout());
            
            // Aggiungi icone
            ImageIcon icona32 = new ImageIcon(GUI.class.getResource("/icona32.png"));
            ImageIcon icona64 = new ImageIcon(GUI.class.getResource("/icona64.png"));
                    
            dialogo.setIconImage(icona32.getImage());
            dialogo.setIconImage(icona64.getImage());
            
            // Crea l'intestazione
            JPanel intestazione = new JPanel(new FlowLayout(FlowLayout.LEFT));
            intestazione.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            // Crea barra del titolo
            JLabel titolo = new JLabel("Risultati dell'algoritmo di Bellman-Ford");
            titolo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            
            intestazione.add(titolo);
            
            dialogo.add(intestazione, BorderLayout.NORTH);
            
            // Nome colonne
            String[] colonne = {"Nodo", "Distanza", "Precedente"};
            // Dati per ogni riga
            Object[][] dati = new Object[nodi.size()][3];
            
            int riga = 0;
            for (Nodo nodo : nodi) {
                // Leggi la distanza
                String distanza = (distanze.get(nodo) == Integer.MAX_VALUE) ? "∞" : distanze.get(nodo).toString();
                // Leggi il precedente
                String precedente = (predecessori.get(nodo) == null) ? "-" : predecessori.get(nodo).getNumero() + "";
                
                dati[riga][0] = nodo.getNumero();
                dati[riga][1] = distanza;
                dati[riga][2] = precedente;
                
                riga++;
            }
            
            // Sorta l'array in ordine decrescente del numero del nodo
            Arrays.sort(dati, (a, b) -> {
                Integer numA = (Integer) a[0];
                Integer numB = (Integer) b[0];
                return numA.compareTo(numB);
            });
            
            // Crea una tabella con i dati
            JTable tabella = new JTable(dati, colonne);
            tabella.setDefaultEditor(Object.class, null); // Rende la tabella non modificabile
            tabella.setRowHeight(24);
            tabella.getTableHeader().setReorderingAllowed(false);
            
            // Rendi la tabella scorrevole aggiungendola ad un JScrollPane
            JScrollPane pannelloScorrevole = new JScrollPane(tabella);
            pannelloScorrevole.setBorder(new EmptyBorder(0, 15, 0, 15));
            dialogo.add(pannelloScorrevole, BorderLayout.CENTER);
            
            // Pannello inferiore per scrivere nodo iniziale
            JPanel inferiore = new JPanel(new FlowLayout(FlowLayout.LEFT));
            inferiore.setBorder(new EmptyBorder(10, 15, 10, 15));
            JLabel inizio = new JLabel("Nodo iniziale: " + nodoIniziale.getNumero());
            inizio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            inferiore.add(inizio);
            
            dialogo.add(inferiore, BorderLayout.SOUTH);
            
            dialogo.setSize(450, 370);
            dialogo.setResizable(false);
            dialogo.setLocationRelativeTo(null);
            dialogo.setVisible(true);
        });
    }
}