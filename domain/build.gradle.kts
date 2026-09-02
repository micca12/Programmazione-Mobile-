plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.micca.taskmanager.domain"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // domain non dipende da nessun altro modulo: e' il cuore, e deve restare povero.
    // Le coroutine servono perche' i repository espongono Flow/StateFlow.
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
