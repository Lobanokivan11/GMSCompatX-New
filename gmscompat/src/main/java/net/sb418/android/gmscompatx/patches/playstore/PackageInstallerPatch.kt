/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.playstore

import android.content.IntentSender
import android.content.pm.PackageInstaller
import com.android.internal.gmscompat.PlayStoreHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: call [PlayStoreHooks] callbacks.
 *
 * Implements:
 * - [94da5aad6c: PackageInstaller.java#L510](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/pm/PackageInstaller.java#L510)
 * - [94da5aad6c: PackageInstaller.java#L1474-L1492](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/content/pm/PackageInstaller.java#L1474-L1492)
 */
@Suppress("ClassName")
internal object PackageInstallerPatch : IPatch {
	override fun install() {
		PackageInstaller_createSession.install()
		PackageInstaller_Session_commit.install()
	}

	private object PackageInstaller_createSession : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					PackageInstaller::class, "createSession",
					PackageInstaller.SessionParams::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			PlayStoreHooks.adjustSessionParams(param.args[0] as PackageInstaller.SessionParams)
		}
	}

	private object PackageInstaller_Session_commit : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(PackageInstaller.Session::class, "commit", IntentSender::class),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// TODO: implement getSilentUpdateWaitMillis

			param.args[0] = PlayStoreHooks.wrapCommitStatusReceiver(
				param.thisObject as PackageInstaller.Session,
				param.args[0] as IntentSender,
			)
		}
	}
}
