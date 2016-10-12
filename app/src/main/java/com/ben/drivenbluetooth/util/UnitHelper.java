package com.ben.drivenbluetooth.util;

import com.ben.drivenbluetooth.Global;

/**
 * Created by BNAGY4 on 12/10/2016.
 */
public final class UnitHelper {
    public static double getSpeed(double speedMPS, Global.UNIT unit) {
        double retSpeed;
        switch (unit) {
            case MPH:
                retSpeed = speedMPS * 2.24;
                break;
            case KPH:
                retSpeed = speedMPS * 3.6;
                break;
            case FFF:
                retSpeed = speedMPS * 6013;
                break;
            case KNOT:
                retSpeed = speedMPS * 1.94;
                break;
            case MPS:
                retSpeed = speedMPS;
                break;
            default:
                retSpeed = speedMPS;
        }

        return retSpeed;
    }

    public static String getSpeedText(double speedMPS, Global.UNIT unit) {
        String retString = "";
        speedMPS = getSpeed(speedMPS, unit);
        switch (unit) {
            case MPH:
                retString = String.format("%.0f mph", speedMPS);
                break;
            case KPH:
                retString = String.format("%.0f kph", speedMPS);
                break;
            case MPS:
                retString = String.format("%.0f m/s", speedMPS);
                break;
            case FFF:
                retString = String.format("%.0f kFl/Fn", speedMPS / 1000);
                break;
            case KNOT:
                retString = String.format("%.0f kts", speedMPS);
                break;
        }

        return retString;
    }

    public static float getMaxSpeed(Global.UNIT unit) {
        float retSpeed;
        switch (unit) {
            case MPH:
                retSpeed = 50;
                break;
            case KPH:
                retSpeed = 70;
                break;
            case MPS:
                retSpeed = 20;
                break;
            case FFF:
                retSpeed = 120000;
                break;
            case KNOT:
                retSpeed = 40;
                break;
            default:
                retSpeed = 50;
                break;
        }

        return retSpeed;
    }
}
