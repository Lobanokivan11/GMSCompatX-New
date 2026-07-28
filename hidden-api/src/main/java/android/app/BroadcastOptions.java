package android.app;

import android.os.Bundle;
import android.os.PowerExemptionManager.ReasonCode;
import android.os.PowerExemptionManager.TempAllowListType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public class BroadcastOptions /* extends ComponentOptions */ {
	public BroadcastOptions(@NonNull Bundle opts) { throw new RuntimeException("stub"); }
	public @TempAllowListType int getTemporaryAppAllowlistType() { throw new RuntimeException("stub"); }
	public long getTemporaryAppAllowlistDuration() { throw new RuntimeException("stub"); }
	public Bundle toBundle() { throw new RuntimeException("stub"); }
	public @Nullable String getTemporaryAppAllowlistReason() { throw new RuntimeException("stub"); }
	public @ReasonCode int getTemporaryAppAllowlistReasonCode() { throw new RuntimeException("stub"); }
	public void setTemporaryAppAllowlist(long duration, @TempAllowListType int type, @ReasonCode int reasonCode,
	                                     @Nullable String reason) {
		throw new RuntimeException("stub");
	}
}
