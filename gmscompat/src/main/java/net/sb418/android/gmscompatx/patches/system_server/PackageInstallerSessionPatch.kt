/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.content.pm.PackageManagerHidden
import com.android.internal.gmscompat.GmsHooks
import com.android.internal.gmscompat.GmsInfo
import com.android.server.pm.InstallSource
import com.android.server.pm.PackageInstallerSession
import com.android.server.pm.PackageManagerException
import com.android.server.pm.PackageManagerService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: prevent updating GMS apps beyond the current max supposed version.
 *
 * Implements: [94da5aad6c: PackageInstallerSession.java#L3031-L3065](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/services/core/java/com/android/server/pm/PackageInstallerSession.java#L3031-L3065)
 */
internal object PackageInstallerSessionPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(PackageInstallerSession::class, "validateApkInstallLocked"), this)
	}

	fun allowUnknownGmsUpdates(): Boolean {
		// TODO: implement actual check
		return true
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception was thrown
		if (param.hasThrowable()) return

		val thisObj = param.thisObject as PackageInstallerSession

		// extract fields
		val mInstallSource = XposedHelpers.getObjectField(thisObj, "mInstallSource") as InstallSource
		val initiatingPackageName = XposedHelpers.getObjectField(mInstallSource, "initiatingPackageName") as String?
		val mInstallerUid = XposedHelpers.getIntField(thisObj, "mInstallerUid")
		val mPackageName = XposedHelpers.getObjectField(thisObj, "mPackageName") as String
		val mVersionCode = XposedHelpers.getLongField(thisObj, "mVersionCode")
		val mPm = XposedHelpers.getObjectField(thisObj, "mPm") as PackageManagerService

		val isInstallerShell = mInstallerUid == android.os.Process.SHELL_UID

		if (initiatingPackageName != null && !isInstallerShell && !allowUnknownGmsUpdates()
				&& initiatingPackageName != "app.grapheneos.apps") {
			val maxVersion = when (mPackageName) {
				// XXX: this is likely going to fail (calling `GmsHooks.config()` from system_server)
				// TODO: come up with a better implementation
				GmsInfo.PACKAGE_GMS_CORE -> GmsHooks.config().maxGmsCoreVersion
				GmsInfo.PACKAGE_PLAY_STORE -> GmsHooks.config().maxPlayStoreVersion
				else -> return
			}

			if (mVersionCode <= maxVersion) return

			// GOS: "lock that is held at this point is per-session lock, call into PackageManager is safe"
			val pkg = mPm.snapshotComputer().getPackage(mPackageName)
			if (pkg != null && pkg.longVersionCode == mVersionCode) return

			throw PackageManagerException(PackageManagerHidden.INSTALL_FAILED_SESSION_INVALID, String.format(
				"Installation of %s version %d is disallowed to prevent breaking gmscompat. Max allowed version is %d",
				mPackageName,
				mVersionCode,
				maxVersion,
			))
		}
	}
}
