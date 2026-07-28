/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.AppOpsManager
import android.app.AppOpsManagerHidden
import android.os.IBinder
import android.os.Process
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: convert `AppOpsManager.*Op` calls into `AppOpsManager.*ProxyOp` calls where necessary.
 *
 * Implements:
 * - [94da5aad6c: AppOpsManager.java#L8530](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/AppOpsManager.java#L8530)
 * - [94da5aad6c: AppOpsManager.java#L9068](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/AppOpsManager.java#L9068)
 * - [94da5aad6c: AppOpsManager.java#L9297](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/AppOpsManager.java#L9297)
 */
@Suppress("ClassName")
internal object AppOpsManagerPatch : IPatch {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				AppOpsManager::class,
				"noteOpNoThrow",
				Int::class,
				Int::class,
				String::class,
				String::class,
				String::class
			),
			AppOpsManager_noteOpNoThrow
		)

		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				AppOpsManager::class,
				"startOpNoThrow",
				IBinder::class,
				Int::class,
				Int::class,
				String::class,
				Boolean::class,
				String::class,
				String::class,
				Int::class,
				Int::class
			),
			AppOpsManager_startOpNoThrow
		)

		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				AppOpsManager::class,
				"finishOp",
				IBinder::class,
				Int::class,
				Int::class,
				String::class,
				String::class
			),
			AppOpsManager_finishOp
		)
	}

	private object AppOpsManager_noteOpNoThrow : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val uid = param.args[1] as Int

			if (uid != Process.myUid()) {
				val thisObj = param.thisObject as AppOpsManager
				val op = param.args[0] as Int
				val packageName = param.args[2] as String?
				val attributionTag = param.args[3] as String?
				val message = param.args[4] as String?

				param.result = thisObj.noteProxyOpNoThrow(
					AppOpsManagerHidden.opToPublicName(op),
					packageName,
					uid,
					attributionTag,
					message
				)
			}
		}
	}

	private object AppOpsManager_startOpNoThrow : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val uid = param.args[2] as Int

			if (uid != Process.myUid()) {
				val thisObj = param.thisObject as AppOpsManager
				val op = param.args[1] as Int
				val packageName = param.args[3] as String
				val attributionTag = param.args[5] as String?
				val message = param.args[6] as String?

				param.result = thisObj.startProxyOpNoThrow(
					AppOpsManagerHidden.opToPublicName(op),
					uid,
					packageName,
					attributionTag,
					message
				)
			}
		}
	}

	private object AppOpsManager_finishOp : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			val uid = param.args[2] as Int

			if (uid != Process.myUid()) {
				val thisObj = param.thisObject as AppOpsManager
				val op = param.args[1] as Int
				val packageName = param.args[3] as String
				val attributionTag = param.args[4] as String?

				param.result = thisObj.finishProxyOp(
					AppOpsManagerHidden.opToPublicName(op),
					uid,
					packageName,
					attributionTag
				)
			}
		}
	}
}
