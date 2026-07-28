// Top-level build file where you can add configuration options common to all sub-projects/modules.

// load common plugins here to avoid loading multiple copies
plugins {
	alias(libs.plugins.android.app) apply false
	alias(libs.plugins.android.lib) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.refine) apply false
}
