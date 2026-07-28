/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.dynamite

import com.android.internal.gmscompat.dynamite.GmsDynamiteClientHooks
import dalvik.system.DelegateLastClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.io.File

/**
 * Purpose: modify dalvik class loader to support our custom mechanism for loading Dynamite modules
 * (using file descriptors).
 *
 * Implements:
 * - [7a7852258b: DelegateLastClassLoader.java#L114-117](https://github.com/GrapheneOS/platform_libcore/commit/7a7852258b52e155e59dfc5b5769acdd6a811e37#diff-cfe1d595ff141a2d8ceaf05b5bdd75e1d88d5b65d0355c8762b0f41e241df279R114-R117)
 * - [7a7852258b: DexPathList.java#L380](https://github.com/GrapheneOS/platform_libcore/commit/7a7852258b52e155e59dfc5b5769acdd6a811e37#diff-492f70da8f485b2fc44da584fbb9288290fb9c85419ae96af17ddc667fcc9732R380)
 */
@Suppress("ClassName")
internal object DalvikLoaderPatch : IPatch {
	override fun install() {
		DelegateLastClassLoader_constructor.install()
		DexPathList_makeDexElements.install()
	}

	private object DelegateLastClassLoader_constructor : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findConstructorExactKt(
					DelegateLastClassLoader::class,
					String::class,
					String::class,
					ClassLoader::class,
					Boolean::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// replace file paths of Dynamite modules with `/gmscompat_fd_%d` file descriptor references
			param.args[0] = GmsDynamiteClientHooks.maybeModifyClassLoaderPath(param.args[0] as String?, false)
			param.args[1] = GmsDynamiteClientHooks.maybeModifyClassLoaderPath(param.args[1] as String?, true)
		}
	}

	private object DexPathList_makeDexElements : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					"dalvik.system.DexPathList", "makeDexElements",
					MutableList::class,
					File::class,
					MutableList::class,
					ClassLoader::class,
					Boolean::class,
				),
				this
			)
		}

		/**
		 * Wrapper around [File] that returns true for the first call to [isFile].
		 */
		private class WrappedFile(pathname: String) : File(pathname) {
			/** Whether this object has had [isFile] called yet. */
			var tripped = false

			override fun isFile(): Boolean {
				return if (tripped) {
					super.isFile()
				} else {
					tripped = true
					true
				}
			}
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// replace Dynamite File objects with our own wrapped objects
			val files = param.args[0] as List<File>

			param.args[0] = files.map {
				if (it.path.startsWith("/gmscompat_fd_")) {
					WrappedFile(it.path)
				} else {
					it
				}
			}.toList()
		}
	}
}
