/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ClassLoader} implementation which holds a static collection of classes.
 */
public final class StaticClassLoader extends ClassLoader {
	/** Internal storage of defined classes. */
	private final ImmutableMap<String, Class<?>> knownClasses;

	static {
		registerAsParallelCapable();
	}

	public StaticClassLoader(@Nullable ClassLoader parent, Map<String, Class<?>> classes) {
		super(parent);
		this.knownClasses = ImmutableMap.copyOf(classes);
	}

	public static class Builder {
		private final HashMap<String, Class<?>> classes = new HashMap<>();
		private @Nullable ClassLoader parent = null;

		/**
		 * Create a new StaticClassLoader builder, using the system class loader as the parent class loader.
		 */
		public Builder() {}

		/**
		 * Create a new StaticClassLoader builder, using the specified parent class loader for delegation.
		 * @param parent The parent class loader.
		 */
		public Builder(@Nullable ClassLoader parent) {
			this.parent = parent;
		}

		/**
		 * Add a class to the class loader.
		 * @param clazz The class to add.
		 * @return The class previously stored with this name, if one exists.
		 */
		public @Nullable Class<?> addClass(Class<?> clazz) {
			return this.classes.put(clazz.getName(), clazz);
		}

		/**
		 * Add a new class to the class loader with a custom name.
		 * @param name The full name of the class.
		 * @param clazz The class to store.
		 * @return The class previously stored with this name, if one exists.
		 */
		public @Nullable Class<?> addClass(@NonNull String name, @NonNull Class<?> clazz) {
			return this.classes.put(name, clazz);
		}

		/**
		 * Add a collection of classes to the class loader.
		 * @param newClasses The classes to add.
		 */
		public void addClasses(@NonNull Collection<Class<?>> newClasses) {
			for (var clazz : newClasses) {
				this.classes.put(clazz.getName(), clazz);
			}
		}

		/**
		 * Remove a class from the class loader.
		 * @param name The fully-qualified name of the class.
		 * @return The removed class, or null if no class was found.
		 */
		public @Nullable Class<?> removeClass(@NonNull String name) {
			return this.classes.remove(name);
		}

		/**
		 * Build the {@link StaticClassLoader} instance.
		 */
		public StaticClassLoader build() {
			return new StaticClassLoader(this.parent, this.classes);
		}
	}


	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = knownClasses.get(name);
		if (clazz != null) {
			return clazz;
		} else {
			throw new ClassNotFoundException();
		}
	}
}
