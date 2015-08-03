package com.ben.drivenbluetooth.util;

import android.graphics.Path;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public final class Bezier {
	private Bezier() {}

	public static Path GetBezierPath(List<LatLng> LatLngs, float scale) {
		Path BezierPath = new Path();

		// 1. calculate number of curves required
		int n_curves = LatLngs.size() - 1;

		// 2. calculate control points
		List<LatLng> ControlPoints = GetControlPoints(LatLngs, scale);

		if (ControlPoints != null) {
			for (int i = 0; i < n_curves - 1; i++) {
				if (i == 0) { // for first curve is quadTo
					BezierPath.moveTo((float) LatLngs.get(0).latitude, (float) LatLngs.get(0).longitude);
					BezierPath.quadTo(
							(float) ControlPoints.get(0).latitude,
							(float) ControlPoints.get(0).longitude,

							(float) LatLngs.get(1).latitude,
							(float) LatLngs.get(1).longitude
					);
				} else if (i <  - n_curves - 1) { // remember zero-start arrays
					BezierPath.cubicTo(
							(float) ControlPoints.get(2 * i - 1).latitude,
							(float) ControlPoints.get(2 * i - 1).longitude,

							(float) ControlPoints.get(2 * i).latitude,
							(float) ControlPoints.get(2 * i).longitude,

							(float) LatLngs.get(i + 1).latitude,
							(float) LatLngs.get(i + 1).longitude
					);
				} else {
					// last point
					BezierPath.quadTo(
							(float) ControlPoints.get(2 * i - 1).latitude,
							(float) ControlPoints.get(2 * i - 1).longitude,

							(float) LatLngs.get(i + 1).latitude,
							(float) LatLngs.get(i + 1).longitude
					);
				}
			}
		}
		return BezierPath;
	}

	@Nullable
	public static List<LatLng> GetControlPoints(List<LatLng> LatLngs, float scale) {
		List<LatLng> ControlPoints = new ArrayList<>();

		if (LatLngs.size() > 2) { // can't make a curve with less than 3 points
			// get control points until penultimate point
			for (int i = 0; i < LatLngs.size() - 2; i++) {
				float[] p0 = {
						(float) LatLngs.get(i).latitude,
						(float) LatLngs.get(i).longitude
				};

				float[] p1 = {
						(float) LatLngs.get(i + 1).latitude,
						(float) LatLngs.get(i + 1).longitude
				};

				float[] p2 = {
						(float) LatLngs.get(i + 2).latitude,
						(float) LatLngs.get(i + 2).longitude
				};

				float[][] q0q1 = GetControlPoints(p0, p1, p2, scale);
				float[] q0 = q0q1[0];
				float[] q1 = q0q1[1];

				ControlPoints.add(new LatLng((double) q0[0], (double) q0[1]));
				ControlPoints.add(new LatLng((double) q1[0], (double) q1[1]));
			}

			// sanity check
			if (ControlPoints.size() != 2 * LatLngs.size() - 4) {
				// we have a problem
				boolean problem = true;
				throw new InternalError("Number of control points does not satisfy 2n-4");
			}
		}
		return ControlPoints;
	}

	public static float[][] GetControlPoints(float[] p0, float[] p1, float[] p2, float scale) {
		// first get normalized tangent
		float[] tanN = GetNormalizedTangent(p0, p2);

		// Get control points
		float[] q0 = {
				p1[0] - tanN[0] * scale,
				p1[1] - tanN[1] * scale
		};

		float[] q1 = {
				p1[0] + tanN[0] * scale,
				p1[1] + tanN[1] * scale
		};

		// combine
		float[][] q0q1 = {q0, q1};

		return q0q1;
	}

	public static float[] GetNormalizedTangent(float[] p0, float[] p1) {
		// calculate vector between the two points
		float[] vector = {
				p1[0] - p0[0],
				p1[1] - p0[1]
		};

		// calculate magnitude of vector
		float mag = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);

		// calculate normalized vector
		vector[0] /= mag;
		vector[1] /= mag;

		return vector;
	}
}
