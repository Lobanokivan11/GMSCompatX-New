/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.system_server

import android.content.pm.parsing.result.ParseInput
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.Log
import com.android.server.ext.GmsSysServerHooks
import com.android.server.pm.pkg.parsing.ParsingPackage
import com.android.server.pm.pkg.parsing.ParsingPackageUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.patches.SystemServerPatches.TAG
import net.sb418.android.gmscompatx.util.MethodFinder
import net.sb418.android.gmscompatx.util.WeakHashSet

/**
 * Purpose: modify package info in [ParsingPackageUtils.parseBaseApplication] using [GmsSysServerHooks] callbacks.
 *
 * Implements: [3ac920ce4c: ParsingPackageUtils.java#L2208-L2209](https://github.com/GrapheneOS/platform_frameworks_base/blob/3ac920ce4cb44c009d9415a83c9391a673027c1c/services/core/java/com/android/server/pm/pkg/parsing/ParsingPackageUtils.java#L2208-L2209)
 */
internal object PackageParsingUtilsPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				ParsingPackageUtils::class, "parseBaseApplication",
				ParseInput::class, ParsingPackage::class, Resources::class, XmlResourceParser::class, Int::class
			),
			this,
		)
	}

	/** Track [ParseInput] implementations that we've already hooked. */
	private val hookedCallbacks = WeakHashSet<Class<out ParseInput>>()

	/** Packages flagged for modification (by [GmsSysServerHooks] callbacks). */
	private val targetPackages = WeakHashSet<Any>()

	override fun beforeHookedMethod(param: MethodHookParam) {
		val input = param.args[0] as ParseInput
		val pkg = param.args[1] as ParsingPackage

		val inputClass: Class<out ParseInput> = input.javaClass

		// hook the success method of this object's class (if we haven't already)
		synchronized(hookedCallbacks) {
			if (!hookedCallbacks.contains(inputClass)) {
				// all we can rely on is that the method is named `success` and accepts a single argument
				val targetMethod = XposedHelpers.findMethodBestMatch(inputClass, "success", Any::class.java)
				Log.v(TAG, String.format("Dynamically hooking success method `%s`", targetMethod))
				XposedBridge.hookMethod(targetMethod, ParseInput_success())
				hookedCallbacks.add(inputClass)
			}
		}

		// flag the package for modification
		synchronized(targetPackages) {
			targetPackages.add(pkg)
		}
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		val pkg = param.args[1]

		// cleanup package flag
		synchronized(targetPackages) {
			targetPackages.remove(pkg)
		}
	}

	@Suppress("ClassName")
	private class ParseInput_success : XC_MethodHook() {
		override fun beforeHookedMethod(param: MethodHookParam) {
			// this method will be called with arbitrary types; we only want to hook on ParsingPackage objects
			val pkg = param.args[0] as? ParsingPackage ?: return

			// skip if this package isn't flagged
			synchronized(targetPackages) {
				if (!targetPackages.contains(pkg)) return
			}

			// call hooks
			GmsSysServerHooks.maybeAddServiceDuringParsing(pkg)
			GmsSysServerHooks.fixupPermissions(pkg)
		}
	}
}
