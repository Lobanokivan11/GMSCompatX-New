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
			return
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

		val classesToInject = when (packageName) {
			// GmsCompat app - relies on many internal classes
			"app.grapheneos.gmscompat" -> arrayOf(
			//	android.app.compat.gms.GmsCompat::class,
			//	com.android.internal.gmscompat.BinderRedirector::class,
				com.android.internal.gmscompat.IClientOfGmsCore2Gca::class,
				com.android.internal.gmscompat.IClientOfGmsCore2Gca.Stub::class,
				com.android.internal.gmscompat.IGms2Gca::class,
				com.android.internal.gmscompat.IGms2Gca.Stub::class,
				com.android.internal.gmscompat.IGca2Gms::class,
			//	com.android.internal.gmscompat.GmsCompatApp::class,
				com.android.internal.gmscompat.GmsCompatConfig::class,
			//	com.android.internal.gmscompat.GmsInfo::class,
			//	com.android.internal.gmscompat.StubDef::class,
			//	com.android.internal.gmscompat.client.GmsCompatClientService::class,
				com.android.internal.gmscompat.dynamite.server.IFileProxyService::class,
			//	com.android.internal.gmscompat.flags.GmsFlag::class,
			)
			// all other apps
			else -> emptyArray<KClass<*>>()
		}

		// inject classes into app
		if (classesToInject.isNotEmpty()) {
			Log.d(TAG, "Injecting ${classesToInject.size} classes into $packageName")
			injectAppClassLoader(param.classLoader, classesToInject)
		}

		// apply system app patches (if needed)
		SystemAppPatcher.getPatchsetFor(packageName)?.install()
	}

	/**
	 * Inject our own [classes] into the [target] class loader, by replacing its parent.
	 */
	private fun injectAppClassLoader(target: ClassLoader, classes: Array<KClass<*>>) {
		val originalParent = XposedHelpers.getObjectField(target, "parent") as ClassLoader
		val newLoaderBuilder = StaticClassLoader.Builder(originalParent)

		for (clazz in classes) {
			newLoaderBuilder.addClass(clazz.java)
		}

		val newLoader = newLoaderBuilder.build()
		XposedHelpers.setObjectField(target, "parent", newLoader)
	}
}
