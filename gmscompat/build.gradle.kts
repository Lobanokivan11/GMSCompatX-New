plugins {
	alias(libs.plugins.android.lib)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.refine)
}

android {
	namespace = Vars.namespace
	compileSdk = Vars.sdkTarget

	defaultConfig {
		minSdk = Vars.sdkMin
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}

	buildToolsVersion = Vars.buildTools

	compileOptions {
		sourceCompatibility = Vars.jvmVersion
		targetCompatibility = Vars.jvmVersion
	}
	kotlinOptions.jvmTarget = Vars.jvmVersion.toString()

	buildFeatures.aidl = true
}

dependencies {
	// core dependencies
	implementation(libs.andx.annotation)
	implementation(libs.fastutil)
	implementation(libs.guava)

	// Xposed API
	compileOnly(libs.xposed)

	// hidden API
	compileOnly(project(":hidden-api"))
	implementation(libs.refine.runtime)
}
