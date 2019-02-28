plugins {
    id("com.android.library")
    kotlin("multiplatform")
    `maven-publish`
    id("com.jfrog.bintray")
}

android {
    setDefaults()
}

kotlin {
    android()
    sourceSets {
        getByName("androidMain").dependencies {
            api(Libs.kotlin.stdlibJdk7)
            api(Libs.kotlinX.coroutines.android)
            api(Libs.androidX.annotation)
            implementation(Libs.splitties.appctx)
            implementation(Libs.splitties.mainthread)
            implementation(Libs.splitties.systemservices)
            implementation(Libs.timber)
        }
    }
}
