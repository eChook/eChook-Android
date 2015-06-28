package com.ben.drivenbluetooth.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by BNAGY4 on 24/06/2015.
 */
public class CyclingArrayList<E> extends ArrayList<E> {

	private int selectedItem = -1;

	public CyclingArrayList() {
		super();
	}

	public CyclingArrayList(int capacity) {
		super(capacity);
	}

	public CyclingArrayList(Collection<? extends E> collection) {
		super(collection);
	}

	public E cycle() {
		if (++selectedItem < size()) {
			return get(selectedItem);
		} else {
			selectedItem = 0;
			return get(selectedItem);
		}
	}
}
