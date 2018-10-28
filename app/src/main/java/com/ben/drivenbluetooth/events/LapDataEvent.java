package com.ben.drivenbluetooth.events;

// Gets used to distribute last laps average data once a new lap has been started
public class LapDataEvent {
    public double voltage;
    public double current;
    public double time;


    public LapDataEvent(double _voltage) {
        voltage = _voltage;
    }
}
