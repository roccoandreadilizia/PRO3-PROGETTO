package com.example.prog3progetto.Utils;

import java.io.Serializable;

public class Coppia implements Serializable {

    private Object oggetto1;
    private Object oggetto2;

    public Coppia(Object oggetto1, Object oggetto2) {
        this.oggetto1 = oggetto1;
        this.oggetto2 = oggetto2;
    }

    public Object getOggetto1() {
        return oggetto1;
    }

    public void setOggetto1(Object idUtente) {
        this.oggetto1 = idUtente;
    }

    public Object getOggetto2() {
        return oggetto2;
    }

    public void setOggetto2(Object datiUtente) {
        this.oggetto2 = datiUtente;
    }
}
