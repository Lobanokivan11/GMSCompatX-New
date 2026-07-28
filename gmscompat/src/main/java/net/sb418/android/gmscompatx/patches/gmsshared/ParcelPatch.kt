/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.os.Parcel
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: intercept remote exceptions according to [GmsHooks.interceptException].
 *
 * Implements [94da5aad6c: Parcel.java#L2989-L2993](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Parcel.java#L2989-L2993)
 */
internal object ParcelPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(Parcel::class, "readException", Int::class, String::class),
			this
		)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		if (GmsHooks.interceptException(param.throwable as Exception, param.thisObject as Parcel))
			param.throwable = null
	}
}
