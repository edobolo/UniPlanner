# UniPlanner 🎓

*🇬🇧 [Read this document in English](README.md)*

UniPlanner è un'applicazione desktop progettata per aiutare gli studenti universitari a tenere traccia del proprio percorso accademico in modo semplice e intuitivo. 

Gestisci i tuoi esami, monitora i CFU, tieni d'occhio le scadenze e calcola la tua media, tutto in un unico posto. I dati vengono salvati in modo sicuro e automatico sul tuo computer, senza bisogno di connessione internet.

## ✨ Funzionalità Principali

* **Gestione Esami:** Aggiungi il tuo piano di studi e segna gli esami man mano che li superi.
* **Libretto e Statistiche:** Registra i voti e i relativi crediti, visualizzando l'andamento della tua carriera tramite grafici interattivi.
* **Calcolo della Media:** Monitora costantemente la tua media ponderata e aritmetica (con supporto personalizzabile per il peso delle lodi e punti bonus laurea).
* **Timer Pomodoro Integrato:** Gestisci sessioni di studio e pause direttamente dall'app per massimizzare la produttività.
* **Tracciamento Tempo di Studio:** Registra quanto tempo dedichi effettivamente a ogni singola materia.
* **Gestione Scadenze:** Salva le date dei tuoi appelli per avere sempre sotto controllo il calendario degli esami.
* **Obiettivi e Gamification:** Resta motivato grazie a un sistema di trofei sbloccabili che premia i tuoi traguardi (dalle maratone di studio ai voti eccellenti).
* **Esportazione e Backup:** Genera un PDF professionale del tuo libretto o esporta/importa i tuoi dati in formato CSV per non perdere mai i progressi.
* **Personalizzazione Totale:**
    * Imposta obiettivi di CFU totali e media desiderata.
    * Modalità Scura (Dark Mode) inclusa per non affaticare la vista durante le sessioni notturne.

## 🚀 Installazione e Avvio

L'applicazione è stata pacchettizzata per essere utilizzata facilmente su Windows senza la necessità di installare Java separatamente.

1. Vai nella sezione **Releases** di questo repository e scarica il file `Setup_UniPlanner.zip`.
2. Estrai il contenuto dell'archivio `.zip` in una cartella a tua scelta.
3. Fai doppio clic sul file di installazione (`Setup_UniPlanner.exe`).
4. Segui la breve procedura guidata (Wizard) per installare l'app e creare una comoda icona sul Desktop.
5. Avvia UniPlanner e inizia a organizzare la tua vita universitaria!

## 🛠️ Tecnologie Utilizzate

* **Linguaggio:** Java
* **Salvataggio Dati:** Sistema I/O nativo (salvataggio locale in `user.home/UniplannerDati`)
* **Distribuzione:** `jpackage` per l'eseguibile nativo e Inno Setup per l'installer di Windows.

## 👨‍💻 Autore

Sviluppato da **[Edoardo Bolognini/edobolo]**
* Contatto/Email: [bologniniedoardo@gmail.com]
* Profilo GitHub: [https://github.com/edobolo]

## 📝 Crediti e Librerie

Questo progetto utilizza le seguenti risorse open-source:
* **Icone:** Fornite da [SVG Repo](https://www.svgrepo.com/).
* **Tema Grafico:** [FlatLaf](https://github.com/JFormDesigner/FlatLaf) (Licenza Apache 2.0).
* **Gestione Date:** [LGoodDatePicker](https://github.com/LGoodDatePicker/LGoodDatePicker) (Licenza MIT).
