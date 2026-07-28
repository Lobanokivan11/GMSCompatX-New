package android.os;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public final class ServiceManager {
	public static class ServiceNotFoundException extends Exception {}

	public static IBinder getServiceOrThrow(String name) throws ServiceNotFoundException {
		throw new RuntimeException("stub");
	}
}
