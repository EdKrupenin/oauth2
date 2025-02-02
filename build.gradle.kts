// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.hilt) apply false
}

buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}