plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")

//    id("com.android.application") version "8.1.0" apply false
//    id("com.android.library") version "8.1.0" apply false
//    id("org.jetbrains.kotlin.android") version "1.5.31" apply false

    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.eFarm"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.efarm"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    // Versions
    val kotlin_version = "1.7.0"
    val compose_version = "1.0.0-rc02"
    val paging_version = "3.1.1"
    val lottie_version = "4.2.0"
    val coroutines_version = "1.5.2"
    val hilt_version = "2.42"
    val retrofit_version = "2.9.0"
    val okhttp_version = "4.9.1"
    val gson_version = "2.8.8"

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:30.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx:21.0.4")
    implementation("com.google.firebase:firebase-database-ktx:20.0.5")
    implementation("com.google.firebase:firebase-firestore-ktx:24.1.1")
    implementation("com.google.firebase:firebase-storage-ktx")


    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    implementation ("androidx.fragment:fragment:1.3.6")


    // Image Loading Libraries
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.airbnb.android:lottie:$lottie_version")

    // Preference
    implementation("androidx.preference:preference-ktx:1.1.1")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")

    // Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp_version")
    implementation("com.google.code.gson:gson:$gson_version")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt ("com.google.dagger:hilt-android-compiler:$hilt_version")
    kapt ("androidx.hilt:hilt-compiler:1.0.0")

    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("com.jakewharton.rxbinding2:rxbinding:2.0.0")

    // Flexbox Layout
    implementation("com.google.android.flexbox:flexbox:3.0.0")
}
