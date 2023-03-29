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
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ServerController implements Initializable {


    public Utente utente = null;
    @FXML
    private ListView listLog;

    public static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> socketThreadStart()).start();
    }



    public void socketThreadStart() {
        try {
            ServerSocket s = new ServerSocket(4445);//la porta l'ho scelta a caso, in modo che non andasse ad interferire con altri processi (ad esempio la porta 8080)
            System.out.println("Creazione di un socket all'indirizzo: " + s.getLocalSocketAddress());

            ExecutorService exec= Executors.newFixedThreadPool(3);
            while (true) {//creo un loop infinito per gestire i client che fanno richiesta di connessione al socket


                Socket incoming = s.accept();//questo while non va in loop infinito poichè si ferma subito in s.accept(), e aspetta che un client faccia richiesta al server


                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GestoreThread(incoming);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void GestoreThread(Socket incoming)throws IOException, ClassNotFoundException{

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

                                String user = (String) c.getOggetto2();

                                printOnLog("Login by " + user);
                                outputStream.writeObject(new Utente(user));//Scrivo nell'output del socket
                                break;


                            case 2:
                                Coppia p2 = (Coppia) c.getOggetto2();
                                Utente utet = (Utente) p2.getOggetto1();
                                List<Email> newMail = leggiCasella((String) utet.getEmail(), (Integer) p2.getOggetto2());
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
                                    printOnLog(sending.getMittente() + " Ha inviato una nuova email");//messaggio sul terminale del server

                                    for (String u : dests) {//per ogni user trovato nell'elenco dei destinatari
                                        emails = leggiCasella(u,-1);
                                        int lastID = 0;
                                        if (emails.size() > 0)
                                            //se l'elenco delle mail di user non è vuoto allora prendo l'ultimo id e lo incremento
                                            lastID = emails.get(emails.size() - 1).getId() + 1;
                                        sending.setId(lastID);//setto il nuovo id -> se l'elenco è vuoto è uguale a zero
                                        emails.add(sending);//aggiungo la nuova email

                                        scriviCasella(u, emails);//scrivo il nuovo elenco di user con la nuova mail aggiunta
                                        printOnLog(u + " Ha ricevuto una nuova email");
                                        for (String s : dests) {

                                            Boolean message = true;
                                            outputStream.flush();
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
                                outputStream.flush();
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
                try{
                    outputStream.writeObject(message);
                }catch (SocketException ee){
                    System.out.println("Socket chiuso!");
                }
            }catch (StreamCorruptedException e){

            }
            finally {//chiudo nuovamente l'InputStream e l'OutputStream del socket nel caso ci fossero state eccezioni da catturare

                try{
                    incoming.close();
                    outputStream.close();
                    inputStram.close();
                }catch (SocketException e){
                    System.out.println("Socket chiuso!");
                }
            }
        } else System.out.println("Il Socket è chiuso");
        }




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

        readWriteLock.readLock().lock();
        try {
            int ll = (int) file.length();
            if (ll != 0) {
                InputStream fis = new FileInputStream(file);
                JsonReader jsonReader = Json.createReader(fis);
                JsonArray jsonArray = jsonReader.readArray();

                fis.close();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject mail = jsonArray.getJsonObject(i);
                    JsonArray destArray = mail.getJsonArray("destinatari");
                    List<String> dests = new ArrayList<>();
                    for (JsonValue s : destArray) {
                        dests.add(s.toString());
                    }
                    Email e = new Email(mail.getString("mittente"), dests, mail.getString("oggetto"), mail.getString("testo"),
                            mail.getString("data"));
                    e.setId(mail.getInt("id"));

                    if (e.getId() > lastID) {
                        casella.add(e);
                    }

                }
                jsonReader.close();
            }

        }finally {
            readWriteLock.readLock().unlock();
        }
        return casella;

    }


    private void  scriviCasella(String email, List<Email> lettere) throws FileNotFoundException {
        readWriteLock.writeLock().lock();
            try {
                JsonArrayBuilder mailListBuilder = Json.createArrayBuilder();
                for (Email e : lettere) {

                    JsonObjectBuilder mailBuilder = Json.createObjectBuilder();
                    JsonArrayBuilder destsBuilder = Json.createArrayBuilder();
                    for (int i = 0; i < e.destinatari.size(); i++) {
                        destsBuilder.add(e.destinatari.get(i).substring(0, e.destinatari.get(i).toString().length()).replace("\"", ""));
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
                OutputStream os = new FileOutputStream(dataPathCasella(email));
                JsonWriter jsonWriter = Json.createWriter(os);
                jsonWriter.writeArray(mailJsonObj);
                //per scrivere infine l'array json all'interno del file
                jsonWriter.close();
            }finally {
                readWriteLock.writeLock().unlock();
            }

    }


    private void printOnLog(String msg) {
        try{listLog.getItems().add(msg);}
        catch (IllegalStateException e){}
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




}