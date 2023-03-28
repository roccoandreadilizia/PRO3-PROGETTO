package com.example.prog3progetto.Server.controller;


import com.example.prog3progetto.Utils.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javax.json.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerController implements Initializable {

    public static ArrayList<CoppiaUtenteSocket> clients;

    public Utente utente = null;
    @FXML
    private ListView listLog;





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> socketThreadStart()).start();
    }

    /**
     * Creazione di un nuovo Server socket  e utilizzo i thread
     * per permettere a più client di connettersi contemporaneamente
     * */

    public void socketThreadStart() {
        try {
            ServerSocket s = new ServerSocket(4445);//la porta l'ho scelta a caso, in modo che non andasse ad interferire con altri processi (ad esempio la porta 8080)
            clients = new ArrayList<>();
            System.out.println("Creazione di un socket all'indirizzo: " + s.getLocalSocketAddress());
            int i = 1;
            ExecutorService exec= Executors.newFixedThreadPool(2);
            while (true) {//creo un loop infinito per gestire i client che fanno richiesta di connessione al socket

                int finalI = i;//contatore dei thread
                Socket incoming = s.accept();//questo while non va in loop infinito poichè si ferma subito in s.accept(), e aspetta che un client faccia richiesta al server


                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clients.add(new CoppiaUtenteSocket(incoming, null));//aggiungo un clients (socket-user) -> user è nullo poichè devo ancora fare il login
                            GestoreThread(incoming, finalI);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                i++;
                /*new Thread() {
                    @Override
                    public void run() {

                        /**
                         * Quando un thread aggiorna la UI (User Interface) tale update deve essere eseguito nel main thread
                         * (che esegue il metodo principale del programma e aggiorna gli elementi dell'interfaccia utente)
                         * per questo motivo ho usato il metodo Platform.runLater(() -> {}) per poter modificare la scena da ogni thread
                         *
                         * RunLater viene usato come se fosse un sistema di coda, quindi metterà il thread in coda e lo eseguirà non appena
                         * il main thread potrà
                         * Questa tecnica è particolarmente usata nelle applicazioni di JavaFX poichè l'applicazione è il main Thread
                         *
                         * Esso significa che verrà eseguito ad un tempo indefinito nel futuro, di solito esso viene eseguito immediatamente
                         * a meno che il main thread non sia occupato, in questo caso il thread aspetterà il suo turno
                         *
                         *  Fonte: https://www.youtube.com/watch?v=IOb9jJkKCZk
                         * */
                     /*   Platform.runLater(() -> {
                            try {
                                clients.add(new CoppiaUtenteSocket(incoming, null));//aggiungo un clients (socket-user) -> user è nullo poichè devo ancora fare il login
                                GestoreThread(incoming, finalI);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }.start();

                i++;*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     ThreadHandler serve per leggere in modo ciclico l'inputStream ed eseguire delle azioni
     in base a cosa hanno inviato i Client nel Server

     Le operazioni sono così gestite:
     1)Client Start
     2)Richiesta di mail
     3)Richiesta di invio di una mail
     4)Richiesta di eliminare una mail
     */
    public void GestoreThread(Socket incoming, int indice)throws IOException, ClassNotFoundException{

        ObjectInputStream inputStram = new ObjectInputStream(incoming.getInputStream()); //input del socket
        ObjectOutputStream outputStream = new ObjectOutputStream(incoming.getOutputStream()); //output del socket
        outputStream.flush();

        if(!incoming.isClosed()){ //il socket è chiuso o aperto?
            try{
                while(true){
                    Object obj = inputStram.readObject(); //leggo l'input

                    if(obj instanceof Coppia){
                        Coppia c = (Coppia) obj;
                        switch ((Integer) c.getOggetto1()){

                            case 1: //caso in cui un Client viene accesso ed evvettua la connessione al Server
                                Utente user = new Utente((String) c.getOggetto2());
                                utente = user;

                                //controllo email anche da client


                                //setto la coppia username-socket (prima username era null)
                                ServerController.clients.set(indice - 1,
                                        new CoppiaUtenteSocket(ServerController.clients.get(indice - 1).socket, utente.getEmail()));

                                printOnLog("Login by " + user.getEmail());
                                outputStream.writeObject(user);//Scrivo nell'output del socket
                                break;


                            case 2:
                                //List<Email> newMail = FileQuery.readMailJSON();
                                Coppia p = (Coppia) c.getOggetto2();

                                List<Email> newMail = leggiCasella(utente.getEmail(), (Integer) p.getOggetto2());
                                outputStream.writeObject(newMail);
                                break;


                            case 3:
                                //caso di scrittura di una mail
                                Email sending = (Email) c.getOggetto2();
                                List<String> dests = sending.getDestinatari(); //estraggo i destinatari

                                //QUI FARE IL CHECK SE TUTTI GLI USER SONO CORRETTI
                                Boolean check = true;
                                for(String d: dests){
                                   check =  existUser(d.toString());
                                   if(!check){
                                       printOnLog(sending.getMittente() + " Ha inviato una nuova email: INVIO FALLITO");
                                       Boolean message = false;
                                       outputStream.writeObject(message);//Scrivo nell'output del socket
                                       break;
                                   }
                                }

                                if(check) {
                                    List<Email> emails = null;

                                    for (String u : dests) {//per ogni user trovato nell'elenco dei destinatari
                                        emails = leggiCasella(u,-1);
                                        int lastID = 0;
                                        if (emails.size() > 0)
                                            //se l'elenco delle mail di user non è vuoto allora prendo l'ultimo id e lo incremento
                                            lastID = emails.get(emails.size() - 1).getId() + 1;
                                        sending.setId(lastID);//setto il nuovo id -> se l'elenco è vuoto è uguale a zero
                                        emails.add(sending);//aggiungo la nuova email

                                        scriviCasella(u, emails);//scrivo il nuovo elenco di user con la nuova mail aggiunta

                                        printOnLog(sending.getMittente() + " Ha inviato una nuova email");//messaggio sul terminale del server
                                        for (String s : dests) {
                                            printOnLog(s + " Ha ricevuto una nuova email");
                                            Boolean message = true;
                                            outputStream.writeObject(message);//Scrivo nell'output del socket
                                        }
                                    }
                                }

                                break;

                            case 4:
                                Coppia UserANDId= (Coppia) c.getOggetto2();

                                String indirizzo= (String) UserANDId.getOggetto1();
                                Integer idMail= (Integer) UserANDId.getOggetto2();

                                Boolean esito = eliminaMail(indirizzo, idMail);

                                if(esito){
                                    printOnLog("Mail ID: "+ idMail + " eliminata da: " + indirizzo);
                                }else{
                                    printOnLog("Eliminazione email: TENTATIVO FALLITO");
                                }
                                outputStream.writeObject(new Coppia("esito",esito));

                                break;

                            default: //chiudo l'InputStream e l'OutputStream del socket
                                outputStream.close();
                                inputStram.close();
                                incoming.close();
                        }
                    }
                }
            }catch (EOFException e) {
                Boolean message = false;
                outputStream.writeObject(message);
            } finally {//chiudo nuovamente l'InputStream e l'OutputStream del socket nel caso ci fossero state eccezioni da catturare
                incoming.close();
                outputStream.close();
                inputStram.close();
            }
        } else System.out.println("Il Socket è chiuso");
        }

        /*try{
            ServerSocket s= new ServerSocket(4445);

            while(true) {

                Socket socket = s.accept();
                System.out.println("Accettato socket " + socket);
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());//prendo l'input dal socket (la mail e la action)
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());//prendo l'output del socket

                try {



                    String[] user = "tizio@gmail.com".split("@");
                    //List<Email> newMail = FileQuery.readMailJSON();
                    List<Email> newMail = leggiCasella(user[0]);
                    outStream.writeObject(newMail);
                } finally {
                    socket.close();
                    outStream.close();
                    inStream.close();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }*/



    private Boolean eliminaMail(String email, int id) throws IOException {

        ArrayList<Email> casella = leggiCasella(email,-1);
        for (Email e : casella) {
            if (e.getId() == id) {
                casella.remove(e);
                scriviCasella(email, casella);
                return true;
            }
        }
        return false;
    }

    private ArrayList<Email> leggiCasella(String email,int lastID) throws IOException {
        ArrayList<Email> casella= new ArrayList<>();

        File file = new File(dataPathCasella(email));
        int ll= (int) file.length();
        if(ll!=0){
            InputStream fis = new FileInputStream(file);
            JsonReader jsonReader=Json.createReader(fis);
            JsonArray jsonArray= jsonReader.readArray();

            fis.close();

            for(int i=0; i<jsonArray.size(); i++){
                JsonObject mail=jsonArray.getJsonObject(i);
                JsonArray destArray= mail.getJsonArray("destinatari");
                List<String> dests=new ArrayList<>();
                for (JsonValue s : destArray) {
                    dests.add(s.toString());
                }
                Email e = new Email(mail.getString("mittente"), dests, mail.getString("oggetto"), mail.getString("testo"),
                        mail.getString("data"));
                e.setId(mail.getInt("id"));

                if(e.getId()>lastID){
                    casella.add(e);
                }

            }
            jsonReader.close();
        }
        return casella;


    }


    private void  scriviCasella(String email, List<Email> lettere) throws FileNotFoundException {
            JsonArrayBuilder mailListBuilder = Json.createArrayBuilder();
            for (Email e : lettere) {

                JsonObjectBuilder mailBuilder = Json.createObjectBuilder();
                JsonArrayBuilder destsBuilder = Json.createArrayBuilder();
                for (int i = 0; i < e.destinatari.size(); i++) {
                    destsBuilder.add(e.destinatari.get(i).substring(0,e.destinatari.get(i).toString().length()).replace("\"", ""));
                }
                //inserisco tutti gli elementi della mail all'interno di un oggetto json
                mailBuilder.add("id", e.id)
                        .add("mittente", e.mittente)
                        .add("destinatari", destsBuilder)
                        .add("oggetto", e.oggetto)
                        .add("testo", e.testo)
                        .add("data", e.data);
                mailListBuilder.add(mailBuilder);
            }
            JsonArray mailJsonObj = mailListBuilder.build();//costruisco tale array
            //creo un nuovo file (o lo sovrascrivo) e lo salvo all'interno della cartella riservata all'utente
            //che differenzio in base all'id
            OutputStream os = new FileOutputStream( dataPathCasella(email));
            JsonWriter jsonWriter = Json.createWriter(os);
            jsonWriter.writeArray(mailJsonObj);
            //per scrivere infine l'array json all'interno del file
            jsonWriter.close();

    }


    private void printOnLog(String msg) {
        listLog.getItems().add(msg);
    }


    private boolean existUser(String mail) throws IOException {

        File file = new File(dataPathCasella("user"));
        InputStream fis = new FileInputStream(file);
        JsonReader jsonReader=Json.createReader(fis);
        JsonArray arrayPartenza= jsonReader.readArray();
        JsonObject PrimoElement= arrayPartenza.getJsonObject(0);
        JsonArray emailCampo= PrimoElement.getJsonArray("email");

        for(int i=0; i<emailCampo.size(); i++){
            String address=emailCampo.getString(i);
            if(mail.equals(address)){return true;}
        }
        jsonReader.close();
        fis.close();
        return false;
    }


    private String dataPathCasella(String email) {
        String[] s=email.split("@");
        //return "C:\\Users\\ilmit\\Desktop\\PRO3-PROGETTO\\PROG3-PROGETTO\\src\\main\\java\\com\\example\\prog3progetto\\Server\\CASELLE\\" + s[0] + ".json";
        return "C:\\Users\\Dili\\Desktop\\PRO3-PROGETTO\\PROG3-PROGETTO\\src\\main\\java\\com\\example\\prog3progetto\\Server\\CASELLE\\" + s[0] + ".json";

    }



    //nella initialize crea un thread pool, in cui esegui il server socket/sochet accept while e sticazzi


}