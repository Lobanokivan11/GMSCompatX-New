/*
 * Copyright sudoBash418
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.internal.gmscompat;

import android.app.compat.gms.GmsCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import net.sb418.android.gmscompatx.patches.BinderRedirectorPatches;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Obtains from GmsCompatApp objects that are needed to create HybridBinder
 * and handles redirection state changes.
 */
public final class BinderRedirector implements Parcelable {
	private static final String TAG = "BinderRedirector";

	private static volatile boolean enabled;
	private static String[] redirectableInterfaces;
	private static String[] notableInterfaces;

	private static RedirectionStateListener redirectionStateListener;
	private static BinderRedirector[] cache;

	public final IBinder destination;
	public final int[] transactionCodes;

	public BinderRedirector(IBinder destination, int[] transactionCodes) {
		this.destination = destination;
		this.transactionCodes = transactionCodes;
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void maybeInit(Intent intent) {
		if (!GmsInfo.PACKAGE_GMS_CORE.equals(intent.getPackage())) {
			return;
		}
		if (GmsCompat.isGmsCore()) {
			return;
		}
		synchronized (BinderRedirector.class) {
			if (enabled) {
				return;
			}
			if (GmsCompat.isClientOfGmsCore()) {
				try {
					Object clientService = GmsCompatApp.iClientOfGmsCore2Gca();
					if (clientService == null) {
						Log.w(TAG, "GMSCompatX: iClientOfGmsCore2Gca is null. Disabling redirector.");
						return;
					}

					ArrayList<String> notableIfaces = new ArrayList<>(10);
					java.lang.reflect.Method getRedirectableMethod = clientService.getClass().getMethod(
						"getRedirectableInterfaces", 
						List.class
					);
					getRedirectableMethod.setAccessible(true);
					
					redirectableInterfaces = (String[]) getRedirectableMethod.invoke(clientService, notableIfaces);
					notableIfaces.toArray(notableInterfaces = new String[notableIfaces.size()]);
					
					enabled = true;

					// install BinderRedirector patches
					BinderRedirectorPatches.INSTANCE.install();
					
				} catch (java.lang.reflect.InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RemoteException) {
						throw GmsCompatApp.callFailed((RemoteException) cause);
					}
					throw new RuntimeException(cause);
				} catch (Throwable t) {
					Log.e(TAG, "GMSCompatX: Reflective maybeInit failed", t);
				}
			}
		}
	}

	public static BinderRedirector maybeGet(String interface_) {
		if (redirectableInterfaces == null) return null;
		
		int id = Arrays.binarySearch(redirectableInterfaces, interface_);
		if (id >= 0) {
			BinderRedirector rd = obtain(id);
			if (rd != null && rd.destination != null) {
				return rd;
			}
		} else if (notableInterfaces != null && Arrays.binarySearch(notableInterfaces, interface_) >= 0) {
			try {
				Object clientService = GmsCompatApp.iClientOfGmsCore2Gca();
				if (clientService != null) {
					java.lang.reflect.Method m = clientService.getClass().getMethod("onNotableInterfaceAcquired", String.class);
					m.setAccessible(true);
					m.invoke(clientService, interface_);
				}
			} catch (java.lang.reflect.InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RemoteException) {
					throw GmsCompatApp.callFailed((RemoteException) cause);
				}
			} catch (Throwable t) {
				Log.e(TAG, "GMSCompatX: Reflective onNotableInterfaceAcquired failed", t);
			}
		}
		return null;
	}

	private static BinderRedirector obtain(int id) {
		BinderRedirector[] cache = BinderRedirector.cache;
		if (cache != null) {
			BinderRedirector cached = cache[id];
			if (cached != null) {
				return cached;
			}
		}
		synchronized (BinderRedirector.class) {
			if (redirectionStateListener == null) {
				if (redirectableInterfaces == null) return null;
				redirectionStateListener = RedirectionStateListener.register();
				BinderRedirector.cache = new BinderRedirector[redirectableInterfaces.length];
			}
			redirectionStateListener.usedRedirections |= (1L << id);
		}
		BinderRedirector rd = null;
		try {
			Object clientService = GmsCompatApp.iClientOfGmsCore2Gca();
			if (clientService != null) {
				java.lang.reflect.Method m = clientService.getClass().getMethod("getBinderRedirector", int.class);
				m.setAccessible(true);
				rd = (BinderRedirector) m.invoke(clientService, id);
			}
		} catch (java.lang.reflect.InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RemoteException) {
				throw GmsCompatApp.callFailed((RemoteException) cause);
			}
		} catch (Throwable t) {
			Log.e(TAG, "GMSCompatX: Reflective getBinderRedirector failed", t);
		}
		
		if (rd != null && BinderRedirector.cache != null) {
			BinderRedirector.cache[id] = rd;
		}
		return rd;
	}

	public static class RedirectionStateListener extends BroadcastReceiver {
		public static final String INTENT_ACTION = GmsCompatApp.PKG_NAME + ".ACTION_REDIRECTION_STATE_CHANGED";
		public static final String PERMISSION = GmsCompatApp.PKG_NAME + ".permission.REDIRECTION_STATE_CHANGED_BROADCAST";
		public static final String KEY_REDIRECTION_ID = "id";

		volatile long usedRedirections;

		static RedirectionStateListener register() {
			RedirectionStateListener l = new RedirectionStateListener();
			GmsCompat.appContext().registerReceiver(l, new IntentFilter(INTENT_ACTION), PERMISSION, null);
			return l;
		}

		public void onReceive(Context context, Intent intent) {
			int id = intent.getIntExtra(KEY_REDIRECTION_ID, 0);
			if ((usedRedirections & (1L << id)) != 0) {
				Log.d(TAG, "state of redirection (id " + id + ") changed, calling System.exit(0)");
				System.exit(0);
			}
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		IBinder binder = destination;
		dest.writeBoolean(binder != null);
		if (binder != null) {
			dest.writeStrongBinder(destination);
			dest.writeIntArray(transactionCodes);
		}
	}

	public static final Parcelable.Creator<BinderRedirector> CREATOR = new Parcelable.Creator<>() {
		@Override
		public BinderRedirector createFromParcel(Parcel source) {
			if (!source.readBoolean()) {
				return new BinderRedirector(null, null);
			}
			IBinder destination = source.readStrongBinder();
			int[] transactionCodes = source.createIntArray();
			return new BinderRedirector(destination, transactionCodes);
		}

		@Override
		public BinderRedirector[] newArray(int size) {
			return new BinderRedirector[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
