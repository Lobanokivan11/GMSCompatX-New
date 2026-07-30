/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.content.Context
import android.os.IBinder
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.patches.SystemServerPatches.TAG

object ServiceManagerPublishPatch : IPatch {
    override fun install() {
        try {
            val amsClass = Class.forName("com.android.server.am.ActivityManagerService")
            XposedBridge.hookAllMethods(amsClass, "systemReady", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val amsInstance = param.thisObject
                    val systemContext = XposedHelpers.getObjectField(amsInstance, "mContext") as? Context
                    
                    if (systemContext != null) {
                        publishGmsCompatService(systemContext)
                    } else {
                        Log.e(TAG, "GMSCompatX: Unable to extract mContext from ActivityManagerService")
                    }
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG, "GMSCompatX: Failed to find or hook ActivityManagerService#systemReady", t)
        }
    }

    private fun publishGmsCompatService(systemContext: Context) {
        try {
            val gmsCompatContext = systemContext.createPackageContext(
                "app.grapheneos.gmscompat",
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            val gmsCompatClassLoader = gmsCompatContext.classLoader
            val binderGms2GcaClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, gmsCompatClassLoader)
            val instanceField = runCatching { 
                binderGms2GcaClass.getDeclaredField("INSTANCE") 
            }.getOrNull()
            
            if (instanceField == null) {
                Log.w(TAG, "GMSCompatX: BinderGms2Gca.INSTANCE field not found. Structure changed, skipping publication.")
                return
            }
            
            instanceField.isAccessible = true
            val rawInstance = instanceField.get(null)
            if (rawInstance !is IBinder) {
                Log.w(TAG, "GMSCompatX: BinderGms2Gca instance is not an IBinder. Skipping ServiceManager publication.")
                return
            }

            val smClass = Class.forName("android.os.ServiceManager")
            val addServiceMethod = smClass.getMethod("addService", String::class.java, IBinder::class.java)
            addServiceMethod.invoke(null, "app.grapheneos.gmscompat.BinderGms2Gca", rawInstance)
            
            Log.d(TAG, "GMSCompatX: Successfully published BinderGms2Gca directly to System ServiceManager.")
        } catch (e: ClassNotFoundException) {
            Log.w(TAG, "GMSCompatX: app.grapheneos.gmscompat classes not found, skipping publication.")
        } catch (t: Throwable) {
            Log.e(TAG, "GMSCompatX: Failed to dynamically publish service to system registry", t)
        }
    }
}
