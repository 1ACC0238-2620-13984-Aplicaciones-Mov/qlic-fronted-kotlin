plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val externalBuildRoot = providers.environmentVariable("QLIC_ANDROID_BUILD_ROOT").orNull
    ?: if (rootDir.absolutePath.contains("OneDrive", ignoreCase = true)) {
        "C:/Android/Build/qlic-frontend-mobile"
    } else {
        null
    }

externalBuildRoot?.let { buildRoot ->
    layout.buildDirectory.set(file("$buildRoot/root"))
    subprojects {
        layout.buildDirectory.set(file("$buildRoot/${project.name}"))
    }
}
