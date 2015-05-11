package com.driven.rowan.drivenbluetooth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by BNAGY4 on 29/04/2015.
 */
public class DataBar extends View {

	public Paint p;
	public int color;
	public int max;
	public int min;
	private int nValue;
	//private int width;

	/* CONSTRUCTOR */
	public DataBar(Context context) {
		this(context, null);
	}

	public DataBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.DataBar,
				0, 0);

		try {
			max = a.getInt(R.styleable.DataBar_max, 0);
			min = a.getInt(R.styleable.DataBar_min, 0);
			nValue = this.min;
			//width = a.getInt(R.styleable.DataBar_width, 10);
			color = a.getColor(R.styleable.DataBar_BarColor, 0x00);
		} finally {
			a.recycle();
		}

		init();
	}

	public DataBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.DataBar,
				0, 0);

		try {
			max = a.getInt(R.styleable.DataBar_max, 0);
			min = a.getInt(R.styleable.DataBar_min, 0);
			nValue = this.min;
			//width = a.getInt(R.styleable.DataBar_width, 10);
			color = a.getColor(R.styleable.DataBar_BarColor, Color.BLACK);
		} finally {
			a.recycle();
		}
		init();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (canvas != null) {
			// calculate new height based on value
			int height = this.getMeasuredHeight();
			int width = this.getMeasuredWidth();
			float h = (float) height - (float) (nValue - min) / (max - min) * height + min;
			// canvas.drawRect(LEFT, TOP, RIGHT, BOTTOM, PAINT);
			canvas.drawRect(0, (int) h, width, height, this.p);
		}
	}

	public void init() {
		this.p = new Paint();
		p.setColor(this.color);
	}

	public void setValue(float value) {
		if (value >= (float) this.min && value <= (float) this.max) {
			this.nValue = (int) value;
		} else if (value > (float) this.max) {
		 	this.nValue = this.max;
		} else {
			this.nValue = this.min;
		}

		this.invalidate();
	}

	public void setValue(double value) {
		if (value >= (double) this.min && value <= (double) this.max) {
			this.nValue = (int) value;
		} else {
			this.nValue = 0;
		}

		this.invalidate();
	}

	public void setValue(int value) {
		if (value >= this.min && value <= this.max) {
			this.nValue = value;
		} else {
			this.nValue = 0;
		}

		this.invalidate();
	}

	private int RGScale(int percent) {
		if (percent < 0) { percent = 0; }
		if (percent > 100) { percent = 100;}

		int r = 0;
		int g = 0;
		int b = 0;

		if (percent <= 50) {
			r = 255;
			g = (int) (percent * 5.1);
			b = 0;
		} else if (percent > 50) {
			r = (int) (255 - (percent - 50) * 5.1);
			g = 255;
			b = 0;
		}

		return Color.rgb(r, g, b);
	}
}
