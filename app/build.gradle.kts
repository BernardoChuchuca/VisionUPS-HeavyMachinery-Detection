plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bernardo.visionups"
    compileSdk {
        // Mantenemos tu configuración de SDK 36 (Android 16 Preview)
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.hardwaredetection"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"

                // NUEVO: Pasamos las rutas de tus librerías en C: a CMake
                arguments += listOf(
                    "-DOpenCV_DIR=C:/OpenCV/sdk/native/jni",
                    "-DONNX_DIR=C:/ONNX"
                )
            }
        }

        // NUEVO: Filtramos arquitecturas para evitar errores y reducir peso
        ndk {
            abiFilters.add("arm64-v8a") // Para tu celular físico
            abiFilters.add("armeabi-v7a")
            abiFilters.add("x86_64")    // Para el emulador de PC
        }
    }

    // NUEVO: Importante para empaquetar los archivos .so (librerías) en el APK
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(
                "C:/OpenCV/sdk/native/libs",
                "C:/ONNX/jni"
            )
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val camerax_version = "1.3.0-alpha04" // Versión estable para JNI
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // Necesario para manejar permisos fácil
    implementation("androidx.activity:activity-ktx:1.8.2")
}