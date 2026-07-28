/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.os.Binder
import android.os.Process
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.WeakHashMap

/**
 * Purpose: call [GmsHooks.onBinderTransaction] from [Binder.execTransact].
 *
 * Implements [94da5aad6c: Binder.java#L1253-L1261](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Binder.java#L1253-L1261)
 */
internal object BinderPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				Binder::class, "execTransact",
				Int::class, Long::class, Long::class, Int::class
			),
			this
		)
	}

	/** Tracks previous calling UIDs for each [Binder]. */
	private val PREVIOUS_UIDS = WeakHashMap<Binder, Int>(4)

	override fun beforeHookedMethod(param: MethodHookParam) {
		val thisObj = param.thisObject as Binder

		val callingUid = Binder.getCallingUid()

		// original patch uses a volatile field instead
		val previousUid = synchronized(PREVIOUS_UIDS) {
			PREVIOUS_UIDS.putIfAbsent(thisObj, callingUid)
		}

		if (previousUid != callingUid && Process.isApplicationUid(callingUid)) {
			GmsHooks.onBinderTransaction(Binder.getCallingPid(), callingUid)
		}
	}
}
