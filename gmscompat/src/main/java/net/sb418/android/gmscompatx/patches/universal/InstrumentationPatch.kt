/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.universal

import android.app.Instrumentation
import android.app.compat.gms.GmsCompat
import android.content.Context
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.patches.UniversalPatches.TAG
import java.util.concurrent.atomic.AtomicBoolean

internal object InstrumentationPatch : IPatch, XC_MethodHook() {
	private val isGmsCompatAppHookInstalled = AtomicBoolean(false)

	override fun install() {
		XposedBridge.hookAllMethods(Instrumentation::class.java, "newApplication", this)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		for (arg in param.args) {
			if (arg is Context) {
				val classLoader = arg.classLoader ?: return
				if (isGmsCompatAppHookInstalled.compareAndSet(false, true)) {
					try {
						val gmsCompatAppClass = Class.forName("com.android.internal.gmscompat.GmsCompatApp", false, classLoader)
						XposedBridge.hookAllMethods(gmsCompatAppClass, "connect", object : XC_MethodHook() {
							override fun beforeHookedMethod(connectParam: MethodHookParam) {
								try {
									var isGmsCompatApkUpdated = false
									try {
										Class.forName("app.grapheneos.gmscompat.BinderClientOfGmsCore2Gca", false, classLoader)
										isGmsCompatApkUpdated = true
									} catch (e: ClassNotFoundException) {}
									if (!isGmsCompatApkUpdated) {
										val iGca2GmsClass = Class.forName("com.android.internal.gmscompat.IGca2Gms", false, classLoader)
										val binderField = XposedHelpers.findFieldIfExists(gmsCompatAppClass, "gms2Gca")
										if (binderField != null) {
											val gms2GcaBinder = binderField.get(null)
											
											if (gms2GcaBinder != null) {
												Log.w(TAG, "GMSCompatX: Mismatch detected in GmsCompatApp! Old APK running on New OS. Forcing safe downgrade redirect.")
												val processName = connectParam.args.getOrNull(0) as? String ?: ""
												val iGca2GmsInstance = connectParam.args.getOrNull(1)
												
												if (iGca2GmsInstance != null) {
													val config = XposedHelpers.callMethod(gms2GcaBinder, "connectGmsCore", processName, iGca2GmsInstance)
													connectParam.result = config
													return
												}
											}
										}
									}
								} catch (t: Throwable) {
									Log.e(TAG, "GMSCompatX: Error inside GmsCompatApp redirect shield", t)
								}
							}
						})
						Log.d(TAG, "GMSCompatX: Active Runtime GmsCompatApp Shield installed successfully!")
					} catch (t: Throwable) {
						Log.e(TAG, "GMSCompatX: Failed to dynamically inject GmsCompatApp redirection", t)
						isGmsCompatAppHookInstalled.set(false)
					}
				}
				try {
					GmsCompat.maybeEnable(arg)
				} catch (t: Throwable) {
					Log.e(TAG, "GmsCompat.maybeEnable execution suppressed", t)
				}
				return
			}
		}
		Log.e(TAG, "newApplication() hook: failed to locate Context in argument list!")
	}
}
