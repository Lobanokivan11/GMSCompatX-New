/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.util;

import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

/**
 * Reflection helper utilities for GMSCompat.
 */
public final class ReflectUtils {
	private ReflectUtils() {}

	private static final ArrayMap<Class<? extends Settings.NameValueTable>, ArraySet<String>> NVT_FIELD_CACHE =
		new ArrayMap<>(2);  // pre-allocate space for the two tables

	/**
	 * Checks whether a {@link Settings.NameValueTable} contains a given key.
	 *
	 * @param table The NameValueTable class to check.
	 * @param key   The key name.
	 * @apiNote Not thread-safe.
	 */
	public static <T extends Settings.NameValueTable> boolean nvtHasKey(Class<T> table, @NonNull String key) {
		// original: `return sNameValueCache.mAllFields.contains(key);`

		ArraySet<String> mAllFields;
		if ((mAllFields = NVT_FIELD_CACHE.get(table)) == null) {
			try {
				// get `sNameValueCache`
				Field nvcField = table.getDeclaredField("sNameValueCache");
				nvcField.setAccessible(true);
				Object sNameValueCache = nvcField.get(null);
				assert sNameValueCache != null;

				// get `mAllFields`
				Field afField = sNameValueCache.getClass().getDeclaredField("mAllFields");
				afField.setAccessible(true);
				//noinspection unchecked
				mAllFields = (ArraySet<String>) afField.get(sNameValueCache);
				assert mAllFields != null;

				// cache `mAllFields`
				NVT_FIELD_CACHE.put(table, mAllFields);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("reflection failed", e);
			}
		}

		return mAllFields.contains(key);
	}
}
