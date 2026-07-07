plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.budgetku.dummybank"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.budgetku.dummybank"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.gson)
    implementation(libs.zxing.android.embedded)
    
    // Firebase for Cloud Sync
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.database)
}
