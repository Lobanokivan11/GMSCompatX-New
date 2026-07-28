/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

/** Shared logging tag prefix for all patch sets. */
const val BASE_TAG = "GMSCompatX.Patches"

/**
 * Shared base for all patchsets.
 *
 * Implements installation protection to ensure only one installation can occur.
 * (This is implemented at the patchset level to reduce overhead.)
 */
abstract class AbstractPatchSet: IPatch {
	/** Whether this patchset has been installed already. */
	private var installed = AtomicBoolean(false)

	/** Array of patches to install. */
	protected abstract val patches: Array<IPatch>

	/**
	 * Check whether this patchset has already been installed,
	 * and throw [IPatch.AlreadyInstalledException] if so.
	 *
	 * Acts as a synchronization point.
	 */
	fun checkAlreadyInstalled() {
		if (!installed.compareAndSet(false, true)) {
			throw IPatch.AlreadyInstalledException()
		}
	}

	/**
	 * Optional method for implementations to define a "pre-install check".
	 *
	 * Usually includes a sanity-check to verify invariants that the patches rely on.
	 *
	 * @throws IPatch.CannotInstallException
	 */
	open fun preinstallCheck() {}

	override fun install() {
		// log debug message
		if (PatchContext.procInfoInitialized.get())
			Log.d(BASE_TAG, "Installing ${this::class.simpleName} (pkg=${PatchContext.packageName}, process=${PatchContext.processName})")
		else
			Log.d(BASE_TAG, "Installing ${this::class.simpleName}")

		// run preinstallation checks
		preinstallCheck()
		checkAlreadyInstalled()

		// install each patch
		for (patch in patches) {
			patch.install()
		}
	}
}
