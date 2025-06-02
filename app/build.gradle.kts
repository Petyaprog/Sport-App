plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
 }

android {
    namespace = "com.example.realmadrid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.realmadrid"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
//        dataBinding = true
        viewBinding = true
//        compose = true
    }
}

dependencies {
    implementation(libs.glide)
    implementation(libs.car.ui.lib)
    annotationProcessor(libs.glideCompiler)
    implementation(libs.sqliteassethelper)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.v493)
    implementation(libs.logging.interceptor.v493)
    implementation(libs.kotlinx.coroutines.android.v160)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Android Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx.v251)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v251)
    implementation(libs.androidx.lifecycle.livedata.ktx.v251)

    // AndroidX
    implementation(libs.androidx.core.ktx.v1100)
    implementation(libs.androidx.appcompat)
    //noinspection GradleDependency
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    //noinspection GradleDependency
    implementation(libs.androidx.navigation.ui.ktx)

    // Тестирование
    testImplementation(libs.junit)
    //noinspection GradleDependency
    androidTestImplementation(libs.androidx.junit)
    //noinspection GradleDependency
    androidTestImplementation(libs.androidx.espresso.core)
}
