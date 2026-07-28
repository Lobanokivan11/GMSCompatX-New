/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import com.android.internal.gmscompat.BinderRedirector
import com.android.internal.gmscompat.HybridBinder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.util.MethodFinder
import net.sb418.android.gmscompatx.util.SyncWeakHashSet

/**
 * Patchset that applies hooks for [BinderRedirector].
 */
@Suppress("ClassName")
object BinderRedirectorPatches : AbstractPatchSet() {
	private const val TAG = "$BASE_TAG.BinderRedirector"

	override fun preinstallCheck() {
		if (!BinderRedirector.enabled()) {
			throw IPatch.CannotInstallException("BinderRedirector#enabled returned false!")
		}
	}

	override val patches: Array<IPatch> = arrayOf(
		Binder_attachInterface,
		Binder_onTransact,
		Parcel_readStrongBinder,
	)


	/** Set of [Binder] objects which require redirection checks. */
	private val REDIRECTION_CHECK_BINDERS = SyncWeakHashSet<Binder>(4)

	/** Set of [Parcel] objects which require redirection checks. */
	private val REDIRECTION_CHECK_PARCELS = SyncWeakHashSet<Parcel>(4)

	/**
	 * Implements: [94da5aad6c: Binder.java#L684-L686](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Binder.java#L684-L686)
	 */
	private object Binder_attachInterface : IPatch, XC_MethodHook() {
		override fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(Binder::class, "attachInterface", IInterface::class, String::class),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val descriptor = param.args[1] as String?
			if (descriptor == "com.google.android.gms.common.internal.IGmsCallbacks") {
				REDIRECTION_CHECK_BINDERS.add(param.thisObject as Binder)
			}
		}
	}

	/**
	 * Implements: [94da5aad6c: Binder.java#L1288](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Binder.java#L1288)
	 */
	private object Binder_onTransact : IPatch, XC_MethodHook() {
		override fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					Binder::class, "onTransact", Int::class, Parcel::class, Parcel::class, Int::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			if (REDIRECTION_CHECK_BINDERS.contains(param.thisObject)) {
				REDIRECTION_CHECK_PARCELS.add(param.args[1] as Parcel)
			}
		}
	}

	/**
	 * Implements: [94da5aad6c: Binder.java#L3143-L3148](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/os/Binder.java#L3143-L3148)
	 */
	private object Parcel_readStrongBinder : IPatch, XC_MethodHook() {
		override fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(Parcel::class, "readStrongBinder"), this)
		}

		override fun afterHookedMethod(param: MethodHookParam) {
			// skip hook if an exception was thrown
			if (param.hasThrowable()) return

			if (param.result != null && REDIRECTION_CHECK_PARCELS.contains(param.thisObject)) {
				val hb = HybridBinder.maybeCreate(param.result as IBinder)
				if (hb != null) {
					param.result = hb
				}
			}
		}
	}
}
