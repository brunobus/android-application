buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath(kotlin("gradle-plugin", version = "1.3.61"))
        classpath("io.realm:realm-gradle-plugin:6.0.2")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:1.0.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}