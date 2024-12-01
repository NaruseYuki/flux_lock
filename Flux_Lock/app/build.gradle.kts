import com.android.build.gradle.internal.tasks.databinding.DataBindingGenBaseClassesTask
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.yushin.flux_lock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yushin.flux_lock"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17)) // Java 11を指定
        }
    }

    kotlinOptions {
        jvmTarget = "17"
        javaParameters = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.appium:java-client:8.5.1") // 最新バージョンはAppium公式ドキュメントで確認
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("com.jakewharton.rxrelay3:rxrelay:3.0.1")
    implementation ("com.google.dagger:hilt-android:2.48.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    ksp ("com.google.dagger:hilt-compiler:2.48.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    /** sesame sdk  ==> */
    //sesame sdk

    ksp("androidx.room:room-compiler:2.5.0")
    implementation (project(":sesame-sdk"))
    //sesame sdk use room save db
    implementation ("androidx.room:room-runtime:2.5.0")
    implementation ("androidx.room:room-ktx:2.5.0")

    //sesame sdk use aws
    implementation ("com.amazonaws:aws-android-sdk-apigateway-core:2.19.3")
    implementation ("com.amazonaws:aws-android-sdk-iot:2.19.3")

    /** end sesame sdk  <== */
    androidComponents {
        onVariants(selector().all()) { variant ->
            afterEvaluate {
                project.tasks.getByName("ksp${variant.name.capitalize()}Kotlin") {
                    val dataBindingTask =
                        try {
                            val taskName = "dataBindingGenBaseClasses${variant.name.capitalize()}"
                            project.tasks.getByName(taskName) as DataBindingGenBaseClassesTask
                        } catch (e: UnknownTaskException) {
                            return@getByName
                        }

                    project.tasks.getByName("ksp${variant.name.capitalize()}Kotlin") {
                        (this as AbstractKotlinCompileTool<*>).setSource(dataBindingTask.sourceOutFolder)
                    }
                }
            }
        }
    }
}

