import org.gradle.api.JavaVersion

/**
 * Constants that are used throughout the repo.
 */
object Vars {
	// root namespace for Java / Kotlin / Android
	const val namespace = "net.sb418.android.gmscompatx"

	// minimum supported Android SDK version
	const val sdkMin = 33
	// target Android SDK version
	const val sdkTarget = 33

	// Android SDK build tools version
	const val buildTools = "34.0.0"

	// target language compatibility level (for Java + Kotlin)
	val jvmVersion = JavaVersion.VERSION_11
}
