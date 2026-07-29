/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.os.IBinder
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.patches.SystemServerPatches.TAG

object ServiceManagerPublishPatch : IPatch {
    override fun install() {
        XposedHelpers.findAndHookMethod(
            "com.android.server.am.ActivityManagerService",
            null,
            "systemReady",
            Runnable::class.java,
            com.android.server.utils.TimingsTraceAndSlog::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    publishGmsCompatService()
                }
            }
        )
    }

    private fun publishGmsCompatService() {
        try {
            val smClass = Class.forName("android.os.ServiceManager")
            val addServiceMethod = smClass.getMethod("addService", String::class.java, IBinder::class.java)
            val binderGms2GcaClass = Class.forName("app.grapheneos.gmscompat.BinderGms2Gca")
            val instanceField = binderGms2GcaClass.getDeclaredField("INSTANCE")
            instanceField.isAccessible = true
            val binderInstance = instanceField.get(null) as IBinder
            addServiceMethod.invoke(null, "com.android.internal.gmscompat.IGms2Gca", binderInstance)
            Log.d(TAG, "GMSCompatX: Successfully forced published IGms2Gca directly to System ServiceManager!")
        } catch (t: Throwable) {
            Log.e(TAG, "GMSCompatX: Failed to dynamically publish IGms2Gca service to system registry", t)
        }
    }
}
