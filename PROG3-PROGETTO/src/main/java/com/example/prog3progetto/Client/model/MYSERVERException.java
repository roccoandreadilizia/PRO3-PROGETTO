package com.example.prog3progetto.Client.model;

public class MYSERVERException extends Exception {
    String s;

    public MYSERVERException(String message, String s) {
        super(message);
        this.s = s;
    }
}
