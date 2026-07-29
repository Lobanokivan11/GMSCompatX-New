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
	private val isProxyHookInstalled = AtomicBoolean(false)

	override fun install() {
		XposedBridge.hookAllMethods(Instrumentation::class.java, "newApplication", this)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		for (arg in param.args) {
			if (arg is Context) {
				val classLoader = arg.classLoader ?: return
				if (isProxyHookInstalled.compareAndSet(false, true)) {
					try {
						val proxyClass = Class.forName("com.android.internal.gmscompat.IGms2Gca\$Stub\$Proxy", false, classLoader)
						val fileProxyClass = Class.forName("com.android.internal.gmscompat.dynamite.server.IFileProxyService", false, classLoader)
						val connectGmsCoreMethod = XposedHelpers.findMethodExact(proxyClass, "connectGmsCore", String::class.java, Class.forName("com.android.internal.gmscompat.IGca2Gms", false, classLoader), fileProxyClass)
						
						XposedBridge.hookMethod(connectGmsCoreMethod, object : XC_MethodHook() {
							override fun beforeHookedMethod(proxyParam: MethodHookParam) {
								var isGmsCompatApkUpdated = false
								try {
									Class.forName("app.grapheneos.gmscompat.BinderClientOfGmsCore2Gca", false, classLoader)
									isGmsCompatApkUpdated = true
								} catch (e: ClassNotFoundException) {}
								if (!isGmsCompatApkUpdated) {
									Log.w(TAG, "GMSCompatX: Client proxy intercepted! Target APK is old. Redirecting to 2-param connectGmsCore call.")
									
									val mRemote = XposedHelpers.getObjectField(proxyParam.thisObject, "mRemote") as android.os.IBinder
									val processName = proxyParam.args[0] as String
									val iGca2GmsInstance = proxyParam.args[1]
									val data = android.os.Parcel.obtain()
									val reply = android.os.Parcel.obtain()
									try {
										data.writeInterfaceToken("com.android.internal.gmscompat.IGms2Gca")
										data.writeString(processName)
										data.writeStrongBinder(XposedHelpers.callMethod(iGca2GmsInstance, "asBinder") as android.os.IBinder)
										mRemote.transact(2, data, reply, 0)
										reply.readException()
										if (reply.readInt() != 0) {
											val configClass = Class.forName("com.android.internal.gmscompat.GmsCompatConfig", false, classLoader)
											val creatorField = configClass.getField("CREATOR")
											val creator = creatorField.get(null)
											proxyParam.result = XposedHelpers.callMethod(creator, "createFromParcel", reply)
										} else {
											proxyParam.result = null
										}
									} finally {
										reply.recycle()
										data.recycle()
									}
								}
							}
						})
						Log.d(TAG, "GMSCompatX: Outgoing Client Proxy Hook successfully installed!")
					} catch (t: Throwable) {
						Log.e(TAG, "GMSCompatX: Failed to install client proxy fallback hook", t)
						isProxyHookInstalled.set(false)
					}
				}

				try {
					GmsCompat.maybeEnable(arg)
				} catch (t: Throwable) {
					Log.e(TAG, "GmsCompat.maybeEnable crashed", t)
				}
				return
			}
		}
		Log.e(TAG, "newApplication() hook: failed to locate Context in argument list!")
	}
}
