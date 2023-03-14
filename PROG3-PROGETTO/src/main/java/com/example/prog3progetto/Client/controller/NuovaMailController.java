package com.example.prog3progetto.Client.controller;

import com.example.prog3progetto.Client.model.ClientModel;
import com.example.prog3progetto.Utils.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NuovaMailController implements Initializable {

    @FXML
    public TextField destField;
    @FXML
    public TextField oggettoField;
    @FXML
    public TextArea textField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML

    public void inviaMail(ActionEvent actionEvent) {

        try {
            ClientModel.sendMail(new Email("tizio@gmail.com", null, oggettoField.toString(), textField.toString(), null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
