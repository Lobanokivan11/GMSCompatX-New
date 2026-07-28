package android.os;

import android.annotation.UserIdInt;
import android.content.Context;
import android.content.pm.UserInfo;

import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(UserManager.class)
public class UserManagerHidden {
	public static String USER_TYPE_FULL_SYSTEM;

	// stub: uses StringDef
	@Retention(RetentionPolicy.SOURCE)
	public @interface UserRestrictionKey {}

	public UserManagerHidden(Context context, IUserManager service) {
		throw new RuntimeException("stub");
	}

	public boolean hasUserRestrictionForUser(@NonNull @UserRestrictionKey String restrictionKey,
	                                         @NonNull UserHandle userHandle) {
		throw new RuntimeException("stub");
	}
	public boolean hasBaseUserRestriction(@NonNull @UserRestrictionKey String restrictionKey,
	                                      @NonNull UserHandle userHandle) {
		throw new RuntimeException("stub");
	}
	public boolean isSystemUser() { throw new RuntimeException("stub"); }
	public UserInfo getUserInfo(@UserIdInt int userId) { throw new RuntimeException("stub"); }
	public @NonNull List<UserInfo> getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated) {
		throw new RuntimeException("stub");
	}
	public int getUserSerialNumber(@UserIdInt int userId) { throw new RuntimeException("stub"); }
	public @UserIdInt int getUserHandle(int userSerialNumber) { throw new RuntimeException("stub"); }
	public List<UserInfo> getProfiles(@UserIdInt int userId) { throw new RuntimeException("stub"); }
	public List<UserInfo> getUsers() { throw new RuntimeException("stub"); }
	public @NonNull int[] getProfileIds(@UserIdInt int userId, boolean enabledOnly) { throw new RuntimeException("stub"); }
	public UserInfo getProfileParent(@UserIdInt int userId) { throw new RuntimeException("stub"); }
	public boolean isManagedProfile() { throw new RuntimeException("stub"); }
}
