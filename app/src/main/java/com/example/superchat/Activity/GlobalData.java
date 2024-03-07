package com.example.superchat.Activity;

import com.example.superchat.Model.Messages;

import java.util.ArrayList;
import java.util.List;

public class GlobalData {
    private static GlobalData instance;
    private List<Messages> messagesList;

    private GlobalData() {
        // Private constructor to enforce singleton pattern
        messagesList = new ArrayList<>();
    }

    public static synchronized GlobalData getInstance() {
        if (instance == null) {
            instance = new GlobalData();
        }
        return instance;
    }

    public List<Messages> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }
}

