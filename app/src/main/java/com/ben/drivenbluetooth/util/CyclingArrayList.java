package com.ben.drivenbluetooth.util;

import java.util.ArrayList;
import java.util.Collection;

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
		if (++selectedItem >= size()) {
			selectedItem = 0;
		}
		return get(selectedItem);
	}

	public E reverseCycle() {
		if (--selectedItem < 0) {
			selectedItem = size() - 1;
		}
		return get(selectedItem);
	}

	public E getActiveElement() {
		try {
			return get(selectedItem);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
}
