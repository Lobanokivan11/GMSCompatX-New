package android.content.pm;

import android.annotation.UserIdInt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public class UserInfo /* implements Parcelable */ {
	public static int FLAG_FULL;
	public static int FLAG_SYSTEM;

	// stub: uses IntDef
	@Retention(RetentionPolicy.SOURCE)
	public @interface UserInfoFlag {}

	public @UserIdInt int id;
	public int serialNumber;
	public @UserInfoFlag int flags;
	public String userType;
}
