package android.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.ApplicationInfoFlags;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.PackageInfoFlags;
import android.content.pm.PackageManagerHidden;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public class ApplicationPackageManager extends PackageManagerHidden {
	protected ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
		throw new RuntimeException("stub");
	}

	public int getUserId() {
		throw new RuntimeException("stub");
	}

	public PackageInfo getPackageInfoAsUser(String packageName, PackageInfoFlags flags, int userId)
			throws NameNotFoundException {
		throw new RuntimeException("stub");
	}

	public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
		throw new RuntimeException("stub");
	}

	public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
		throw new RuntimeException("stub");
	}

	public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
		throw new RuntimeException("stub");
	}

	public boolean hasSystemFeature(String name) {
		throw new RuntimeException("stub");
	}

	public void addOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
		throw new RuntimeException("stub");
	}

	public void removeOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
		throw new RuntimeException("stub");
	}

	public ApplicationInfo getApplicationInfoAsUser(String packageName, ApplicationInfoFlags flags, int userId)
			throws NameNotFoundException {
		throw new RuntimeException("stub");
	}

	public String[] getPackagesForUid(int uid) {
		throw new RuntimeException("stub");
	}

	public @NonNull List<SharedLibraryInfo> getSharedLibraries(PackageInfoFlags flags) {
		throw new RuntimeException("stub");
	}

	public PackageInfo getPackageInfo(VersionedPackage versionedPackage, PackageInfoFlags flags)
			throws NameNotFoundException {
		throw new RuntimeException("stub");
	}

	public List<ApplicationInfo> getInstalledApplicationsAsUser(ApplicationInfoFlags flags, int userId) {
		throw new RuntimeException("stub");
	}

	public List<PackageInfo> getInstalledPackagesAsUser(PackageInfoFlags flags, int userId) {
		throw new RuntimeException("stub");
	}

	public int getApplicationEnabledSetting(String packageName) {
		throw new RuntimeException("stub");
	}

	public String getInstallerPackageName(String packageName) {
		throw new RuntimeException("stub");
	}

	public @NonNull InstallSourceInfo getInstallSourceInfo(String packageName) throws NameNotFoundException {
		throw new RuntimeException("stub");
	}
}
