/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import net.sb418.android.gmscompatx.patches.PatchContext
import net.sb418.android.gmscompatx.patches.SystemAppPatcher
import net.sb418.android.gmscompatx.patches.SystemServerPatches
import net.sb418.android.gmscompatx.patches.UniversalPatches
import net.sb418.android.gmscompatx.util.ClassLoaderUtils
import net.sb418.android.gmscompatx.util.StaticClassLoader
import kotlin.reflect.KClass

/**
 * Entrypoint for the Xposed module.
 */
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
	companion object {
		const val TAG = "GMSCompatX"
	}

	override fun initZygote(startupParam: StartupParam) {
		// install universal patches
		UniversalPatches.install()
	}

	override fun handleLoadPackage(param: LoadPackageParam) {
		Log.d(TAG, "handleLoadPackage: package=${param.packageName}, process=${param.processName}, isFirst=${param.isFirstApplication}")

		// ignore if we've already patched this process
		if (!param.isFirstApplication)
			return
		// apply gmscompat patch to fix error
		if (param.packageName == "app.grapheneos.gmscompat")
			net.sb418.android.gmscompatx.patches.BinderProviderPatch.install(param.classLoader)
		// initialize patch context
		PatchContext.packageName = param.packageName
		PatchContext.processName = param.processName
		if (!PatchContext.procInfoInitialized.compareAndSet(false, true))
			throw IllegalStateException("PatchContext app info already initialized!")

		if (param.packageName == "android") {
			onSystemServerLoad(param)
		} else {
			onApplicationLoad(param)
		}
	}

	private fun onSystemServerLoad(param: LoadPackageParam) {
		// rebase our class loader onto the system_server's, so we can access its classes
		if (!ClassLoaderUtils.rebaseClassLoader(javaClass.classLoader!!, param.classLoader)) {
			Log.wtf(TAG, "Failed to rebase our class loader onto the system_server class loader!")
		}

		// install system_server patches
		SystemServerPatches.install()
	}

	private fun onApplicationLoad(param: LoadPackageParam) {
		val packageName: String = param.packageName
		val classNamesToInject: Array<String> = when (packageName) {
			"app.grapheneos.gmscompat" -> {
				runCatching {
					Class.forName("app.grapheneos.gmscompat.BinderGms2Gca", false, param.classLoader)
					arrayOf(
						"app.grapheneos.gmscompat.BinderGms2Gca",
						"app.grapheneos.gmscompat.BinderGms2Gca\$DeathRecipient",
						"app.grapheneos.gmscompat.BinderClientOfGmsCore2Gca",
						"app.grapheneos.gmscompat.BinderProvider",
						"app.grapheneos.gmscompat.Notifications",
						"app.grapheneos.gmscompat.Redirections"
					)
				}.getOrElse { e ->
					Log.e(TAG, "GMSCompatX: New GmsCompat structures not found. skipping injection.", e)
					emptyArray()
				}
			}
		else -> emptyArray()
		}
		if (classNamesToInject.isNotEmpty()) {
			val resolvedClasses = mutableListOf<Class<*>>()
			for (className in classNamesToInject) {
				try {
					val clazz = Class.forName(className, false, param.classLoader)
					resolvedClasses.add(clazz)
				} catch (e: Throwable) {
					Log.w(TAG, "GMSCompatX: Failed to resolve class for injection: $className")
				}
			}

			if (resolvedClasses.isNotEmpty()) {
				Log.d(TAG, "Injecting ${resolvedClasses.size} classes into $packageName")
				injectAppClassLoader(param.classLoader, resolvedClasses.toTypedArray())
			}
		}
		try {
			SystemAppPatcher.getPatchsetFor(packageName)?.install()
		} catch (t: Throwable) {
			Log.e(TAG, "GMSCompatX: Failed to install SystemAppPatcher for $packageName", t)
		}
	}

	/**
	 * Inject our own [classes] into the [target] class loader, by replacing its parent.
	 */
	private fun injectAppClassLoader(target: ClassLoader, classes: Array<Class<*>>) {
		val originalParent = XposedHelpers.getObjectField(target, "parent") as ClassLoader
		val newLoaderBuilder = StaticClassLoader.Builder(originalParent)

		for (clazz in classes) {
			newLoaderBuilder.addClass(clazz)
		}

		val newLoader = newLoaderBuilder.build()
		XposedHelpers.setObjectField(target, "parent", newLoader)
	}
}
