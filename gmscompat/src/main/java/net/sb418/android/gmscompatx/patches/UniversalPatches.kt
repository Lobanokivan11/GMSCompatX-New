/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import net.sb418.android.gmscompatx.patches.universal.ActivityPatch
import net.sb418.android.gmscompatx.patches.universal.ApplicationPackageManagerPatch
import net.sb418.android.gmscompatx.patches.universal.ContentResolverPatch
import net.sb418.android.gmscompatx.patches.universal.InitBinderRedirectorPatch
import net.sb418.android.gmscompatx.patches.universal.InstrumentationPatch
import net.sb418.android.gmscompatx.patches.universal.ServiceCreationPatch

/**
 * Patchset that applies to all apps and the system_server.
 */
object UniversalPatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.Universal"

	override val patches: Array<IPatch> = arrayOf(
		// app-only entrypoints
		InstrumentationPatch,
		ServiceCreationPatch,
		InitBinderRedirectorPatch,
		ContentResolverPatch,

		// package info spoofing
		ApplicationPackageManagerPatch,

		// missing app detection
		ActivityPatch,
	)
}
