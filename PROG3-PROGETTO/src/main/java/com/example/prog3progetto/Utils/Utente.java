package com.example.prog3progetto.Utils;

import java.io.Serializable;

public class Utente implements Serializable {

    private String email;



    private String headerEmail;

    public Utente(String email) {
        this.email = email;
        String[] temp = email.split("@");
        this.headerEmail = temp[0];
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String userFolder() {

        return "C:\\Users\\Dili\\Desktop\\PRO3-PROGETTO\\PROG3-PROGETTO\\src\\main\\java\\com\\example\\prog3progetto\\Server\\CASELLE\\" + headerEmail + ".json";
    }

    public String getHeaderEmail() {
        return headerEmail;
    }
}
