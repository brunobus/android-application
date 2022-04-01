buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath(kotlin("gradle-plugin", version = "1.5.20"))
        classpath("io.realm:realm-gradle-plugin:10.10.1")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:3.1.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // TODO expandablelayout is missing from mavenCentral and exoplayer needs to be updated
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}