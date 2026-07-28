/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.os.UserHandle
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: override `UserHandle.isOwner` and `UserHandle.isSystem` to always return true.
 *
 * Implements [94da5aad6c: UserHandle.java#L569-L585](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/UserHandle.java#L569-L585)
 */
internal object UserHandlePatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(UserHandle::class, "isOwner"), this)
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(UserHandle::class, "isSystem"), this)
	}

	override fun replaceHookedMethod(param: MethodHookParam): Boolean = true
}
