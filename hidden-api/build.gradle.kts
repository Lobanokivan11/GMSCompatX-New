plugins {
	alias(libs.plugins.android.lib)
	alias(libs.plugins.kotlin.android)
}

android {
	namespace = Vars.namespace + ".hiddenapi"
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

	// target Java 1.8 to avoid "package exists in another module" error
	val jvmVersion = JavaVersion.VERSION_1_8
	compileOptions {
		sourceCompatibility = jvmVersion
		targetCompatibility = jvmVersion
	}
	kotlinOptions.jvmTarget = jvmVersion.toString()

	buildFeatures.aidl = true

	// package AIDL files for library consumers
	aidlPackagedList.addAll(
		// find all AIDL file paths, relative to their respective AIDL source root
		sourceSets["main"].aidl.getSourceDirectoryTrees()
			.flatMap { root -> root.files.map { it.toRelativeString(root.dir) } }
	)
}

dependencies {
	// core dependencies
	implementation(libs.andx.annotation)

	// Refine dependencies
	annotationProcessor(libs.refine.annotationprocessor)
	compileOnly(libs.refine.annotation)
}
