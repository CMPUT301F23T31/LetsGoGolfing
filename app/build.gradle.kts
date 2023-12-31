plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.letsgogolfing"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.letsgogolfing"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.mlkit:barcode-scanning:16.0.3")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.activity:activity:1.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    compileOnly(files("${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar"))

    implementation("org.json:json:20210307")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("androidx.test:core:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")


    // Android Instrumentation Testing Dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    // Can't get ML Kit to work, using Firebase Vision instead (deprecated)
    testImplementation ("org.robolectric:robolectric:4.11.1") // use the latest version

    // ui automator - to disable animations for UI tests
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    // AndroidX Test - Core library
    androidTestImplementation("androidx.test:core:1.5.0")
    // AndroidX Test - Assertions and runner
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    compileOnly(files("${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar"))
}