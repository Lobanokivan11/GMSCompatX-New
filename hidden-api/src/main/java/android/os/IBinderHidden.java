package android.os;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;

import dev.rikka.tools.refine.RefineAs;

/**
 * Stub of partially-hidden class - based on `android-13.0.0_r54`.
 */
@RefineAs(IBinder.class)
public interface IBinderHidden {
	public void shellCommand(@Nullable FileDescriptor in, @Nullable FileDescriptor out,
	                         @Nullable FileDescriptor err,
	                         @NonNull String[] args, @Nullable ShellCallback shellCallback,
	                         @NonNull ResultReceiver resultReceiver) throws RemoteException;

	public default @Nullable IBinder getExtension() throws RemoteException {
		throw new RuntimeException("stub");
	}
}
