# Visualizzatore Grafi

Il **Visualizzatore Grafi** è un programma per creare, modificare e analizzare grafi orientati pesati. L'applicazione fornisce un'interfaccia grafica intuitiva per manipolare grafi e implementa gli algoritmi di Dijkstra e Bellman-Ford per il calcolo dei cammini minimi.

## Indice

1. [Introduzione](#introduzione)
2. [Funzionalità](#funzionalità)
3. [Compilazione ed Esecuzione](#compilazione-ed-esecuzione)
   1. [Prerequisiti](#prerequisiti)
   2. [Clonazione del Repository](#clonazione-del-repository)
   3. [Compilazione del Progetto](#compilazione-del-progetto)
   4. [Esecuzione dell'Applicazione](#esecuzione-dellapplicazione)
4. [Licenza](#licenza)

## Introduzione

Il Visualizzatore Grafi offre un ambiente grafico per la manipolazione di grafi orientati pesati. L'applicazione è stata sviluppata in Java utilizzando Swing per l'interfaccia grafica e implementa gli algoritmi di Dijkstra e Bellman-Ford per il calcolo dei cammini minimi.

I grafi sono strutture composte da nodi e connessioni che li collegano.

## Funzionalità

- **Creazione e Modifica di Grafi**:
  - Aggiunta e rimozione di nodi
  - Creazione di connessioni pesate tra nodi
  - Rimozione di connessioni esistenti
  - Spostamento dei nodi tramite drag and drop

- **Visualizzazione**:
  - Zoom in/out sul grafo
  - Spostamento della vista
  - Evidenziazione dei percorsi calcolati

- **Algoritmi di Cammini Minimi**:
  - **Dijkstra**: Per grafi con pesi non negativi
  - **Bellman-Ford**: Supporta anche grafi con pesi negativi (ma senza cicli negativi)

- **Gestione File**:
  - Salvataggio dei grafi su file
  - Caricamento di grafi da file

## Compilazione ed Esecuzione

### Prerequisiti

Prima di compilare il progetto, assicurati che i seguenti requisiti siano soddisfatti:

- **Java Development Kit (JDK)** versione 17 o superiore
- **Maven** per la gestione delle dipendenze e la compilazione

### Clonazione del Repository

Clona il repository sulla tua macchina locale usando `git`:

```bash
git clone https://github.com/uauei123/visualizzatore-grafi.git
cd visualizzatore-grafi
```

### Compilazione del Progetto

Utilizza Maven per compilare il progetto:

```bash
mvn clean package
```

Questo comando compilerà il progetto e creerà un file JAR eseguibile nella cartella `target`.

### Esecuzione dell'Applicazione

Dopo la compilazione, puoi eseguire l'applicazione con il seguente comando:

```bash
java -jar App-1.0-SNAPSHOT.jar
```

## Licenza

Questo progetto è distribuito sotto la licenza MIT - consulta il file [LICENSE](LICENSE) per ulteriori dettagli.