package com.android.server.pm.pkg.parsing;

import android.content.pm.SigningDetails;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Stub of hidden class - based on `android-13.0.0_r54`.
 */
public interface ParsingPackageRead extends PkgWithoutStateAppInfo, PkgWithoutStatePackageInfo
		/*, ParsingPackageInternal */ {
	@Nullable Bundle getMetaData();

	SigningDetails getSigningDetails();
}
