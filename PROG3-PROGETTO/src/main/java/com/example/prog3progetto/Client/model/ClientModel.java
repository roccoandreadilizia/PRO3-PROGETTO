package com.example.prog3progetto.Client.model;

import com.example.prog3progetto.Utils.Coppia;
import com.example.prog3progetto.Utils.Email;
import com.example.prog3progetto.Utils.Utente;
import javafx.scene.control.Alert;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

public class ClientModel {

    public static Socket socket;
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;
    public static Object obj;
    private static List<Email> casella;
    public Utente utente;



    public Email currentEmail = null;
    public  int bottoneCliccato;

    //lista delle mail
    //porta e ip
    //allert dal server
    //inviare/inoltrare/rispondere a una mail
    //eliminare una mail


    /*Metodo usato in fase d'inizializzazzione per effettuare una sorta di login da tastiera*/
    public void clientStart(String email) throws IOException{



        try{
            socket = new Socket("127.0.0.1",4445);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            Coppia c = new Coppia(1,email);
            outputStream.writeObject(c);

            inputStream = new ObjectInputStream(socket.getInputStream());
            obj = inputStream.readObject();

            if(obj instanceof Utente){
                utente = (Utente) obj;
                System.out.println("Accesso effettuato");
            }else{
                System.out.println("Errore accesso");
            }
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (ConnectException ce) {
            startAlert("Server Offline, prova a riconnetterti");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public Boolean sendMail(Email e) throws IOException, EOFException, ClassNotFoundException {
        if(e.destinatari==null){
            startAlert();
            return false;
        }
        boolean sent = false;
        Socket sendSocket = new Socket("127.0.0.1", 4445);
        outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); //è ciò che mandiamo al server

        Coppia c = new Coppia(3,e);

        outputStream.writeObject(c);
        inputStream = new ObjectInputStream(sendSocket.getInputStream());

        obj=inputStream.readObject();

        if(obj instanceof Boolean) {
            Boolean m = (Boolean) obj;
            sent = m;
        }

        outputStream.flush();
        outputStream.close();

        sendSocket.close();
        return sent;


    }

    /** Chiedo al server la lista delle mail associate all'user */
    public List<Email> askMail() throws IOException {

        try {
            if(socket==null||socket.isClosed()){
                socket = new Socket("127.0.0.1",4445);
            }
            outputStream = new ObjectOutputStream(socket.getOutputStream());//ciò che mando al server
            Coppia c = new Coppia(2,utente);
            outputStream.writeObject(c);


            inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Collegato");


            obj=inputStream.readObject();
            casella = (List<Email>) obj;
            System.out.println("ricevuto roba");



        } catch (ConnectException ce) {
            //ClientController.startAlert("Server Offline, prova a riconnetterti");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            //outputStream.flush();
            //outputStream.close();
        }
        return casella;
    }




    public void startAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Errore!");
        alert.setHeaderText("Destinatario NON trovato!");
        alert.show();
    }
    public void startAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(s);
        alert.show();
    }

    public Email getEmailFromList(int listViewIndex) {
        return casella.get(listViewIndex);
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
