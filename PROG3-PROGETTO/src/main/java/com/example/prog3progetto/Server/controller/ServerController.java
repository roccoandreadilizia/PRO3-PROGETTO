package com.example.prog3progetto.Server.controller;


import com.example.prog3progetto.Utils.Coppia;
import com.example.prog3progetto.Utils.CoppiaUtenteSocket;
import com.example.prog3progetto.Utils.Email;
import com.example.prog3progetto.Utils.Utente;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import javax.json.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;




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
            int i = 1;
            while (true) {//creo un loop infinito per gestire i client che fanno richiesta di connessione al socket

                int finalI = i;//contatore dei thread
                Socket incoming = s.accept();//questo while non va in loop infinito poichè si ferma subito in s.accept(), e aspetta che un client faccia richiesta al server

                new Thread() {
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
                        Platform.runLater(() -> {
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

                i++;
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
                                /*


                                DOVREMMO METTERE UN FILE CONTENENTE DELLE EMAIL E CONFRONTARLE?

                                 */

                                //setto la coppia username-socket (prima username era null)
                                ServerController.clients.set(indice - 1, new CoppiaUtenteSocket(ServerController.clients.get(indice - 1).socket, utente.getEmail()));

                                listLog.getItems().add("Login by " + user.getEmail());
                                outputStream.writeObject(user);//Scrivo nell'output del socket
                                break;

                            case 2:
                                //List<Email> newMail = FileQuery.readMailJSON();
                                List<Email> newMail = leggiCasella(utente.getEmail());
                                outputStream.writeObject(newMail);
                                break;
                        }
                    }
                }
            }catch (EOFException e) {
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


    private ArrayList<Email> leggiCasella(String email) throws IOException {
        ArrayList<Email> casella= new ArrayList<>();

        File file = new File(utente.userFolder());
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
                for (JsonValue s : destArray) {//scompongo l'array json di destinatari
                    dests.add(s.toString());
                }
                Email e = new Email(mail.getString("mittente"), dests, mail.getString("oggetto"), mail.getString("testo"),
                        mail.getString("data"));
                e.setId(mail.getInt("id"));
                casella.add(e);
            }
            jsonReader.close();
        }


        return casella;


    }

    private void scriviCasella(String email, Email lettera){

    }

    //nella initialize crea un thread pool, in cui esegui il server socket/sochet accept while e sticazzi
    //crea un metodo per scrivere in una cassella
    //crea un metodo per leggere tutte le mail da una cassella


}