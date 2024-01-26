buildscript {

    val hilt_version = "2.42"
    val kotlin_coroutines_version = "1.3.9"
    val lifecycle_version = "2.2.0"

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath ("com.google.gms:google-services:4.3.14")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:$hilt_version")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0") // Use the latest version
        classpath ("com.android.tools.build:gradle:7.0.0") // Use the latest version

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

//allprojects {
//    repositories {
//        google()
//        jcenter()
//        mavenCentral()

//    }
//}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}
