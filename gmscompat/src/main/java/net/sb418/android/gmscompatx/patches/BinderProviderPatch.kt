/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.os.Bundle
import android.os.IBinder
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch

object BinderProviderPatch {
    private const val TAG = "GMSCompatX.BinderProviderPatch"
    private const val KEY_BINDER = "binder"

    fun install(classLoader: ClassLoader) {
        try {
            val providerClass = Class.forName("app.grapheneos.gmscompat.BinderProvider", false, classLoader)
            XposedHelpers.findAndHookMethod(
                providerClass,
                "call",
                String::class.java,
                String::class.java,
                String::class.java,
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val methodArg = param.args[1] as? String ?: "0"
                        val binderClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
                        val instanceField = binderClass.getDeclaredField("INSTANCE")
                        instanceField.isAccessible = true
                        val rawBinder = instanceField.get(null) as IBinder
                        val replyBundle = Bundle()
                        replyBundle.putBinder(KEY_BINDER, rawBinder)
                        param.result = replyBundle
                        Log.d(TAG, "Bypassed Android Sandbox: Intercepted BinderProvider#call for type: $methodArg")
                    }
                }
            )
            Log.d(TAG, "BinderProviderPatch successfully hooks target class with 4-arg signature!")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install BinderProviderPatch", t)
        }
    }
}
