module com.example.prog3progetto {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;
    requires javafx.base;
    requires javax.json;
    requires json.simple;

    exports com.example.prog3progetto.Client.controller;
    opens com.example.prog3progetto.Client.controller to javafx.fxml;

    exports com.example.prog3progetto.Server.controller;
    opens com.example.prog3progetto.Server.controller to javafx.fxml;

    exports com.example.prog3progetto;
    opens com.example.prog3progetto to javafx.fxml;
}