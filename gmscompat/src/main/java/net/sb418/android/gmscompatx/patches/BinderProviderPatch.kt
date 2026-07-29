/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.os.Bundle
import android.os.IBinder
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object BinderProviderPatch {
    private const val TAG = "GMSCompatX.BinderProviderPatch"
    private const val KEY_BINDER = "binder"

    fun install(classLoader: ClassLoader) {
        try {
            val binderGms2GcaClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
            XposedHelpers.findAndHookMethod(
                binderGms2GcaClass,
                "connectGmsCore",
                String::class.java,
                IBinder::class.java,
                "com.android.internal.gmscompat.dynamite.server.IFileProxyService",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val processName = param.args[0] as? String
                            val callerBinder = param.args[1] as? IBinder
                            val fileProxyService = param.args[2]

                            Log.w(TAG, "GMSCompatX: Intercepted connectGmsCore for process: $processName. Preventing AbstractMethodError.")
                            if (fileProxyService != null) {
                                XposedHelpers.setStaticObjectField(binderGms2GcaClass, "dynamiteFileProxyService", fileProxyService)
                            }
                            param.result = null 
                            
                        } catch (t: Throwable) {
                            Log.e(TAG, "GMSCompatX: Error inside connectGmsCore hook hook", t)
                        }
                    }
                }
            )
            Log.d(TAG, "GMSCompatX: High-level connectGmsCore patch installed successfully!")
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "GMSCompatX: BinderGms2Gca not found in this ClassLoader. Target package context mismatch.")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install primary BinderGms2Gca patch", t)
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
