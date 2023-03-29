package com.example.prog3progetto.Client.model;

import com.example.prog3progetto.ClientController;
import com.example.prog3progetto.ClientView;
import com.example.prog3progetto.Utils.Coppia;
import com.example.prog3progetto.Utils.Email;
import com.example.prog3progetto.Utils.Utente;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Scanner;

public class ClientModel {

    public static Socket socket;
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;

    public static Object obj;
    private static List<Email> casella;

    public Utente utente;

    public static int maxIdEmailLetta = -1;

    public Email currentEmail = null;
    public  int bottoneCliccato;



    /*Metodo usato in fase d'inizializzazzione per effettuare una sorta di login da tastiera*/
    public void clientStart(String email) throws IOException, MYSERVERException {
        try{
            socket = new Socket("127.0.0.1",4445);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            Coppia c = new Coppia(1,email);
            outputStream.writeObject(c);

            inputStream = new ObjectInputStream(socket.getInputStream());
            obj = inputStream.readObject();

            if(obj instanceof Utente){
                utente = (Utente) obj;
                startInfoAlert("Benvenuto " + utente.getHeaderEmail());
            }else{
                System.out.println("Errore accesso");
            }
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (ConnectException ce) {
            startNegativeAlert("Server Offline, prova a riconnetterti");
            maxIdEmailLetta = -1;
            casella = null;
            throw new MYSERVERException("Server OFFLINE","Server off");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public Boolean sendMail(Email e) throws IOException, ClassNotFoundException, MYSERVERException {

        if(e.getDestinatari()==null){
            startNegativeAlert("Nessun destinatario inserito!");
            return false;
        }

        boolean sent = false;
        Socket sendSocket = new Socket("127.0.0.1", 4445);

        try{
            ClientController.mutuaEsclusione = true;
            outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); //è ciò che mandiamo al server

            Coppia c = new Coppia(3,e);

            outputStream.writeObject(c);
            inputStream = new ObjectInputStream(sendSocket.getInputStream());

            obj=inputStream.readObject();

            if(obj instanceof Boolean) {
                Boolean m = (Boolean) obj;
                sent = m;
            }
        }catch (ConnectException ex) {
            startNegativeAlert("Server Offline, prova a riconnetterti!");
            throw new MYSERVERException("Server OFFLINE","Server off");
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            ClientController.mutuaEsclusione = false;//libero la mutua esclusione
            outputStream.flush();
            outputStream.close();

        }
        sendSocket.close();
        return sent;


    }

    /** Chiedo al server la lista delle mail associate all'utente */
    public List<Email> askMail() throws IOException, MYSERVERException {

        List<Email> casellaRefresh = null;

        try {
            if(socket==null||socket.isClosed()){
                socket = new Socket("127.0.0.1",4445);
            }
            outputStream = new ObjectOutputStream(socket.getOutputStream());//ciò che mando al server
            Coppia p = new Coppia(utente,maxIdEmailLetta);
            Coppia c = new Coppia(2,p);
            outputStream.writeObject(c);



            inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Collegato");


            obj=inputStream.readObject();

            if(casella == null){
                casella = (List<Email>) obj;
                for (Email e: casella){
                    if(e.getId() > maxIdEmailLetta){
                        maxIdEmailLetta = e.getId();
                    }
                }
            }else{
                casellaRefresh = (List<Email>) obj;
                for (Email e: casellaRefresh){
                    if(e.getId() > maxIdEmailLetta){
                        maxIdEmailLetta = e.getId();
                    }
                    /*
                    * Incremento la lista casella per poterla utilizzare
                    * durante la selezione di una email da listView
                    *
                    * Se così non facessi creerei inconsistenza tra
                    * l'indice della listView e la lunghezza della casella
                    * */
                    casella.add(e);
                }
            }

        } catch (ConnectException ce) {
            startNegativeAlert("Server Offline, prova a riconnetterti");
            throw new MYSERVERException("Server OFFLINE","Server off");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            outputStream.flush();
            inputStream.close();
            outputStream.close();
        }
        if(casellaRefresh != null){
            return casellaRefresh;
        }else{
            return casella;
        }

    }

    /** Chiedo al server di eliminare la email associata selezionata */
    public boolean deleteMail(Email email) throws IOException, MYSERVERException {


        Socket sendSocket = null;
        Boolean esito = false;
        try {
            sendSocket = new Socket("127.0.0.1", 4445);
            ClientController.mutuaEsclusione = true;
            outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); //è ciò che mandiamo al server


            Coppia c = new Coppia(utente.getEmail(), email.getId());
            Coppia c2 = new Coppia(4, c);
            outputStream.writeObject(c2);

            inputStream = new ObjectInputStream(sendSocket.getInputStream());
            obj = inputStream.readObject();

            if(obj instanceof Coppia){
                Coppia coppiaEsito = (Coppia) obj;
                esito = (Boolean) coppiaEsito.getOggetto2();

                if(esito){
                    startInfoAlert("Email eliminata!");
                }else{
                    startInfoAlert("Email NON eliminata!");
                }
            }


        } catch (ConnectException ce) {
            startNegativeAlert("Server Offline, prova a riconnetterti");
            throw new MYSERVERException("Server OFFLINE","Server off");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ClientController.mutuaEsclusione = false;
            outputStream.flush();
            inputStream.close();
            outputStream.close();

        }
        sendSocket.close();
        return esito;
    }


    public void startNegativeAlert(String s) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore!");
                alert.setHeaderText(s);
                alert.showAndWait();
            }}
        );
    }

    public void startInfoAlert(String s) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(s);
                alert.showAndWait();
            }}
        );
    }

    public Email getEmailFromList(int listViewIndex) {

        try{
            return casella.get(listViewIndex);
        }catch(IndexOutOfBoundsException e){return null;}
    }

    public String getEmail(){
        return utente.getEmail();
    }

    public Email getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(Email currentEmail) {
        this.currentEmail = currentEmail;
    }

    public int getBottoneCliccato() {
        return bottoneCliccato;
    }

    public void setBottoneCliccato(int bottoneCliccato) {
        this.bottoneCliccato = bottoneCliccato;
    }
}

