package com.ben.drivenbluetooth.events;

public class SnackbarEvent {
    public final String message;

    public SnackbarEvent(String message) {
        this.message = message;
    }

    public SnackbarEvent(Exception e) {
        this.message = e.getMessage();
    }
}
