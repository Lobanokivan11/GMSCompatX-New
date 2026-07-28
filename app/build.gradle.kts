plugins {
	alias(libs.plugins.android.app)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.refine)
}

android {
	namespace = Vars.namespace
	compileSdk = Vars.sdkTarget

	defaultConfig {
		applicationId = Vars.namespace
		targetSdk = Vars.sdkTarget
		minSdk = Vars.sdkMin

		versionCode = 1
		versionName = "0.1.0"
	}

	buildTypes {
		fun applyOptimizations(buildType: com.android.build.api.dsl.ApplicationBuildType) {
			buildType.isMinifyEnabled = true
			buildType.isShrinkResources = true
			buildType.proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}

		release {
			// sign with debug keys
			signingConfig = signingConfigs.getByName("debug")
			applyOptimizations(this)
		}

		create("debugOptimized") {
			initWith(getByName("debug"))
			matchingFallbacks += listOf("debug", "release")
			applyOptimizations(this)
		}
	}

	// remove Kotlin reflection metadata
	packagingOptions.resources.excludes.add("**/*.kotlin_builtins")

	buildToolsVersion = Vars.buildTools

	compileOptions {
		sourceCompatibility = Vars.jvmVersion
		targetCompatibility = Vars.jvmVersion
	}
	kotlinOptions.jvmTarget = Vars.jvmVersion.toString()
}

dependencies {
	// core dependencies
	implementation(libs.andx.annotation)

	// GMSCompat implementation
	implementation(project(":gmscompat"))

	// hidden API (required for compiling gmscompat)
	compileOnly(project(":hidden-api"))

	// Xposed API
	compileOnly(libs.xposed)
}
