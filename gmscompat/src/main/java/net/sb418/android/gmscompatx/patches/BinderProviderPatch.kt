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
import net.sb418.android.gmscompatx.patches.IPatch
import java.util.concurrent.atomic.AtomicBoolean

object BinderProviderPatch {
    private const val TAG = "GMSCompatX.BinderProviderPatch"
    private const val KEY_BINDER = "binder"
    private val isSignatureShieldInstalled = AtomicBoolean(false)

    fun install(classLoader: ClassLoader) {
        try {
            val providerClass = Class.forName("app.grapheneos.gmscompat.BinderProvider", false, classLoader)
            XposedBridge.hookAllMethods(providerClass, "call", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val resultBundle = param.result as? Bundle ?: return
                    val binder = resultBundle.getBinder(KEY_BINDER)
                    if (binder != null) {
                        val methodArg = param.args.getOrNull(1) as? String ?: "0"
                        if (isSignatureShieldInstalled.compareAndSet(false, true)) {
                            try {
                                val binderGms2GcaClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
                                XposedBridge.hookAllMethods(binderGms2GcaClass, "connectGmsCore", object : XC_MethodHook() {
                                    override fun beforeHookedMethod(connectParam: MethodHookParam) {
                                        try {
                                            Log.w(TAG, "GMSCompatX: connectGmsCore call intercepted in active runtime! Resolving signature mismatch.")
                                            
                                            val instance = connectParam.thisObject
                                            val args = connectParam.args
                                            
                                            val processName = args[0] as String
                                            val callerBinderObject = args[1]
                                            val fileProxyService = args.getOrNull(2)
                                            val rawBinder: IBinder = if (callerBinderObject is IBinder) {
                                                callerBinderObject
                                            } else {
                                                XposedHelpers.callMethod(callerBinderObject, "asBinder") as IBinder
                                            }
                                            if (fileProxyService != null) {
                                                XposedHelpers.setStaticObjectField(binderGms2GcaClass, "dynamiteFileProxyService", fileProxyService)
                                            }
                                            XposedHelpers.callMethod(instance, "connect", "com.google.android.gms", processName, rawBinder)
                                            connectParam.result = null
                                            
                                            Log.d(TAG, "GMSCompatX: Signature mismatch successfully bypassed.")
                                            return
                                        } catch (t: Throwable) {
                                            Log.e(TAG, "GMSCompatX: Error inside lazy connectGmsCore shield", t)
                                        }
                                    }
                                })
                                Log.d(TAG, "GMSCompatX: In-process BinderGms2Gca Signature Shield injected successfully!")
                            } catch (t: Throwable) {
                                Log.e(TAG, "GMSCompatX: Delayed signature shield installation failed", t)
                                isSignatureShieldInstalled.set(false)
                            }
                        }

                        Log.d(TAG, "Successfully processed original Binder check from GmsCompat for type: $methodArg")
                    } else {
                        Log.w(TAG, "Binder not found in original result bundle")
                    }
                }
            })
            Log.d(TAG, "BinderProviderPatch optimization installed!")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install BinderProviderPatch", t)
        }
    }
}
