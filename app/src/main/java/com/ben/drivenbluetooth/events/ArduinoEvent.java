package com.ben.drivenbluetooth.events;

public class ArduinoEvent {
    // https://docs.google.com/spreadsheets/d/1894rswb_CalcgParDVzyCzok7YSILqCtenP4maTdhaY/edit#gid=1544820641
    public enum EventType {
        Volts,
        VoltsAux,
        Amps,
        AmpHours,
        ThrottleInput,
        ThrottleActual,
        WheelSpeedMPS,
        MotorSpeedRPM,
        TemperatureA,
        TemperatureB,
        TemperatureC,
        GearRatio,
        FanDuty,
        BrakeStatus,
        CycleView,
        LaunchMode,
        PerformanceMetric,
        WattHours
    }

    public double data;

    public final EventType eventType;

    public ArduinoEvent(EventType e, double _data) {

        eventType = e;
        data = _data;
    }
}
