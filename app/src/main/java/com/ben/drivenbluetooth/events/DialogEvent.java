package com.ben.drivenbluetooth.events;

public class DialogEvent {
    public final String title;
    public final String message;

    public DialogEvent(String _title, String _message) {
        title = _title;
        message = _message;
    }
}
