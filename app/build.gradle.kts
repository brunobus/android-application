import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("realm-android")
    id("com.getkeepsafe.dexcount")
}

android {
    compileSdkVersion(27)
    buildToolsVersion = "27.0.3"

    defaultConfig {
        applicationId = "hr.bpervan.novaeva.main"
        minSdkVersion(16)
        targetSdkVersion(27)
        versionCode = 160020901
        versionName = "2.9.1"
    }

    buildTypes {
        getByName("debug") {
            isShrinkResources = false
            isMinifyEnabled = true
            isUseProguard = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isUseProguard = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        setTargetCompatibility(JavaVersion.VERSION_1_8)
        setSourceCompatibility(JavaVersion.VERSION_1_8)
    }
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.code.gson:gson:2.8.2")

    implementation("com.android.support:support-compat:27.1.1")
    implementation("com.android.support:support-media-compat:27.1.1")
    implementation("com.android.support:appcompat-v7:27.1.1")
    implementation("com.android.support:design:27.1.1")
    implementation("com.android.support:recyclerview-v7:27.1.1")
    implementation("com.android.support.constraint:constraint-layout:1.1.2")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:0.3")
    implementation("io.reactivex.rxjava2:rxjava:2.1.14")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")

    implementation("com.nostra13.universalimageloader:universal-image-loader:1.9.5")
    implementation("com.google.android.gms:play-services-analytics:12.0.1")
    implementation("com.google.android.gms:play-services-location:12.0.1")

    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.3.0")

    implementation("com.google.android.exoplayer:exoplayer:2.7.0")
    implementation("com.google.android.exoplayer:extension-mediasession:2.7.0")

    implementation("net.cachapa.expandablelayout:expandablelayout:2.9.2")
}
