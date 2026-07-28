/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import net.sb418.android.gmscompatx.patches.system_server.ActivityManagerServicePatch
import net.sb418.android.gmscompatx.patches.system_server.ActivityStarterPatch
import net.sb418.android.gmscompatx.patches.system_server.AppsFilterImplPatch
import net.sb418.android.gmscompatx.patches.system_server.DefaultPermissionGrantPolicyPatch
import net.sb418.android.gmscompatx.patches.system_server.PackageInstallerSessionPatch
import net.sb418.android.gmscompatx.patches.system_server.PackageParsingUtilsPatch

/**
 * Patchset that applies to the `system_server`.
 *
 * Note that we can access `system_server` classes through our class loader
 * (because of the rebase performed in `XposedModule`).
 */
object SystemServerPatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.SystemServer"

	override val patches: Array<IPatch> = arrayOf(
		ActivityManagerServicePatch,
		ActivityStarterPatch,
		AppsFilterImplPatch,
		DefaultPermissionGrantPolicyPatch,
		PackageInstallerSessionPatch,
		PackageParsingUtilsPatch,
	)
}
