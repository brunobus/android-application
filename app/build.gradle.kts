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
    compileSdkVersion(28)
    buildToolsVersion = "28.0.3"

    defaultConfig {
        applicationId = "hr.bpervan.novaeva.main"
        minSdkVersion(16)
        targetSdkVersion(28)
        versionCode = 160030004
        versionName = "3.0.4"
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
            isUseProguard = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions("server")

    productFlavors {
        create("development") {
            dimension = "server"

            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            resValue("string", "app_name", "Dev Eva")

            buildConfigField("long", "VOCATION_ROOT_ID", "3214")
            buildConfigField("String", "V2_SERVER_URI", "\"http://novaeva.com\"")
            buildConfigField("String", "V3_SERVER_URI", "\"http://vps423121.ovh.net:8080\"")
            buildConfigField("String", "V3_SERVER_AUTH", "\"Basic Z2FsYWRyaWVsMTozU0t5Szg=\"")
            buildConfigField("String", "DB_NAME", "\"NovaEvaDb_dev.realm\"")
        }

        create("production") {
            dimension = "server"

            resValue("string", "app_name", "Nova Eva")

            buildConfigField("long", "VOCATION_ROOT_ID", "2603")
            buildConfigField("String", "V2_SERVER_URI", "\"http://novaeva.com\"")
            buildConfigField("String", "V3_SERVER_URI", "\"http://ns3115316.ip-54-38-194.eu:8080\"")
            buildConfigField("String", "V3_SERVER_AUTH", "\"Basic QW5kUm9pRDAwMTpkM2JXYXdnRFpqcXFWV2dESlJLag==\"")
            buildConfigField("String", "DB_NAME", "\"NovaEvaDb_prod.realm\"")
        }
    }

    compileOptions {
        setTargetCompatibility(JavaVersion.VERSION_1_8)
        setSourceCompatibility(JavaVersion.VERSION_1_8)
    }
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("com.google.code.gson:gson:2.8.5")

    implementation("androidx.core:core:1.0.1")
    implementation("androidx.core:core-ktx:1.0.1")
    implementation("androidx.media:media:1.0.0")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("io.reactivex.rxjava2:rxjava:2.2.3")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")

    implementation("com.google.android.material:material:1.0.0")
    implementation("com.google.android.gms:play-services-analytics:16.0.6")
    implementation("com.google.android.gms:play-services-location:16.0.0")
    implementation("com.google.android.exoplayer:exoplayer:2.7.0")
    implementation("com.google.android.exoplayer:extension-mediasession:2.7.0")

    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    implementation("com.squareup.retrofit2:converter-gson:2.4.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")

    implementation("net.cachapa.expandablelayout:expandablelayout:2.9.2")
    implementation("com.nostra13.universalimageloader:universal-image-loader:1.9.5")
}