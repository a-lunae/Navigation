package com.example.navigation;

import android.annotation.SuppressLint;

import java.io.Serializable;

public class DbRow implements Serializable {
    private int id;
    private String name, mac, signalStrength;

    public DbRow(int id, String name, String mac, String signalStrength) {
        this.id = id;
        this.name = name;
        this.mac = mac;
        this.signalStrength = signalStrength;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    @Override
    public String toString() {
        return "Имя: " + name +
                "   MAC-адрес:" + mac +
                "   Сила сигнала=" + signalStrength;
    }
}
