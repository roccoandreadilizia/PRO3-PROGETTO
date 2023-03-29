package com.example.prog3progetto;

import com.example.prog3progetto.Client.controller.NuovaMailController;
import com.example.prog3progetto.Client.model.*;
import com.example.prog3progetto.Utils.Email;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
    public Button replayButton, replayAllButton, forwardButton, newEmailButton, deleteButton;

    public int listViewIndex = 0;
    public Email currentEmail;

    public static String myUser = null;

    public static Boolean mutuaEsclusione = false;

    public ClientModel getModel() {
        return model;
    }

    private ClientModel model;
    


    /*
    * Metodo per inzializzare il Modello che 
    * farà riferimento a questa istanza di clientController!
    * 
    * Si occuperà della prima fase di "Accesso"
    * e del riempimento della listView
    * 
    * */
    public void initModel(ClientModel model){
        this.model = model;

        try {
            model.clientStart(myUser);
            richiediMail();

            /*
            * Thread per la ricarica automatica della listView.
            * 
            * Verrà eseguito ogni 10 secondi
            * */
            RefreshThread t = new RefreshThread(this);
            t.start();       


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
        nameUserLabel.setText("Ciao " + myUser);

    }




    public void richiediMail(){

        try {
            List<Email> visualizza = model.askMail();
            for (Email e : visualizza) {
                int temp = e.getId();
                String mittente =e.getMittente();
                String oggetto= e.getOggetto();
                emailListView.getItems().add(mittente + ": " + oggetto +" ----- "+e.getData());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshEmail(){
        try {
            List<Email> visualizza = model.askMail();
            for (Email e : visualizza) {
                int temp = e.getId();
                String mittente =e.getMittente();
                String oggetto= e.getOggetto();

                emailListView.getItems().add(mittente + ": " + oggetto +" ----- "+e.getData());

            }
            if(visualizza.size()!=0){
                model.startInfoAlert("Hai ricevuto " + visualizza.size() + " nuove email");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void nuovaMail(ActionEvent actionEvent) throws IOException {
            creaFinestra();
    }

    public void deleteMail(ActionEvent actionEvent) throws IOException{
        model.setBottoneCliccato(5);
        if(model.getCurrentEmail()!=null){
            if(model.deleteMail(model.getCurrentEmail())){
                emailListView.getItems().remove(listViewIndex);
            }

        }
    }

    public void rispondiAll(ActionEvent actionEvent) throws IOException{
        model.setBottoneCliccato(2);
        if(model.getCurrentEmail()!=null){
            creaFinestra();
        }
    }

    public void rispondi(ActionEvent actionEvent) throws IOException{
        model.setBottoneCliccato(1);
        if(model.getCurrentEmail()!=null){
            creaFinestra();
        }
    }

    public void inoltra(ActionEvent actionEvent) throws IOException{
        model.setBottoneCliccato(3);
        if(model.getCurrentEmail()!=null){
            creaFinestra();
        }
    }

    public void creaFinestra() throws IOException{
        BorderPane root = new BorderPane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("newmail.fxml"));
        root.setCenter(fxmlLoader.load());
        NuovaMailController nuovaMailController = fxmlLoader.getController();
        nuovaMailController.initModel(this.model);

        Scene scene = new Scene(root, 600, 400);
        stage = new Stage();
        stage.setTitle("Nuova Email!");
        stage.setScene(scene);
        stage.show();
    }


    
    public void selectEmailFromView(MouseEvent args){
        listViewIndex = emailListView.getSelectionModel().getSelectedIndex();
        //seleziona l'indice della mail nella list view
        currentEmail = model.getEmailFromList(listViewIndex);
        

        if (currentEmail != null) {
            model.setCurrentEmail(currentEmail);
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

class RefreshThread extends Thread{

    ClientController clientController;
    public RefreshThread(ClientController clientController) {
        this.clientController=clientController;
    }



    public void run() {
        while(true){

            System.out.println("pre refresh");
            if(clientController.mutuaEsclusione == false){

                clientController.refreshEmail();

            }
            System.out.println("refresh");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}