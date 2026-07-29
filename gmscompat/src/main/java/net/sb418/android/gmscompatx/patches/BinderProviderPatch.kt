/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch

object BinderProviderPatch {
    private const val TAG = "GMSCompatX.BinderProviderPatch"
    private const val KEY_BINDER = "binder"

    fun install(classLoader: ClassLoader) {
        try {
            val stubClass = Class.forName("com.android.internal.gmscompat.IGms2Gca\$Stub", false, classLoader)
            
            XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(
                    stubClass, "onTransact",
                    Int::class.java, Parcel::class.java, Parcel::class.java, Int::class.java
                ),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(binderParam: MethodHookParam) {
                        val code = binderParam.args[0] as Int
                        if (code == 218) {
                            try {
                                Log.w(TAG, "GMSCompatX: Global intercept of transaction 218 in AIDL Stub! Preventing AbstractMethodError.")
                                
                                val rawBinder = binderParam.thisObject as IBinder
                                val data = binderParam.args[1] as Parcel
                                val reply = binderParam.args[2] as Parcel
                                data.enforceInterface("com.android.internal.gmscompat.IGms2Gca")
                                val processName = data.readString()
                                val iGca2GmsBinder = data.readStrongBinder()
                                val fileProxyBinder = data.readStrongBinder()
                                val iGca2GmsClass = Class.forName("com.android.internal.gmscompat.IGca2Gms", false, classLoader)
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
                                Log.e(TAG, "GMSCompatX: Fatal error inside global IPC transaction fallback", t)
                            }
                        }
                    }
                }
            )
            Log.d(TAG, "GmsCompat AIDL Stub Global IPC protection hook installed successfully!")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install global AIDL protection. Fallback to classic provider hook.", t)
        }
        try {
            val providerClass = Class.forName("app.grapheneos.gmscompat.BinderProvider", false, classLoader)
            XposedBridge.hookAllMethods(providerClass, "call", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val resultBundle = param.result as? Bundle ?: return
                    val binder = resultBundle.getBinder(KEY_BINDER)
                    if (binder != null) {
                        val methodArg = param.args.getOrNull(1) as? String ?: "0"
                        Log.d(TAG, "Successfully processed original Binder check from GmsCompat for type: $methodArg")
                    }
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to run secondary BinderProvider check", t)
        }
    }
}
