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

package com.android.internal.gmscompat;

import static com.android.internal.gmscompat.GmsHooks.inPersistentGmsCoreProcess;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.compat.gms.GmsCompat;
import android.content.Context;
import android.content.ContextHidden;
import android.database.ContentObserver;
import android.database.ContentObserverHidden;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.internal.gmscompat.dynamite.server.FileProxyService;

import net.sb418.android.gmscompatx.util.ReflectUtils;

import org.jetbrains.annotations.Contract;

import dev.rikka.tools.refine.Refine;

public final class GmsCompatApp {
	private static final String TAG = "GmsCompat/GCA";
	public static final String PKG_NAME = "app.grapheneos.gmscompat";

	@SuppressWarnings("FieldCanBeLocal")
	// written to fields to prevent GC from collecting them
	private static BinderGca2Gms binderGca2Gms;
	@SuppressWarnings("FieldCanBeLocal")
	private static FileProxyService dynamiteFileProxyService;

	private static IGms2Gca binderGms2Gca;

	public static final String KEY_BINDER = "binder";

    static GmsCompatConfig connect(Context ctx, String processName) {
		BinderGca2Gms gca2Gms = new BinderGca2Gms();
		binderGca2Gms = gca2Gms;

		try {
			IBinder rawBinder = getBinder(BINDER_IGms2Gca);
			IGms2Gca iGms2Gca = IGms2Gca.Stub.asInterface(rawBinder);
			binderGms2Gca = iGms2Gca;
			Object realProxy = null;
			try {
				Class<?> proxyClass = Class.forName("com.android.internal.gmscompat.IGms2Gca$Stub$Proxy");
				java.lang.reflect.Constructor<?> ctor = proxyClass.getDeclaredConstructor(IBinder.class);
				ctor.setAccessible(true);
				realProxy = ctor.newInstance(rawBinder);
			} catch (Exception ex) {
				Log.e(TAG, "Failed to instantiate dynamic AIDL Proxy, falling back to compile-time Stub", ex);
			}
			if (GmsCompat.isGmsCore()) {
				FileProxyService fileProxyService = null;
				if (inPersistentGmsCoreProcess) {
					fileProxyService = new FileProxyService(ctx);
					dynamiteFileProxyService = fileProxyService;

					Refine.<ContextHidden>unsafeCast(ctx).getMainThreadHandler()
						.postDelayed(GmsCompatApp::maybeShowContactsSyncNotification, 3000L);
				}

				if (realProxy != null) {
					try {
						for (java.lang.reflect.Method m : realProxy.getClass().getDeclaredMethods()) {
							if (m.getName().equals("connectGmsCore")) {
								m.setAccessible(true);
								return (GmsCompatConfig) m.invoke(realProxy, processName, gca2Gms, fileProxyService);
							}
						}
					} catch (Exception ex) {
						Log.e(TAG, "Dynamic connectGmsCore invocation failed", ex);
					}
				}
				return iGms2Gca.connectGmsCore(processName, gca2Gms, fileProxyService);
			} else {
				if (realProxy != null) {
					try {
						for (java.lang.reflect.Method m : realProxy.getClass().getDeclaredMethods()) {
							if (m.getName().equals("connect")) {
								m.setAccessible(true);
								return (GmsCompatConfig) m.invoke(realProxy, ctx.getPackageName(), processName, gca2Gms);
							}
						}
					} catch (Exception ex) {
						Log.e(TAG, "Dynamic connect invocation failed", ex);
					}
				}
				return iGms2Gca.connect(ctx.getPackageName(), processName, gca2Gms);
			}
		} catch (RemoteException e) {
			throw callFailed(e);
		}
	}


	public static IGms2Gca iGms2Gca() {
		return binderGms2Gca;
	}

	private static volatile IClientOfGmsCore2Gca binderClientOfGmsCore2Gca;

	public static IClientOfGmsCore2Gca iClientOfGmsCore2Gca() {
		IClientOfGmsCore2Gca cache = binderClientOfGmsCore2Gca;
		if (cache != null) {
			return cache;
		}

		if (GmsCompat.isGmsCore()) {
			throw new IllegalStateException();
		}

		IBinder binder = getBinder(BINDER_IClientOfGmsCore2Gca);
		IClientOfGmsCore2Gca iface = IClientOfGmsCore2Gca.Stub.asInterface(binder);
		// benign race, it's fine to obtain this interface more than once
		binderClientOfGmsCore2Gca = iface;
		return iface;
	}

	public static final int BINDER_IGms2Gca = 0;
	public static final int BINDER_IClientOfGmsCore2Gca = 1;

	private static IBinder getBinder(int which) {
		String authority = PKG_NAME + ".BinderProvider";
		int retry = 0;
		
		while (retry < 5) {
			try {
				Bundle bundle = GmsCompat.appContext().getContentResolver()
					.call(authority, Integer.toString(which), null, null);
				
				if (bundle != null) {
					IBinder binder = bundle.getBinder(KEY_BINDER);
					if (binder != null) {
						DeathRecipient.register(binder);
						Log.d(TAG, "Successfully acquired Binder on retry: " + retry);
						return binder;
					}
				}
			} catch (Throwable t) {
				Log.w(TAG, "Transient error connecting to BinderProvider, retrying...", t);
			}
			
			retry++;
			try {
				Thread.sleep(500); 
			} catch (InterruptedException ignored) {}
		}
		Log.e(TAG, "Fatal: call to " + authority + " failed after 5 retries");
		System.exit(1);
		return null;
	}

