/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.compat.gms.GmsCompat
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.location.LocationRequestHidden
import android.os.BuildHidden
import android.provider.Settings
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.GMSPatches.TAG
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.concurrent.Executor

/**
 * Purpose: modify location requests to avoid requiring privileged permissions.
 *
 * Implements: [94da5aad6c: LocationManager.java#L1550](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/location/java/android/location/LocationManager.java#L1550)
 */
internal object FilterLocationRequestsPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				LocationManager::class, "requestLocationUpdates",
				String::class, LocationRequest::class, Executor::class, LocationListener::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		// if the app doesn't have any location permission, skip the entire method
		if (!GmsCompat.hasPermission(ACCESS_COARSE_LOCATION)) {
			param.result = null
			return
		}

		val locationRequest = param.args[1] as LocationRequestHidden

		// requires privileged UPDATE_APP_OPS_STATS permission
		locationRequest.setHideFromAppOps(false)
		// requires privileged WRITE_SECURE_SETTINGS permission
		locationRequest.setLocationSettingsIgnored(false)
		// requires privileged UPDATE_DEVICE_STATS permission
		locationRequest.setWorkSource(null)

		if (BuildHidden.isDebuggable()) {
			val mContext = XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
			val cr = mContext.contentResolver
			val key = "gmscompat_skip_gnss_location_updates"
			if (Settings.Global.getInt(cr, key, 0) == 1) {
				Log.d(TAG, "requestLocationUpdates is skipped because $key global setting is enabled")
				param.result = null
				return
			}
		}

		// pass modified location request to original method
		param.args[1] = locationRequest
	}
}
