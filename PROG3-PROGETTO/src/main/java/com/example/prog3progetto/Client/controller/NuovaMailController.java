package com.example.prog3progetto.Client.controller;

import com.example.prog3progetto.Client.model.ClientModel;
import com.example.prog3progetto.Utils.Email;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

public class NuovaMailController implements Initializable {

    @FXML
    public TextField destField;
    @FXML
    public TextField oggettoField;
    @FXML
    public TextArea textField;
    @FXML
    public Button sendMail;

    public static String destinatari = "";
    public static String oggetto = "";
    public static String testo = "";

    public ClientModel modello;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    /**Setto inizialmente il valore dei destinatari,dell'oggetto o del testo
     * se sono stati precedentemente settati, altrimenti saranno vuoti */
            destField.setText(destinatari);
            oggettoField.setText(oggetto);
            textField.setText(testo);
            destinatari = "";
            oggetto = "";
            testo = "";
    }

    public void initModel(ClientModel model){
        this.modello = model;

        try {/* qui se dobbiamo gestire le reply/inoltra */

        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }


    @FXML
    public void inviaMail(ActionEvent actionEvent) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            modello.sendMail(new Email( modello.getEmail(), listaDestinatari() ,
                    oggettoField.getText(), textField.getText(), formatter.format(date).toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ciaoNuovaFinestra");
    }

    public List<String> listaDestinatari(){
        List<String> dests=new ArrayList<>() ;
        String stringazione=destField.getText();
        String[] array=stringazione.split(";");
        for (String s: array) {
            dests.add(s);
        }
        return dests;
    }
}
