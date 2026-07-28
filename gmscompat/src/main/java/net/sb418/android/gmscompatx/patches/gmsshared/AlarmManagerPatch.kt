/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.app.AlarmManager
import android.app.AlarmManagerHidden
import android.app.PendingIntent
import android.os.WorkSource
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder
import java.util.concurrent.Executor

/**
 * Purpose: adjust scheduled alarms to avoid issues from lack of permissions:
 * 1. demote scheduled alarms from `WINDOW_EXACT` to `WINDOW_HEURISTIC` (needs `SCHEDULE_EXACT_ALARM | USE_EXACT_ALARM`)
 * 2. skip setting `workSource` (needs `UPDATE_DEVICE_STATS`)
 *
 * Implements [94da5aad6c: AlarmManager.java#L940-L946](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/apex/jobscheduler/framework/java/android/app/AlarmManager.java#L940-L946)
 */
internal object AlarmManagerPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findMethodExactKt(
				AlarmManager::class,
				"setImpl",
				Int::class,
				Long::class,
				Long::class,
				Long::class,
				Int::class,
				PendingIntent::class,
				AlarmManager.OnAlarmListener::class,
				String::class,
				Executor::class,
				WorkSource::class,
				AlarmManager.AlarmClockInfo::class
			),
			this
		)
	}

	override fun beforeHookedMethod(param: MethodHookParam) {
		val thisObj = param.thisObject as AlarmManager
		val windowMillis = param.args[2] as Long

		// demote exact alarms if missing the required permissions
		if (windowMillis == AlarmManagerHidden.WINDOW_EXACT && !thisObj.canScheduleExactAlarms()) {
			param.args[2] = AlarmManagerHidden.WINDOW_HEURISTIC
		}

		// set workSource to null
		param.args[9] = null
	}
}
