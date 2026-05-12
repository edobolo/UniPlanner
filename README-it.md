# UniPlanner 🎓

*🇬🇧 [Read this document in English](README.md)*

UniPlanner è un'applicazione desktop progettata per aiutare gli studenti universitari a tenere traccia del proprio percorso accademico in modo semplice e intuitivo. 

Gestisci i tuoi esami, monitora i CFU, tieni d'occhio le scadenze e calcola la tua media, tutto in un unico posto. I dati vengono salvati in modo sicuro e automatico sul tuo computer, senza bisogno di connessione internet.

## ✨ Key Features

* **Exam Management:** Add your exams and mark which ones you've already passed.
* **Grade Book & Analytics:** Record your grades, track corresponding credits, and visualize your progress over time with interactive charts.
* **GPA Calculator:** Keep track of your average grade (includes customizable support for "Cum Laude" weight and bonus points).
* **Built-in Pomodoro Timer:** Manage your study sessions and breaks directly within the app to maximize focus and productivity.
* **Study Time Tracking:** Log the hours you spend studying for each specific exam to understand where your effort goes.
* **Deadline Tracker:** Save the dates of your upcoming exam sessions so you are never caught unprepared.
* **Achievements & Gamification:** Stay motivated with an unlockable trophy system that rewards your study milestones and academic successes.
* **Export & Backup:** Generate a beautiful PDF version of your grade book, or export/import all your data as a CSV file.
* **Custom Settings:**  Set your target credits (e.g., 180 for a Bachelor's degree).
    * Set your target average grade to see how far you are from your goal.
    * Dark mode available to reduce eye strain during late-night study sessions.

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
