buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.25")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}
