/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.util;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.robv.android.xposed.XposedHelpers;

/**
 * Utilities for messing with class loaders.
 */
public final class ClassLoaderUtils {
	private ClassLoaderUtils() {}

	public final static class ClassLoaderIterator implements Iterator<ClassLoader> {
		private ClassLoader loader;

		ClassLoaderIterator(ClassLoader loader) {
			this.loader = loader;
		}

		@Override
		public boolean hasNext() {
			return loader != null;
		}

		@Override
		public ClassLoader next() {
			var res = this.loader;
			if (res == null) {
				throw new NoSuchElementException();
			} else {
				this.loader = res.getParent();
				return res;
			}
		}
	}

	public final static class ClassLoaderIterable implements Iterable<ClassLoader> {
		private final ClassLoader loader;

		public ClassLoaderIterable(ClassLoader loader) {
			this.loader = loader;
		}

		@NonNull
		@Override
		public Iterator<ClassLoader> iterator() {
			return new ClassLoaderIterator(this.loader);
		}
	}

	/**
	 * Rebase one class loader onto another, such that the hierarchy of {@code loaderB} becomes a strict prefix of
	 * the hierarchy of {@code loaderA}.
	 * @param loaderA Class loader to rebase
	 * @param loaderB Class loader to rebase onto
	 * @return Whether the rebase was successful.
	 *         This will only be false if none of {@code loaderA}'s ancestors are within {@code loaderB}'s hierarchy.
	 */
	public static boolean rebaseClassLoader(@NonNull final ClassLoader loaderA, @NonNull final ClassLoader loaderB) {
		var iterableA = new ClassLoaderIterable(loaderA);
		var iterableB = new ClassLoaderIterable(loaderB);

		for (var parentA : iterableA) {
			for (var parentB : iterableB) {
				// check if parentA is a direct child of a common ancestor between loaderA and loaderB
				if (parentA.getParent() == parentB) {
					// rebase onto loaderB
					XposedHelpers.setObjectField(parentA, "parent", loaderB);
					return true;
				}
			}
		}

		return false;
	}
}
