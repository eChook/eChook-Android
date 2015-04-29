package com.driven.rowan.drivenbluetooth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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
	private int width;
	private Rect rect = new Rect(0 , 0, 0, 0);

	/* CONSTRUCTOR */
	public DataBar(Context context) {
		this(context, null);
	}

	public DataBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DataBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.DataBar,
				0, 0);

		try {
			this.max = a.getInt(R.styleable.DataBar_max, 0);
			this.min = a.getInt(R.styleable.DataBar_min, 0);
			this.nValue = this.min;
			this.width = a.getInt(R.styleable.DataBar_width, 10);
			this.color = a.getColor(R.styleable.DataBar_color, 0xff);
		} finally {
			a.recycle();
		}
		init();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (canvas != null) {
			this.rect.set(0, 0, width, min + nValue);
			canvas.drawRect(rect, this.p);
		}
	}

	public void init() {
		this.p = new Paint();
		p.setColor(this.color);
	}

	public void setValue(float value) {
		if (value >= (float) this.min && value <= (float) this.max) {
			this.nValue = (int) value;
		} else {
		 	this.nValue = 0;
		}
	}

	public void setValue(double value) {
		if (value >= (double) this.min && value <= (double) this.max) {
			this.nValue = (int) value;
		} else {
			this.nValue = 0;
		}
	}

	public void setValue(int value) {
		if (value >= this.min && value <= this.max) {
			this.nValue = value;
		} else {
			this.nValue = 0;
		}
	}
}
