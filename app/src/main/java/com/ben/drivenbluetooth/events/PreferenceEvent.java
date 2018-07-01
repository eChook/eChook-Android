package com.ben.drivenbluetooth.events;

public class PreferenceEvent {
    public enum EventType {
        ModeChange,
        BTDeviceNameChange,
        CarNameChange,
        UDPChange,
        LocationChange,
        DataFileSettingChange
    }
    public final EventType eventType;
    public PreferenceEvent(EventType e) {
        eventType = e;
    }
}
