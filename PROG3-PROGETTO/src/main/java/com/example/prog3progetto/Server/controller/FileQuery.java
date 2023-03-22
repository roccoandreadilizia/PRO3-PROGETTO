package com.example.prog3progetto.Server.controller;

import com.example.prog3progetto.Utils.Email;

import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileQuery {

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    /** Leggo il file json e restituisco la lista di mail, in base all'user passato */
    public static List<Email> readMailJSON() throws IOException {

        List<Email> newMailList = new ArrayList<>();
        File file = new File("C:\\Users\\Dili\\Desktop\\PRO3-PROGETTO\\PROG3-PROGETTO\\src\\main\\java\\com\\example\\prog3progetto\\Server\\CASELLE\\tizio.json");


        try {
            if(file.length()!= 0) {

                InputStream fis = new FileInputStream(file);//leggo il file
                JsonReader jsonReader = Json.createReader(fis);
                JsonArray jsonArray = jsonReader.readArray();//leggo l'array json

                fis.close();//qui possiamo già chiudere la risorsa di lettura del file

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject mail = jsonArray.getJsonObject(i);
                    JsonArray destsArray = mail.getJsonArray("destinatari");
                    List<String> destinatari = new ArrayList<>();
                    for (JsonValue s : destsArray) {//scompongo l'array json di destinatari
                        destinatari.add(s.toString());
                    }
                    //salvo tutto dentro Email e
                    Email e = new Email(mail.getString("mittente"), destinatari, mail.getString("oggetto"), mail.getString("testo"), mail.getString("data"));
                    e.setId(mail.getInt("id"));//setto l'id della mail
                    newMailList.add(e);
                    //e.printMailContent();
                }
                jsonReader.close();
            }
        } finally {

        }

        return newMailList;
    }
}
