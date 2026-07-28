/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.util

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * General reflection helper methods for finding methods.
 */
object MethodFinder {
	// cache default class loader
	private val defaultClassLoader: ClassLoader? by lazy { javaClass.classLoader ?: ClassLoader.getSystemClassLoader() }

	/**
	 * Get the JVM array type for a given class.
	 */
	fun arrayType(clazz: Class<out Any>): Class<out Any> =
		java.lang.reflect.Array.newInstance(clazz, 0).javaClass

	/**
	 * Get the JVM array type for a given class.
	 */
	fun arrayType(clazz: KClass<out Any>): KClass<out Any> =
		java.lang.reflect.Array.newInstance(clazz.java, 0)::class

	/**
	 * Resolve a method based on its [className], [name], and [parameterTypes].
	 *
	 * This overload resolves the class using the default class loader.
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExactKt(className: String, name: String, vararg parameterTypes: KClass<out Any>): Method =
		findMethodExactKt(className, defaultClassLoader, name, *parameterTypes)

	/**
	 * Resolve a method based on its [className], [name], and [parameterTypes].
	 *
	 * This overload resolves the class using the provided [classLoader].
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExactKt(className: String, classLoader: ClassLoader?, name: String, vararg parameterTypes: KClass<out Any>): Method =
		findMethodExactKt(Class.forName(className, true, classLoader).kotlin, name, *parameterTypes)

	/**
	 * Resolve a method based on its [class][clazz], [name], and [parameterTypes].
	 *
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExactKt(clazz: KClass<out Any>, name: String, vararg parameterTypes: KClass<out Any>): Method =
		findMethodExact(clazz.java, name, *parameterTypes.map { it.java }.toTypedArray())

	/**
	 * Resolve a method based on its [className], [name], and [parameterTypes].
	 *
	 * This overload resolves the class using the default class loader.
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExact(className: String, name: String, vararg parameterTypes: Class<out Any>): Method =
		findMethodExact(className, defaultClassLoader, name, *parameterTypes)

	/**
	 * Resolve a method based on its [className], [name], and [parameterTypes].
	 *
	 * This overload resolves the class using the provided [classLoader].
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExact(className: String, classLoader: ClassLoader?, name: String, vararg parameterTypes: Class<out Any>): Method =
		findMethodExact(Class.forName(className, true, classLoader), name, *parameterTypes)

	/**
	 * Resolve a method based on its [class][clazz], [name], and [parameterTypes].
	 *
	 * Throws [NoSuchMethodException] if the method was not found.
	 */
	fun findMethodExact(clazz: Class<out Any>, name: String, vararg parameterTypes: Class<out Any>): Method {
		val method = clazz.getDeclaredMethod(name, *parameterTypes)
		method.isAccessible = true
		return method
	}


	/**
	 * Resolve a class constructor based on its [className] and [parameterTypes].
	 *
	 * This overload resolves the class using the default class loader.
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun findConstructorExactKt(className: String, vararg parameterTypes: KClass<out Any>): Constructor<out Any> =
		findConstructorExactKt(className, defaultClassLoader, *parameterTypes)

	/**
	 * Resolve a class constructor based on its [className] and [parameterTypes].
	 *
	 * This overload resolves the class using the provided [classLoader].
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun findConstructorExactKt(className: String, classLoader: ClassLoader?, vararg parameterTypes: KClass<out Any>): Constructor<out Any> =
		findConstructorExactKt(Class.forName(className, true, classLoader).kotlin, *parameterTypes)

	/**
	 * Resolve a class constructor based on its [class][clazz] and [parameterTypes].
	 *
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun <T : Any> findConstructorExactKt(clazz: KClass<out T>, vararg parameterTypes: KClass<out Any>): Constructor<out T> =
		findConstructorExact(clazz.java, *parameterTypes.map { it.java }.toTypedArray())

	/**
	 * Resolve a class constructor based on its [className] and [parameterTypes].
	 *
	 * This overload resolves the class using the default class loader.
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun findConstructorExact(className: String, vararg parameterTypes: Class<out Any>): Constructor<out Any> =
		findConstructorExact(className, defaultClassLoader, *parameterTypes)

	/**
	 * Resolve a class constructor based on its [className] and [parameterTypes].
	 *
	 * This overload resolves the class using the provided [classLoader].
	 *
	 * Throws [ClassNotFoundException] if the class was not found.
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun findConstructorExact(className: String, classLoader: ClassLoader?, vararg parameterTypes: Class<out Any>): Constructor<out Any> =
		findConstructorExact(Class.forName(className, true, classLoader), *parameterTypes)

	/**
	 * Resolve a class constructor based on its [class][clazz] and [parameterTypes].
	 *
	 * Throws [NoSuchMethodException] if the constructor method was not found.
	 */
	fun <T : Any> findConstructorExact(clazz: Class<out T>, vararg parameterTypes: Class<out Any>): Constructor<out T> {
		val constructor = clazz.getDeclaredConstructor(*parameterTypes)
		constructor.isAccessible = true
		return constructor
	}
}
