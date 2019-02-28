plugins {
    id("com.android.application")
    kotlin("multiplatform")
}

android {
    compileSdkVersion(ProjectVersions.androidSdk)
    buildToolsVersion(ProjectVersions.androidBuildTools)
    defaultConfig {
        applicationId = "com.beepiz.cameracoroutines.sample"
        minSdkVersion(14)
        targetSdkVersion(ProjectVersions.androidSdk)
        versionCode = 1
        versionName = ProjectVersions.thisLibrary
        resConfigs("en", "fr")
        proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
        proguardFile("proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

kotlin {
    android()
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.Experimental")
                useExperimentalAnnotation("splitties.experimental.ExperimentalSplittiesApi")
                useExperimentalAnnotation("splitties.lifecycle.coroutines.PotentialFutureAndroidXLifecycleKtxApi")
            }
        }
        getByName("androidMain").dependencies {
            implementation(project(":cameracoroutines"))

            with(Libs) {
                arrayOf(
                    kotlin.stdlibJdk7,
                    androidX.annotation,
                    androidX.appCompat,
                    androidX.cardView,
                    androidX.coreKtx,
                    androidX.constraintLayout,
                    androidX.fragment,
                    androidX.lifecycle.extensions,
                    google.material,
                    timber,
                    kotlinX.coroutines.android,
                    firebase.core,
                    firebase.crash,
                    splitties.appctx,
                    splitties.activities,
                    splitties.checkedlazy,
                    splitties.archLifecycle,
                    splitties.mainthread,
                    splitties.preferences,
                    splitties.viewsSelectable,
                    splitties.toast,
                    splitties.typesaferecyclerview,
                    splitties.viewsDslConstraintlayout,
                    splitties.viewsDslAppcompat,
                    splitties.viewsDslMaterial
                )
            }.forEach {
                implementation(it)
            }
        }
    }
}

dependencies {
    arrayOf(
        Libs.splitties.viewsDslIdePreview,
        Libs.splitties.stethoInit
    ).forEach {
        debugImplementation(it)
    }
}
