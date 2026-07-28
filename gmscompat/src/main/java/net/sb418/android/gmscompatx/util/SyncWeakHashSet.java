/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A {@link HashSet} that contains a weak reference to each entry, based on {@link WeakHashMap}.
 * <p>
 * All methods are synchronized, but iterators still require external synchronization.
 */
public final class SyncWeakHashSet<T> extends AbstractSet<T> implements Set<T> {
	/** Internal backing map */
	private final WeakHashMap<T, Object> map;

	/** Marker object for map values */
	private static final Object PRESENT = new Object();

	public SyncWeakHashSet() {
		map = new WeakHashMap<>();
	}

	public SyncWeakHashSet(int initialCapacity) {
		map = new WeakHashMap<>(initialCapacity);
	}

	@Override
	public synchronized @NonNull Iterator<T> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public synchronized int size() {
		return map.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	@SuppressWarnings("SuspiciousMethodCalls")
	public synchronized boolean contains(@Nullable Object o) {
		return map.containsKey(o);
	}

	@Override
	public synchronized boolean add(T e) {
		return map.put(e, PRESENT) == null;
	}

	@Override
	public synchronized boolean remove(@Nullable Object o) {
		return map.remove(o) == PRESENT;
	}

	@Override
	public synchronized void clear() {
		map.clear();
	}
}
