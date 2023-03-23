package com.example.prog3progetto.Utils;

import java.net.Socket;

/**
 * Creo una coppia utente-socket
 * */
public class CoppiaUtenteSocket {
    public Socket socket;
    public String utente;

    public CoppiaUtenteSocket(Socket socket, String utente) {
        this.socket = socket;
        this.utente = utente;
    }
}