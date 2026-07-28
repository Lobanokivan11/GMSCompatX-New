/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import com.android.internal.gmscompat.sysservice.GmcTelephonyManager
import com.android.internal.telephony.IPhoneStateListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: filter out privileged telephony callback events via [GmcTelephonyManager.filterTelephonyCallbackEvents].
 *
 * Implements:
 * - [94da5aad6c: TelephonyRegistryManager.java#L269](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/telephony/TelephonyRegistryManager.java#L269)
 * - [94da5aad6c: TelephonyRegistryManager.java#L298](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/telephony/TelephonyRegistryManager.java#L298)
 */
internal object FilterTelephonyCallbackEventsPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				"com.android.internal.telephony.ITelephonyRegistry\$Stub\$Proxy",
				"listenWithEventList",
				Boolean::class,
				Boolean::class,
				Int::class,
				String::class,
				String::class,
				IPhoneStateListener::class,
				IntArray::class,
				Boolean::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val events = param.args[6] as IntArray

		val newEvents = GmcTelephonyManager.filterTelephonyCallbackEvents(events)

		if (newEvents.isNotEmpty()) {
			// use filtered set of events
			param.args[6] = newEvents
		} else {
			// event list is empty, skip original method call
			param.result = null
		}
	}
}
