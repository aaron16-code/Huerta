plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "cl.duoc.huertoescolar"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.huertoescolar"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        val apiUrl = providers.gradleProperty("HUERTO_API_URL").orElse("http://10.0.2.2:8080/").get()
        require(apiUrl.endsWith("/")) { "HUERTO_API_URL debe terminar en /" }
        buildConfigField("String", "HUERTO_API_URL", "\"$apiUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*ScreenKt*",
                    "cl.duoc.huertoescolar.navigation.*",
                    "cl.duoc.huertoescolar.ui.theme.*",
                    "cl.duoc.huertoescolar.ui.components.*",
                    "cl.duoc.huertoescolar.MainActivity*",
                    "*_Impl*",
                    "*.BuildConfig"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    implementation(libs.androidx.compose.material.icons.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.mockk)

}