/*
 * SPDX-FileCopyrightText: sudoBash418
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sb418.android.gmscompatx.patches.gmsshared

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.android.internal.gmscompat.GmsHooks
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import net.sb418.android.gmscompatx.patches.IPatch
import net.sb418.android.gmscompatx.util.MethodFinder

/**
 * Purpose: call [GmsHooks.onSQLiteOpenHelperConstructed]
 *
 * Implements: [94da5aad6c: SQLiteOpenHelper.java:L175-177](https://github.com/GrapheneOS/platform_frameworks_base/blob/94da5aad6c3184e8afc540bd4cda80fa8a1f2cb6/core/java/android/database/sqlite/SQLiteOpenHelper.java#L175-L177)
 */
internal object SQLiteOpenHelperPatch : IPatch, XC_MethodHook() {
	override fun install() {
		XposedBridge.hookMethod(
			MethodFinder.findConstructorExactKt(
				SQLiteOpenHelper::class,
				Context::class,
				String::class,
				Int::class,
				Int::class,
				SQLiteDatabase.OpenParams.Builder::class,
			),
			this,
		)
	}

	override fun afterHookedMethod(param: MethodHookParam) {
		// skip hook if exception thrown
		if (param.hasThrowable()) return

		// run callback
		GmsHooks.onSQLiteOpenHelperConstructed(
			param.thisObject as SQLiteOpenHelper,
			param.args[0] as Context?,
		)
	}
}
