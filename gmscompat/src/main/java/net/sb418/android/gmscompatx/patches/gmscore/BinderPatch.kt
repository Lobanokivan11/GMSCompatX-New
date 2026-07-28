/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmscore

import android.os.Binder
import android.os.IInterface
import android.os.Parcel
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import net.sb418.android.gmscompatx.util.SyncWeakHashSet

/**
 * Purpose: intercept `IGmsServiceBroker` service calls for permission spoofing.
 *
 * Implements:
 * - [3ac920ce4c: Binder.java#L688-L690](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/core/java/android/os/Binder.java#L688-L690)
 * - [3ac920ce4c: Binder.java#L1296-L1298](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/core/java/android/os/Binder.java#L1296-L1298)
 * - [3ac920ce4c: Binder.java#L1340-L1342](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/core/java/android/os/Binder.java#L1340-L1342)
 */
@Suppress("ClassName")
internal object BinderPatch : IPatch {
	override fun install() {
		Binder_attachInterface.install()
		Binder_onTransact.install()
	}

	/** Set of [Binder]s marked as "GmsServiceBroker". */
	private val GMS_SERVICE_BROKER_BINDERS = SyncWeakHashSet<Binder>()

	/** Set of [Binder]s to call the late hook for. */
	private val BINDERS_DO_LATE_CALL = SyncWeakHashSet<Binder>()

	private object Binder_attachInterface : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(Binder::class, "attachInterface", IInterface::class, String::class),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val descriptor = param.args[1] as String?
			if (descriptor == GmsHooks.GMS_SERVICE_BROKER_INTERFACE_DESCRIPTOR) {
				GMS_SERVICE_BROKER_BINDERS.add(param.thisObject as Binder)
			}
		}
	}

	/**
	 * NOTE: This hook runs later than the original patch - may cause issues.
	 */
	private object Binder_onTransact : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					Binder::class, "onTransact", Int::class, Parcel::class, Parcel::class, Int::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val binder = param.thisObject as Binder

			if (GMS_SERVICE_BROKER_BINDERS.contains(binder)) {
				val code = param.args[0] as Int
				val data = param.args[1] as Parcel

				if (GmsHooks.onBeginGmsServiceBrokerCall(code, data)) {
					BINDERS_DO_LATE_CALL.add(binder)
				}
			}
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			if (BINDERS_DO_LATE_CALL.contains(param.thisObject)) {
				GmsHooks.onEndGmsServiceBrokerCall()
			}
		}
	}
}
