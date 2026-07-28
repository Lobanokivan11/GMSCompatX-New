/*
 * Copyright sudoBash418
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.gmscompat.sysservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.compat.gms.GmsCompat;
import android.content.Context;
import android.os.WorkSource;
import android.telephony.TelephonyCallbackHidden;
import android.telephony.TelephonyManagerHidden;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.Executor;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

@SuppressLint("MissingPermission")
public class GmcTelephonyManager extends TelephonyManagerHidden {
	private static final String TAG = "GmcTelephonyManager";

	public GmcTelephonyManager(Context ctx) {
		super(ctx);
	}

	public GmcTelephonyManager(Context context, int subId) {
		super(context, subId);
	}

	public void requestCellInfoUpdate(WorkSource workSource, Executor executor, CellInfoCallback callback) {
		// Attribute the work to GMS instead of the client
		requestCellInfoUpdate(executor, callback);
	}

	@Override
	public void requestCellInfoUpdate(Executor executor, CellInfoCallback callback) {
		if (!GmsCompat.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			return;
		}

		super.requestCellInfoUpdate(executor, callback);
	}

	public static int[] filterTelephonyCallbackEvents(int[] eventsArray) {
		var events = new IntOpenHashSet(eventsArray);

		var sb = new StringBuilder();

		if (!GmsCompat.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
			removeEvents(events, EVENTS_PROT_READ_PHONE_STATE, "READ_PHONE_STATE", sb);
		}

		if (!GmsCompat.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			removeEvents(events, EVENTS_PROT_ACCESS_FINE_LOCATION, "ACCESS_FINE_LOCATION", sb);
		}

		// these events are protected by privileged permissions
		removeEvents(events, EVENTS_PROT_READ_ACTIVE_EMERGENCY_SESSION,
		             "READ_ACTIVE_EMERGENCY_SESSION", sb);
		removeEvents(events, EVENTS_PROT_READ_PRIVILEGED_PHONE_STATE,
		             "READ_PRIVILEGED_PHONE_STATE", sb);
		removeEvents(events, EVENTS_PROT_READ_PRECISE_PHONE_STATE,
		             "READ_PRECISE_PHONE_STATE", sb);

		int[] res = events.toIntArray();

		Log.d(TAG, "registering listener, events: " + Arrays.toString(res) +
			      (sb.length() != 0 ? "\nfiltered events due to missing permission\n" + sb : ""),
		      new Throwable());

		return res;
	}

	private static void removeEvents(IntSet events, int[] eventsToRemove, String permName, StringBuilder sb) {
		boolean filtered = false;

		for (int event : eventsToRemove) {
			if (!events.remove(event)) {
				continue;
			}
			if (!filtered) {
				sb.append(permName);
				sb.append(": {");
				filtered = true;
			}
			sb.append(event);
			sb.append(", ");
		}

		if (filtered) {
			sb.append("}\n");
		}
	}

	private static final int[] EVENTS_PROT_READ_PHONE_STATE = {
		TelephonyCallbackHidden.EVENT_CALL_FORWARDING_INDICATOR_CHANGED,
		TelephonyCallbackHidden.EVENT_MESSAGE_WAITING_INDICATOR_CHANGED,
		TelephonyCallbackHidden.EVENT_EMERGENCY_NUMBER_LIST_CHANGED,
		TelephonyCallbackHidden.EVENT_LEGACY_CALL_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_CALL_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_ACTIVE_DATA_SUBSCRIPTION_ID_CHANGED,
		TelephonyCallbackHidden.EVENT_CELL_INFO_CHANGED,
	};

	private static final int[] EVENTS_PROT_ACCESS_FINE_LOCATION = {
		TelephonyCallbackHidden.EVENT_CELL_LOCATION_CHANGED,
		TelephonyCallbackHidden.EVENT_CELL_INFO_CHANGED,
		TelephonyCallbackHidden.EVENT_REGISTRATION_FAILURE,
		TelephonyCallbackHidden.EVENT_BARRING_INFO_CHANGED,
	};

	private static final int[] EVENTS_PROT_READ_ACTIVE_EMERGENCY_SESSION = {
		TelephonyCallbackHidden.EVENT_OUTGOING_EMERGENCY_CALL,
		TelephonyCallbackHidden.EVENT_OUTGOING_EMERGENCY_SMS,
	};

	private static final int[] EVENTS_PROT_READ_PRIVILEGED_PHONE_STATE = {
		TelephonyCallbackHidden.EVENT_SRVCC_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_VOICE_ACTIVATION_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_RADIO_POWER_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_ALLOWED_NETWORK_TYPE_LIST_CHANGED,
	};

	private static final int[] EVENTS_PROT_READ_PRECISE_PHONE_STATE = {
		TelephonyCallbackHidden.EVENT_PRECISE_DATA_CONNECTION_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_DATA_CONNECTION_REAL_TIME_INFO_CHANGED,
		TelephonyCallbackHidden.EVENT_PRECISE_CALL_STATE_CHANGED,
		TelephonyCallbackHidden.EVENT_CALL_DISCONNECT_CAUSE_CHANGED,
		TelephonyCallbackHidden.EVENT_CALL_ATTRIBUTES_CHANGED,
		TelephonyCallbackHidden.EVENT_IMS_CALL_DISCONNECT_CAUSE_CHANGED,
		TelephonyCallbackHidden.EVENT_REGISTRATION_FAILURE,
		TelephonyCallbackHidden.EVENT_BARRING_INFO_CHANGED,
		TelephonyCallbackHidden.EVENT_PHYSICAL_CHANNEL_CONFIG_CHANGED,
		TelephonyCallbackHidden.EVENT_DATA_ENABLED_CHANGED,
		TelephonyCallbackHidden.EVENT_LINK_CAPACITY_ESTIMATE_CHANGED,
	};
}
