package com.ben.drivenbluetooth.events;

public class LocationEvent {
    public enum EventType {
        NewLap
    }

    public final EventType eventType;

    public LocationEvent(EventType e) {
        eventType = e;
    }
}
