/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.app.compat.gms.GmsCompat
import net.sb418.android.gmscompatx.patches.playstore.EnableSilentAppUpdatesPatch
import net.sb418.android.gmscompatx.patches.playstore.FilePatch
import net.sb418.android.gmscompatx.patches.playstore.PackageInstallerPatch
import net.sb418.android.gmscompatx.patches.playstore.StorageStatsManagerPatch

object PlayStorePatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.PlayStore"

	override fun preinstallCheck() {
		if (!GmsCompat.isPlayStore())
			throw IPatch.CannotInstallException("GmsCompat#isPlayStore returned false!")
	}

	override val patches: Array<IPatch> = arrayOf(
		EnableSilentAppUpdatesPatch,
		FilePatch,
		PackageInstallerPatch,
		StorageStatsManagerPatch,
	)
}
