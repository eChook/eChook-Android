package com.ben.echookcompanion.util;

import android.graphics.Path;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public final class Bezier {
	private Bezier() {}

	@Nullable
	public static Path GetBezierPath(List<LatLng> LatLngs, float k) {
		Path BezierPath = new Path();

		if (LatLngs.size() > 2) {
			// 1. calculate number of curves required
			int n_curves = LatLngs.size() - 1;

			// 2. calculate control points
			List<LatLng> ControlPoints = GetControlPoints(LatLngs, k);

			if (ControlPoints != null) {
				for (int i = 0; i < n_curves; i++) {
					if (i == 0) { // for first curve is quadTo
						BezierPath.moveTo((float) LatLngs.get(0).latitude, (float) LatLngs.get(0).longitude);
						BezierPath.quadTo(
								(float) ControlPoints.get(0).latitude,
								(float) ControlPoints.get(0).longitude,

								(float) LatLngs.get(1).latitude,
								(float) LatLngs.get(1).longitude
						);
					} else if (i < n_curves - 1) { // remember zero-start arrays
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
		} else {
			return null;
		}
	}

	@Nullable
	private static List<LatLng> GetControlPoints(List<LatLng> LatLngs, float k) {
		List<LatLng> ControlPoints = new ArrayList<>();

		/* This algorithm is taken from
		http://www.antigrain.com/research/bezier_interpolation/

		 */

		/* Step 1. Calculate midpoints between each node */
		List<LatLng> MidPoints = GetMidPoints(LatLngs);

		/* Step 2. Calculate points Bi */
		if (MidPoints != null && LatLngs.size() > 2) { // can't make a curve with less than 3 points
			for (int i = 0; i < LatLngs.size() - 2; i++) {
				// a) get distance between consecutive points
				float L1 = GetDistance(LatLngs.get(i), LatLngs.get(i + 1));
				float L2 = GetDistance(LatLngs.get(i + 1), LatLngs.get(i + 2));

				// b) get distance between consecutive midpoints
				float Dm = GetDistance(MidPoints.get(i), MidPoints.get(i + 1));

				// c) get lengths d1 and d2
				float d1 = k * Dm * L1 / (L1 + L2);
				float d2 = k * Dm * L2 / (L1 + L2);

				// d) Get normalized vector between consecutive midpoints
				float[] m0 = {
						(float) MidPoints.get(i).latitude,
						(float) MidPoints.get(i).longitude
				};

				float[] m1 = {
						(float) MidPoints.get(i + 1).latitude,
						(float) MidPoints.get(i + 1).longitude
				};

				float[] d1d2_n = GetNormalizedTangent(m0, m1);

				// e) get actual vectors d1 and d2
				float[] d1_v = {
						- d1d2_n[0] * d1,
						- d1d2_n[1] * d1
				};

				float[] d2_v = {
						d1d2_n[0] * d2,
						d1d2_n[1] * d2
				};

				// f) Calculate control points
				float[] q0 = {
						(float) LatLngs.get(i + 1).latitude + d1_v[0],
						(float) LatLngs.get(i + 1).longitude + d1_v[1]
				};

				float[] q1 = {
						(float) LatLngs.get(i + 1).latitude + d2_v[0],
						(float) LatLngs.get(i + 1).longitude + d2_v[1]
				};

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

	public static Path GetBezierPath(List<LatLng> LatLngs) {
		return GetBezierPath(LatLngs, 1f);
	}

	@Nullable
	private static List<LatLng> GetControlPoints(List<LatLng> LatLngs) {
		return GetControlPoints(LatLngs, 1f);
	}

	private static float GetDistance(LatLng latLng0, LatLng latLng1) {
		float[] vector = {
				(float) latLng1.latitude - (float) latLng0.latitude,
				(float) latLng1.longitude - (float) latLng0.longitude
		};

		return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
	}

	private static List<LatLng> GetMidPoints(List<LatLng> LatLngs) {
		if (LatLngs.size() >= 2) {
			List<LatLng> MidPoints = new ArrayList<>(); // return value

			for (int i = 0; i < LatLngs.size() - 1; i++) {
				float[] p0 = {
						(float) LatLngs.get(i).latitude,
						(float) LatLngs.get(i).longitude
				};

				float[] p1 = {
						(float) LatLngs.get(i + 1).latitude,
						(float) LatLngs.get(i + 1).longitude
				};

				/* 	midpoint calc:
					m0 = p0 + 0.5(p1 - p0)
					m0 = p0 + 0.5p1 - 0.5p0
					m0 = 0.5(p0 + p1);
				 */
				float[] m0 = {
						0.5f * (p0[0] + p1[0]),
						0.5f * (p0[1] + p1[1])
				};
				MidPoints.add(new LatLng(m0[0], m0[1]));
			}
			return MidPoints;
		} else {
			return null;
		}
	}

	private static float[] GetNormalizedTangent(float[] p0, float[] p1) {
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
