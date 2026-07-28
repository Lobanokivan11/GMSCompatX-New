/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.nfc.NfcAdapter
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: intercept attempts to enable NFC.
 *
 * Implements: [94da5aad6c: NfcAdapter.java#L953-L956](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/nfc/NfcAdapter.java#L953-L956)
 */
internal object NfcAdapterPatch : IPatch, XC_MethodReplacement() {
	override fun install() {
		XposedBridge.hookMethod(MethodFinder.findMethodExactKt(NfcAdapter::class, "enable"), this)
	}

	override fun replaceHookedMethod(param: MethodHookParam): Boolean {
		GmsHooks.enableNfc()
		return false
	}
}
