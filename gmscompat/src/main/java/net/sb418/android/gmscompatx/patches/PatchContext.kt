/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import java.util.concurrent.atomic.AtomicBoolean

object PatchContext {
	/** Whether the process information below has been initialized or not. */
	val procInfoInitialized = AtomicBoolean(false)

	/** The name of the package that started this process. */
	var packageName: String? = null
	/** The name of this process. */
	var processName: String? = null
}
