package android.os;

import android.annotation.AppIdInt;
import android.annotation.UserIdInt;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(UserHandle.class)
public final class UserHandleHidden /* implements Parcelable */ {
	public static @UserIdInt int getUserId(int uid) {
		throw new RuntimeException("stub");
	}

	public static @UserIdInt int myUserId() {
		throw new RuntimeException("stub");
	}

	public static int getUid(@UserIdInt int userId, @AppIdInt int appId) {
		throw new RuntimeException("stub");
	}

	public static @AppIdInt int getAppId(int uid) {
		throw new RuntimeException("stub");
	}

	public @UserIdInt int getIdentifier() {
		throw new RuntimeException("stub");
	}
}
