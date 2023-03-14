package com.example.prog3progetto.Client.model;

import com.example.prog3progetto.ClientController;
import com.example.prog3progetto.Utils.Email;
import javafx.scene.control.Alert;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ClientModel {

    public static Socket socket;

    private static ObjectOutputStream outputStream = null;

    private static ObjectInputStream inputStream = null;

    public static Object obj;

    private static List<Email> casella;

    //lista delle mail
    //porta e ip
    //allert dal server
    //inviare/inoltrare/rispondere a una mail
    //eliminare una mail




    public static Boolean sendMail(Email e) throws IOException, EOFException{
        boolean sent = false;
        Socket sendSocket = new Socket("127.0.0.1", 4445);
        outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); //è ciò che mandiamo al server

        outputStream.writeObject(e);
        inputStream = new ObjectInputStream(sendSocket.getInputStream());

        /*try {
            obj = inputStream.readObject();
            if
        } catch (EOFException ex) {
            throw new RuntimeException(ex);
        }
        */
        outputStream.flush();
        outputStream.close();

        sendSocket.close();
        return sent;


    }


    public static List<Email> askMail() throws IOException {

        try {
            if(socket==null||socket.isClosed()){
                socket = new Socket("127.0.0.1",4445);
            }
            outputStream = new ObjectOutputStream(socket.getOutputStream());//ciò che mando al server
            inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Collegato");


            obj=inputStream.readObject();
            casella=(List<Email>) obj;
            System.out.println("ricevuto roba");
            for (Email e: casella) {
                System.out.println(e.getTesto());
            }


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




    public static void startAlert(List<String> notSent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Errore!");
        alert.setHeaderText("Destinatario NON trovato!");
        String dests = "";
        for (int i = 0; i < notSent.size(); i++) {
            if(i == 0) dests += notSent.get(i);
            else dests += " - " + notSent.get(i);
        }
        alert.setContentText(dests);
        alert.show();
    }
    public static void startAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(s);
        alert.show();
    }
}