	static class DeathRecipient implements IBinder.DeathRecipient {
		private static final DeathRecipient INSTANCE = new DeathRecipient();

		private DeathRecipient() {}

		static void register(IBinder b) {
			try {
				b.linkToDeath(INSTANCE, 0);
			} catch (RemoteException e) {
				// binder already died
				INSTANCE.binderDied();
			}
		}

		public void binderDied() {
			// see comment in callFailed()
			Log.e(TAG, PKG_NAME + " died");
			System.exit(1);
		}
	}

	public static RuntimeException callFailed(RemoteException e) {
		// running GmsCompat app process is a hard dependency of sandboxed GMS
		Log.e(TAG, "call failed, calling System.exit(1)", e);
		System.exit(1);
		// unreachable, needed for control flow checks by the compiler
		// (Java doesn't have a concept of "noreturn")
		return e.rethrowAsRuntimeException();
	}

	public static final String NS_DeviceConfig = "config";

	@Contract(pure = true)
	public static @NonNull String deviceConfigNamespace(@NonNull String namespace) {
		// last path component of DeviceConfig.CONTENT_URI
		return NS_DeviceConfig + ':' + namespace;
	}

	public static String getString(String ns, String key) {
		try {
			return iGms2Gca().privSettingsGetString(ns, key);
		} catch (RemoteException e) {
			throw callFailed(e);
		}
	}

	public static boolean putString(String ns, String key, @Nullable String value) {
		try {
			return iGms2Gca().privSettingsPutString(ns, key, value);
		} catch (RemoteException e) {
			throw callFailed(e);
		}
	}

	public static boolean setProperties(@NonNull DeviceConfig.Properties props) {
		String[] keys = props.getKeyset().toArray(new String[0]);
		String[] values = new String[keys.length];

		for (int i = 0; i < keys.length; ++i) {
			values[i] = props.getString(keys[i], null);
		}

		String ns = deviceConfigNamespace(props.getNamespace());

		try {
			return iGms2Gca().privSettingsPutStrings(ns, keys, values);
		} catch (RemoteException e) {
			throw callFailed(e);
		}
	}

	private final static ArraySet<ContentObserver> registeredContentObservers = new ArraySet<>();

	public static boolean registerObserver(@NonNull Uri uri, @NonNull ContentObserver observer) {
		String s = uri.toString();

		String prefix = "content://settings/";

		if (!s.startsWith(prefix)) {
			return false;
		}

		int nsStart = prefix.length();
		int nsEnd = s.indexOf('/', nsStart);

		if (nsEnd < 0 || nsStart == nsEnd) {
			return false;
		}

		String ns = s.substring(nsStart, nsEnd);
		String key = s.substring(nsEnd + 1);

		switch (ns) {
			// keep in sync with Settings.NameValueCache#maybeGetGmsCompatNamespace
			case "global":
				if (ReflectUtils.nvtHasKey(Settings.Global.class, key)) {
					return false;
				}
				break;
			case "secure":
				if (ReflectUtils.nvtHasKey(Settings.Secure.class, key)) {
					return false;
				}
				break;
			default:
				return false;
		}

		android.database.IContentObserver iObserver =
			Refine.<ContentObserverHidden>unsafeCast(observer).getContentObserver();

		try {
			iGms2Gca().privSettingsRegisterObserver(ns, key, iObserver);
		} catch (RemoteException e) {
			throw callFailed(e);
		}

		synchronized (registeredContentObservers) {
			registeredContentObservers.add(observer);
		}

		return true;
	}

	public static boolean unregisterObserver(@NonNull ContentObserver observer) {
		synchronized (registeredContentObservers) {
			if (registeredContentObservers.contains(observer)) {
				registeredContentObservers.remove(observer);
			} else {
				return false;
			}
		}

		android.database.IContentObserver iObserver =
			Refine.<ContentObserverHidden>unsafeCast(observer).getContentObserver();

		try {
			iGms2Gca().privSettingsUnregisterObserver(iObserver);
		} catch (RemoteException e) {
			throw callFailed(e);
		}
		return true;
	}

	static void maybeShowContactsSyncNotification() {
		if (GmsCompat.hasPermission(Manifest.permission.WRITE_CONTACTS)) {
			return;
		}

		Context ctx = GmsCompat.appContext();
		var am = ctx.getSystemService(AccountManager.class);

		am.addOnAccountsUpdatedListener(accounts -> {
			// invoked only for Google accounts, "updateImmediately" arg ensures that it'll be called
			// even if account is already added
			if (accounts.length != 0) {
				try {
					iGms2Gca().maybeShowContactsSyncNotification();
				} catch (RemoteException e) {
					throw callFailed(e);
				}
			}
		}, Refine.<ContextHidden>unsafeCast(ctx).getMainThreadHandler(), true);
	}

	private GmsCompatApp() {}
}
