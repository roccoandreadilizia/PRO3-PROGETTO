package com.example.prog3progetto;

import com.example.prog3progetto.Client.model.ClientModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        BorderPane root = new BorderPane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mailview.fxml"));
        root.setCenter(fxmlLoader.load());
        ClientController clientController = fxmlLoader.getController();

        ClientModel model = new ClientModel();
        clientController.initModel(model);



        Scene scene = new Scene(root, 900, 500);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}