/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.sysapps

import android.content.ContentValues
import android.provider.Downloads
import com.android.providers.downloads.DownloadProvider
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: allow `COLUMN_NOTIFICATION_CLASS` used by the Play Store (harmless according to GrapheneOS)
 *
 * Implements: [providers/DownloadProvider: a61f1ff4db](https://github.com/GrapheneOS/platform_packages_providers_DownloadProvider/commit/a61f1ff4db915bfd293ee4822c844b8f7e703058)
 */
internal object DownloadProviderPatch : IPatch, XC_MethodHook() {
	override fun install() {
		runCatching {
			val classLoader = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
			val method = MethodFinder.findMethodExact(
				"com.android.providers.downloads.DownloadProvider",
				classLoader,
				"checkInsertPermissions",
				ContentValues::class.java
			)

			XposedBridge.hookMethod(method, this)
		}
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		var values = param.args[0] as ContentValues

		// check if we need to modify the values
		if (!values.containsKey(Downloads.Impl.COLUMN_NOTIFICATION_CLASS))
			return

		// modify a clone of the original argument
		values = ContentValues(values)
		values.remove(Downloads.Impl.COLUMN_NOTIFICATION_CLASS)

		param.args[0] = values
	}
}
