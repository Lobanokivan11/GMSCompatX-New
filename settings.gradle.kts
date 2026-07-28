pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	repositories {
		google()
		mavenCentral()
		maven {
			name = "XposedAPI"
			url = uri("https://api.xposed.info")
		}
	}
}

rootProject.name = "GMSCompatX"

// GMSCompatX application
include(":app")
// GMSCompatX library
include(":gmscompat")
// Hidden Android API stubs
include(":hidden-api")
