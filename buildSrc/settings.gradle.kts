// enable version catalog access from buildSrc
dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files(rootDir.path + "/../gradle/libs.versions.toml"))
		}
	}
}
