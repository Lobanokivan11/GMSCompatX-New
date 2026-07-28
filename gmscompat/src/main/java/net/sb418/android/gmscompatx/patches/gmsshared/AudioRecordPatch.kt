/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.compat.gms.GmsCompat
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Parcel
import android.os.SystemClock
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.Arrays
import java.util.WeakHashMap

/**
 * Purpose: spoof audio recordings when GMS tries to record without the `RECORD_AUDIO` permission.
 */
@Suppress("ClassName")
internal object AudioRecordPatch : IPatch {
	override fun install() {
		AudioRecord_constructor.install()
		AudioRecord_native_setup.install()
		AudioRecord_startRecording.install()
		AudioRecord_stop.install()
		AudioRecord_read.install()
		AudioRecord_release.install()
	}

	private const val TAG_SPOOF_RECORD = "SpoofAudioTrack"

	/**
	 * Map of [AudioRecord]s that we are spoofing (because GMS lacks permission).
	 * Values are the calculated "milliseconds in bytes".
	 */
	private val SPOOFED_RECORDS = WeakHashMap<AudioRecord, Double>()

	/**
	 * Implements [94da5aad6c: AudioRecord.java#L464-L470](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/AudioRecord.java#L464-L470)
	 */
	private object AudioRecord_constructor : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findConstructorExactKt(
					AudioRecord::class,
					AudioAttributes::class, AudioFormat::class, Int::class, Int::class, Context::class, Int::class
				),
				this,
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// spoof recording if GMS is missing permission to record
			if (!GmsCompat.hasPermission(android.Manifest.permission.RECORD_AUDIO)) {
				Log.d(TAG_SPOOF_RECORD, "constructor")

				val format = param.args[1] as AudioFormat

				SPOOFED_RECORDS[param.thisObject as AudioRecord] =
					1000.0 / (format.frameSizeInBytes * format.sampleRate).toDouble()
			}
		}
	}

	/**
	 * Implements [94da5aad6c: AudioRecord.java#L471](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/AudioRecord.java#L471)
	 */
	private object AudioRecord_native_setup : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(
				MethodFinder.findMethodExactKt(
					AudioRecord::class,
					"native_setup",
					Object::class,
					Object::class,
					IntArray::class,
					Int::class,
					Int::class,
					Int::class,
					Int::class,
					IntArray::class,
					Parcel::class,
					Long::class,
					Int::class
				),
				this,
			)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			// check if we need to spoof
			if (SPOOFED_RECORDS.containsKey(param.args[0] as AudioRecord)) {
				// skip original call, return success
				param.result = AudioRecord.SUCCESS
			}
		}
	}

	/**
	 * Implements [94da5aad6c: AudioRecord.java#L1362-L1365](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/AudioRecord.java#L1362-L1365)
	 */
	private object AudioRecord_startRecording : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(AudioRecord::class, "startRecording"), this)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			if (SPOOFED_RECORDS.containsKey(param.thisObject as AudioRecord)) {
				Log.d(TAG_SPOOF_RECORD, "startRecording")
				// skip original call
				param.result = null
			}
		}
	}

	/**
	 * Implements [94da5aad6c: AudioRecord.java#L1410-L1413](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/AudioRecord.java#L1410-L1413)
	 */
	private object AudioRecord_stop : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(AudioRecord::class, "stop"), this)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			if (SPOOFED_RECORDS.containsKey(param.thisObject as AudioRecord)) {
				Log.d(TAG_SPOOF_RECORD, "stop")
				// skip original call
				param.result = null
			}
		}
	}

	/**
	 * Implements [94da5aad6c: AudioRecord.java#L1463-L1467](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/media/java/android/media/AudioRecord.java#L1463-L1467)
	 */
	private object AudioRecord_read : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(AudioRecord::class, "read",
				ByteArray::class, Int::class, Int::class), this)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			val record = param.thisObject as AudioRecord

			if (SPOOFED_RECORDS.containsKey(record)) {
				Log.d(TAG_SPOOF_RECORD, "read")

				val audioData = param.args[0] as ByteArray
				val offsetInBytes = param.args[1] as Int
				val sizeInBytes = param.args[2] as Int

				Arrays.fill(audioData, offsetInBytes, offsetInBytes + sizeInBytes, 0.toByte())
				SystemClock.sleep((sizeInBytes.toDouble() * SPOOFED_RECORDS[record]!!).toLong())
				param.result = sizeInBytes
			}
		}
	}

	private object AudioRecord_release : XC_MethodHook() {
		fun install() {
			XposedBridge.hookMethod(MethodFinder.findMethodExactKt(AudioRecord::class, "release"), this)
		}

		override fun beforeHookedMethod(param: MethodHookParam) {
			SPOOFED_RECORDS.remove(param.thisObject)
		}
	}
}
