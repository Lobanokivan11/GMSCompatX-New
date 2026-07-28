/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import net.sb418.android.gmscompatx.patches.sysapps.DownloadProviderPatch
import net.sb418.android.gmscompatx.patches.sysapps.PermissionControllerUtilsPatch

/**
 * Patchset that applies to certain system apps.
 *
 * Technically, each app has its own patchset, but creating a separate file for each
 * would lead to a lot of redundant code.
 */
class SystemAppPatcher private constructor(override val patches: Array<IPatch>) : AbstractPatchSet() {
	companion object {
		const val TAG = "$BASE_TAG.SystemApp"

		/**
		 * Get the patchset that applies to the given [package][packageName].
		 * Returns `null` if no patches need to be applied to the package.
		 */
		fun getPatchsetFor(packageName: String): SystemAppPatcher? {
			return SystemAppPatcher(getPatchesForPackage(packageName) ?: return null)
		}

		/**
		 * Get the patches that apply to the given [package][packageName].
		 */
		private fun getPatchesForPackage(packageName: String): Array<IPatch>? =
			when (packageName) {
				// DownloadProvider
				"com.android.providers.downloads" -> arrayOf(DownloadProviderPatch)
				// PermissionController
				"com.android.permissioncontroller" -> arrayOf(PermissionControllerUtilsPatch)
				// all other apps
				else -> null
			}
	}
}
