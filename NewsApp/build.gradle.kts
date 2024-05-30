buildscript {
    repositories {
        google()
        maven { url = uri("https://www.jitpack.io" ) }
    }
    val hilt_version = "2.50"
    dependencies {
        classpath ("com.google.gms:google-services:4.4.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hilt_version")
    }
}

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.2" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}