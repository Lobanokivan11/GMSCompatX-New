/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmscore

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.IMediaProjection
import android.media.projection.MediaProjection
import android.os.Handler
import android.view.Surface
import com.android.internal.gmscompat.GmcMediaProjectionService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose:
 * 1. run [GmcMediaProjectionService] callbacks
 * 2. remove privileged `VIRTUAL_DISPLAY_FLAG_SECURE` flag from [MediaProjection.createVirtualDisplay] calls
 *
 * Implements:
 * - [94da5aad6c: MediaProjection.java#L63](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/projection/MediaProjection.java#L63)
 * - [94da5aad6c: MediaProjection.java#L156](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/projection/MediaProjection.java#L156)
 * - [94da5aad6c: MediaProjection.java#L264](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/projection/MediaProjection.java#L264)
 */
@Suppress("ClassName")
internal object MediaProjectionPatch : IPatch {
	override fun install() {
		MediaProjection_constructor.install()
		MediaProjectionCallback_onStop.install()
		MediaProjection_createVirtualDisplay.install()
	}

	private object MediaProjection_constructor : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findConstructorExactKt(MediaProjection::class, Context::class, IMediaProjection::class),
				this,
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) = GmcMediaProjectionService.start()
	}

	private object MediaProjectionCallback_onStop : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExact(
					"android.media.projection.MediaProjection\$MediaProjectionCallback",
					"onStop"
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam?) = GmcMediaProjectionService.stop()
	}

	private object MediaProjection_createVirtualDisplay : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					MediaProjection::class, "createVirtualDisplay",
					String::class, Int::class, Int::class, Int::class, Int::class, Surface::class,
					VirtualDisplay.Callback::class, Handler::class
				),
				this
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// GOS: "requires the privileged CAPTURE_SECURE_VIDEO_OUTPUT permission"
			param.args[4] = (param.args[4] as Int).and(DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE.inv())
		}
	}
}
