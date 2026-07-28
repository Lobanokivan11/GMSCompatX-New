/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.app.compat.gms.GmsCompat
import android.util.ArrayMap
import com.android.server.pm.pkg.PackageStateInternal
import com.android.server.utils.WatchedArraySet
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: mark GMS apps as [force-queryable](https://developer.android.com/training/package-visibility/automatic)
 *
 * Implements: [94da5aad6c: AppsFilterImpl.java#L521-524,L531](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/services/core/java/com/android/server/pm/AppsFilterImpl.java#L531)
 */
internal object AppsFilterImplPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				"com.android.server.pm.AppsFilterImpl", "addPackageInternal",
				PackageStateInternal::class, ArrayMap::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val newPkgSetting = param.args[0] as PackageStateInternal
		val newPkg = newPkgSetting.pkg

		if (GmsCompat.isGmsApp(
				newPkg.packageName,
				newPkg.signingDetails.signatures,
				newPkg.signingDetails.pastSigningCertificates,
				newPkg.isPrivileged,
				newPkg.sharedUserId,
			)
		) {
			val mForceQueryableLock = XposedHelpers.getObjectField(param.thisObject, "mForceQueryableLock")

			synchronized (mForceQueryableLock) {
				val mForceQueryable =
					XposedHelpers.getObjectField(param.thisObject, "mForceQueryable") as WatchedArraySet<Int>
				mForceQueryable.add(newPkgSetting.appId)
			}
		}
	}
}
