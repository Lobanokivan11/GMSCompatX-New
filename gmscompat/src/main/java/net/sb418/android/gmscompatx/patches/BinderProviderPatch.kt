/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.os.Binder
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
            val binderGms2GcaClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
            XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(
                    Binder::class.java, "onTransact",
                    Int::class.java, Parcel::class.java, Parcel::class.java, Int::class.java
                ),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(binderParam: MethodHookParam) {
                        val instance = binderParam.thisObject
                        if (binderGms2GcaClass.isInstance(instance)) {
                            val code = binderParam.args[0] as Int
                            if (code == 218) {
                                try {
                                    Log.w(TAG, "GMSCompatX: Successfully intercepted raw transaction 218 at boot! Preventing AbstractMethodError.")
                                    
                                    val data = binderParam.args[1] as Parcel
                                    val reply = binderParam.args[2] as Parcel
                                    data.enforceInterface("com.android.internal.gmscompat.IGms2Gca")
                                    val processName = data.readString()
                                    val iGca2GmsBinder = data.readStrongBinder()
                                    val fileProxyBinder = data.readStrongBinder()
                                    val iGca2GmsClass = Class.forName(iGca2GmsBinder.interfaceDescriptor, false, classLoader)
                                    val iGca2GmsInstance = XposedHelpers.callStaticMethod(iGca2GmsClass, "asInterface", iGca2GmsBinder)
                                    if (fileProxyBinder != null) {
                                        val fileProxyClass = Class.forName("com.android.internal.gmscompat.dynamite.server.IFileProxyService", false, classLoader)
                                        val fileProxyInstance = XposedHelpers.callStaticMethod(fileProxyClass, "asInterface", fileProxyBinder)
                                        XposedHelpers.setStaticObjectField(binderGms2GcaClass, "dynamiteFileProxyService", fileProxyInstance)
                                    }
                                    XposedHelpers.callMethod(instance, "connectGmsCore", processName, iGca2GmsBinder)
                                    reply.writeNoException()
                                    reply.writeInt(0)
                                    binderParam.result = true
                                    return
                                        
                                } catch (t: Throwable) {
                                    Log.e(TAG, "GMSCompatX: Critical failure inside root IPC transaction redirect", t)
                                }
                            }
                        }
                    }
                }
            )
            Log.d(TAG, "GMSCompatX: Root Binder IPC protection shield installed successfully!")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install root BinderProviderPatch shield", t)
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
