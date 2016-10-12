package com.ben.drivenbluetooth.util;

import com.ben.drivenbluetooth.Global;

import java.util.ArrayList;
import java.util.Collections;

public final class GearHelper {

    private final static double TARGET_RPM = 1850;
    private static int CurrentGear = 0;
    private static ArrayList<Double> exactRatios = new ArrayList<>();

    public static int GetGear(double gearRatio, int motorTeeth, int[] wheelTeeth) {
        if (wheelTeeth != null && motorTeeth != 0) {
            double[][] gearRatioRanges = getRatioRange(motorTeeth, wheelTeeth);

            for (int i = 0; i < gearRatioRanges.length; i++) {
                // lower bound is [0], upper bound is [1]
                if (gearRatio >= gearRatioRanges[i][0] && gearRatio <= gearRatioRanges[i][1]) {
                    CurrentGear = i + 1;
                    return CurrentGear;
                } // else continue with loop
            }
            // if we reach here then we have an indeterminate gear ratio
            CurrentGear = -1;
        } else {
            // wheel teeth or motor teeth haven't been defined
            CurrentGear = 0;
            return CurrentGear;
        }

        // redundant
        return CurrentGear;
    }
    
    private static double[][] getRatioRange(int motorTeeth, int[] wheelTeeth) {
        ArrayList<Double> exactRatios = getExactRatios(motorTeeth, wheelTeeth);
        
        double[][] gearRatioRange = new double[exactRatios.size()][2];
        for (int i = 0; i < exactRatios.size(); i++) {
            double exactRatio = exactRatios.get(i);
            double delta;

            // always these two EXCEPT on the last turn
            if (i < exactRatios.size() - 1) {
                delta = Math.abs(exactRatios.get(i) - exactRatios.get(i + 1));
                if (i == 0) {
                    // first element we have to be special
                    gearRatioRange[i][1] = exactRatio + delta / 2;
                }

                gearRatioRange[i][0] = exactRatio - delta / 2;
                gearRatioRange[i + 1][1] = exactRatios.get(i + 1) + delta / 2;
            } else {
                // special for the last element as well
                delta = Math.abs(gearRatioRange[i][1] - exactRatio);
                gearRatioRange[i][0] = exactRatio - delta / 2;
            }
        }
        return gearRatioRange;
    }

    private static ArrayList<Double> getExactRatios(int motorTeeth, int[] wheelTeeth) {
        exactRatios.clear();
        for (int tooth : wheelTeeth) {
            exactRatios.add((double) tooth / (double) motorTeeth);
        }

        Collections.sort(exactRatios); // sort ascending
        Collections.reverse(exactRatios); // reverse to sort descending
        // now the zeroth element contains the highest gear ratio i.e. the lowest gear index

        return exactRatios;
    }
    
    public static int ShiftIndicator(double motorRPM, double gearRatio) {
        // -1 to shift down
        // 0 for no shift
        // 1 to shift up

        double deltaToTargetRPM = Math.abs(TARGET_RPM - motorRPM);

        for (int i = 0; i < exactRatios.size(); i++) {
            double estimatedRPM = exactRatios.get(i) / gearRatio * motorRPM;
            if (Math.abs(TARGET_RPM - estimatedRPM) < deltaToTargetRPM) {
                Global.IdealGear = i + 1;
                if (i < CurrentGear - 1) return -1;
                else if (i > CurrentGear - 1) return 1;
                else return 0;
            }
        }
        return 0;
    }
}
