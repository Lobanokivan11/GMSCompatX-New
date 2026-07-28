/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

/**
 * Represents a patch (or set of patches) that can be installed.
 */
interface IPatch {
	/**
	 * Install this patch(set) - generally by hooking methods with Xposed.
	 * @throws CannotInstallException If this patch(set) cannot be installed.
	 * @throws AlreadyInstalledException If this patch(set) has already been installed.
	 */
	fun install()

	/**
	 * Thrown if this patch(set) cannot be installed.
	 */
	open class CannotInstallException : IllegalStateException {
		constructor() : super()
		constructor(msg: String) : super(msg)
	}

	/**
	 * Thrown if this patch(set) has already been installed.
	 */
	class AlreadyInstalledException : CannotInstallException()
}
