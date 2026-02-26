plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

apply(plugin = "stringfog")
configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    packageName = "com.github.megatronking.stringfog.app"
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

android {
    namespace = "com.ping.night.story"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.videostory.moon.saver.xy"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "${rootProject.name}-v${versionName}-${versionCode}")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../moon_story_release.jks")
            storePassword = "thirtyfive8023@.."
            keyAlias = "thirtyfive"
            keyPassword = "thirtyfive8023@.."
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")

    //ump
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
    //fb
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    //热云
    implementation("com.reyun.solar.engine.oversea:solar-engine-core:1.2.9.6")
    //instal ref
    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation("com.github.megatronking.stringfog:xor:5.0.0")
    //okhttp
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")
    implementation("com.tencent:mmkv:1.3.14")

    //gson
    implementation("com.google.code.gson:gson:2.13.1")

    //lottie
    implementation("com.airbnb.android:lottie:6.7.1")

    //gilde
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    //room
    implementation("androidx.room:room-runtime:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    implementation("androidx.room:room-paging:2.7.2")
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("com.github.lihangleo2:ShadowLayout:3.4.1")

    //admob 广告
    implementation("com.google.android.gms:play-services-ads:24.9.0")
//    //admob 广告聚合
    implementation("com.google.ads.mediation:applovin:13.5.1.0")
    implementation("com.google.ads.mediation:ironsource:9.2.0.0")
    implementation("com.google.ads.mediation:mintegral:17.0.31.0")
    implementation("com.google.ads.mediation:pangle:7.8.0.8.0")
    implementation("com.unity3d.ads:unity-ads:4.16.5")
    implementation("com.google.ads.mediation:unity:4.16.5.0")
}