/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.content.ContextHidden
import android.content.ContextParamsHidden
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Handler
import android.os.UserHandle
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.concurrent.Executor

/**
 * Purpose: patch multiple methods of [android.app.ContextImpl]. See individual patch documentation for details.
 *
 * Implements:
 * - [94da5aad6c: ContextImpl.java#L2134-L2136](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L2134-L2136)
 * - [94da5aad6c: ContextImpl.java#L2052](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L2052)
 * - [3ac920ce4c: ContextImpl.java#L2263-L2265](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/core/java/android/app/ContextImpl.java#L2263-L2265)
 */
@Suppress("ClassName")
internal object ContextImplPatch : IPatch {
	val CLASS_CONTEXT_IMPL = Class.forName("android.app.ContextImpl").kotlin

	override fun install() {
		ContextImpl_getSystemService.install()
		ContextImpl_bindServiceCommon.install()
		ContextImpl_checkSelfPermission.install()
	}

	/**
	 * Purpose: hide system services according to [GmsHooks.isHiddenSystemService].
	 *
	 * Implements [94da5aad6c: ContextImpl.java#L2134-L2136](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L2134-L2136)
	 */
	private object ContextImpl_getSystemService : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(CLASS_CONTEXT_IMPL, "getSystemService", String::class),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val name = param.args[0] as String
			if (GmsHooks.isHiddenSystemService(name)) {
				param.result = null
			}
		}
	}

	/**
	 * Purpose: remove privileged `BIND_ALLOW_BACKGROUND_ACTIVITY_STARTS` flag during `ContextImpl.bindServiceCommon`
	 * (requires `START_ACTIVITIES_FROM_BACKGROUND`).
	 *
	 * Implements [94da5aad6c: ContextImpl.java#L2052](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/ContextImpl.java#L2052)
	 *
	 * NOTE: this patch moves the change to the start of the method (compared to GrapheneOS).
	 */
	private object ContextImpl_bindServiceCommon : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					CLASS_CONTEXT_IMPL, "bindServiceCommon",
					Intent::class, ServiceConnection::class, Int::class, String::class, Handler::class,
					Executor::class, UserHandle::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			param.args[2] = (param.args[2] as Int) and ContextHidden.BIND_ALLOW_BACKGROUND_ACTIVITY_STARTS.inv()
		}
	}

	/**
	 * Purpose: spoof self permission checks according to [GmsHooks.shouldSpoofSelfPermissionCheck].
	 *
	 * Implements [3ac920ce4c: ContextImpl.java#L2263-L2265](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/core/java/android/app/ContextImpl.java#L2263-L2265)
	 */
	private object ContextImpl_checkSelfPermission : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(CLASS_CONTEXT_IMPL, "checkSelfPermission", String::class),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// if permission is null, let original method throw an exception
			val permission = param.args[0] as String? ?: return

			// don't spoof renounced permissions
			val mParams = XposedHelpers.getObjectField(param.thisObject, "mParams") as ContextParamsHidden
			if (mParams.isRenouncedPermission(permission)) return

			if (GmsHooks.shouldSpoofSelfPermissionCheck(permission))
				param.result = PERMISSION_GRANTED
		}
	}
}
