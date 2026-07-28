/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.app.ApplicationPackageManager
import android.content.Context
import com.android.internal.gmscompat.sysservice.GmcPackageManager
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.lang.reflect.Field

/**
 * Purpose: swap [ApplicationPackageManager] for [GmcPackageManager].
 *
 * Implements: [94da5aad6c: ContextImpl.java#L406](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L406)
 */
internal object UseGmcPackageManagerPatch : IPatch, XC_MethodReplacement() {
	// cache reflection objects
	private val refContextImplClass = Class.forName("android.app.ContextImpl")
	@SuppressLint("DiscouragedPrivateApi")
	private val refPackageManagerField: Field = refContextImplClass.getDeclaredField("mPackageManager")

	init {
		refPackageManagerField.isAccessible = true
	}

	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExact(refContextImplClass, "getPackageManager"),
			this
		)
	}

	override fun replaceHookedMethod(param: MethodHookParam): GmcPackageManager? {
		val thisObj = param.thisObject as Context  // technically ContextImpl, but it isn't public

		// return cached GmcPackageManager if present
		var gpm = refPackageManagerField.get(thisObj) as GmcPackageManager?
		if (gpm != null)
			return gpm

		// based on original method implementation
		val pm = ActivityThread.getPackageManager() ?: return null

		// create new GmcPackageManager
		gpm = GmcPackageManager.fromContext(thisObj, pm)

		// cache + return
		refPackageManagerField.set(thisObj, gpm)
		return gpm
	}
}
