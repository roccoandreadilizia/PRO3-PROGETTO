package com.example.prog3progetto.Client.controller;

import com.example.prog3progetto.Client.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ClientController implements Initializable {

    public static Stage stage = null;

    @FXML
    public Label fromLabel;
    @FXML
    public Label toLabel;
    @FXML
    public Label objectLabel;
    @FXML
    public ScrollPane textPane;
    @FXML
    public Label nameUserLabel;
    @FXML
    public ListView emailListView;
    @FXML
    public Label textLabel;

    @FXML
    public Button replayButton, replayAllButton, forwardButton, newMailButton, deleteButton;


    String myUser = null;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fromLabel.setVisible(false);
        toLabel.setVisible(false);
        objectLabel.setVisible(false);
        textLabel.setVisible(false);

        System.out.println("Inserisci email utente:");
        Scanner s = new Scanner(System.in);
        myUser = s.nextLine();
        nameUserLabel.setText(myUser);

        try {

            refreshMail();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        //richiedi le mail

        //fillare le mail
        //scrivere benvenuto user!
        //mettere un controlla costantemente la tua casella?
    }


    public void scriviMail(ActionEvent actionEvent) throws IOException{
        // da fare

    }

    public void refreshMail(){

        try {
            ClientModel.askMail();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }





}