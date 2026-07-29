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

internal object InstrumentationPatch : IPatch, XC_MethodHook() {

	override fun install() {
		XposedBridge.hookAllMethods(Instrumentation::class.java, "newApplication", this)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		for (arg in param.args) {
			if (arg is Context) {
				val packageName = arg.packageName
				val classLoader = arg.classLoader ?: return
				if (packageName == "app.grapheneos.gmscompat") {
					try {
						val binderClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
						val stubClass = binderClass.superclass
						XposedBridge.hookMethod(
							XposedHelpers.findMethodExact(
								stubClass, "onTransact",
								Int::class.java, android.os.Parcel::class.java, android.os.Parcel::class.java, Int::class.java
							),
							object : XC_MethodHook() {
								override fun beforeHookedMethod(binderParam: MethodHookParam) {
									val code = binderParam.args[0] as Int
									if (code == 218) {
										try {
											Log.w(TAG, "GMSCompatX: Successfully intercepted incoming transaction 218 inside gmscompat process! Preventing AbstractMethodError.")
											
											val rawBinder = binderParam.thisObject as android.os.IBinder
											val data = binderParam.args[1] as android.os.Parcel
											val reply = binderParam.args[2] as android.os.Parcel
											data.enforceInterface("com.android.internal.gmscompat.IGms2Gca")
											val processName = data.readString()
											val iGca2GmsBinder = data.readStrongBinder()
											val fileProxyBinder = data.readStrongBinder()
											val iGca2GmsClass = Class.forName(iGca2GmsBinder.interfaceDescriptor, false, classLoader)
											val iGca2GmsInstance = XposedHelpers.callStaticMethod(iGca2GmsClass, "asInterface", iGca2GmsBinder)
											val gmsCompatConfigInstance = XposedHelpers.callMethod(rawBinder, "connectGmsCore", processName, iGca2GmsInstance)
											reply.writeNoException()
											if (gmsCompatConfigInstance != null) {
												reply.writeInt(1)
												XposedHelpers.callMethod(gmsCompatConfigInstance, "writeToParcel", reply, 0)
											} else {
												reply.writeInt(0)
											}
											binderParam.result = true
											return
										} catch (t: Throwable) {
											Log.e(TAG, "GMSCompatX: Internal fallback error during transaction 218 processing", t)
										}
									}
								}
							}
						)
						Log.d(TAG, "GMSCompatX: In-process BinderGms2Gca Stub shield installed successfully!")
					} catch (t: Throwable) {
						Log.e(TAG, "GMSCompatX: Critical failure while installing in-process Binder shield", t)
					}
				}

				try {
					GmsCompat.maybeEnable(arg)
				} catch (t: Throwable) {
					Log.e(TAG, "GmsCompat.maybeEnable execution safely bypassed", t)
				}
				return
			}
		}
		Log.e(TAG, "newApplication() hook: failed to locate Context in argument list!")
	}
}
