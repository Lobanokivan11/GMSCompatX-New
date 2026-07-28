/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.app.compat.gms.GmsCompat
import net.sb418.android.gmscompatx.patches.gmscore.BinderPatch
import net.sb418.android.gmscompatx.patches.gmscore.MediaProjectionPatch

/**
 * Patchset that applies to GMS Core (`com.google.android.gms`).
 *
 * All patches implicitly assume that [GmsCompat.isGmsCore] is true.
 */
object GMSCorePatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.GMSCore"

	override fun preinstallCheck() {
		if (!GmsCompat.isGmsCore())
			throw IPatch.CannotInstallException("GmsCompat#isGmsCore returned false!")
	}

	override val patches: Array<IPatch> = arrayOf(
		BinderPatch,
		MediaProjectionPatch,
	)
}
