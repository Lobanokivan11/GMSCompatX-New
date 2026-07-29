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
import net.sb418.android.gmscompatx.patches.IPatch

object BinderProviderPatch {
    private const val TAG = "GMSCompatX.BinderProviderPatch"
    private const val KEY_BINDER = "binder"

    fun install(classLoader: ClassLoader) {
        try {
            val providerClass = Class.forName("app.grapheneos.gmscompat.BinderProvider", false, classLoader)
            
            XposedBridge.hookAllMethods(providerClass, "call", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
					val resultBundle = param.result as? Bundle ?: return
        			val binder = resultBundle.getBinder(KEY_BINDER)
        			if (binder != null) {
                    	val methodArg = param.args.getOrNull(1) as? String ?: "0"
                    	try {
                        	val binderClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, classLoader)
        	                val instanceField = binderClass.getDeclaredField("INSTANCE")
	                        instanceField.isAccessible = true
    	                    val rawBinder = instanceField.get(null) as IBinder
            	            val replyBundle = Bundle()
                        	replyBundle.putBinder(KEY_BINDER, rawBinder)
                	        param.result = replyBundle
                    	    Log.d(TAG, "Bypassed Android Sandbox (Before): Intercepted call for type: $methodArg")
	                    } catch (t: Throwable) { 
							Log.e(TAG, "Error resolving Binder inside hook (Safely bypassed)", t)
						}
						Log.d(TAG, "Successfully intercepted original Binder from GmsCompat")
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
