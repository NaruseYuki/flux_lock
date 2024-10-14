// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    id ("dagger.hilt.android.plugin") version "2.48.1" apply false
}

buildscript{
    dependencies{
        classpath ("com.google.dagger:hilt-android-gradle-plugin")
    }
}