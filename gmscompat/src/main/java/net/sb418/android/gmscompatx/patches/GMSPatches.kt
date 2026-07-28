/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches

import android.app.compat.gms.GmsCompat
import net.sb418.android.gmscompatx.patches.gmsshared.ActivityManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ActivityPatch
import net.sb418.android.gmscompatx.patches.gmsshared.AlarmManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.AppOpsManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ApplicationPatch
import net.sb418.android.gmscompatx.patches.gmsshared.AudioRecordPatch
import net.sb418.android.gmscompatx.patches.gmsshared.BinderPatch
import net.sb418.android.gmscompatx.patches.gmsshared.BroadcastOptionsPatch
import net.sb418.android.gmscompatx.patches.gmsshared.BuildPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ConnectivityManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ContentResolverPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ContextImplBroadcastFilterPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ContextImplPatch
import net.sb418.android.gmscompatx.patches.gmsshared.DeviceConfigPatch
import net.sb418.android.gmscompatx.patches.gmsshared.DownloadManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.FilterLocationRequestsPatch
import net.sb418.android.gmscompatx.patches.gmsshared.FilterTelephonyCallbackEventsPatch
import net.sb418.android.gmscompatx.patches.gmsshared.InstrumentationPatch
import net.sb418.android.gmscompatx.patches.gmsshared.IntentPatch
import net.sb418.android.gmscompatx.patches.gmsshared.NfcAdapterPatch
import net.sb418.android.gmscompatx.patches.gmsshared.ParcelPatch
import net.sb418.android.gmscompatx.patches.gmsshared.PendingIntentPatch
import net.sb418.android.gmscompatx.patches.gmsshared.PowerExemptionManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.SQLiteOpenHelperPatch
import net.sb418.android.gmscompatx.patches.gmsshared.SettingsPatch
import net.sb418.android.gmscompatx.patches.gmsshared.SharedPreferencesImplPatch
import net.sb418.android.gmscompatx.patches.gmsshared.UseGmcPackageManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.UseGmcTelephonyManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.UseGmcUserManagerPatch
import net.sb418.android.gmscompatx.patches.gmsshared.UserHandlePatch

/**
 * Patchset that applies to all GMS apps (GSF, GMS Core, Play Store, and GSA).
 *
 * All patches implicitly assume that [GmsCompat.isEnabled] is true.
 */
object GMSPatches : AbstractPatchSet() {
	const val TAG = "$BASE_TAG.GMS"

	override fun preinstallCheck() {
		if (!GmsCompat.isEnabled())
			throw IPatch.CannotInstallException("GmsCompat#isEnabled returned false!")
	}

	override val patches: Array<IPatch> = arrayOf(
		ActivityManagerPatch,
		ActivityPatch,
		AlarmManagerPatch,
		AppOpsManagerPatch,
		ApplicationPatch,
		AudioRecordPatch,
		BinderPatch,
		BroadcastOptionsPatch,
		BuildPatch,
		ConnectivityManagerPatch,
		ContentResolverPatch,
		ContextImplBroadcastFilterPatch,
		ContextImplPatch,
		DeviceConfigPatch,
		DownloadManagerPatch,
		FilterLocationRequestsPatch,
		FilterTelephonyCallbackEventsPatch,
		InstrumentationPatch,
		IntentPatch,
		NfcAdapterPatch,
		ParcelPatch,
		PendingIntentPatch,
		PowerExemptionManagerPatch,
		SQLiteOpenHelperPatch,
		SettingsPatch,
		SharedPreferencesImplPatch,
		UseGmcPackageManagerPatch,
		UseGmcTelephonyManagerPatch,
		UseGmcUserManagerPatch,
		UserHandlePatch,
	)
}
