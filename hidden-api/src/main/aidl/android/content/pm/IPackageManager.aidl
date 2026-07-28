package android.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

/**
 * AIDL stub.
 * This is only a stub because the original AIDL depends on many other AIDL files and hidden classes.
 */
interface IPackageManager {
    PackageInfo getPackageInfo(String packageName, long flags, int userId);

    ApplicationInfo getApplicationInfo(String packageName, long flags, int userId);

    int checkPermission(String permName, String pkgName, int userId);
}
