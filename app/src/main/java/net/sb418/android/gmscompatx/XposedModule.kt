/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * Entrypoint for the Xposed module.
 * Fully isolated to prevent R8/Proguard inline contamination.
 */
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
	companion object {
		const val TAG = "GMSCompatX"
	}

	override fun initZygote(startupParam: StartupParam) {
		runCatching {
			net.sb418.android.gmscompatx.patches.UniversalPatches.install()
		}.onFailure { t ->
			Log.e(TAG, "GMSCompatX: Failed to install UniversalPatches", t)
		}
	}

	override fun handleLoadPackage(param: LoadPackageParam) {
		runCatching {
			if (!param.isFirstApplication)
				return
			if (param.packageName == "app.grapheneos.gmscompat") {
				runCatching {
					val patchClass = Class.forName("net.sb418.android.gmscompatx.patches.BinderProviderPatch")
					val installMethod = patchClass.getDeclaredMethod("install", ClassLoader::class.java)
					installMethod.isAccessible = true
					installMethod.invoke(null, param.classLoader)
					Log.d(TAG, "GMSCompatX: BinderProviderPatch invoked successfully via reflection.")
				}.onFailure { e ->
					Log.e(TAG, "GMSCompatX: Safe execution of BinderProviderPatch failed. ROM mismatch.")
				}
			}
			runCatching {
				val ctxClass = Class.forName("net.sb418.android.gmscompatx.patches.PatchContext")
				val pkgField = ctxClass.getDeclaredField("packageName")
				val procField = ctxClass.getDeclaredField("processName")
				val initField = ctxClass.getDeclaredField("procInfoInitialized")
				
				pkgField.isAccessible = true
				procField.isAccessible = true
				initField.isAccessible = true
				
				pkgField.set(null, param.packageName)
				procField.set(null, param.processName)
				val atomicBool = initField.get(null)
				atomicBool.javaClass.getMethod("compareAndSet", Boolean::class.java, Boolean::class.java).invoke(atomicBool, false, true)
			}
			if (param.packageName == "android") {
				runCatching {
					val serverClass = Class.forName("net.sb418.android.gmscompatx.patches.SystemServerPatches")
					serverClass.getDeclaredMethod("install").invoke(null)
				}
			} else if (param.packageName != "app.grapheneos.gmscompat") {
				runCatching {
					val appPatcherClass = Class.forName("net.sb418.android.gmscompatx.patches.SystemAppPatcher")
					val getPatchset = appPatcherClass.getDeclaredMethod("getPatchsetFor", String::class.java)
					val patchset = getPatchset.invoke(null, param.packageName)
					patchset?.javaClass?.getDeclaredMethod("install")?.invoke(patchset)
				}
			}
		}.onFailure { t ->
			Log.e(TAG, "GMSCompatX: Top-level handleLoadPackage trapped an error safely.", t)
		}
	}
}
