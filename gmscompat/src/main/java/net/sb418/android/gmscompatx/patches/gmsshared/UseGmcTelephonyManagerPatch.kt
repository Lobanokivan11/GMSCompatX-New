/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.annotation.SuppressLint
import android.app.SystemServiceRegistry
import android.app.SystemServiceRegistry.ContextAwareServiceProducerWithBinder
import android.content.Context
import android.telecom.PhoneAccountHandle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.android.internal.gmscompat.sysservice.GmcTelephonyManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: Swap [android.telephony.TelephonyManager] for [GmcTelephonyManager].
 *
 * Implements:
 * - [94da5aad6c: TelephonyFrameworkInitializer.java:L70](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/telephony/java/android/telephony/TelephonyFrameworkInitializer.java#L70)
 * - [94da5aad6c: TelephonyManager.java:L651,L667](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/telephony/java/android/telephony/TelephonyManager.java#L651-L667)
 */
@Suppress("ClassName")
internal object UseGmcTelephonyManagerPatch : IPatch {
	override fun install() {
		SystemServiceRegistry_registerContextAwareService.install()
		TelephonyManager_createForSubscriptionId.install()
		TelephonyManager_createForPhoneAccountHandle.install()
	}

	/**
	 * Note: Original patch was in [android.telephony.TelephonyFrameworkInitializer].
	 */
	private object SystemServiceRegistry_registerContextAwareService : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(SystemServiceRegistry::class, "registerContextAwareService",
					String::class, Class::class, ContextAwareServiceProducerWithBinder::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// intercept telephony service registration
			if (param.args[0] as String == Context.TELEPHONY_SERVICE) {
				// replace service producer lambda
				param.args[2] = { ctx: Context -> GmcTelephonyManager(ctx) }
			}
		}
	}

	private object TelephonyManager_createForSubscriptionId : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					TelephonyManager::class, "createForSubscriptionId",
					Int::class
				),
				this
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam): GmcTelephonyManager {
			return GmcTelephonyManager(
				XposedHelpers.getObjectField(param.thisObject, "mContext") as Context,
				param.args[0] as Int,
			)
		}
	}

	private object TelephonyManager_createForPhoneAccountHandle : XC_MethodReplacement() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					TelephonyManager::class, "createForPhoneAccountHandle",
					PhoneAccountHandle::class
				),
				this
			)
		}

		override fun replaceHookedMethod(param: MethodHookParam): GmcTelephonyManager? {
			val thisObj = param.thisObject as TelephonyManager

			// region: reimplementation of original method
			@SuppressLint("MissingPermission")
			val subId = thisObj.getSubscriptionId(param.args[0] as PhoneAccountHandle)
			if (!SubscriptionManager.isValidSubscriptionId(subId)) {
				return null
			}
			// endregion

			return GmcTelephonyManager(
				XposedHelpers.getObjectField(param.thisObject, "mContext") as Context,
				subId,
			)
		}
	}
}
