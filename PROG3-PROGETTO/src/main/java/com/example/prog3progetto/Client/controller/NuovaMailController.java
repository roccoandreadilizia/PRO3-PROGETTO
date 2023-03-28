package com.example.prog3progetto.Client.controller;

import com.example.prog3progetto.Client.model.ClientModel;
import com.example.prog3progetto.ClientController;
import com.example.prog3progetto.Utils.Email;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class NuovaMailController implements Initializable {

    public static Stage stage = null;
    @FXML
    public TextField destField;
    @FXML
    public TextField oggettoField;
    @FXML
    public TextArea textField;
    @FXML
    public Button sendMail;

    public  String destinatari = "";
    public  String oggetto = "";
    public  String testo = "";

    public ClientModel modello;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    /**Setto inizialmente il valore dei destinatari,dell'oggetto o del testo
     * se sono stati precedentemente settati, altrimenti saranno vuoti */

    }

    public void initModel(ClientModel model){
        this.modello = model;

        try {/* qui se dobbiamo gestire le reply/inoltra */

            if(modello.getBottoneCliccato() == 0){
                destinatari = "";
                oggetto = "";
                testo = "";
            }
            if(modello.getBottoneCliccato() == 1){
                destinatari = model.getCurrentEmail().getMittente();
                oggetto = "";
                testo = "";
            }
            if(modello.getBottoneCliccato() == 2){
                destinatari = destReplyAll();
                oggetto = "";
                testo = "";
            }
            if(modello.getBottoneCliccato() == 3){
                destinatari = "";
                oggetto = modello.getCurrentEmail().getOggetto();
                testo = modello.getCurrentEmail().getTesto();
            }

            destField.setText(destinatari);
            oggettoField.setText(oggetto);
            textField.setText(testo);


        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private String destReplyAll() {
        //metto in un array le mail dei destinatari che sono separate da una virgola
        String[] split = modello.getCurrentEmail().destinatariToString().split(",");
        String tot = "";


        for(int i = 0; i< split.length;i++){
            String currSplit = split[i].replace("\"", "");//serve per eliminare \"
            if(currSplit.equals(modello.getEmail())){
            }else{
                tot += ";" + currSplit ;
            }
        }
        return modello.getCurrentEmail().getMittente() + tot;
    }


    @FXML
    public void inviaMail(ActionEvent actionEvent) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            Boolean invio = modello.sendMail(new Email( modello.getEmail(), listaDestinatari() ,
                    oggettoField.getText(), textField.getText(), formatter.format(date).toString()));
            if(invio){
                modello.startInfoAlert("Email inviata con successo!");
                ClientController.stage.close();
            }else{
                modello.startNegativeAlert("Campo destinatari errati!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listaDestinatari(){
        List<String> dests=new ArrayList<>() ;
        String stringazione=destField.getText();
        String[] array=stringazione.split(";");
        for (String s: array) {
            if(validateEmail(s)){
                return null;
            }
            dests.add(s);
        }
        return dests;
    }

    public static Boolean validateEmail(String s){
        if(s.matches(String.valueOf(Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$", Pattern.CASE_INSENSITIVE)))){
            return false;
        }else {
            return true;
        }
    }


    public String getDestinatari() {
        return destinatari;
    }

    public void setDestinatari(String destinatari) {
        this.destinatari = destinatari;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }
}
