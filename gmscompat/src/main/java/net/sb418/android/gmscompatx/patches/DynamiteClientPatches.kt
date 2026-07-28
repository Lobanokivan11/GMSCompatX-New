/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks
import net.sb418.android.gmscompatx.patches.dynamite.ApkAssetsPatch
import net.sb418.android.gmscompatx.patches.dynamite.DalvikLoaderPatch
import net.sb418.android.gmscompatx.patches.dynamite.FilePatch

/**
 * Patchset that applies to Dynamite client apps (part of Dynamic Delivery).
 *
 * All patches implicitly assume that [GmsDynamiteClientHooks.enabled] is true.
 *
 * Relevant docs: [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery)
 */
object DynamiteClientPatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.DynamiteClient"

	override fun preinstallCheck() {
		if (!GmsDynamiteClientHooks.enabled())
			throw IPatch.CannotInstallException("GmsDynamiteClientHooks#enabled returned false!")
	}

	override val patches: Array<IPatch> = arrayOf(
		ApkAssetsPatch,
		DalvikLoaderPatch,
		FilePatch,
	)
}
