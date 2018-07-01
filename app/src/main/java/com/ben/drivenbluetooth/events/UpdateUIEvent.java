package com.ben.drivenbluetooth.events;

public class UpdateUIEvent {
    public enum EventType {
        DataFile
    }

    public final EventType eventType;

    public UpdateUIEvent(EventType e) {
        eventType = e;
    }
}
