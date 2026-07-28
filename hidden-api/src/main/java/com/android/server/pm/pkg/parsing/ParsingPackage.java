package com.android.server.pm.pkg.parsing;

import com.android.server.pm.pkg.component.ParsedService;
import com.android.server.pm.pkg.component.ParsedUsesPermission;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
@SuppressWarnings("UnusedReturnValue")
public interface ParsingPackage extends ParsingPackageRead {
	ParsingPackage addUsesPermission(ParsedUsesPermission parsedUsesPermission);
	ParsingPackage addService(ParsedService parsedService);
	ParsingPackage sortServices();
}
