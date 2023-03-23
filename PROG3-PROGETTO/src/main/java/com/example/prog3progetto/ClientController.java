package com.example.prog3progetto;

import com.example.prog3progetto.Client.model.*;
import com.example.prog3progetto.Utils.Email;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    public int listViewIndex = 0;
    public Email currentEmail;

    public static String myUser = null;
    private ClientModel model;



    
    public void initModel(ClientModel model){
        this.model = model;

        try {
            model.clientStart(nameUserLabel.getText());
            refreshMail();


        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

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

    }




    public void refreshMail(){

        try {
            List<Email> visualizza = model.askMail();
            for (Email e : visualizza) {
                String mittente =e.getMittente();
                String oggetto= e.getOggetto();
                emailListView.getItems().add(oggetto+" ----- "+e.getData());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void nuovaMail(ActionEvent actionEvent) throws IOException {
        creaFinestra();
    }

    public void deleteMail(ActionEvent actionEvent) throws IOException{

    }

    public void rispondiAll(ActionEvent actionEvent) throws IOException{

    }

    public void rispondi(ActionEvent actionEvent) throws IOException{

    }

    public void inoltra(ActionEvent actionEvent) throws IOException{

    }

    public void creaFinestra() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("newmail.fxml"));
        Parent content = loader.load();
        stage = new Stage();
        stage.setScene(new Scene(content));
        stage.show();

    }

    public void selectEmailFromView(MouseEvent args){
        listViewIndex = emailListView.getSelectionModel().getSelectedIndex();//seleziona l'index della mail nella list view
        currentEmail = model.getEmailFromList(listViewIndex);//prende la mail corrispondente all'hash index

        if (currentEmail != null) {
            fromLabel.setText(currentEmail.getMittente());
            toLabel.setText(currentEmail.destinatariToString().replace("\"", ""));//per eliminare le virgolette "a@a.a"
            objectLabel.setText(currentEmail.getOggetto());
            textLabel.setText(currentEmail.getTesto());

            replayAllButton.setVisible(true);
            replayButton.setVisible(true);
            forwardButton.setVisible(true);
            fromLabel.setVisible(true);
            toLabel.setVisible(true);
            objectLabel.setVisible(true);
            textLabel.setVisible(true);
            deleteButton.setVisible(true);
        }
    }

}