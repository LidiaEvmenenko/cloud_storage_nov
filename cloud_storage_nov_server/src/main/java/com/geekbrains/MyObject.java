package com.geekbrains;

import java.io.Serializable;

public class MyObject implements Serializable {
    private String name;
    protected byte[] mas;

    MyObject(String name){
        this.name = name;
        this.mas = new byte[100];
    }

    MyObject(String name, byte[] mas){
        this.name = name;
        this.mas = mas;
    }

    public void setMas(byte[] mas) {
        this.mas = mas;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] getMas() {
        return mas;
    }
}