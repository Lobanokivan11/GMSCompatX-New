/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.SystemServiceRegistry
import android.content.Context
import android.os.IUserManager
import android.os.ServiceManager
import com.android.internal.gmscompat.sysservice.GmcUserManager
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch

/**
 * Purpose: swap [android.os.UserManager] for [GmcUserManager].
 *
 * Implements [94da5aad6c: SystemServiceRegistry.java#L810-L812](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/app/SystemServiceRegistry.java#L810-L812)
 */
internal object UseGmcUserManagerPatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		// get service fetcher registry
		val systemServiceFetchers = XposedHelpers.getStaticObjectField(SystemServiceRegistry::class.java, "SYSTEM_SERVICE_FETCHERS") as Map<String, Any>

		// find the `createService` method on the user service fetcher
		val userServiceFetcher =
			systemServiceFetchers[Context.USER_SERVICE]!!.javaClass.declaredMethods.first { it.name == "createService" }

		XposedBridge.hookMethod(userServiceFetcher, this)
	}

	@Throws(ServiceManager.ServiceNotFoundException::class)
	override fun replaceHookedMethod(param: MethodHookParam): GmcUserManager {
		val ctx = param.args[0] as Context  // technically ContextImpl, but it's not public

		val binder = ServiceManager.getServiceOrThrow(Context.USER_SERVICE)
		val service = IUserManager.Stub.asInterface(binder)

		return GmcUserManager(ctx, service)
	}
}
